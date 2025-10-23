package io.taurine.mixin.create;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.content.kinetics.deployer.DeployerRenderer;
import com.simibubi.create.content.kinetics.saw.SawRenderer;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = {
        SawRenderer.class,
        DeployerRenderer.class
})
public class BlockEntityWithFilterSlotRendererMixin {
    @Redirect(
            method = "renderSafe*",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/simibubi/create/foundation/blockEntity/behaviour/filtering/FilteringRenderer;renderOnBlockEntity(Lcom/simibubi/create/foundation/blockEntity/SmartBlockEntity;FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;II)V"
            )
    )
    private static void shouldRenderFiltering(SmartBlockEntity instance, float delta, PoseStack ms, MultiBufferSource buffer, int light, int overlay) {
        // TODO: unsupported item models
    }
}

