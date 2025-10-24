package io.taurine.visual

import net.minecraft.world.item.ItemDisplayContext

interface ItemRendering {
    val itemDisplayContext: ItemDisplayContext
    val itemRendering: ItemRenderingHelper
}

