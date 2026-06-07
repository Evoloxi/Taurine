package io.taurine.mixin.create.accessor;

import com.simibubi.create.content.kinetics.deployer.DeployerBlockEntity;
import io.taurine.duck.DeployerDuck;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(DeployerBlockEntity.class)
public interface DeployerBlockEntityAccessor extends DeployerDuck {
    @Accessor("heldItem")
    ItemStack getHeldItem();

    @Accessor("reach")
    float getReach();
}
