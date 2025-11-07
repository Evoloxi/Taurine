package io.taurine

import com.google.common.collect.MapMaker
import dev.engine_room.flywheel.api.event.EndClientResourceReloadEvent
import dev.engine_room.flywheel.api.event.ReloadLevelRendererEvent
import dev.engine_room.flywheel.lib.util.RendererReloadCache
import dev.engine_room.vanillin.item.ItemModels
import it.unimi.dsi.fastutil.objects.Reference2BooleanOpenHashMap
import net.minecraft.world.item.ItemStack
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.fml.common.EventBusSubscriber
import java.lang.ref.WeakReference
import java.util.IdentityHashMap

object ModelCache {

    fun reload(event: EndClientResourceReloadEvent) {
        //SUPPORTED.clear()
    }
    // need something better fr
    private val SUPPORTED = RendererReloadCache<ItemStack, Boolean>(ItemModels::isSupported)

    @JvmStatic
    fun isSupported(stack: ItemStack): Boolean {
        return SUPPORTED.get(stack)
    }
}