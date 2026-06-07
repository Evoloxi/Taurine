package io.taurine.mixin;

import com.simibubi.create.content.kinetics.deployer.DeployerBlockEntity;
import io.taurine.duck.DeployerDuck;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;

@Mixin(DeployerBlockEntity.class)
public abstract class DeployerBlockEntityMixin implements DeployerDuck {
    @Unique
    private static final MethodHandle taurine$enumMode;
    static {
        try {
            Field f = DeployerBlockEntity.class.getDeclaredField("mode");
            f.setAccessible(true);
            taurine$enumMode = MethodHandles.lookup().unreflectGetter(f);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException("Failed to get mode handle", e);
        }
    }

    @Override
    public boolean taurine$isPunching() {
        try {
            return ((Enum<?>) taurine$enumMode.invoke(this)).ordinal() == 0;
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }
}
