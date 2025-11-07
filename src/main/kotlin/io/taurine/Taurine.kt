package io.taurine

import dev.engine_room.flywheel.api.event.EndClientResourceReloadEvent
import net.minecraft.resources.ResourceLocation
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.fml.common.Mod
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent
import org.apache.logging.log4j.Level
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import thedarkcolour.kotlinforforge.neoforge.forge.MOD_BUS

@Mod(Taurine.ID)
object Taurine {
    const val ID = "taurine"

    val LOGGER: Logger = LogManager.getLogger(ID)

    init {
        LOGGER.log(Level.INFO, "Taurine/init")
        MOD_BUS.addListener<FMLClientSetupEvent>(this::onSetup)
    }

    @SubscribeEvent
    fun onSetup(event: FMLClientSetupEvent) {
        MOD_BUS.addListener<EndClientResourceReloadEvent>(ModelCache::reload)
        LOGGER.log(Level.INFO, "Taurine/setup")
    }

    operator fun invoke(path: String): ResourceLocation {
        return ResourceLocation.fromNamespaceAndPath(ID, path)
    }
}
