package io.taurine.mixin.create;

import com.simibubi.create.content.kinetics.base.KineticBlockEntityVisual;
import com.simibubi.create.content.kinetics.belt.BeltBlockEntity;
import com.simibubi.create.content.kinetics.belt.BeltVisual;
import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import dev.engine_room.flywheel.lib.visual.SimpleDynamicVisual;
import io.taurine.visual.BeltItemLayerVisual;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = BeltVisual.class, remap = false)
public abstract class BeltVisualMixin extends KineticBlockEntityVisual<BeltBlockEntity> implements SimpleDynamicVisual {
    @Unique
    private BeltItemLayerVisual taurine$itemLayerVisual;

    public BeltVisualMixin(VisualizationContext ctx, BeltBlockEntity blockEntity, float partialTick) {
        super(ctx, blockEntity, partialTick);
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    void init(VisualizationContext context, BeltBlockEntity blockEntity, float partialTick, CallbackInfo ci) {
        taurine$itemLayerVisual = new BeltItemLayerVisual(context, blockEntity, partialTick);
    }

    @Inject(method = "updateLight", at = @At("TAIL"))
    private void onUpdateLight(float partialTick, CallbackInfo ci) {
        taurine$itemLayerVisual.updateLight(partialTick);
    }

    @Inject(method = "update", at = @At("TAIL"))
    private void onUpdate(float pt, CallbackInfo ci) {
        taurine$itemLayerVisual.update(pt);
    }

    @Inject(method = "_delete", at = @At("TAIL"))
    private void onUpdate(CallbackInfo ci) {
        taurine$itemLayerVisual.delete();
    }

    @Override
    public void beginFrame(Context context) {
        taurine$itemLayerVisual.beginFrame(context);
    }
}
