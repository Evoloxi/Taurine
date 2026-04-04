package io.taurine

import net.minecraft.resources.ResourceLocation
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent
import org.apache.logging.log4j.Level
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import thedarkcolour.kotlinforforge.forge.MOD_BUS

@Mod(Taurine.ID)
object Taurine {
    const val ID = "taurine"

    val LOGGER: Logger = LogManager.getLogger(ID)

    init {
        LOGGER.log(Level.INFO, "Taurine/init")
        MOD_BUS.addListener(this::onSetup)
    }

    @SubscribeEvent
    fun onSetup(event: FMLClientSetupEvent) {
        LOGGER.log(Level.INFO, "Taurine/setup")
    }

    operator fun invoke(path: String): ResourceLocation {
        return ResourceLocation.fromNamespaceAndPath(ID, path)
    }
}
