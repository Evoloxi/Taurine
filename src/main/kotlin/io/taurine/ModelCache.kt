package io.taurine

import dev.engine_room.flywheel.lib.util.RendererReloadCache
import dev.engine_room.vanillin.item.ItemModels
import net.minecraft.world.item.ItemStack

object ModelCache {
    private val SUPPORTED = RendererReloadCache<ItemStack, Boolean>(ItemModels::isSupported)

    @JvmStatic
    fun canBeInstanced(stack: ItemStack): Boolean {
        return SUPPORTED.get(stack)
    }

    inline val ItemStack.canBeInstanced: Boolean
        @JvmName($$"inline$canBeInstanced")
        get() = canBeInstanced(this)
}