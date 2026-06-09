package io.taurine.mixin.create.accessor;

import com.simibubi.create.content.kinetics.belt.transport.BeltInventory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(BeltInventory.class)
public interface BeltInventoryAccessor {
    @Accessor("beltMovementPositive")
    boolean isBeltMovementPositive();
}
