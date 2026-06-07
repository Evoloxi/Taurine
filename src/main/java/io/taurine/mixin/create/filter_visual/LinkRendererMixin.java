package io.taurine.mixin.create.filter_visual;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.content.redstone.link.LinkBehaviour;
import com.simibubi.create.content.redstone.link.LinkRenderer;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueBoxRenderer;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueBoxTransform;
import dev.engine_room.flywheel.api.visualization.VisualizationManager;
import io.taurine.ModelCache;
import io.taurine.mixin.create.accessor.LinkBehaviourAccessor;
import net.createmod.catnip.data.Iterate;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LinkRenderer.class)
public class LinkRendererMixin {

    @Definition(id = "behaviour", local = @Local(type = LinkBehaviour.class, name = "behaviour"))
    @Expression("behaviour == null")
    @Inject(
            method = "renderOnBlockEntity",
            at = @At("MIXINEXTRAS:EXPRESSION"),
            cancellable = true
    )
    private static void filter(SmartBlockEntity be, float partialTicks, PoseStack ms, MultiBufferSource buffer, int light, int overlay, CallbackInfo ci, @Local(name = "behaviour") LinkBehaviour behaviour) {
        if (behaviour == null) return;

        for (boolean first : Iterate.trueAndFalse) {
            ValueBoxTransform transform = first ? ((LinkBehaviourAccessor) behaviour).getFirstSlot() : ((LinkBehaviourAccessor) behaviour).getSecondSlot();
            ItemStack stack = first ? ((LinkBehaviourAccessor) behaviour).getFrequencyFirst().getStack() : ((LinkBehaviourAccessor) behaviour).getFrequencyLast().getStack();

            if (!(VisualizationManager.supportsVisualization(be.getLevel()) && ModelCache.canBeInstanced(stack))) {
                ms.pushPose();
                transform.transform(be.getLevel(), be.getBlockPos(), be.getBlockState(), ms);
                ValueBoxRenderer.renderItemIntoValueBox(stack, ms, buffer, light, overlay);
                ms.popPose();
            }
        }

        ci.cancel();
    }
}