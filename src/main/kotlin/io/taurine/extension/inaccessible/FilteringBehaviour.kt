package io.taurine.extension.inaccessible

import com.simibubi.create.content.logistics.filter.FilterItemStack
import com.simibubi.create.foundation.blockEntity.behaviour.filtering.FilteringBehaviour
import io.taurine.mixin.create.accessor.FilteringBehaviourAccessor
import net.minecraft.world.item.ItemStack
import java.util.function.Consumer
import java.util.function.Predicate
import java.util.function.Supplier

val FilteringBehaviour.filter: FilterItemStack
    get() = (this as FilteringBehaviourAccessor).filter

val FilteringBehaviour.predicate: Predicate<ItemStack>
    get() = (this as FilteringBehaviourAccessor).predicate

val FilteringBehaviour.callback: Consumer<ItemStack>
    get() = (this as FilteringBehaviourAccessor).callback

val FilteringBehaviour.isActive: Supplier<Boolean>
    get() = (this as FilteringBehaviourAccessor).isActive

val FilteringBehaviour.showCountPredicate: Supplier<Boolean>
    get() = (this as FilteringBehaviourAccessor).showCountPredicate

val FilteringBehaviour.recipeFilter: Boolean
    get() = (this as FilteringBehaviourAccessor).recipeFilter

val FilteringBehaviour.fluidFilter: Boolean
    get() = (this as FilteringBehaviourAccessor).fluidFilter
