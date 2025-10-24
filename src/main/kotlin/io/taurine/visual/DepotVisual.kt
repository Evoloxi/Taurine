package io.taurine.visual

import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.math.Axis
import com.simibubi.create.content.kinetics.belt.BeltHelper
import com.simibubi.create.content.kinetics.belt.transport.TransportedItemStack
import com.simibubi.create.content.logistics.box.PackageItem
import com.simibubi.create.content.logistics.depot.DepotBlockEntity
import dev.engine_room.flywheel.api.instance.Instance
import dev.engine_room.flywheel.api.visual.DynamicVisual
import dev.engine_room.flywheel.api.visualization.VisualizationContext
import dev.engine_room.flywheel.lib.transform.TransformStack
import dev.engine_room.flywheel.lib.visual.AbstractBlockEntityVisual
import dev.engine_room.flywheel.lib.visual.SimpleDynamicVisual
import io.taurine.ModelCache
import io.taurine.extension.inaccessible.depotBehaviour
import io.taurine.extension.inaccessible.heldItem
import io.taurine.extension.inaccessible.incoming
import io.taurine.extension.inaccessible.processingOutputBuffer
import net.createmod.catnip.math.VecHelper
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.LightTexture
import net.minecraft.core.Direction
import net.minecraft.util.Mth
import net.minecraft.world.item.ItemDisplayContext
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.LightLayer
import net.minecraft.world.phys.Vec3
import java.util.*
import java.util.function.Consumer

class DepotVisual(
    visualizationContext: VisualizationContext, be: DepotBlockEntity, delta: Float
) : AbstractBlockEntityVisual<DepotBlockEntity>(
    visualizationContext, be, delta
), SimpleDynamicVisual, ItemRendering {

    override val itemDisplayContext = ItemDisplayContext.FIXED
    override val dispatcher by dispatcherDelegate

    private inline fun forAllIncomingItems(action: (TransportedItemStack) -> Unit) {
        val list = mutableListOf<TransportedItemStack>()
        val behaviour = blockEntity.depotBehaviour
        behaviour.heldItem?.let(list::add)
        list.addAll(behaviour.incoming)
        for (tis in list.filter { ModelCache.isSupported(it.stack) }) {
            action(tis)
        }
    }

    fun updateInstances(delta: Float) {
        val ms = PoseStack()
        val msr = TransformStack.of(ms)
        msr.translate(visualPosition)
        ms.pushPose()
        ms.translate(.5f, 15 / 16f, .5f)

        val light = LightTexture.pack(
            level.getBrightness(LightLayer.BLOCK, pos),
            level.getBrightness(LightLayer.SKY, pos)
        )
        val itemPosition = VecHelper.getCenterOf(pos)

        forAllIncomingItems { tis ->
            ms.pushPose()
            msr.nudge(0)
            val offset = Mth.lerp(delta, tis.prevBeltPosition, tis.beltPosition)
            var sideOffset = Mth.lerp(delta, tis.prevSideOffset, tis.sideOffset)

            if (tis.insertedFrom.axis.isHorizontal) {
                val offsetVec = Vec3.atLowerCornerOf(
                    tis.insertedFrom.opposite.normal
                ).scale((.5f - offset).toDouble())
                ms.translate(offsetVec.x, offsetVec.y, offsetVec.z)
                val alongX = tis.insertedFrom.clockWise.axis === Direction.Axis.X
                if (!alongX) sideOffset *= -1f
                ms.translate(if (alongX) sideOffset else 0f, 0f, if (alongX) 0f else sideOffset)
            }

            val itemStack = tis.stack
            val angle = tis.angle
            val r = Random(0)
            renderItem(ms, light, itemStack, angle.toFloat(), r, itemPosition)
            ms.popPose()
        }

        for (i in 0..< blockEntity.depotBehaviour.processingOutputBuffer.slots) {
            val stack = blockEntity.depotBehaviour.processingOutputBuffer.getStackInSlot(i)
            if (stack.isEmpty) continue
            ms.pushPose()
            msr.nudge(i)

            val renderUpright = BeltHelper.isItemUpright(stack)
            msr.rotateYDegrees(360 / 8f * i)
            ms.translate(.35f, 0f, 0f)
            if (renderUpright) msr.rotateYDegrees(-(360 / 8f * i))
            val r = Random(i + 1L)
            val angle = 360f * r.nextFloat()
            renderItem(
                ms, light, stack, if (renderUpright) angle + 90f else angle, r, itemPosition
            )
            ms.popPose()
        }
    }


    fun renderItem(
        ms: PoseStack,
        stackLight: Int,
        stack: ItemStack,
        angle: Float,
        r: Random,
        itemPosition: Vec3
    ) {
        val itemRenderer = Minecraft.getInstance().itemRenderer
        val msr = TransformStack.of(ms)
        val count = (Mth.log2((stack.count))) / 2
        val bakedModel = itemRenderer.getModel(stack, null, null, 0)
        val blockItem = bakedModel.isGui3d
        val renderUpright = BeltHelper.isItemUpright(stack)

        ms.pushPose()
        msr.rotateYDegrees(angle)

        if (renderUpright) {
            val renderViewEntity = Minecraft.getInstance().cameraEntity
            if (renderViewEntity != null) {
                val positionVec = renderViewEntity.position()
                val diff = itemPosition.subtract(positionVec)
                val yRot = (Mth.atan2(diff.x, diff.z) + Math.PI).toFloat()
                ms.mulPose(Axis.YP.rotation(yRot))
            }
            ms.translate(0f, 3 / 32f, -1 / 16f)
        }

        for (i in 0..count) {
            ms.pushPose()
            if (blockItem) ms.translate(r.nextFloat() * .0625f * i, 0f, r.nextFloat() * .0625f * i)

            if (PackageItem.isPackage(stack)) {
                ms.translate(0f, 4 / 16f, 0f)
                ms.scale(1.5f, 1.5f, 1.5f)
            } else
                ms.scale(.5f, .5f, .5f)

            if (!blockItem && !renderUpright) {
                ms.translate(0f, -3 / 16f, 0f)
                msr.rotateXDegrees(90f)
            }

            dispatcher.instances.get(stack).apply {
                setIdentityTransform()
                setTransform(ms)
                light = stackLight
                setChanged()
            }
            ms.popPose()

            if (!renderUpright) {
                if (!blockItem) msr.rotateYDegrees(10f)
                ms.translate(0f, if (blockItem) 1 / 64f else 1 / 16f, 0f)
            } else {
                ms.translate(0f, 0f, -1 / 16f)
            }
        }

        ms.popPose()
    }

    var dirty = false
    override fun beginFrame(ctx: DynamicVisual.Context) {
        if (!dirty) return
        val diff: Float = .5f - (blockEntity.depotBehaviour.heldItem?.beltPosition ?: run {
            dirty = false
            return
        })
        if (diff > 1 / 512f) {
            animate(ctx.partialTick())
        } else {
            dirty = false
        }
    }

    init {
        update(delta)
    }

    fun animate(delta: Float) {
        dispatcher.instances.resetCount()
        updateInstances(delta)
        dispatcher.instances.discardExtra()
    }

    override fun update(delta: Float) {
        dirty = true
        animate(delta)
    }

    override fun _delete() {
        dispatcher.instances.delete()
    }

    override fun collectCrumblingInstances(consumer: Consumer<Instance?>) = Unit

    override fun updateLight(partialTick: Float) {
        animate(partialTick)
    }
}