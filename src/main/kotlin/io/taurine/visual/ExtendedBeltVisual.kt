package io.taurine.visual

import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.math.Axis
import com.simibubi.create.content.kinetics.belt.*
import com.simibubi.create.content.kinetics.belt.transport.TransportedItemStack
import com.simibubi.create.content.logistics.box.PackageItem
import com.simibubi.create.foundation.mixin.accessor.LevelRendererAccessor
import dev.engine_room.flywheel.api.model.Model
import dev.engine_room.flywheel.api.visual.DynamicVisual
import dev.engine_room.flywheel.api.visual.LightUpdatedVisual
import dev.engine_room.flywheel.api.visualization.VisualizationContext
import dev.engine_room.flywheel.lib.instance.InstanceTypes
import dev.engine_room.flywheel.lib.instance.TransformedInstance
import dev.engine_room.flywheel.lib.visual.SimpleDynamicVisual
import dev.engine_room.vanillin.item.ItemModels
import io.taurine.SmartPreservingRecycler
import io.taurine.ModelCache
import io.taurine.ModelCache.hashItem
import io.taurine.PreservingInstanceRecycler
import io.taurine.mesh.ShadowMesh.SHADOW_MODEL
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import net.createmod.ponder.api.level.PonderLevel
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.LightTexture
import net.minecraft.core.Direction
import net.minecraft.core.Vec3i
import net.minecraft.util.Mth
import net.minecraft.util.RandomSource
import net.minecraft.world.level.Level
import net.minecraft.world.level.LightLayer
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.Vec3
import org.joml.Matrix4f
import org.joml.Vector3f

class ExtendedBeltVisual(
    ctx: VisualizationContext, val belt: BeltBlockEntity, partialTick: Float
) : BeltVisual(
    ctx, belt, partialTick
), SimpleDynamicVisual, LightUpdatedVisual {

    private val instances = SmartPreservingRecycler<Int, TransformedInstance> {
        updateModelsForItems(belt.inventory.transportedItems)
        instancerProvider().instancer(
            InstanceTypes.TRANSFORMED, // TODO: custom type for uniform motion
            hashToModel[it]!!
        ).createInstance()
    }

    private val hashToModel = Int2ObjectOpenHashMap<Model>()
    private var modelKeys = hashToModel.keys

    fun updateModelsForItems(items: List<TransportedItemStack>) {
        for (item in items) {
            val hash = hashItem(item.stack)
            if (!modelKeys.contains(hash) && ModelCache.isSupported(item.stack, hash)) {
                val model = ModelCache.getModel(item.stack, belt.level!!, hash)!!
                hashToModel[hash] = model
            }
        }
        modelKeys = hashToModel.keys
    }

    val shadows = PreservingInstanceRecycler {
        instancerProvider().instancer(InstanceTypes.SHADOW, SHADOW_MODEL, 1).createInstance()
    }

    fun shouldCullItem(itemPos: Vec3, level: Level): Boolean {
        if (level is PonderLevel)
            return false

        val accessor = Minecraft.getInstance().levelRenderer as LevelRendererAccessor;
        val frustum = accessor.`create$getCapturedFrustum`() ?: accessor.`create$getCullingFrustum`()

        val itemBB = AABB(
            itemPos.x - 0.25,
            itemPos.y - 0.25,
            itemPos.z - 0.25,
            itemPos.x + 0.25,
            itemPos.y + 0.25,
            itemPos.z + 0.25
        )

        return !frustum!!.isVisible(itemBB)
    }

    var relight = true
    override fun updateLight(partialTick: Float) {
        dirty = true
        relight = true
        super.updateLight(partialTick)
    }

    private fun animate(
        partialTick: Float,
        beltFacing: Direction,
        directionVec: Vec3i,
        slope: BeltSlope,
        verticality: Int,
        slopeAlongX: Boolean,
        beltStartOffset: Vec3,
        beltLength: Int,
        beltSpeed: Float,
        transported: TransportedItemStack,
        pPoseStack: PoseStack,
        random: RandomSource,
        hash: Int
    ) {
        val itemStack = transported.stack
        val bakedModel = ItemModels.getModel(itemStack)
        random.setSeed(transported.angle.toLong())

        val offset = if (beltSpeed == 0f) {
            transported.beltPosition
        } else {
            Mth.lerp(partialTick, transported.prevBeltPosition, transported.beltPosition)
        }

        val sideOffset = if (beltSpeed == 0f) {
            transported.sideOffset
        } else {
            Mth.lerp(partialTick, transported.prevSideOffset, transported.sideOffset)
        }

        val verticalMovement = if (offset < 0.5f) 0f else {
            verticality * (Mth.clamp(offset, 0.5f, beltLength - 0.5f) - 0.5f)
        }
        // TODO: start: combine arithmetics & avoid creating objects
        val offsetVec = Vector3f(directionVec.x.toFloat(), directionVec.y.toFloat(), directionVec.z.toFloat())
            .mul(offset)
        if (verticalMovement != 0f) offsetVec.add(0f, verticalMovement, 0f)

        val onSlope = slope != BeltSlope.HORIZONTAL &&
                Mth.clamp(offset, 0.5f, beltLength - 0.5f) == offset
        val tiltForward = ((slope == BeltSlope.DOWNWARD) xor
                (beltFacing.axisDirection == Direction.AxisDirection.POSITIVE)) ==
                (beltFacing.axis == Direction.Axis.Z)
        val slopeAngle = if (onSlope) if (tiltForward) -45f else 45f else 0f

        val itemPos = beltStartOffset.add(
            visualPosition.x.toDouble() + offsetVec.x,
            visualPosition.y.toDouble() + offsetVec.y,
            visualPosition.z.toDouble() + offsetVec.z
        )
        // TODO: end
        /*if (shouldCullItem(itemPos, level)) { // TODO: move to somwhere else?
            return
        }*/

        pPoseStack.setIdentity()
        //TransformStack.of(pPoseStack).nudge(transported.angle)

        val alongX = beltFacing.clockWise.axis == Direction.Axis.X
        val adjustedSideOffset = if (alongX) +sideOffset else -sideOffset
        pPoseStack.translate(
            itemPos.x + (if (alongX) adjustedSideOffset else 0f),
            itemPos.y,
            itemPos.z + (if (!alongX) adjustedSideOffset else 0f)
        )


        val updateLight = dirty || relight || ((
                (offset * directionVec.x * 10).toInt() +
                (offset * verticality *    10).toInt() +
                (offset * directionVec.z * 10).toInt()) % 10 == 0)

        val stackLight = if (updateLight) {
            val lightPos = visualPosition.offset(
                (directionVec.x * offset).toInt(),
                (verticality * offset).toInt(),
                (directionVec.z * offset).toInt()
            )
            LightTexture.pack(
                level.getBrightness(LightLayer.BLOCK, lightPos),
                level.getBrightness(LightLayer.SKY, lightPos)
            )
        } else 0

        val renderUpright = BeltHelper.isItemUpright(itemStack)
        val blockItem = bakedModel.isGui3d
        val count = Mth.log2(itemStack.count) / 2
        val slopeOffset = 1f / 8f

        if (!renderUpright) {
            pPoseStack.mulPose(
                (if (slopeAlongX) Axis.ZP else Axis.XP).rotationDegrees(slopeAngle)
            )
        }

        if (onSlope) {
            pPoseStack.translate(0f, slopeOffset, 0f)
        }

        if (!onSlope) {
            shadows.get().apply {
                val matrix = pPoseStack.last().pose()

                val sx = matrix.m30()
                val sy = matrix.m31() - 0.12f  // -1/8 + 0.005
                val sz = matrix.m32()

                x = sx - 0.2f
                y = sy
                z = sz - 0.2f
                entityX = sx
                entityZ = sz
                radius = 0.2f
                alpha = 0.5f
                sizeX = 0.4f
                sizeZ = 0.4f

                setChanged()
            }
        }

        if (renderUpright) {
            mc.cameraEntity?.let { renderViewEntity ->
                val positionVec = renderViewEntity.position()
                val vectorForOffset = BeltHelper.getVectorForOffset(belt, offset)
                val diff = vectorForOffset.subtract(positionVec)
                val yRot = Mth.atan2(diff.x, diff.z).toFloat() + Math.PI.toFloat()
                pPoseStack.mulPose(Axis.YP.rotation(yRot))
            }
            pPoseStack.translate(0f, 3f / 32f, 1f / 16f)
        }

        val box = PackageItem.isPackage(itemStack)
        val rotYAngle = Axis.YP.rotationDegrees(transported.angle.toFloat())
        val scaleValue = if (box) 1.5f else 0.5f

        val baseMatrix = pPoseStack.last().pose()
        for (i in 0..count) {
            val itemMatrix = baseMatrix.clone() as Matrix4f
            itemMatrix.rotate(rotYAngle)

            if (!blockItem && !renderUpright) {
                itemMatrix.translate(0.0f, -0.09375f, 0.0f)
                itemMatrix.rotate(rotX90)
            }

            if (blockItem && !box) {
                itemMatrix.translate(
                    random.nextFloat() * 0.0625f * i,
                    0f,
                    random.nextFloat() * 0.0625f * i
                )
            }

            if (box) {
                itemMatrix.translate(0.0f, 0.25f, 0.0f)
            }

            itemMatrix.scale(scaleValue, scaleValue, scaleValue)

            instances.get(hash).apply {
                setTransform(itemMatrix)
                if (updateLight) light(stackLight)
                setChanged()
            }

            if (!renderUpright) {
                if (!blockItem) {
                    baseMatrix.rotate(rotY10)
                }
                baseMatrix.translate(0.0f, if (blockItem) 0.015625f else 0.0625f, 0.0f)
            } else {
                baseMatrix.translate(0.0f, 0.0f, -0.0625f)
            }
        }
    }

    val pPoseStack = PoseStack()

    var dirty = true
    override fun update(pt: Float) {
        dirty = true
        updateModelsForItems(belt.inventory.transportedItems)
        super.update(pt)
    }

    override fun beginFrame(ctx: DynamicVisual.Context) {
        if (!belt.isController) return

        val inv = belt.inventory ?: return

        if (!dirty && belt.speed == 0f && !belt.networkDirty) return

        instances.resetCount()
        shadows.resetCount()

        val beltFacing = belt.blockState.getValue(BeltBlock.HORIZONTAL_FACING)
        val directionVec = beltFacing.normal
        val slope = belt.blockState.getValue(BeltBlock.SLOPE)
        val slopeAlongX = beltFacing.axis == Direction.Axis.X
        val beltStartOffset = Vec3.atLowerCornerOf(directionVec)
            .scale(-.5)
            .add(.5, (15 / 16.0), .5)
        val verticality = if (slope == BeltSlope.DOWNWARD) -1 else if (slope == BeltSlope.UPWARD) 1 else 0
        pPoseStack.pushPose()
        for (stack in inv.transportedItems) {
            val hash = hashItem(stack.stack)
            if (ModelCache.isSupported(stack.stack, hash)) {
                if (!dirty && stack.beltPosition == stack.prevBeltPosition && stack.sideOffset == stack.prevSideOffset) { // TODO dynamic aka upright items
                    val count = Mth.log2(stack.stack.count) / 2 + 1
                    instances.preserve(hash, count)
                    shadows.preserve(1)
                    continue
                }
                animate(
                    ctx.partialTick(),
                    beltFacing,
                    directionVec,
                    slope,
                    verticality,
                    slopeAlongX,
                    beltStartOffset,
                    belt.beltLength,
                    belt.getSpeed(),
                    stack,
                    pPoseStack,
                    RANDOM.get(),
                    hash
                )
            }
        }

        pPoseStack.popPose()
        instances.discardExtra()
        shadows.discardExtra()

        dirty = false
        relight = false
    }

    override fun _delete() {
        instances.delete()
        shadows.delete()
        super._delete()
    }

    companion object {
        private val RANDOM: ThreadLocal<RandomSource> = ThreadLocal.withInitial(RandomSource::createNewThreadLocalInstance)
        private val rotX90 = Axis.XP.rotationDegrees(90f)
        private val rotY10 = Axis.YP.rotationDegrees(10f)
        private val mc by lazy { Minecraft.getInstance() }
    }
}