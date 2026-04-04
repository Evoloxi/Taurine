package io.taurine.mixin.vanillin.compat.continuity;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import dev.engine_room.vanillin.item.ItemModels;
import me.fallenbreath.conditionalmixin.api.annotation.Condition;
import me.fallenbreath.conditionalmixin.api.annotation.Restriction;
import net.minecraft.client.resources.model.BakedModel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Restriction(require = {
        @Condition("continuity")
})
@Mixin(value = ItemModels.class, remap = false)
public abstract class ItemModelsMixin {

    @Unique
    private static final Class taurine$emissiveBakedModel;

    static {
        try {
            taurine$emissiveBakedModel = Class.forName("me.pepperbell.continuity.client.model.EmissiveBakedModel");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @ModifyReturnValue(
            method = "isSupported(Lnet/minecraft/client/resources/model/BakedModel;)Z",
            at = @At(
                    value = "RETURN",
                    ordinal = 2
            )
    )
    private static boolean onIsSupported(boolean original, @Local(name = "c") Class<? extends BakedModel> clazz) {
        return original || clazz == taurine$emissiveBakedModel; // Continuity wraps every model in an EmissiveBakedModel if there are any emissive textures ~Pepper
    }
}
