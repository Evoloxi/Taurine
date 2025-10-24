package io.taurine.visual

import com.simibubi.create.content.kinetics.simpleRelays.BracketedKineticBlockEntity
import com.simibubi.create.content.kinetics.simpleRelays.ICogWheel
import dev.engine_room.flywheel.api.model.Model
import dev.engine_room.flywheel.api.visual.BlockEntityVisual
import dev.engine_room.flywheel.api.visualization.VisualizationContext
import dev.engine_room.flywheel.lib.model.Models
import dev.lopyluna.gnkinetics.content.blocks.kinetics.custom_cogs.CogType
import dev.lopyluna.gnkinetics.content.blocks.kinetics.custom_cogs.CustomCogWheelBlock
import io.taurine.extension.inaccessible.cogType
import org.jetbrains.annotations.ApiStatus

@Deprecated("janky")
@ApiStatus.ScheduledForRemoval
object OmniCogwheelVisual {
    @JvmStatic
    fun create(
        context: VisualizationContext,
        blockEntity: BracketedKineticBlockEntity,
        partialTick: Float
    ): BlockEntityVisual<BracketedKineticBlockEntity> {
        val model = getModel(blockEntity)
            ?: throw IllegalStateException("No model found for block: ${blockEntity.blockState.block}")

        val blockState = blockEntity.blockState
        val type = (blockState.block as? CustomCogWheelBlock)?.cogType ?:
            throw IllegalStateException("Block is not a CustomCogWheelBlock: ${blockState.block}")
        val isLarge = ICogWheel.isLargeCog(blockState)
        val isShaftless = type == CogType.SHAFTLESS || type == CogType.HOLLOWED

        return when {
            isLarge && isShaftless -> ShaftlessCogwheelVisual.Large(context, blockEntity, partialTick, model)
            isLarge -> CogwheelWithShaftVisual.Large(context, blockEntity, partialTick, model)
            isShaftless -> ShaftlessCogwheelVisual(context, blockEntity, partialTick, model)
            else -> CogwheelWithShaftVisual(context, blockEntity, partialTick, model)
        }
    }

    private fun getModel(blockEntity: BracketedKineticBlockEntity): Model? {
        val block = blockEntity.blockState.block as? CustomCogWheelBlock ?: return null
        return Models.partial(block.model)
    }
}

