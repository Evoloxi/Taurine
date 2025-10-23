package io.taurine.visual

import com.mojang.blaze3d.vertex.PoseStack
import com.simibubi.create.content.redstone.link.LinkBehaviour
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity
import com.simibubi.create.infrastructure.config.AllConfigs
import dev.engine_room.flywheel.api.visualization.VisualizationContext
import io.taurine.ModelCache
import io.taurine.extension.inaccessible.firstSlot
import io.taurine.extension.inaccessible.frequencyFirst
import io.taurine.extension.inaccessible.frequencyLast
import io.taurine.extension.inaccessible.secondSlot
import io.taurine.extension.translate
import net.createmod.catnip.data.Iterate
import net.createmod.catnip.math.VecHelper
import net.minecraft.client.Minecraft
import net.minecraft.world.item.ItemDisplayContext
import net.minecraft.world.item.ItemStack

class LinkVisual<T : SmartBlockEntity>(
    visualizationContext: VisualizationContext, be: T, delta: Float
) : ItemRenderingBlockEntityVisual<T>(
    visualizationContext, be, delta
), ValueBoxVisual {
    override val itemDisplayContext = ItemDisplayContext.FIXED

    override fun renderOnBlockEntity(ms: PoseStack) {
        if (be.isRemoved) return

        /*val cameraEntity = Minecraft.getInstance().cameraEntity
        val max = AllConfigs.client().filterItemRenderDistance.f
        if (!be.isVirtual && cameraEntity != null && cameraEntity.position().distanceToSqr(
                VecHelper.getCenterOf(be.blockPos)
        ) > (max * max)) return*/

        val behaviour = be.getBehaviour(LinkBehaviour.TYPE) ?: return

        for (first in Iterate.trueAndFalse) {
            val transform = if (first) behaviour.firstSlot else behaviour.secondSlot
            val stack = if (first) behaviour.frequencyFirst.stack else behaviour.frequencyLast.stack

            ms.pushPose()
            transform.transform(be.getLevel(), be.blockPos, be.blockState, ms)
            renderItemIntoValueBox(stack, ms)
            ms.popPose()
        }
    }

    override fun update(partialTick: Float) {
        instances.resetCount()
        val ms = PoseStack().apply {
            translate(visualPos)
        }
        renderOnBlockEntity(ms)
        updateLight(partialTick)
        instances.discardExtra()
    }

    init {
        update(delta)
    }
}