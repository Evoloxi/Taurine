package io.taurine.visual

import com.simibubi.create.AllPartialModels
import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer
import com.simibubi.create.content.kinetics.base.RotatingInstance
import com.simibubi.create.content.kinetics.base.SingleAxisRotatingVisual
import com.simibubi.create.content.kinetics.simpleRelays.BracketedKineticBlockEntity
import com.simibubi.create.content.kinetics.simpleRelays.BracketedKineticBlockEntityRenderer
import com.simibubi.create.foundation.render.AllInstanceTypes
import dev.engine_room.flywheel.api.instance.Instance
import dev.engine_room.flywheel.api.model.Model
import dev.engine_room.flywheel.api.visualization.VisualizationContext
import dev.engine_room.flywheel.lib.model.Models
import java.util.function.Consumer

open class CogwheelWithShaftVisual(
    context: VisualizationContext,
    blockEntity: BracketedKineticBlockEntity,
    partialTick: Float, model: Model
) :
    SingleAxisRotatingVisual<BracketedKineticBlockEntity>(
        context,
        blockEntity,
        partialTick,
        model
    ) {

    protected val additionalShaft: RotatingInstance

    init {
        val axis = KineticBlockEntityRenderer.getRotationAxisOf(blockEntity)

        additionalShaft =
            instancerProvider().instancer<RotatingInstance>(AllInstanceTypes.ROTATING, Models.partial(AllPartialModels.COGWHEEL_SHAFT))
                .createInstance()


        additionalShaft.rotateToFace(axis)
            .setup(blockEntity)
            .setRotationOffset(BracketedKineticBlockEntityRenderer.getShaftAngleOffset(axis, pos))
            .setPosition(visualPosition)
            .setChanged()
    }

    override fun update(pt: Float) {
        super.update(pt)
        additionalShaft.setup(blockEntity)
            .setRotationOffset(BracketedKineticBlockEntityRenderer.getShaftAngleOffset(rotationAxis(), pos))
            .setChanged()
    }

    override fun updateLight(partialTick: Float) {
        super.updateLight(partialTick)
        relight(additionalShaft)
    }

    override fun _delete() {
        super._delete()
        additionalShaft.delete()
    }

    override fun collectCrumblingInstances(consumer: Consumer<Instance?>) {
        super.collectCrumblingInstances(consumer)
        consumer.accept(additionalShaft)
    }

    open class Large(
        context: VisualizationContext,
        blockEntity: BracketedKineticBlockEntity,
        partialTick: Float, model: Model
    ) : CogwheelWithShaftVisual(
            context,
            blockEntity,
            partialTick,
            model
        )
}