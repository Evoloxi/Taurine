package io.taurine

import io.taurine.visual.TaurineVisuals
import net.minecraft.resources.ResourceLocation
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.fml.common.Mod
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent
import net.neoforged.fml.event.lifecycle.InterModEnqueueEvent
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
        MOD_BUS.addListener(this::onSetup)
        MOD_BUS.addListener(this::onInterModEnqueue)
    }

    @SubscribeEvent
    fun onSetup(event: FMLClientSetupEvent) {
        LOGGER.log(Level.INFO, "Taurine/setup")
    }

    @SubscribeEvent
    fun onInterModEnqueue(event: InterModEnqueueEvent) {
        LOGGER.log(Level.INFO, "Taurine/interqueue")
        TaurineVisuals.init()
    }

    operator fun invoke(path: String): ResourceLocation {
        return ResourceLocation.fromNamespaceAndPath(ID, path)
    }
}
