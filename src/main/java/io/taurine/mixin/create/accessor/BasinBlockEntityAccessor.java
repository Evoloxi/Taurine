package io.taurine.mixin.create.accessor;

import com.simibubi.create.content.processing.basin.BasinBlockEntity;
import net.createmod.catnip.animation.LerpedFloat;
import net.createmod.catnip.data.IntAttached;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(BasinBlockEntity.class)
public interface BasinBlockEntityAccessor {
    @Accessor("itemCapability")
    IItemHandlerModifiable getItemCapability();

    @Accessor("ingredientRotation")
    LerpedFloat getIngredientRotation();

    @Accessor("visualizedOutputItems")
    List<IntAttached<ItemStack>> getVisualizedOutputItems();
}
