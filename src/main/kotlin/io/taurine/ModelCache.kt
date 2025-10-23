package io.taurine

import dev.engine_room.flywheel.api.model.Model
import dev.engine_room.vanillin.item.ItemModels
import io.taurine.mixin.minecraft.accessor.ItemStackAccessor
import it.unimi.dsi.fastutil.Hash
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.world.item.ItemDisplayContext
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level
import java.util.concurrent.ConcurrentHashMap

object ModelCache {
    // TODO: consider com.google.common.collect.MapMaker#MapMaker.weakValues().makeMap() or RendererReloadCache!!!
    private val table: Int2ObjectOpenHashMap<Model> = Int2ObjectOpenHashMap()

    fun hashItem(stack: ItemStack, context: ItemDisplayContext = ItemDisplayContext.FIXED): Int {
        @Suppress("CAST_NEVER_SUCCEEDS")
        val itemId = BuiltInRegistries.ITEM.getId((stack as ItemStackAccessor).nullableItem)
        val nbtSize = stack.components.size()
        val hash = (itemId shl 16) or (nbtSize shl 4) or (context.id.toInt() and 0xF)
        return hash and Int.MAX_VALUE
    }

    fun getModel(itemStack: ItemStack, level: Level, hash: Int = hashItem(itemStack)): Model? {
        if (table.containsKey(hash)) return table[hash]
        return if (isSupported(itemStack)) {
            val model = ItemModels.get(
                level,
                itemStack,
                ItemDisplayContext.BY_ID.apply(hash and 0xF)
            )
            table[hash] = model
            model
        } else {
            null
        }
    }

    private val supportedCache = ConcurrentHashMap<Int, Boolean>(32, Hash.FAST_LOAD_FACTOR)

    @JvmStatic
    @JvmOverloads
    fun isSupported(stack: ItemStack, hash: Int = hashItem(stack)): Boolean {
        return supportedCache.computeIfAbsent(hash) {
            ItemModels.isSupported(stack)
        }
    }
}