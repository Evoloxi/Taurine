package io.taurine.visual

import com.mojang.blaze3d.vertex.PoseStack
import com.simibubi.create.content.kinetics.simpleRelays.AbstractSimpleShaftBlock
import net.minecraft.client.Minecraft
import net.minecraft.client.resources.model.BakedModel
import net.minecraft.tags.BlockTags
import net.minecraft.world.item.BlockItem
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.FenceBlock

interface ValueBoxVisual : ItemRendering {

    fun renderOnBlockEntity(ms: PoseStack)

    fun renderItemIntoValueBox(
        filter: ItemStack,
        ms: PoseStack
    ) {
        val mc = Minecraft.getInstance()
        val modelWithOverrides: BakedModel = mc.itemRenderer.getModel(filter, null, null, 0)
        val blockItem = modelWithOverrides.isGui3d
        val scale = (if (!blockItem) 0.5f else 1f) + 1 / 64f
        val zOffset = (if (!blockItem) -0.15f else 0f) + customZOffset(filter.item)
        ms.scale(scale, scale, scale)
        ms.translate(0f, 0f, zOffset)
        dispatcher.renderItem(ms, filter)
    }

    private fun customZOffset(item: Item): Float {
        val nudge = -.1f
        if (item is BlockItem) {
            val block = item.block
            if (block is AbstractSimpleShaftBlock)
                return nudge
            if (block is FenceBlock)
                return nudge
            if (block.builtInRegistryHolder().`is`(BlockTags.BUTTONS))
                return nudge
            if (block == Blocks.END_ROD)
                return nudge
        }
        return 0f
    }
}