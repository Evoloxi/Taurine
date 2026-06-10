package io.taurine.extension.inaccessible

import dev.engine_room.flywheel.backend.compile.component.InstanceAssemblerComponent
import dev.engine_room.flywheel.backend.glsl.generate.GlslExpr
import io.taurine.mixin.flywheel.accessor.InstanceAssemblerComponentAccessor

fun InstanceAssemblerComponent.access(
    uintOffset: Int
): GlslExpr = (this as InstanceAssemblerComponentAccessor).taurine_access(uintOffset)
