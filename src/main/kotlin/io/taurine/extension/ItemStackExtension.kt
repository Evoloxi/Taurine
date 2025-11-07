package io.taurine.extension

import io.taurine.ModelCache
import net.minecraft.world.item.ItemStack

interface ItemStackExtension {
    @Suppress("FunctionName")
    fun `taurine$getCanBeInstanced`(): Boolean
}

inline val ItemStack.canBeInstanced: Boolean
    @JvmName("canBeInstanced")
    get() = ModelCache.isSupported(this)

@JvmRecord // remove
data class ReloadVolatileKey(val id: Int, val value: Boolean)