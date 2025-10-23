package io.taurine.visual

import com.mojang.blaze3d.vertex.PoseStack
import com.simibubi.create.AllBlocks
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity
import com.simibubi.create.foundation.blockEntity.behaviour.ValueBoxTransform
import com.simibubi.create.foundation.blockEntity.behaviour.filtering.FilteringBehaviour
import com.simibubi.create.foundation.blockEntity.behaviour.filtering.SidedFilteringBehaviour
import dev.engine_room.flywheel.api.visualization.VisualizationContext
import io.taurine.ModelCache
import io.taurine.extension.translate
import net.createmod.catnip.data.Iterate
import net.minecraft.core.BlockPos
import net.minecraft.world.item.ItemDisplayContext
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.block.state.BlockState

class FilterVisual<T : SmartBlockEntity>(
    visualizationContext: VisualizationContext, be: T, delta: Float
) : ItemRenderingBlockEntityVisual<T>(
    visualizationContext, be, delta
), ValueBoxVisual {
    override val itemDisplayContext = ItemDisplayContext.FIXED



    override fun renderOnBlockEntity(ms: PoseStack) {
        if (be.isRemoved) return

        val blockPos: BlockPos = be.blockPos

        for (b in be.allBehaviours) {
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
            val blockState: BlockState = be.blockState

            if (slotPositioning is ValueBoxTransform.Sided) {
                val side = slotPositioning.side
                for (d in Iterate.directions) {
                    val filter: ItemStack = b.getFilter(d)
                    if (filter.isEmpty) continue

                    slotPositioning.fromSide(d)
                    if (!slotPositioning.shouldRender(level, blockPos, blockState)) continue

                    ms.pushPose()
                    slotPositioning.transform(level, blockPos, blockState, ms)
                    if (AllBlocks.CONTRAPTION_CONTROLS.has(blockState))
                    //TODO: ValueBoxRenderer.renderFlatItemIntoValueBox(filter, ms, buffer, light, overlay)
                    else
                        renderItemIntoValueBox(filter, ms)
                    ms.popPose()
                }
                slotPositioning.fromSide(side)
                return
            } else if (slotPositioning.shouldRender(level, blockPos, blockState)) {
                ms.pushPose()
                slotPositioning.transform(level, blockPos, blockState, ms)
                renderItemIntoValueBox(b.filter, ms)
                ms.popPose()
            }
        }
    }

    init {
        update(delta)
    }

    override fun update(partialTick: Float) {
        instances.resetCount()
        val ms = PoseStack().apply {
            translate(visualPosition)
        }
        renderOnBlockEntity(ms)
        updateLight(partialTick)
        instances.discardExtra()
    }

    override fun renderItem(ms: PoseStack, item: ItemStack)  {
        val key = ModelCache.hashItem(item, ItemDisplayContext.FIXED)
        loadModel(item, key) ?: return
        instances.get(key).apply {
            setIdentityTransform()
            setTransform(ms)
            setChanged()
        }
    }
}