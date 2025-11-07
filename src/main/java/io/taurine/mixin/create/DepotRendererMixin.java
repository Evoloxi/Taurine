package io.taurine.mixin.create;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;
import com.simibubi.create.content.kinetics.belt.transport.TransportedItemStack;
import com.simibubi.create.content.logistics.depot.DepotRenderer;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import dev.engine_room.flywheel.api.visualization.VisualizationManager;
import io.taurine.extension.ItemStackExtensionKt;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Iterator;
import java.util.List;

@Mixin(DepotRenderer.class)
public class DepotRendererMixin {
    @Redirect(
            method = "renderItemsOf",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/util/List;iterator()Ljava/util/Iterator;"
            )
    )
    private static Iterator<TransportedItemStack> filterLoop(List<TransportedItemStack> instance, @Local(argsOnly = true) SmartBlockEntity be, @Share("taurine$vis") LocalBooleanRef supportsVisualization) {
        if (!VisualizationManager.supportsVisualization(be.getLevel())) {
            return instance.iterator();
        }
        supportsVisualization.set(true);
        return instance.stream()
                .filter(t -> !ItemStackExtensionKt.canBeInstanced(t.stack))
                .iterator();
    }

    @ModifyExpressionValue(
            method = "renderItemsOf",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/item/ItemStack;isEmpty()Z"
            )
    )
    private static boolean asd(boolean original, @Local(name = "stack") ItemStack stack, @Share("taurine$vis") LocalBooleanRef supportsVisualization) {
        return original || (supportsVisualization.get() && ItemStackExtensionKt.canBeInstanced(stack));
    }
}
