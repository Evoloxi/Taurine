package io.taurine.visual

import com.simibubi.create.foundation.blockEntity.SmartBlockEntity
import dev.engine_room.flywheel.api.instance.Instance
import dev.engine_room.flywheel.api.model.Model
import dev.engine_room.flywheel.api.visualization.VisualizationContext
import dev.engine_room.flywheel.lib.instance.InstanceTypes
import dev.engine_room.flywheel.lib.instance.TransformedInstance
import dev.engine_room.flywheel.lib.visual.AbstractBlockEntityVisual
import io.taurine.SmartPreservingRecycler
import io.taurine.ModelCache
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
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
    protected val hashToModel = Int2ObjectOpenHashMap<Model>()
    abstract val itemDisplayContext: ItemDisplayContext

    val instances = SmartPreservingRecycler<Int, TransformedInstance> {
        instancerProvider().instancer(
            InstanceTypes.TRANSFORMED,
            hashToModel[it]!!
        ).createInstance()
    }

    protected fun loadModel(item: ItemStack, hash: Int = ModelCache.hashItem(item, itemDisplayContext)): Model? {
        return if (!hashToModel.containsKey(hash) && ModelCache.isSupported(item, hash)) {
            val model = ModelCache.getModel(item, level, hash)
            hashToModel.put(hash, model)
        } else {
            hashToModel[hash]
        }
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

    override fun collectCrumblingInstances(consumer: Consumer<Instance?>) {}
}