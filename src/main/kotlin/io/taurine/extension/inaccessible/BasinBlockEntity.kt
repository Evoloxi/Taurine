package io.taurine.extension.inaccessible

import com.simibubi.create.content.processing.basin.BasinBlockEntity
import io.taurine.mixin.create.accessor.BasinBlockEntityAccessor
import net.createmod.catnip.animation.LerpedFloat
import net.createmod.catnip.data.IntAttached
import net.minecraft.world.item.ItemStack
import net.neoforged.neoforge.items.IItemHandlerModifiable

val BasinBlockEntity.visualizedOutputItems: List<IntAttached<ItemStack>>
    get() = (this as BasinBlockEntityAccessor).visualizedOutputItems

val BasinBlockEntity.ingredientRotation: LerpedFloat
    get() = (this as BasinBlockEntityAccessor).ingredientRotation

val BasinBlockEntity.itemCapability: IItemHandlerModifiable?
    get() = (this as BasinBlockEntityAccessor).itemCapability