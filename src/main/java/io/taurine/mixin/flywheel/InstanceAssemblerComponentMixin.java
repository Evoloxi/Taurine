package io.taurine.mixin.flywheel;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import dev.engine_room.flywheel.api.layout.*;
import dev.engine_room.flywheel.backend.compile.LayoutInterpreter;
import dev.engine_room.flywheel.backend.compile.component.InstanceAssemblerComponent;
import dev.engine_room.flywheel.backend.glsl.generate.GlslBlock;
import dev.engine_room.flywheel.backend.glsl.generate.GlslExpr;
import io.taurine.flywheel.HalfFloatCompatible;
import io.taurine.flywheel.TaurineFloatRepr;
import io.taurine.flywheel.gl.TaurineGlslStmt;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

@Mixin(InstanceAssemblerComponent.class)
public class InstanceAssemblerComponentMixin implements HalfFloatCompatible {

    @Shadow
    @Final
    private static boolean BIG_ENDIAN;

    @Shadow
    protected GlslExpr access(int uintOffset) {
        throw new AssertionError();
    }

    @Unique
    private final Map<Integer, String> taurine$halfTemps = new HashMap<>();

    @Unique
    private @Nullable GlslBlock taurine$body;

    @Override
    public void setTaurine$body(@Nullable GlslBlock taurine$body) {
        this.taurine$body = taurine$body;
    }

    @Override
    public @Nullable GlslBlock getTaurine$body() {
        return taurine$body;
    }

    @Inject(
            method = "source",
            at = @At(
                    value = "INVOKE",
                    target = "Ldev/engine_room/flywheel/backend/compile/component/InstanceAssemblerComponent;generateUnpacking(Ldev/engine_room/flywheel/backend/glsl/generate/GlslBuilder;)V",
                    shift = At.Shift.AFTER
            )
    )
    private void reset(CallbackInfoReturnable<String> cir) {
        taurine$halfTemps.clear();
    }

    @Definition(id = "repr", method = "repr")
    @Expression("? = ?.repr()")
    @Inject(
            method = "unpackScalar(Ldev/engine_room/flywheel/api/layout/ScalarElementType;I)Ldev/engine_room/flywheel/backend/glsl/generate/GlslExpr;",
            at = @At(value = "MIXINEXTRAS:EXPRESSION", shift = At.Shift.AFTER),
            cancellable = true
    )
    private void unpackScalar(
            ScalarElementType type,
            int byteOffset,
            CallbackInfoReturnable<GlslExpr> cir,
            @Local(name = "repr") ValueRepr repr
    ) {
        if (repr == TaurineFloatRepr.HALF_FLOAT) {
            int shortOffset = byteOffset / Short.BYTES;
            cir.setReturnValue(taurine$unpackHalfFloatScalar(shortOffset, taurine$body));
        }
    }

    @Definition(id = "size", method = "Ldev/engine_room/flywheel/api/layout/VectorElementType;size()I")
    @Expression("? = ?.size()")
    @Inject(
            method = "unpackVector(Ldev/engine_room/flywheel/api/layout/VectorElementType;I)Ldev/engine_room/flywheel/backend/glsl/generate/GlslExpr;",
            at = @At(value = "MIXINEXTRAS:EXPRESSION", shift = At.Shift.AFTER),
            cancellable = true
    )
    private void unpackVector(
            VectorElementType type,
            int byteOffset,
            CallbackInfoReturnable<GlslExpr> cir,
            @Local(name = "repr") ValueRepr repr,
            @Local(name = "size") int size
            ) {
        if (repr == TaurineFloatRepr.HALF_FLOAT) {
            int shortOffset = byteOffset / Short.BYTES;
            cir.setReturnValue(taurine$unpackHalfFloatVector(LayoutInterpreter.vectorTypeName(type), size, shortOffset, taurine$body));
        }
    }

    @WrapOperation(
            method = "unpackMatrix",
            at = @At(
                    value = "INVOKE",
                    target = "Ldev/engine_room/flywheel/backend/compile/component/InstanceAssemblerComponent;unpackVector(Ljava/lang/String;IIILjava/util/function/Function;)Ldev/engine_room/flywheel/backend/glsl/generate/GlslExpr;"
            )
    )
    private GlslExpr unpackMatrix(
            InstanceAssemblerComponent instance,
            String outType,
            int size, int byteOffset, int byteSize,
            Function<GlslExpr, GlslExpr> unpackingFunc,
            Operation<GlslExpr> original,
            @Local(name = "repr") FloatRepr repr
    ) {
        if  (repr == TaurineFloatRepr.HALF_FLOAT) {
            int shortOffset = byteOffset / byteSize;
            return taurine$unpackHalfFloatVector(outType, size, shortOffset, taurine$body);
        } else {
            return original.call(instance, outType, size, byteOffset, byteSize, unpackingFunc);
        }
    }

    /**
     * @param body exists for ease of porting from non-mixin implementation
     * */
    @Override
    public @NotNull GlslExpr taurine$unpackHalfFloatScalar(int shortOffset, @Nullable GlslBlock body) {
        int wordOffset = shortOffset / 2;

        int lane = shortOffset % 2;
        if (BIG_ENDIAN) {
            lane = 1 - lane;
        }

        GlslExpr vec2 = taurine$halfTempForWord(wordOffset, body);
        return vec2.swizzle(lane == 0 ? "x" : "y");
    }

    @Override
    public @NotNull GlslExpr taurine$halfTempForWord(int wordOffset, @Nullable GlslBlock body) {
        if (body != null) {
            String name = taurine$halfTemps.computeIfAbsent(wordOffset, k -> {
                String tempName = "_flw_h" + k;
                // vec2 _flw_h<k> = unpackHalf2x16(access(k));
                body.add(TaurineGlslStmt.declare("vec2", tempName, access(k).callFunction("unpackHalf2x16")));
                return tempName;
            });
            return GlslExpr.variable(name);
        }

        return access(wordOffset).callFunction("unpackHalf2x16");
    }

    @Override
    public @NotNull GlslExpr taurine$unpackHalfFloatVector(@NotNull String outType, int size, int shortOffset, @Nullable GlslBlock body) {
        if (size == 2 && (shortOffset % 2 == 0) != BIG_ENDIAN) {
            int wordOffset = shortOffset / 2;
            return access(wordOffset).callFunction("unpackHalf2x16");
        }

        List<GlslExpr> args = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            args.add(taurine$unpackHalfFloatScalar(shortOffset + i, body));
        }
        return GlslExpr.call(outType, args);
    }
}

