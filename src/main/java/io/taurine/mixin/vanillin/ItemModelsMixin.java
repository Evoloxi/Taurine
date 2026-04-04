package io.taurine.mixin.vanillin;

import dev.engine_room.flywheel.lib.util.RendererReloadCache;
import dev.engine_room.vanillin.item.ItemModels;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.PotionItem;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.ForgeRegistries;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = ItemModels.class, remap = false)
public class ItemModelsMixin {

    @Unique
    private static final RendererReloadCache<Block, Boolean> taurine$hasColorCache = new RendererReloadCache<>(block -> {
        var ref = ForgeRegistries.BLOCKS.getDelegateOrThrow(block);
        return ((BlockColorsAccessor) Minecraft.getInstance().getBlockColors()).adhesive$blockColors().get(ref) != null;
    });

    @Inject(
            method = "doesNotHaveItemColors",
            at = @At("HEAD"), // most common case
            cancellable = true
    )
    private static void doesNotHaveItemColors(Item item, CallbackInfoReturnable<Boolean> cir) {
        if (item instanceof BlockItem blockItem) {
            cir.setReturnValue(!taurine$hasColorCache.get(blockItem.getBlock()));
        } else if (item instanceof PotionItem) {
            cir.setReturnValue(false);
        }
    }
}
