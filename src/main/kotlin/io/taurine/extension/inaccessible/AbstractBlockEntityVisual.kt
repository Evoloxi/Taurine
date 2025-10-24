package io.taurine.extension.inaccessible

import dev.engine_room.flywheel.api.instance.InstancerProvider
import dev.engine_room.flywheel.lib.visual.AbstractBlockEntityVisual
import io.taurine.mixin.flywheel.AbstractVisualAccessor
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.entity.BlockEntity

val <T : BlockEntity> AbstractBlockEntityVisual<T>.instancerProvider: InstancerProvider
    get() = (this as AbstractVisualAccessor).instancerProvider

val <T : BlockEntity> AbstractBlockEntityVisual<T>.level: Level
    get() = (this as AbstractVisualAccessor).level