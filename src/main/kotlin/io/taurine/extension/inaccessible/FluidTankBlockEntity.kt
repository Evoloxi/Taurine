package io.taurine.extension.inaccessible

import com.simibubi.create.content.fluids.tank.FluidTankBlockEntity
import io.taurine.mixin.create.accessor.FluidTankBlockEntityAccessor

val FluidTankBlockEntity.window: Boolean
    get() = (this as FluidTankBlockEntityAccessor).window