package io.taurine.extension.inaccessible

import com.simibubi.create.content.kinetics.belt.transport.BeltInventory
import io.taurine.mixin.create.accessor.BeltInventoryAccessor

val BeltInventory.beltMovementPositive: Boolean
    get() = (this as BeltInventoryAccessor).isBeltMovementPositive