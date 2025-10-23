package io.taurine.mixin.create;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.renderer.SmartBlockEntityRenderer;
import dev.engine_room.flywheel.api.visualization.VisualizationManager;
import net.minecraft.client.renderer.MultiBufferSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(SmartBlockEntityRenderer.class)
public class FilteringRendererMixin {
    @WrapWithCondition(
            method = "renderSafe(Lcom/simibubi/create/foundation/blockEntity/SmartBlockEntity;FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;II)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/simibubi/create/foundation/blockEntity/behaviour/filtering/FilteringRenderer;renderOnBlockEntity(Lcom/simibubi/create/foundation/blockEntity/SmartBlockEntity;FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;II)V"
            )
    )
    private static boolean onRenderOnBlockEntity(SmartBlockEntity be, float max, PoseStack ms, MultiBufferSource buf, int side, int sided) {
        return !VisualizationManager.supportsVisualization(be.getLevel()); // TODO: unsupported item models
    }
}


