package io.taurine.mixin.flywheel;

import dev.engine_room.flywheel.backend.gl.GlNumericType;
import org.lwjgl.opengl.GL42;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Arrays;

@Mixin(value = GlNumericType.class)
public class GlNumericTypeMixin {
    @Shadow
    @Final
    @Mutable
    private static GlNumericType[] $VALUES;

    @Invoker("<init>")
    public static GlNumericType taurine$invokeInit(String enumName, int ordinal, int bytes, String name, int glEnum) {
        throw new AssertionError();
    }

    @Inject(method = "<clinit>", at = @At("TAIL"))
    private static void addValues(CallbackInfo ci) {
        GlNumericType myValue = taurine$invokeInit("HALF_FLOAT", $VALUES.length, Short.BYTES, "float", GL42.GL_HALF_FLOAT);
        $VALUES = taurine$extendArray($VALUES, myValue);
    }

    @Unique
    private static GlNumericType[] taurine$extendArray(GlNumericType[] original, GlNumericType newEntry) {
        GlNumericType[] newArray = Arrays.copyOf(original, original.length + 1);
        newArray[original.length] = newEntry;
        return newArray;
    }
}