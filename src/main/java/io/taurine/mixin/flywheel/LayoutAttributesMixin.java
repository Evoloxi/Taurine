package io.taurine.mixin.flywheel;

import dev.engine_room.flywheel.api.layout.FloatRepr;
import dev.engine_room.flywheel.backend.LayoutAttributes;
import dev.engine_room.flywheel.backend.gl.GlNumericType;
import io.taurine.flywheel.TaurineFloatRepr;
import io.taurine.flywheel.TaurineGlNumericType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LayoutAttributes.class)
public abstract class LayoutAttributesMixin {
    @Inject(
            method = "toGlType(Ldev/engine_room/flywheel/api/layout/FloatRepr;)Ldev/engine_room/flywheel/backend/gl/GlNumericType;",
            at = @At("HEAD"),
            cancellable = true
    )
    private static void toGlType(FloatRepr repr, CallbackInfoReturnable<GlNumericType> cir) {
        if (repr == TaurineFloatRepr.HALF_FLOAT) cir.setReturnValue(TaurineGlNumericType.HALF_FLOAT);
    }
}
