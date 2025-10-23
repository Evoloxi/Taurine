package io.taurine.mixin.create;

import com.simibubi.create.content.kinetics.saw.SawBlockEntity;
import com.simibubi.create.content.kinetics.saw.SawVisual;
import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import io.taurine.visual.FilterVisual;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SawVisual.class)
public class SawVisualMixin {
    @Unique
    private FilterVisual<SawBlockEntity> taurine$filterVisual;

    @Inject(method = "<init>", at = @At("RETURN"))
    void init(VisualizationContext context, SawBlockEntity blockEntity, float partialTick, CallbackInfo ci) {
        taurine$filterVisual = new FilterVisual<>(context, blockEntity, partialTick);
        taurine$filterVisual.update(partialTick); // why the fuck do I need to call this when it is FilterVisual's init
    }

    @Inject(method = "updateLight", at = @At("TAIL"))
    private void onUpdateLight(float partialTick, CallbackInfo ci) {
        taurine$filterVisual.updateLight(partialTick);
    }

    @Inject(method = "update", at = @At("TAIL"))
    private void onUpdate(float pt, CallbackInfo ci) {
        taurine$filterVisual.update(pt);
    }

    @Inject(method = "_delete", at = @At("TAIL"))
    private void onUpdate(CallbackInfo ci) {
        taurine$filterVisual.delete();
    }
}
