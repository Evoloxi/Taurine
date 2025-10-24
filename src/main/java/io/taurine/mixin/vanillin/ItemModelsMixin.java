package io.taurine.mixin.vanillin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import dev.engine_room.vanillin.item.ItemModels;
import me.fallenbreath.conditionalmixin.api.annotation.Condition;
import me.fallenbreath.conditionalmixin.api.annotation.Restriction;
import me.pepperbell.continuity.client.model.EmissiveBakedModel;
import net.minecraft.client.resources.model.BakedModel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Restriction(require = {
        @Condition("continuity")
})
@Mixin(ItemModels.class)
public abstract class ItemModelsMixin {
    @ModifyReturnValue(
            method = "isSupported(Lnet/minecraft/client/resources/model/BakedModel;)Z",
            at = @At(
                    value = "RETURN",
                    ordinal = 2
            )
    )
    private static boolean onIsSupported(boolean original, @Local(name = "c") Class<? extends BakedModel> clazz) {
        return original || clazz == EmissiveBakedModel.class; // Continuity wraps every model in an EmissiveBakedModel if there are any emissive textures ~Pepper
    }
}
