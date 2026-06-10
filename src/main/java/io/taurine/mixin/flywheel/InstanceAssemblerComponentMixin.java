package io.taurine.mixin.flywheel;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import dev.engine_room.flywheel.api.layout.FloatRepr;
import dev.engine_room.flywheel.api.layout.ValueRepr;
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
        FLOAT_UNPACKING_FUNCS.put(TaurineFloatRepr.HALF, e -> e.callFunction("unpackHalf2x16").swizzle("x"));
    }

    @WrapOperation(
            method = "unpackVector(Ldev/engine_room/flywheel/api/layout/VectorElementType;I)Ldev/engine_room/flywheel/backend/glsl/generate/GlslExpr;",
            at = @At(
                    value = "INVOKE",
                    target = "Ldev/engine_room/flywheel/backend/compile/component/InstanceAssemblerComponent;unpackVector(Ljava/lang/String;IIILjava/util/function/Function;)Ldev/engine_room/flywheel/backend/glsl/generate/GlslExpr;"
            )
    )
    private GlslExpr unpackFloat16BackedVector(
            InstanceAssemblerComponent instance,
            String outType,
            int size, int byteOffset, int byteSize,
            Function<GlslExpr, GlslExpr> unpackingFunc,
            Operation<GlslExpr> original,
            @Local(name = "repr") ValueRepr repr
    ) {
        if (repr != TaurineFloatRepr.HALF) {
            return original.call(instance, outType, size, byteOffset, byteSize, unpackingFunc);
        }

        return TaurineFloatRepr.unpackHalf2x16(instance, outType, size, byteOffset);
    }
}

