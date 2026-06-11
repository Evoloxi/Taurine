package io.taurine.mixin.flywheel;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.sugar.Local;
import dev.engine_room.flywheel.backend.compile.component.SsboInstanceComponent;
import dev.engine_room.flywheel.backend.glsl.generate.GlslBlock;
import dev.engine_room.flywheel.backend.glsl.generate.GlslBuilder;
import io.taurine.flywheel.HalfFloatCompatible;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SsboInstanceComponent.class)
public abstract class SsboInstanceComponentMixin implements HalfFloatCompatible {
    @Definition(id = "fnBody", local = @Local(type = GlslBlock.class, name = "fnBody"))
    @Definition(id = "GlslBlock", type = GlslBlock.class)
    @Expression("fnBody = new GlslBlock()")
    @Inject(
            method = "generateUnpacking",
            at = @At(
                    value = "MIXINEXTRAS:EXPRESSION",
                    shift = At.Shift.AFTER
            )
    )
    void setBodyImpl(GlslBuilder builder, CallbackInfo ci, @Local(name = "fnBody") GlslBlock fnBody) {
        setTaurine$body(fnBody);
    }

    @Inject(
            method = "generateUnpacking",
            at = @At("RETURN")
    )
    void deleteBody(CallbackInfo ci) {
        setTaurine$body(null);
    }
}
