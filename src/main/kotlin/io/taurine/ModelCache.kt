package io.taurine

import dev.engine_room.flywheel.api.model.Model
import dev.engine_room.flywheel.lib.material.Materials
import dev.engine_room.flywheel.lib.util.RendererReloadCache
import dev.engine_room.vanillin.item.ItemModels
import dev.engine_room.vanillin.item.ItemModels.*
import io.taurine.mixin.minecraft.accessor.ItemStackAccessor
import net.minecraft.client.Minecraft
import net.minecraft.client.resources.model.BakedModel
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.world.item.ItemDisplayContext
import net.minecraft.world.item.ItemStack

object ModelCache {
    // TODO: consider com.google.common.collect.MapMaker#MapMaker.weakValues().makeMap() or RendererReloadCache!!!
    //private val table: Int2ObjectOpenHashMap<Model> = Int2ObjectOpenHashMap()

    /*@Deprecated("replace with record", level = DeprecationLevel.ERROR)
    fun hashItem(stack: ItemStack, context: ItemDisplayContext = ItemDisplayContext.FIXED): Int {
        @Suppress("CAST_NEVER_SUCCEEDS")
        val itemId = BuiltInRegistries.ITEM.getId((stack as ItemStackAccessor).nullableItem)
        val nbtSize = stack.components.size()
        val hash = (itemId shl 16) or (nbtSize shl 4) or (context.id.toInt() and 0xF)
        return hash and Int.MAX_VALUE
    }*/

    //TODO: maybe we need to account for ponder levels too?
    /*fun getModel(stack: ItemStack, context: ItemDisplayContext = ItemDisplayContext.FIXED): Model? {
        return MODEL.get(getKey(stack, context))
    }*/

/*    fun getKey(stack: ItemStack, context: ItemDisplayContext): BakedModelKey {
        val baked = ItemModels.getModel(stack)
        return BakedModelKey(baked, context, Materials.SOLID_BLOCK, stack.hasFoil())
    }*/

    /*fun getModel(key: BakedModelKey): Model? {
        return MODEL.get(key)
    }*/

    //private val supportedCache = ConcurrentHashMap<Int, Boolean>(32, Hash.FAST_LOAD_FACTOR)
    private val SUPPORTED = RendererReloadCache<ItemStack, Boolean> {
        ItemModels.isSupported(it)
    }

    @JvmStatic
    fun isSupported(stack: ItemStack): Boolean {
        return SUPPORTED.get(stack)
    }
}