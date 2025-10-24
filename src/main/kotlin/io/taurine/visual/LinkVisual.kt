package io.taurine.visual

import com.mojang.blaze3d.vertex.PoseStack
import com.simibubi.create.content.redstone.link.LinkBehaviour
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity
import com.simibubi.create.infrastructure.config.AllConfigs
import dev.engine_room.flywheel.api.instance.Instance
import dev.engine_room.flywheel.api.visualization.VisualizationContext
import dev.engine_room.flywheel.lib.visual.AbstractBlockEntityVisual
import io.taurine.ModelCache
import io.taurine.extension.inaccessible.firstSlot
import io.taurine.extension.inaccessible.frequencyFirst
import io.taurine.extension.inaccessible.frequencyLast
import io.taurine.extension.inaccessible.secondSlot
import io.taurine.extension.translate
import net.createmod.catnip.data.Iterate
import net.createmod.catnip.math.VecHelper
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.LightTexture
import net.minecraft.world.item.ItemDisplayContext
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.LightLayer
import java.util.function.Consumer

class LinkVisual<T : SmartBlockEntity>(
    visualizationContext: VisualizationContext, be: T, delta: Float
) : AbstractBlockEntityVisual<T>(
    visualizationContext, be, delta
), ValueBoxVisual {
    override val itemDisplayContext = ItemDisplayContext.FIXED
    override val itemRendering by itemRenderingDelegate

    override fun renderOnBlockEntity(ms: PoseStack) {
        if (blockEntity.isRemoved) return

        /*val cameraEntity = Minecraft.getInstance().cameraEntity
        val max = AllConfigs.client().filterItemRenderDistance.f
        if (!be.isVirtual && cameraEntity != null && cameraEntity.position().distanceToSqr(
                VecHelper.getCenterOf(be.blockPos)
        ) > (max * max)) return*/

        val behaviour = blockEntity.getBehaviour(LinkBehaviour.TYPE) ?: return

        for (first in Iterate.trueAndFalse) {
            val transform = if (first) behaviour.firstSlot else behaviour.secondSlot
            val stack = if (first) behaviour.frequencyFirst.stack else behaviour.frequencyLast.stack

            ms.pushPose()
            transform.transform(level, pos, blockEntity.blockState, ms)
            renderItemIntoValueBox(stack, ms)
            ms.popPose()
        }
    }

    override fun update(partialTick: Float) {
        itemRendering.instances.resetCount()
        val ms = PoseStack().apply {
            translate(visualPos)
        }
        renderOnBlockEntity(ms)
        updateLight(partialTick)
        itemRendering.instances.discardExtra()
    }

    override fun _delete() {
        itemRendering.instances.delete()
    }

    override fun collectCrumblingInstances(p0: Consumer<Instance?>?) {
    }

    override fun updateLight(p0: Float) {
        val packed = LightTexture.pack(
            level.getBrightness(LightLayer.BLOCK, pos),
            level.getBrightness(LightLayer.SKY, pos)
        )
        itemRendering.instances.applyToAll {
            light = packed
            setChanged()
        }
    }

    init {
        update(delta)
    }
}