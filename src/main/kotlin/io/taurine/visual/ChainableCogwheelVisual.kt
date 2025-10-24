package io.taurine.visual

import com.simibubi.create.AllPartialModels
import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer
import com.simibubi.create.content.kinetics.base.RotatingInstance
import com.simibubi.create.content.kinetics.base.SingleAxisRotatingVisual
import com.simibubi.create.content.kinetics.simpleRelays.BracketedKineticBlockEntityRenderer
import com.simibubi.create.foundation.render.AllInstanceTypes
import dev.engine_room.flywheel.api.instance.Instance
import dev.engine_room.flywheel.api.visualization.VisualizationContext
import dev.engine_room.flywheel.lib.model.Models
import dev.lopyluna.gnkinetics.content.blocks.kinetics.chainned_cog.ChainableCogwheelBE
import dev.lopyluna.gnkinetics.register.client.GearsPartialModels
import java.util.function.Consumer

class ChainableCogwheelVisual(
    context: VisualizationContext, blockEntity: ChainableCogwheelBE, partialTick: Float
) : SingleAxisRotatingVisual<ChainableCogwheelBE>(
    context, blockEntity, partialTick, Models.partial(GearsPartialModels.CHAINABLE_COGWHEEL)
) {

    val shaft: RotatingInstance

    init {
        val axis = KineticBlockEntityRenderer.getRotationAxisOf(blockEntity)

        shaft = instancerProvider()
            .instancer<RotatingInstance>(AllInstanceTypes.ROTATING, Models.partial(AllPartialModels.COGWHEEL_SHAFT))
            .createInstance()


        shaft.rotateToFace(axis)
            .setup(blockEntity)
            .setRotationOffset(BracketedKineticBlockEntityRenderer.getShaftAngleOffset(axis, pos))
            .setPosition(visualPosition)
            .setChanged()
    }

    override fun update(pt: Float) {
        super.update(pt)
        shaft.setup(blockEntity)
            .setRotationOffset(BracketedKineticBlockEntityRenderer.getShaftAngleOffset(rotationAxis(), pos))
            .setChanged()
    }

    override fun updateLight(partialTick: Float) {
        super.updateLight(partialTick)
        relight(shaft)
    }

    override fun _delete() {
        super._delete()
        shaft.delete()
    }

    override fun collectCrumblingInstances(consumer: Consumer<Instance?>) {
        super.collectCrumblingInstances(consumer)
        consumer.accept(shaft)
    }
}