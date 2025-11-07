package io.taurine.mixin.create.accessor;

import com.simibubi.create.content.fluids.tank.FluidTankBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(FluidTankBlockEntity.class)
public interface FluidTankBlockEntityAccessor {
    @Accessor("window")
    boolean getWindow();
}
