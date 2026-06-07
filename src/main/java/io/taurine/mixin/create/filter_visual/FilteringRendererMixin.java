package io.taurine.mixin.create.filter_visual;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.simibubi.create.foundation.blockEntity.behaviour.filtering.FilteringBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.filtering.FilteringRenderer;
import dev.engine_room.flywheel.api.visualization.VisualizationManager;
import io.taurine.ModelCache;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = FilteringRenderer.class, priority = 1300)
public abstract class FilteringRendererMixin {

    @Definition(id = "max", local = @Local(type = float.class, name = "max"))
    @Expression("? > (double) (max * max)")
    @ModifyExpressionValue(
            method = "renderOnBlockEntity",
            at = @At(value = "MIXINEXTRAS:EXPRESSION")
    )
    private static boolean behaviour(boolean original, @Local(name = "level") Level level, @Local(name = "behaviour") FilteringBehaviour behaviour) {
        return original || (VisualizationManager.supportsVisualization(level) && ModelCache.canBeInstanced(behaviour.getFilter()));
    }
}


