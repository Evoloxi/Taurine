package io.taurine.visual

import com.mojang.blaze3d.vertex.PoseStack
import com.simibubi.create.AllBlocks
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity
import com.simibubi.create.foundation.blockEntity.behaviour.ValueBoxTransform
import com.simibubi.create.foundation.blockEntity.behaviour.filtering.FilteringBehaviour
import com.simibubi.create.foundation.blockEntity.behaviour.filtering.SidedFilteringBehaviour
import dev.engine_room.flywheel.api.instance.Instance
import dev.engine_room.flywheel.api.visualization.VisualizationContext
import dev.engine_room.flywheel.lib.visual.AbstractBlockEntityVisual
import io.taurine.extension.translate
import net.createmod.catnip.data.Iterate
import net.minecraft.client.renderer.LightTexture
import net.minecraft.world.item.ItemDisplayContext
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.LightLayer
import net.minecraft.world.level.block.state.BlockState
import java.util.function.Consumer

class FilterVisual<T : SmartBlockEntity>(
    visualizationContext: VisualizationContext, be: T, delta: Float
) : AbstractBlockEntityVisual<T>(
    visualizationContext, be, delta
), ValueBoxVisual {
    override val itemDisplayContext = ItemDisplayContext.FIXED
    override val dispatcher by dispatcherDelegate

    override fun renderOnBlockEntity(ms: PoseStack) {
        if (blockEntity.isRemoved) return

        for (b in blockEntity.allBehaviours) {
            if (b !is FilteringBehaviour) continue

            // TODO: limit render distance, may be not necessary tho
            /*if (!be.isVirtual) {
                val cameraEntity: Entity? = Minecraft.getInstance().cameraEntity
                if (cameraEntity != null && level == cameraEntity.level()) {
                    val max = b.renderDistance
                    if (cameraEntity.position().distanceToSqr(VecHelper.getCenterOf(blockPos)) > max * max) {
                        continue
                    }
                }
            }*/

            if (!b.isActive) continue
            if (b.filter.isEmpty && b !is SidedFilteringBehaviour) continue

            val slotPositioning: ValueBoxTransform = b.slotPositioning
            val blockState: BlockState = blockEntity.blockState

            if (slotPositioning is ValueBoxTransform.Sided) {
                val side = slotPositioning.side
                for (d in Iterate.directions) {
                    val filter: ItemStack = b.getFilter(d)
                    if (filter.isEmpty) continue

                    slotPositioning.fromSide(d)
                    if (!slotPositioning.shouldRender(level, pos, blockState)) continue

                    ms.pushPose()
                    slotPositioning.transform(level, pos, blockState, ms)
                    if (AllBlocks.CONTRAPTION_CONTROLS.has(blockState))
                    //TODO: ValueBoxRenderer.renderFlatItemIntoValueBox(filter, ms, buffer, light, overlay)
                    else
                        renderItemIntoValueBox(filter, ms)
                    ms.popPose()
                }
                slotPositioning.fromSide(side)
                return
            } else if (slotPositioning.shouldRender(level, pos, blockState)) {
                ms.pushPose()
                slotPositioning.transform(level, visualPosition, blockState, ms)
                renderItemIntoValueBox(b.filter, ms)
                ms.popPose()
            }
        }
    }

    init {
        update(delta)
    }

    override fun update(partialTick: Float) {
        dispatcher.instances.resetCount()
        val ms = PoseStack().apply {
            translate(visualPosition)
        }
        renderOnBlockEntity(ms)
        updateLight(partialTick)
        dispatcher.instances.discardExtra()
    }

    override fun _delete() {
        dispatcher.instances.delete()
    }

    override fun collectCrumblingInstances(p0: Consumer<Instance?>?) {

    }

    override fun updateLight(p0: Float) {
        val packed = LightTexture.pack(
            level.getBrightness(LightLayer.BLOCK, pos),
            level.getBrightness(LightLayer.SKY, pos)
        )
        dispatcher.updateLight(packed)
    }
}