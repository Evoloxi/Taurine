package io.taurine.visual

import com.mojang.blaze3d.vertex.PoseStack
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity
import dev.engine_room.flywheel.api.instance.Instance
import dev.engine_room.flywheel.api.visualization.VisualizationContext
import dev.engine_room.flywheel.lib.instance.InstanceTypes
import dev.engine_room.flywheel.lib.instance.TransformedInstance
import dev.engine_room.flywheel.lib.visual.AbstractBlockEntityVisual
import dev.engine_room.vanillin.item.ItemModels
import io.taurine.SmartPreservingRecycler
import net.minecraft.client.renderer.LightTexture
import net.minecraft.world.item.ItemDisplayContext
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.LightLayer
import java.util.function.Consumer

abstract class ItemRenderingBlockEntityVisual<T : SmartBlockEntity>(
    visualizationContext: VisualizationContext, val be: T, delta: Float
) : AbstractBlockEntityVisual<T>(
    visualizationContext, be, delta
) {
    abstract val itemDisplayContext: ItemDisplayContext

    val instances = SmartPreservingRecycler<ItemStack, TransformedInstance> {
        instancerProvider().instancer(
            InstanceTypes.TRANSFORMED,
            ItemModels.get(
                level,
                it,
                itemDisplayContext
            )
        ).createInstance()
    }

    override fun _delete() {
        instances.delete()
    }

    override fun updateLight(partialTick: Float) {
        val packed = LightTexture.pack(
            level.getBrightness(LightLayer.BLOCK, pos),
            level.getBrightness(LightLayer.SKY, pos)
        )
        instances.applyToAll {
            light = packed
            setChanged()
        }
    }

    fun renderItem(ms: PoseStack, item: ItemStack) {
        instances.get(item).apply {
            setIdentityTransform()
            setTransform(ms)
            setChanged()
        }
    }

    override fun collectCrumblingInstances(consumer: Consumer<Instance?>) {}
}