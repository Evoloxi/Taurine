package io.taurine.mixin.create.accessor;

import com.simibubi.create.content.processing.basin.BasinBlockEntity;
import net.createmod.catnip.animation.LerpedFloat;
import net.createmod.catnip.data.IntAttached;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandlerModifiable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(value = BasinBlockEntity.class, remap = false)
public interface BasinBlockEntityAccessor {
    @Accessor("itemCapability")
    LazyOptional<IItemHandlerModifiable> getItemCapability();

    @Accessor("ingredientRotation")
    LerpedFloat getIngredientRotation();

    @Accessor("visualizedOutputItems")
    List<IntAttached<ItemStack>> getVisualizedOutputItems();
}
