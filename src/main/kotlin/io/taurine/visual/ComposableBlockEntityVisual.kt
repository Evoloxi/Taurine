package io.taurine.visual

import dev.engine_room.flywheel.api.instance.Instance
import dev.engine_room.flywheel.api.visual.BlockEntityVisual
import dev.engine_room.flywheel.api.visual.DynamicVisual
import dev.engine_room.flywheel.api.visual.LightUpdatedVisual
import dev.engine_room.flywheel.api.visual.SectionTrackedVisual
import dev.engine_room.flywheel.api.visual.TickableVisual
import dev.engine_room.flywheel.api.visual.Visual
import dev.engine_room.flywheel.api.visualization.VisualizationContext
import dev.engine_room.flywheel.lib.visual.AbstractVisual
import dev.engine_room.flywheel.lib.visual.SimpleDynamicVisual
import dev.engine_room.flywheel.lib.visual.SimpleTickableVisual
import dev.engine_room.vanillin.compose.ConfiguredElement
import dev.engine_room.vanillin.compose.VisualizationPredicate
import net.minecraft.world.level.block.entity.BlockEntity
import java.util.function.Consumer
/**
 * Based on https://github.com/Engine-Room/Flywheel/blob/1.20.1/dev/common/src/vanillin/java/dev/engine_room/vanillin/compose/ComposableEntityVisual.java
 * */
class ComposableBlockEntityVisual<T : BlockEntity>(
    ctx: VisualizationContext,
    private val blockEntity: T,
    partialTick: Float,
    private val controller: Controller<T>
) : AbstractVisual(ctx, blockEntity.level, partialTick), BlockEntityVisual<T>, SimpleTickableVisual, SimpleDynamicVisual, LightUpdatedVisual {
    // Parallel array to Controller.elements mapping element configurations to instantiated visuals.
    private val visuals: Array<Visual?> = arrayOfNulls(controller.elements.size)

    // TODO: compute the required interfaces this visual needs to implement to cover the interfaces implemented
    //  by its elements. Proxy seems like it could be a good candidate, but regardless we need to know ahead of
    //  time which interfaces to implement. I have a feeling that configured elements will need to know what class
    //  of visual they create.
    init {
        updateElements(partialTick)
    }

    override fun tick(context: TickableVisual.Context?) {
        updateElements(0.0f)

        for (visual in visuals) {
            if (visual is SimpleTickableVisual) {
                visual.tick(context)
            }
        }
    }

    override fun beginFrame(ctx: DynamicVisual.Context) {
        updateElements(ctx.partialTick())

        for (visual in visuals) {
            if (visual is SimpleDynamicVisual) {
                visual.beginFrame(ctx)
            }
        }
    }

    private fun updateElements(partialTick: Float) {
        if (!controller.predicate.shouldVisualize(visualizationContext, blockEntity)) {
            for (i in visuals.indices) {
                if (visuals[i] != null) {
                    visuals[i]!!.delete()
                    visuals[i] = null
                }
            }

            return
        }

        // Create/delete visual elements as necessary.
        for (i in controller.elements.indices) {
            val element = controller.elements[i]

            val shouldExist = element.shouldVisualize(visualizationContext, blockEntity)
            val exists = visuals[i] != null
            if (shouldExist && !exists) {
                visuals[i] = element.create(visualizationContext, blockEntity, partialTick)
            } else if (!shouldExist && exists) {
                visuals[i]!!.delete()
                visuals[i] = null
            }
        }
    }

    override fun _delete() {
        for (visual in visuals) {
            visual?.delete()
        }
    }

    override fun collectCrumblingInstances(consumer: Consumer<Instance?>) {
        for (visual in visuals) {
            if (visual is BlockEntityVisual<*>) {
                visual.collectCrumblingInstances(consumer)
            }
        }
    }

    override fun update(partialTick: Float) {
        for (visual in visuals) {
            visual?.update(partialTick)
        }
    }

    override fun updateLight(partialTick: Float) {
        for (visual in visuals) {
            if (visual is LightUpdatedVisual) {
                visual.updateLight(partialTick)
            }
        }
    }

    override fun setSectionCollector(collector: SectionTrackedVisual.SectionCollector?) {
        for (visual in visuals) {
            if (visual is LightUpdatedVisual) {
                visual.setSectionCollector(collector)
            }
        }
    }

    /**
     * Shared state between all visuals of the same type. Wrap it in a class so that the actual Visual class can be smaller.
     */
    class Controller<T>(
        val elements: Array<ConfiguredElement<in T>>,
        val predicate: VisualizationPredicate<T>
    )
}
