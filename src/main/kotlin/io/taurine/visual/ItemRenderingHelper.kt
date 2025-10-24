package io.taurine.visual

import com.mojang.blaze3d.vertex.PoseStack
import dev.engine_room.flywheel.api.instance.InstancerProvider
import dev.engine_room.flywheel.lib.instance.InstanceTypes
import dev.engine_room.flywheel.lib.instance.TransformedInstance
import dev.engine_room.vanillin.item.ItemModels
import io.taurine.flywheel.SmartPreservingRecycler
import net.minecraft.world.item.ItemDisplayContext
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level

class ItemRenderingHelper(
    private val instancerProvider: () -> InstancerProvider,
    private val level: Level,
    val itemDisplayContext: ItemDisplayContext
) {
    val instances = SmartPreservingRecycler<ItemStack, TransformedInstance> {
        instancerProvider().instancer(
            InstanceTypes.TRANSFORMED,
            ItemModels.get(level, it, itemDisplayContext)
        ).createInstance()
    }

    fun delete() {
        instances.delete()
    }

    fun updateLight(packed: Int) {
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
}