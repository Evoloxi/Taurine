package io.taurine.mixin.gnkinetics.accessor;

import dev.lopyluna.gnkinetics.content.blocks.kinetics.custom_cogs.CogType;
import dev.lopyluna.gnkinetics.content.blocks.kinetics.custom_cogs.CustomCogWheelBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(CustomCogWheelBlock.class)
public interface CustomCogWheelBlockAccessor {
    @Accessor("cogType")
    CogType getCogType();
}
