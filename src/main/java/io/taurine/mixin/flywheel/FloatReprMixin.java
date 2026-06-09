package io.taurine.mixin.flywheel;

import dev.engine_room.flywheel.api.layout.FloatRepr;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Arrays;

@Mixin(value = FloatRepr.class)
public class FloatReprMixin {
    @Shadow
    @Final
    @Mutable
    private static FloatRepr[] $VALUES;

    @Invoker("<init>")
    public static FloatRepr taurine$invokeInit(String name, int ordinal, int byteSize) {
        throw new AssertionError();
    }

    @Inject(method = "<clinit>", at = @At("TAIL"))
    private static void addValues(CallbackInfo ci) {
        FloatRepr myValue = taurine$invokeInit("HALF", $VALUES.length, Short.BYTES);
        $VALUES = taurine$extendArray($VALUES, myValue);
    }

    @Unique
    private static FloatRepr[] taurine$extendArray(FloatRepr[] original, FloatRepr newEntry) {
        FloatRepr[] newArray = Arrays.copyOf(original, original.length + 1);
        newArray[original.length] = newEntry;
        return newArray;
    }
}