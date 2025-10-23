package io.taurine.mixin.create.accessor;

import com.simibubi.create.content.logistics.filter.FilterItemStack;
import com.simibubi.create.foundation.blockEntity.behaviour.filtering.FilteringBehaviour;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

@Mixin(FilteringBehaviour.class)
public interface FilteringBehaviourAccessor {
    @Accessor("filter")
    FilterItemStack getFilter();

    @Accessor("predicate")
    Predicate<ItemStack> getPredicate();

    @Accessor("callback")
    Consumer<ItemStack> getCallback();

    @Accessor("isActive")
    Supplier<Boolean> getIsActive();

    @Accessor("showCountPredicate")
    Supplier<Boolean> getShowCountPredicate();

    @Accessor("recipeFilter")
    boolean getRecipeFilter();

    @Accessor("fluidFilter")
    boolean getFluidFilter();
}
