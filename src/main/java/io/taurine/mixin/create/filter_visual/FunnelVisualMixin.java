package io.taurine.mixin.create.filter_visual;

import com.simibubi.create.content.logistics.funnel.FunnelBlockEntity;
import com.simibubi.create.content.logistics.funnel.FunnelVisual;
import com.simibubi.create.foundation.blockEntity.behaviour.filtering.FilteringBehaviour;
import dev.engine_room.flywheel.api.visual.Visual;
import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import io.taurine.visual.impl.FilterVisual;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FunnelVisual.class)
public abstract class FunnelVisualMixin implements Visual {
    @Unique
    private FilterVisual<FunnelBlockEntity> taurine$filterVisual;

    @Inject(method = "<init>", at = @At("RETURN"))
    void init(VisualizationContext context, FunnelBlockEntity blockEntity, float partialTick, CallbackInfo ci) {
        if (blockEntity.getBehaviour(FilteringBehaviour.TYPE).isActive()) {
            taurine$filterVisual = new FilterVisual<>(context, blockEntity, partialTick);
        }
    }

    @Inject(method = "updateLight", at = @At("HEAD"))
    private void onUpdateLight(float partialTick, CallbackInfo ci) {
        if (taurine$filterVisual != null) taurine$filterVisual.updateLight(partialTick);
    }

    @Override
    public void update(float partialTick) {
        if (taurine$filterVisual != null) taurine$filterVisual.update(partialTick);
    }

    @Inject(method = "_delete", at = @At("HEAD"))
    private void onUpdate(CallbackInfo ci) {
        if (taurine$filterVisual != null) taurine$filterVisual.delete();
    }
}

