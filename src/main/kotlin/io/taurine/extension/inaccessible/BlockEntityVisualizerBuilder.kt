package io.taurine.extension.inaccessible

import dev.engine_room.vanillin.config.BlockEntityVisualizerBuilder
import io.taurine.mixin.vanillin.accesor.BlockEntityVisualizerBuilderAccessor
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.entity.BlockEntityType

val <T : BlockEntity> BlockEntityVisualizerBuilder<T>.type: BlockEntityType<T>
    get() = (this as BlockEntityVisualizerBuilderAccessor<T>).type