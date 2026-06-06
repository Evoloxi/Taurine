package io.taurine.visual

import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.math.Axis
import com.simibubi.create.content.kinetics.belt.BeltBlock
import com.simibubi.create.content.kinetics.belt.BeltBlockEntity
import com.simibubi.create.content.kinetics.belt.BeltHelper
import com.simibubi.create.content.kinetics.belt.BeltSlope
import com.simibubi.create.content.kinetics.belt.transport.TransportedItemStack
import com.simibubi.create.content.logistics.box.PackageItem
import dev.engine_room.flywheel.api.instance.Instance
import dev.engine_room.flywheel.api.visual.DynamicVisual
import dev.engine_room.flywheel.api.visualization.VisualizationContext
import dev.engine_room.flywheel.lib.instance.InstanceTypes
import dev.engine_room.flywheel.lib.instance.TransformedInstance
import dev.engine_room.flywheel.lib.visual.AbstractBlockEntityVisual
import dev.engine_room.vanillin.item.ItemModels
import io.taurine.ModelCache.canBeInstanced
import io.taurine.flywheel.PreservingInstanceRecycler
import io.taurine.flywheel.SmartPreservingRecycler
import io.taurine.mesh.ShadowMesh.SHADOW_MODEL
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.LightTexture
import net.minecraft.core.Direction
import net.minecraft.core.Vec3i
import net.minecraft.util.Mth
import net.minecraft.util.RandomSource
import net.minecraft.world.item.ItemDisplayContext
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.LightLayer
import net.minecraft.world.phys.Vec3
import org.joml.Matrix4f
import org.joml.Vector3f
import java.util.function.Consumer

class BeltItemLayerVisual(
    ctx: VisualizationContext, val belt: BeltBlockEntity, partialTick: Float
) : AbstractBlockEntityVisual<BeltBlockEntity>(ctx, belt, partialTick) {

    val instances = SmartPreservingRecycler<ItemStack, TransformedInstance> {
        instancerProvider().instancer(
            InstanceTypes.TRANSFORMED,
            ItemModels.get(level, it, ItemDisplayContext.FIXED)
        ).createInstance()
    }

    val shadows = PreservingInstanceRecycler {
        instancerProvider().instancer(InstanceTypes.SHADOW, SHADOW_MODEL, 10).createInstance()
    }

    private var dirty = true
    private var relight = true

    override fun updateLight(partialTick: Float) {
        dirty = true
        relight = true
    }

    init {
        update(partialTick)
    }

    override fun update(pt: Float) {
        dirty = true
    }

    /**
     * Called every frame.
     * <br>
     * The implementation is free to parallelize calls to this method.
     * You must ensure proper synchronization if you need to mutate anything outside this visual.
     * <br>
     * This method and {@link SimpleTickableVisual#tick} will never be called simultaneously.
     * <br>
     * {@link Instancer}/{@link Instance} creation/acquisition is safe here.
     */
    fun beginFrame(ctx: DynamicVisual.Context) {
        if (!belt.isController || doDistanceLimitThisFrame(ctx)) return
        val inv = belt.inventory ?: return
        if (!dirty && belt.speed == 0f && !belt.networkDirty && !hasMovingItems()) return // TODO: dynamic/upright items

        instances.resetCount()
        shadows.resetCount()

        val beltParams = BeltParams.from(belt)
        val pt = ctx.partialTick()
        val ms = PoseStack()

        ms.pushPose()
        for (transported in inv.transportedItems) {
            if (!transported.stack.canBeInstanced) continue

            if (canPreserve(transported)) {
                val count = Mth.log2(transported.stack.count) / 2 + 1
                instances.preserve(transported.stack, count)
                shadows.preserve(1)
                continue
            }

            renderTransported(pt, beltParams, transported, ms)
        }
        ms.popPose()

        instances.discardExtra()
        shadows.discardExtra()

        dirty = false
        relight = false
    }

    private fun canPreserve(transported: TransportedItemStack): Boolean {
        return !(dirty || relight) && transported.beltPosition == transported.prevBeltPosition &&
                transported.sideOffset == transported.prevSideOffset
    }

    private fun hasMovingItems(): Boolean {
        val inv = belt.inventory ?: return false
        return inv.transportedItems.any { t ->
            t.beltPosition != t.prevBeltPosition || t.sideOffset != t.prevSideOffset
        }
    }

    private fun renderTransported(
        pt: Float,
        p: BeltParams,
        transported: TransportedItemStack,
        ms: PoseStack
    ) {
        val itemStack = transported.stack
        val random = RANDOM.get().also { it.setSeed(transported.angle.toLong()) }

        val offset = lerpIfMoving(pt, transported.prevBeltPosition, transported.beltPosition)
        val sideOffset = lerpIfMoving(pt, transported.prevSideOffset, transported.sideOffset)

        val verticalMovement = if (offset < 0.5f) 0f else
            p.verticality * (Mth.clamp(offset, 0.5f, p.beltLength - 0.5f) - 0.5f)

        val offsetVec = Vector3f(p.directionVec.x.toFloat(), p.directionVec.y.toFloat(), p.directionVec.z.toFloat())
            .mul(offset)
            .also { if (verticalMovement != 0f) it.add(0f, verticalMovement, 0f) }

        val onSlope = p.slope != BeltSlope.HORIZONTAL &&
                Mth.clamp(offset, 0.5f, p.beltLength - 0.5f) == offset

        val slopeAngle = computeSlopeAngle(onSlope, p)

        val itemPos = p.beltStartOffset.add(
            (visualPosition.x + offsetVec.x).toDouble(),
            (visualPosition.y + offsetVec.y).toDouble(),
            (visualPosition.z + offsetVec.z).toDouble()
        )

        ms.setIdentity()

        val adjustedSideOffset = if (p.alongX) +sideOffset else -sideOffset
        ms.translate(
            itemPos.x + if (p.alongX) adjustedSideOffset else 0f,
            itemPos.y,
            itemPos.z + if (!p.alongX) adjustedSideOffset else 0f
        )

        val packedLight = computeLight(offset, p)

        val renderUpright = BeltHelper.isItemUpright(itemStack)
        val bakedModel = ItemModels.getModel(itemStack)
        val blockItem = bakedModel.isGui3d
        val box = PackageItem.isPackage(itemStack)
        val count = Mth.log2(itemStack.count) / 2
        val scaleValue = if (box) 1.5f else 0.5f

        if (!renderUpright) {
            ms.mulPose((if (p.slopeAlongX) Axis.ZP else Axis.XP).rotationDegrees(slopeAngle))
        }

        if (onSlope) ms.translate(0f, SLOPE_OFFSET, 0f)

        placeShadow(ms, onSlope)

        if (renderUpright) {
            orientUprightItem(ms, offset)
        }

        val rotYAngle = Axis.YP.rotationDegrees(transported.angle.toFloat())
        val baseMatrix = ms.last().pose()

        for (i in 0..count) {
            val itemMatrix = (baseMatrix.clone() as Matrix4f).apply {
                rotate(rotYAngle)
                applyItemLayering(this, i, blockItem, renderUpright, box, random)
                scale(scaleValue, scaleValue, scaleValue)
            }

            instances.get(itemStack).apply {
                setTransform(itemMatrix)
                if (packedLight != -1) light(packedLight)
                setChanged()
            }

            advanceBaseMatrix(baseMatrix, blockItem, renderUpright)
        }
    }

    private fun lerpIfMoving(pt: Float, prev: Float, current: Float): Float =
        if (belt.speed == 0f) current else Mth.lerp(pt, prev, current)

    private fun computeSlopeAngle(onSlope: Boolean, p: BeltParams): Float {
        if (!onSlope) return 0f
        val tiltForward = ((p.slope == BeltSlope.DOWNWARD) xor (p.beltFacing.axisDirection == Direction.AxisDirection.POSITIVE)) == (p.beltFacing.axis == Direction.Axis.Z)
        return if (tiltForward) -45f else 45f
    }

    private fun computeLight(offset: Float, p: BeltParams): Int {
        val shouldUpdate = dirty || relight || (
                        (offset * p.directionVec.x * 10).toInt() +
                        (offset * p.verticality    * 10).toInt() +
                        (offset * p.directionVec.z * 10).toInt()
                ) % 10 == 0

        if (!shouldUpdate) return -1

        val lightPos = visualPosition.offset(
            (p.directionVec.x * offset).toInt(),
            (p.verticality    * offset).toInt(),
            (p.directionVec.z * offset).toInt()
        )
        return LightTexture.pack(
            level.getBrightness(LightLayer.BLOCK, lightPos),
            level.getBrightness(LightLayer.SKY, lightPos)
        )
    }

    private fun placeShadow(ms: PoseStack, onSlope: Boolean) {
        if (onSlope) return
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
        if (!blockItem && !renderUpright) {
            translate(0f, -0.09375f, 0f)
            rotate(rotX90)
        }
        if (blockItem && !box) {
            translate(random.nextFloat() * 0.0625f * i, 0f, random.nextFloat() * 0.0625f * i)
        }
        if (box) {
            translate(0f, 0.25f, 0f)
        }
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

    @JvmRecord
    private data class BeltParams(
        val beltFacing     : Direction,
        val directionVec   : Vec3i,
        val slope          : BeltSlope,
        val slopeAlongX    : Boolean,
        val alongX         : Boolean,
        val verticality    : Int,
        val beltStartOffset: Vec3,
        val beltLength     : Int,
    ) {
        companion object {
            fun from(belt: BeltBlockEntity): BeltParams {
                val facing = belt.blockState.getValue(BeltBlock.HORIZONTAL_FACING)
                val slope = belt.blockState.getValue(BeltBlock.SLOPE)
                val verticality = when (slope) {
                    BeltSlope.DOWNWARD -> -1
                    BeltSlope.UPWARD -> 1
                    else -> 0
                }
                return BeltParams(
                    beltFacing             = facing,
                    directionVec           = facing.normal,
                    slope                  = slope,
                    slopeAlongX            = facing.axis == Direction.Axis.X,
                    alongX                 = facing.clockWise.axis == Direction.Axis.X,
                    verticality            = verticality,
                    beltStartOffset        = Vec3.atLowerCornerOf(facing.normal)
                                                 .scale(-.5)
                                                 .add(.5, 15 / 16.0, .5),
                    beltLength             = belt.beltLength,
                )
            }
        }
    }

    companion object {
        private val RANDOM: ThreadLocal<RandomSource> =
            ThreadLocal.withInitial(RandomSource::createNewThreadLocalInstance)
        private val rotX90 = Axis.XP.rotationDegrees(90f)
        private val rotY10 = Axis.YP.rotationDegrees(10f)
        private val mc by lazy { Minecraft.getInstance() }
        private const val SLOPE_OFFSET = 1f / 8f
    }
}