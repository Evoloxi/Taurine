package io.taurine.visual.impl

import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.math.Axis
import com.simibubi.create.content.kinetics.belt.BeltBlockEntity
import com.simibubi.create.content.kinetics.belt.BeltHelper
import com.simibubi.create.content.kinetics.belt.BeltSlope
import com.simibubi.create.content.kinetics.belt.transport.TransportedItemStack
import com.simibubi.create.content.logistics.box.PackageItem
import dev.engine_room.flywheel.api.instance.Instance
import dev.engine_room.flywheel.api.visual.TickableVisual
import dev.engine_room.flywheel.api.visualization.VisualizationContext
import dev.engine_room.flywheel.backend.mixin.LevelRendererAccessor
import dev.engine_room.flywheel.lib.instance.InstanceTypes
import dev.engine_room.flywheel.lib.util.RendererReloadCache
import dev.engine_room.flywheel.lib.visual.AbstractBlockEntityVisual
import dev.engine_room.flywheel.lib.visual.SimpleTickableVisual
import dev.engine_room.vanillin.item.ItemModels
import io.taurine.ModelCache.canBeInstanced
import io.taurine.flywheel.ConstantMotionInstance
import io.taurine.flywheel.PreservingInstanceRecycler
import io.taurine.flywheel.SmartPreservingRecycler
import io.taurine.flywheel.TaurineInstanceTypes
import io.taurine.mesh.ShadowMesh.SHADOW_MODEL
import io.taurine.visual.impl.BeltItemLayerVisual.Companion.Flags.has
import io.taurine.visual.BeltParams
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.LightTexture
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.util.Mth
import net.minecraft.util.RandomSource
import net.minecraft.world.item.ItemDisplayContext
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.LightLayer
import net.minecraft.world.level.block.entity.BlockEntity
import org.joml.Matrix4f
import org.joml.Vector3f
import java.util.function.Consumer
import kotlin.math.abs

class BeltItemLayerVisual(
    ctx: VisualizationContext, val belt: BeltBlockEntity, partialTick: Float
) : AbstractBlockEntityVisual<BeltBlockEntity>(ctx, belt, partialTick), SimpleTickableVisual {

    private data class ItemState(
        val stuck: Boolean,
        val angle: Int,
        val stackCount: Int,
        val lightBlock: BlockPos,
        val packedLight: Int,
    )

    init {
        println("INITQQQ")
    }

    private val knownState = Reference2ObjectOpenHashMap<TransportedItemStack, ItemState>(8)

    private val instances = SmartPreservingRecycler<ItemStack, ConstantMotionInstance> {
        instancerProvider().instancer(
            TaurineInstanceTypes.CONSTANT_MOTION,
            ItemModels.get(level, it, ItemDisplayContext.FIXED)
        ).createInstance()
    }

    private val shadows = PreservingInstanceRecycler {
        instancerProvider().instancer(InstanceTypes.SHADOW, SHADOW_MODEL, 10).createInstance()
    }

    private var needsFullRebuild = true
    private var needsRelight = true

    override fun updateLight(partialTick: Float) {
        needsRelight = true
    }

    init { update(partialTick) }

    override fun update(pt: Float) {
        needsFullRebuild = true
    }

    override fun tick(context: TickableVisual.Context) {
        if (!belt.isController) return
        val inv = belt.inventory ?: return

        val beltParams = BeltParams.from(belt)
        val ticks = (Minecraft.getInstance().levelRenderer as LevelRendererAccessor).`flywheel$getTicks`().toFloat()

        if (knownState.size > inv.transportedItems.size) {
            val current = ReferenceOpenHashSet(inv.transportedItems)
            knownState.keys.retainAll(current)
        }

        instances.resetCount()
        shadows.resetCount()

        for (transported in inv.transportedItems) {
            if (!transported.stack.canBeInstanced) continue

            val lightPos = lightPosFor(transported.beltPosition, beltParams)
            val packedLight = LightTexture.pack(
                level.getBrightness(LightLayer.BLOCK, lightPos),
                level.getBrightness(LightLayer.SKY, lightPos)
            )

            val prev = knownState[transported]
            val nearSlopeBoundary = beltParams.slope != BeltSlope.HORIZONTAL && run {
                val tickDelta = abs(belt.speed) / 24f
                val distToEntry = abs(transported.beltPosition - 0.5f)
                val distToExit = abs(transported.beltPosition - (beltParams.beltLength - 0.5f))
                minOf(distToEntry, distToExit) < tickDelta * 2
            }
            val stuck = (belt.speed != 0f && (transported.prevBeltPosition - transported.beltPosition) == 0f)

            val motionChanged = prev == null ||
                    (stuck != prev.stuck) ||
                    ((transported.prevSideOffset - transported.sideOffset) * belt.speed > EPSILON) ||
                    transported.angle != prev.angle ||
                    transported.stack.count != prev.stackCount ||
                    nearSlopeBoundary

            val lightChanged = prev == null || lightPos != prev.lightBlock

            val needsWrite = needsFullRebuild || motionChanged

            val count = Mth.log2(transported.stack.count) / 2

            if (needsWrite || (needsRelight || lightChanged)) {
                var flags = 0
                if (needsWrite)                   flags = flags or Flags.UPDATE_TRANSFORM
                if (lightChanged || needsRelight) flags = flags or Flags.UPDATE_LIGHT
                if (stuck)                        flags = flags or Flags.ITEM_STUCK

                writeInstances(transported, beltParams, ticks, packedLight, flags)
                knownState[transported] = ItemState(
                    stuck = stuck,
                    angle = transported.angle,
                    stackCount = transported.stack.count,
                    lightBlock = lightPos,
                    packedLight = packedLight,
                )
            } else {
                instances.preserve(transported.stack, count + 1)
                shadows.preserve(1)
            }
        }

        instances.discardExtra()
        shadows.discardExtra()

        needsFullRebuild = false
        needsRelight = false
        belt.networkDirty = false
    }

    private fun writeInstances(
        transported: TransportedItemStack,
        p: BeltParams,
        renderTicks: Float,
        packedLight: Int,
        flags: Int
    ) {
        val stack = transported.stack
        val random = RANDOM.get().also { it.setSeed(transported.angle.toLong()) }

        val offset = transported.beltPosition
        val sideOffset = transported.sideOffset

        val verticalMovement = if (offset < 0.5f) 0f
        else p.verticality * (Mth.clamp(offset, 0.5f, p.beltLength - 0.5f) - 0.5f)

        val offsetVec = Vector3f(p.directionVec.x.toFloat(), 0f, p.directionVec.z.toFloat())
            .mul(offset)
            .add(0f, verticalMovement, 0f)

        val onSlope = p.slope != BeltSlope.HORIZONTAL &&
                Mth.clamp(offset, 0.5f, p.beltLength - 0.5f) == offset

        val slopeAngle = computeSlopeAngle(onSlope, p)

        val itemPos = p.beltStartOffset.add(
            (visualPosition.x + offsetVec.x).toDouble(),
            (visualPosition.y + offsetVec.y).toDouble(),
            (visualPosition.z + offsetVec.z).toDouble()
        )

        val ms = PoseStack()
        ms.setIdentity()

        val adjustedSideOffset = if (p.alongX) +sideOffset else -sideOffset
        ms.translate(
            itemPos.x + if (p.alongX) adjustedSideOffset else 0f,
            itemPos.y,
            itemPos.z + if (!p.alongX) adjustedSideOffset else 0f
        )

        val renderUpright = UPRIGHT_CACHE.get(stack)
        val bakedModel = ItemModels.getModel(stack)
        val blockItem = bakedModel.isGui3d
        val box = PackageItem.isPackage(stack)
        val count = Mth.log2(stack.count) / 2
        val scaleValue = if (box) 1.5f else 0.5f

        if (!renderUpright) {
            ms.mulPose((if (p.slopeAlongX) Axis.ZP else Axis.XP).rotationDegrees(slopeAngle))
        }
        if (onSlope) ms.translate(0f, SLOPE_OFFSET, 0f)

        if (!onSlope) placeShadow(ms)

        if (renderUpright) orientUprightItem(ms, offset)

        val rotYAngle = Axis.YP.rotationDegrees(transported.angle.toFloat())
        val baseMatrix = ms.last().pose()

        val motion = if (flags has Flags.ITEM_STUCK) {
            Vector3f()
        } else {
            Vector3f(
                p.directionVec.x.toFloat(),
                if (onSlope) p.verticality.toFloat() else 0f,
                p.directionVec.z.toFloat()
            )
                .mul(belt.directionAwareBeltMovementSpeed * 20)
                .add(
                    if (p.alongX) (sideOffset - transported.prevSideOffset) * 15 else 0f,
                    0.0f,
                    if (!p.alongX) (sideOffset - transported.prevSideOffset) * 15 else 0f
                )
        }
        for (i in 0..count) {
            instances.get(stack).apply {
                if (flags has Flags.UPDATE_TRANSFORM) {
                    val itemMatrix = (baseMatrix.clone() as Matrix4f).apply {
                        rotate(rotYAngle)
                        applyItemLayering(this, i, blockItem, renderUpright, box, random)
                        scale(scaleValue, scaleValue, scaleValue)
                    }
                    this.setTransform(itemMatrix)
                    this.anchorTime = renderTicks / 20f
                    this.motion = motion
                }

                if (flags has Flags.UPDATE_LIGHT) {
                    this.light(packedLight)
                }

                this.setChanged()
            }

            advanceBaseMatrix(baseMatrix, blockItem, renderUpright)
        }
    }

    private fun placeShadow(ms: PoseStack) {
        val matrix = ms.last().pose()
        val sx = matrix.m30()
        val sy = matrix.m31() - 0.12f
        val sz = matrix.m32()
        shadows.get().apply {
            x = sx - 0.2f; y = sy; z = sz - 0.2f
            entityX = sx; entityZ = sz
            radius  = 0.2f; alpha = 0.5f
            sizeX   = 0.4f; sizeZ = 0.4f
            setChanged()
        }
    }

    private fun lightPosFor(offset: Float, p: BeltParams) =
        visualPosition.offset(
            (p.directionVec.x * offset).toInt(),
            (p.verticality * offset + 0.5f).toInt(),
            (p.directionVec.z * offset).toInt()
        )

    private fun computeSlopeAngle(onSlope: Boolean, p: BeltParams): Float {
        if (!onSlope) return 0f
        val tiltForward = ((p.slope == BeltSlope.DOWNWARD) xor (p.beltFacing.axisDirection == Direction.AxisDirection.POSITIVE)) == (p.beltFacing.axis == Direction.Axis.Z)
        return if (tiltForward) -45f else 45f
    }

    private fun orientUprightItem(ms: PoseStack, offset: Float) {
        mc.cameraEntity?.let { renderViewEntity ->
            val positionVec = renderViewEntity.position()
            val vectorForOffset = BeltHelper.getVectorForOffset(belt, offset)
            val diff = vectorForOffset.subtract(positionVec)
            val yRot = Mth.atan2(diff.x, diff.z).toFloat() + Math.PI.toFloat()
            ms.mulPose(Axis.YP.rotation(yRot))
        }
        ms.translate(0f, 3f / 32f, 1f / 16f)
    }

    private fun applyItemLayering(
        m: Matrix4f, i: Int,
        blockItem: Boolean, renderUpright: Boolean, box: Boolean,
        random: RandomSource
    ) = m.run {
        if (!blockItem && !renderUpright) { translate(0f, -0.09375f, 0f); rotate(rotX90) }
        if (blockItem && !box) translate(random.nextFloat() * 0.0625f * i, 0f, random.nextFloat() * 0.0625f * i)
        if (box) translate(0f, 0.25f, 0f)
    }

    private fun advanceBaseMatrix(base: Matrix4f, blockItem: Boolean, renderUpright: Boolean) {
        if (!renderUpright) {
            if (!blockItem) base.rotate(rotY10)
            base.translate(0f, if (blockItem) 0.015625f else 0.0625f, 0f)
        } else {
            base.translate(0f, 0f, -0.0625f)
        }
    }

    override fun _delete() {
        instances.delete()
        shadows.delete()
    }

    override fun collectCrumblingInstances(consumer: Consumer<Instance?>) {

    }

    companion object {
        private object Flags {
            const val UPDATE_TRANSFORM = 1 shl 0
            const val UPDATE_LIGHT = 1 shl 1
            const val ITEM_STUCK = 1 shl 2

            infix fun Int.has(flag: Int): Boolean {
                return (this and flag) == flag
            }
        }
        private const val EPSILON = 1E-5f

        private val UPRIGHT_CACHE = RendererReloadCache(BeltHelper::isItemUpright)
        private val RANDOM: ThreadLocal<RandomSource> = ThreadLocal.withInitial(RandomSource::createNewThreadLocalInstance)
        private val rotX90 = Axis.XP.rotationDegrees(90f)
        private val rotY10 = Axis.YP.rotationDegrees(10f)
        private val mc by lazy { Minecraft.getInstance() }
        private const val SLOPE_OFFSET = 1f / 8f
    }
}

