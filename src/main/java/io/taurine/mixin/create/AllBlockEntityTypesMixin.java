package io.taurine.mixin.create;

import com.llamalad7.mixinextras.injector.ModifyReceiver;
import com.simibubi.create.AllBlockEntityTypes;
import com.simibubi.create.content.kinetics.belt.BeltBlockEntity;
import com.simibubi.create.content.kinetics.saw.SawBlockEntity;
import com.simibubi.create.content.logistics.depot.DepotBlockEntity;
import com.simibubi.create.content.processing.basin.BasinBlockEntity;
import com.simibubi.create.content.redstone.link.RedstoneLinkBlockEntity;
import com.simibubi.create.foundation.data.CreateBlockEntityBuilder;
import com.simibubi.create.foundation.data.CreateRegistrate;
import com.tterrag.registrate.util.nullness.NonNullSupplier;
import dev.engine_room.flywheel.lib.visualization.SimpleBlockEntityVisualizer;
import io.architekton.flywheel.*;
import me.fallenbreath.conditionalmixin.api.annotation.Condition;
import me.fallenbreath.conditionalmixin.api.annotation.Restriction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Slice;

@Restriction(require = @Condition("vanillin"))
@Mixin(AllBlockEntityTypes.class)
public class AllBlockEntityTypesMixin {
    @ModifyArg(
            method = "<clinit>",
            slice = @Slice(
                    from = @At(value = "CONSTANT", args = "stringValue=belt")
            ),
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/simibubi/create/foundation/data/CreateBlockEntityBuilder;visual(Lcom/tterrag/registrate/util/nullness/NonNullSupplier;Ljava/util/function/Predicate;)Lcom/simibubi/create/foundation/data/CreateBlockEntityBuilder;",
                    ordinal = 0
            ),
            index = 0
    )
    private static <T extends BeltBlockEntity> NonNullSupplier<SimpleBlockEntityVisualizer.Factory<T>> extendBeltVisual(
            NonNullSupplier<SimpleBlockEntityVisualizer.Factory<T>> visualFactory
    ) {
        return () -> ExtendedBeltVisual::new;
    }

    @ModifyReceiver(
            method = "<clinit>",
            slice = @Slice(
                    from = @At(value = "CONSTANT", args = "stringValue=depot")
            ),
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/simibubi/create/foundation/data/CreateBlockEntityBuilder;validBlocks([Lcom/tterrag/registrate/util/nullness/NonNullSupplier;)Lcom/tterrag/registrate/builders/BlockEntityBuilder;",
                    ordinal = 0
            )
    )
    private static <T extends DepotBlockEntity, R extends CreateRegistrate> CreateBlockEntityBuilder<T, R> addVisualToChainableCogwheel(
            CreateBlockEntityBuilder<T, R> instance,
            NonNullSupplier<T>[] nonNullSuppliers
    ) {
        return instance.visual(() -> DepotVisual::new, true);
    }

    @ModifyReceiver(
            method = "<clinit>",
            slice = @Slice(
                    from = @At(value = "CONSTANT", args = "stringValue=basin")
            ),
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/simibubi/create/foundation/data/CreateBlockEntityBuilder;validBlocks([Lcom/tterrag/registrate/util/nullness/NonNullSupplier;)Lcom/tterrag/registrate/builders/BlockEntityBuilder;",
                    ordinal = 0
            )
    )
    private static <T extends BasinBlockEntity, R extends CreateRegistrate> CreateBlockEntityBuilder<T, R> addVisualToBasin(
            CreateBlockEntityBuilder<T, R> instance,
            NonNullSupplier<T>[] nonNullSuppliers
    ) {
        return instance.visual(() -> BasinVisual::new, false);
    }

    @ModifyReceiver(
            method = "<clinit>",
            slice = @Slice(
                    from = @At(value = "CONSTANT", args = "stringValue=redstone_link")
            ),
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/simibubi/create/foundation/data/CreateBlockEntityBuilder;validBlocks([Lcom/tterrag/registrate/util/nullness/NonNullSupplier;)Lcom/tterrag/registrate/builders/BlockEntityBuilder;",
                    ordinal = 0
            )
    )
    private static <T extends RedstoneLinkBlockEntity, R extends CreateRegistrate> CreateBlockEntityBuilder<T, R> addVisualToRedstoneLine(
            CreateBlockEntityBuilder<T, R> instance,
            NonNullSupplier<T>[] nonNullSuppliers
    ) {
        return instance.visual(() -> LinkVisual::new, false);
    }
}
