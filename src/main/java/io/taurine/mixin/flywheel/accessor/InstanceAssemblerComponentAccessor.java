package io.taurine.mixin.flywheel.accessor;

import dev.engine_room.flywheel.backend.compile.component.InstanceAssemblerComponent;
import dev.engine_room.flywheel.backend.glsl.generate.GlslExpr;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(InstanceAssemblerComponent.class)
public interface InstanceAssemblerComponentAccessor {
    @Invoker("access")
    GlslExpr taurine_access(int uintOffset);
}
