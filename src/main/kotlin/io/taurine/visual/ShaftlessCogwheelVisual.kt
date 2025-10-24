package io.taurine.visual

import com.simibubi.create.content.kinetics.base.SingleAxisRotatingVisual
import com.simibubi.create.content.kinetics.simpleRelays.BracketedKineticBlockEntity
import dev.engine_room.flywheel.api.model.Model
import dev.engine_room.flywheel.api.visualization.VisualizationContext

open class ShaftlessCogwheelVisual(
    context: VisualizationContext,
    blockEntity: BracketedKineticBlockEntity,
    partialTick: Float,
    model: Model
) : SingleAxisRotatingVisual<BracketedKineticBlockEntity>(
    context,
    blockEntity,
    partialTick,
    model
) {
    open class Large(
        context: VisualizationContext,
        blockEntity: BracketedKineticBlockEntity,
        partialTick: Float,
        model: Model
    ) : ShaftlessCogwheelVisual(
        context,
        blockEntity,
        partialTick,
        model
    )
}