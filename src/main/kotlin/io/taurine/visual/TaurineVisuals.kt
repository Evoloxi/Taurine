package io.taurine.visual

import com.simibubi.create.AllBlockEntityTypes
import com.tterrag.registrate.util.entry.BlockEntityEntry
import dev.engine_room.flywheel.api.visual.Visual
import dev.engine_room.flywheel.api.visualization.VisualizationContext
import dev.engine_room.flywheel.api.visualization.VisualizerRegistry
import dev.engine_room.flywheel.lib.visualization.SimpleBlockEntityVisualizer
import dev.engine_room.vanillin.VanillinXplat
import dev.engine_room.vanillin.compose.ConfiguredElement
import dev.engine_room.vanillin.compose.VisualizationPredicate
import dev.engine_room.vanillin.config.BlockEntityVisualizerBuilder
import dev.engine_room.vanillin.config.Configurator
import dev.engine_room.vanillin.config.VisualConfigValue
import io.taurine.extension.inaccessible.type
import io.taurine.visual.impl.BeltItemLayerVisual
import io.taurine.visual.impl.DepotVisual
import io.taurine.visual.impl.FilterVisual
import io.taurine.visual.impl.LinkVisual
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.entity.BlockEntityType


// https://github.com/Engine-Room/Flywheel/blob/1.20.1/dev/common/src/vanillin/java/dev/engine_room/vanillin/VanillaVisuals.java#L191
object TaurineVisuals {
    val CONFIGURATOR: Configurator = Configurator()

    // Stable visuals are enabled by default always.
    const val STABLE: Boolean = true

    // Experimental visuals are enabled by default in dev.
    val EXPERIMENTAL: Boolean = VanillinXplat.INSTANCE.isDevelopmentEnvironment

    init {
        builder(AllBlockEntityTypes.DEPOT)
            .factory(::DepotVisual)
            .neverSkipVanillaRender()
            .apply(STABLE)

        builder(AllBlockEntityTypes.DEPOT)
            .factory(::DepotVisual)
            .neverSkipVanillaRender()
            .apply(STABLE)

        builder(AllBlockEntityTypes.REDSTONE_LINK)
            .factory(::LinkVisual)
            .neverSkipVanillaRender()
            .apply(STABLE)

        for (entry in [
            AllBlockEntityTypes.BASIN,
            AllBlockEntityTypes.CREATIVE_CRATE,
            AllBlockEntityTypes.SMART_CHUTE,
            AllBlockEntityTypes.SMART_FLUID_PIPE
        ]) builder(entry)
            .factory(::FilterVisual)
            .neverSkipVanillaRender()
            .apply(STABLE)

        /**
         *  These already have a visual associated, so we wrap it in a ComposableBlockEntityVisual together with our new one
         *  */
        for (entry in [
            AllBlockEntityTypes.SAW,
            AllBlockEntityTypes.FUNNEL,
            AllBlockEntityTypes.DEPLOYER
        ]) builder(entry)
            .bundle(::FilterVisual)
            .neverSkipVanillaRender()
            .apply(STABLE)

        builder(AllBlockEntityTypes.BELT)
            .bundle(::BeltItemLayerVisual)
            .neverSkipVanillaRender()
            .apply(STABLE)

        CONFIGURATOR.blockEntities.forEach { [_, entity] -> entity.set(VisualConfigValue.FORCE_ENABLE, null) } //TODO
    }

    private fun <T : BlockEntity> BlockEntityVisualizerBuilder<T>.bundle(visualFactory: SimpleBlockEntityVisualizer.Factory<T>): BlockEntityVisualizerBuilder<T> { // TODO: extend BlockEntityVisualizerBuilder myself
        val original = VisualizerRegistry.getVisualizer(this.type)

        return this.factory { ctx, blockEntity, partialTick ->
            val elements: Array<ConfiguredElement<in T>> = [
                object : ConfiguredElement<T> {
                    override fun create(
                        ctx: VisualizationContext,
                        entity: T,
                        partialTick: Float
                    ): Visual = visualFactory.create(ctx, entity, partialTick)

                    override fun shouldVisualize(
                        ctx: VisualizationContext,
                        entity: T
                    ): Boolean {
                        return true //TODO
                    }
                },
                object : ConfiguredElement<T> {
                    override fun create(
                        ctx: VisualizationContext,
                        entity: T,
                        partialTick: Float
                    ): Visual? {
                        return original?.createVisual(ctx, entity, partialTick)
                    }

                    override fun shouldVisualize(
                        ctx: VisualizationContext,
                        entity: T
                    ): Boolean {
                        return original != null // TODO
                    }
                }
            ]
            val controller = ComposableBlockEntityVisual.Controller(elements, VisualizationPredicate.alwaysTrue())
            ComposableBlockEntityVisual(ctx, blockEntity, partialTick, controller)
        }
    }

    fun <T : BlockEntity> builder(type: BlockEntityType<T>): BlockEntityVisualizerBuilder<T> {
        return BlockEntityVisualizerBuilder(CONFIGURATOR, type)
    }

    fun <T : BlockEntity> builder(type: BlockEntityEntry<T>): BlockEntityVisualizerBuilder<T> {
        return builder(type.get())
    }

    fun init() {}
}