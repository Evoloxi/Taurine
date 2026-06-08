package io.taurine.visual

import com.simibubi.create.AllBlockEntityTypes
import com.simibubi.create.content.kinetics.belt.BeltBlockEntity
import dev.engine_room.flywheel.api.visual.Visual
import dev.engine_room.flywheel.api.visualization.VisualizationContext
import dev.engine_room.flywheel.api.visualization.VisualizerRegistry
import dev.engine_room.flywheel.lib.visualization.SimpleBlockEntityVisualizer
import dev.engine_room.vanillin.VanillinXplat
import dev.engine_room.vanillin.compose.ConfiguredElement
import dev.engine_room.vanillin.config.BlockEntityVisualizerBuilder
import dev.engine_room.vanillin.config.Configurator
import dev.engine_room.vanillin.config.VisualConfigValue
import io.taurine.mixin.vanillin.accesor.BlockEntityVisualizerBuilderAccessor
import io.taurine.visual.impl.BeltItemLayerVisual
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
        builder<BeltBlockEntity>(AllBlockEntityTypes.BELT.get())
            .add(::BeltItemLayerVisual)
            .apply(STABLE)

        CONFIGURATOR.blockEntities.forEach { [_, entity] -> entity.set(VisualConfigValue.FORCE_ENABLE, null) } //TODO
    }

    fun <T : BlockEntity> BlockEntityVisualizerBuilder<T>.add(visualFactory: SimpleBlockEntityVisualizer.Factory<T>): BlockEntityVisualizerBuilder<T> {
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
                        val type = (this@add as BlockEntityVisualizerBuilderAccessor<T>).`accessor$type`()
                        val previous = VisualizerRegistry.getVisualizer(type)
                        return previous?.createVisual(ctx, entity, partialTick)
                    }

                    override fun shouldVisualize(
                        ctx: VisualizationContext,
                        entity: T
                    ): Boolean {
                        return true //TODO
                    }
                }
            ]
            val controller = ComposableBlockEntityVisual.Controller(elements) { _, _ -> true }
            ComposableBlockEntityVisual(ctx, blockEntity, partialTick, controller)
        }
    }

    fun <T : BlockEntity> builder(type: BlockEntityType<T>): BlockEntityVisualizerBuilder<T> {
        return BlockEntityVisualizerBuilder(CONFIGURATOR, type)
    }

    fun init() {
    }
}