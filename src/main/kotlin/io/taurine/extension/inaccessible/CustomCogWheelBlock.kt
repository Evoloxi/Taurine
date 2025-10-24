package io.taurine.extension.inaccessible

import dev.lopyluna.gnkinetics.content.blocks.kinetics.custom_cogs.CogType
import dev.lopyluna.gnkinetics.content.blocks.kinetics.custom_cogs.CustomCogWheelBlock
import io.taurine.mixin.gnkinetics.accessor.CustomCogWheelBlockAccessor

val CustomCogWheelBlock.cogType: CogType
    get() = (this as CustomCogWheelBlockAccessor).cogType