package io.taurine.mixin.flywheel;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import dev.engine_room.flywheel.backend.compile.component.BufferTextureInstanceComponent;
import dev.engine_room.flywheel.backend.compile.component.SsboInstanceComponent;
import dev.engine_room.flywheel.backend.glsl.generate.GlslBlock;
import dev.engine_room.flywheel.backend.glsl.generate.GlslBuilder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({
        SsboInstanceComponent.class,
        BufferTextureInstanceComponent.class,
})
public abstract class InstanceComponentMixin {
    @Shadow
    public abstract String name();

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
    void setBodyRef(
            GlslBuilder builder,
            CallbackInfo ci,
            @Local(name = "fnBody") GlslBlock fnBody,
            @Share("taurine$fnBody") LocalRef<GlslBlock> fnBodyRef
    ) {
        fnBodyRef.set(fnBody);
    }
}
