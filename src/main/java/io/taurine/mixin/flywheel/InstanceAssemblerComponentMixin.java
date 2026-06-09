package io.taurine.mixin.flywheel;

import dev.engine_room.flywheel.api.layout.FloatRepr;
import dev.engine_room.flywheel.backend.compile.component.InstanceAssemblerComponent;
import dev.engine_room.flywheel.backend.glsl.generate.GlslExpr;
import io.taurine.flywheel.TaurineFloatRepr;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.EnumMap;
import java.util.function.Function;

@Mixin(InstanceAssemblerComponent.class)
public abstract class InstanceAssemblerComponentMixin {
    @Shadow
    @Final
    private static EnumMap<FloatRepr, Function<GlslExpr, GlslExpr>> FLOAT_UNPACKING_FUNCS;

    @Inject(method = "<clinit>", at = @At("RETURN"))
    private static void inject(CallbackInfo ci) {
        FLOAT_UNPACKING_FUNCS.put(TaurineFloatRepr.INSTANCE.getHALF(), e -> e.callFunction("unpackHalf2x16").swizzle("x"));
    }
}

