package io.taurine.mixin.create;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.simibubi.create.foundation.blockEntity.behaviour.filtering.FilteringBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.filtering.FilteringRenderer;
import io.taurine.ModelCache;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(FilteringRenderer.class)
public abstract class FilteringRendererMixin {

    @Definition(id = "cameraEntity" , local  = @Local(type = Entity.class, name = "cameraEntity"))
    @Definition(id = "position"     , method = "Lnet/minecraft/world/entity/Entity;position()Lnet/minecraft/world/phys/Vec3;")
    @Definition(id = "distanceToSqr", method = "Lnet/minecraft/world/phys/Vec3;distanceToSqr(Lnet/minecraft/world/phys/Vec3;)D")
    @Expression("cameraEntity.position().distanceToSqr(?) > ?")
    @ModifyExpressionValue(
            method = "renderOnBlockEntity",
            at = @At(value = "MIXINEXTRAS:EXPRESSION")
    )
    private static boolean behaviour(boolean original, @Local(name = "behaviour") FilteringBehaviour behaviour) {
        return original || ModelCache.canBeInstanced(behaviour.getFilter());
    }
}
