package io.taurine.visual

import dev.engine_room.flywheel.lib.visual.AbstractBlockEntityVisual
import io.taurine.extension.inaccessible.instancerProvider
import io.taurine.extension.inaccessible.level
import net.minecraft.world.level.block.entity.BlockEntity
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

class ItemRenderingDelegate<T : BlockEntity>(
    private val visual: AbstractBlockEntityVisual<T>
) : ReadOnlyProperty<ItemRendering, ItemRenderingHelper> {
    private var helper: ItemRenderingHelper? = null

    override fun getValue(thisRef: ItemRendering, property: KProperty<*>): ItemRenderingHelper {
        return helper ?: ItemRenderingHelper(
            visual::instancerProvider,
            visual.level,
            thisRef.itemDisplayContext
        ).also { helper = it }
    }
}

val <T : BlockEntity> AbstractBlockEntityVisual<T>.dispatcherDelegate
    get() = ItemRenderingDelegate(this)