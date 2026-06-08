package io.taurine.mixin.create;

import com.llamalad7.mixinextras.injector.ModifyReceiver;
import com.simibubi.create.AllBlockEntityTypes;
import com.simibubi.create.content.fluids.pipes.SmartFluidPipeBlockEntity;
import com.simibubi.create.content.kinetics.belt.BeltBlockEntity;
import com.simibubi.create.content.kinetics.belt.BeltVisual;
import com.simibubi.create.content.logistics.chute.SmartChuteBlockEntity;
import com.simibubi.create.content.logistics.crate.CreativeCrateBlockEntity;
import com.simibubi.create.content.logistics.depot.DepotBlockEntity;
import com.simibubi.create.content.processing.basin.BasinBlockEntity;
import com.simibubi.create.content.redstone.link.RedstoneLinkBlockEntity;
import com.simibubi.create.foundation.data.CreateBlockEntityBuilder;
import com.simibubi.create.foundation.data.CreateRegistrate;
import com.tterrag.registrate.util.nullness.NonNullSupplier;
import dev.engine_room.flywheel.lib.visualization.SimpleBlockEntityVisualizer;
import io.taurine.visual.impl.DepotVisual;
import io.taurine.visual.impl.FilterVisual;
import io.taurine.visual.impl.LinkVisual;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;

import java.util.function.Predicate;

@Mixin(AllBlockEntityTypes.class)
public class AllBlockEntityTypesMixin {

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
    private static <T extends DepotBlockEntity, R extends CreateRegistrate> CreateBlockEntityBuilder<T, R> addVisualToDepot(
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
        return instance.visual(() -> FilterVisual::new, true);
    }

    @ModifyReceiver(
            method = "<clinit>",
            slice = @Slice(
                    from = @At(value = "CONSTANT", args = "stringValue=creative_crate")
            ),
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/simibubi/create/foundation/data/CreateBlockEntityBuilder;validBlocks([Lcom/tterrag/registrate/util/nullness/NonNullSupplier;)Lcom/tterrag/registrate/builders/BlockEntityBuilder;",
                    ordinal = 0
            )
    )
    private static <T extends CreativeCrateBlockEntity, R extends CreateRegistrate> CreateBlockEntityBuilder<T, R> addVisualToCreativeCrate(
            CreateBlockEntityBuilder<T, R> instance,
            NonNullSupplier<T>[] nonNullSuppliers
    ) {
        return instance.visual(() -> FilterVisual::new, true);
    }

    @ModifyReceiver(
            method = "<clinit>",
            slice = @Slice(
                    from = @At(value = "CONSTANT", args = "stringValue=smart_chute")
            ),
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/simibubi/create/foundation/data/CreateBlockEntityBuilder;validBlocks([Lcom/tterrag/registrate/util/nullness/NonNullSupplier;)Lcom/tterrag/registrate/builders/BlockEntityBuilder;",
                    ordinal = 0
            )
    )
    private static <T extends SmartChuteBlockEntity, R extends CreateRegistrate> CreateBlockEntityBuilder<T, R> addVisualToSmartChute(
            CreateBlockEntityBuilder<T, R> instance,
            NonNullSupplier<T>[] nonNullSuppliers
    ) {
        return instance.visual(() -> FilterVisual::new, true);
    }

    @ModifyReceiver(
            method = "<clinit>",
            slice = @Slice(
                    from = @At(value = "CONSTANT", args = "stringValue=smart_fluid_pipe")
            ),
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/simibubi/create/foundation/data/CreateBlockEntityBuilder;validBlocks([Lcom/tterrag/registrate/util/nullness/NonNullSupplier;)Lcom/tterrag/registrate/builders/BlockEntityBuilder;",
                    ordinal = 0
            )
    )
    private static <T extends SmartFluidPipeBlockEntity, R extends CreateRegistrate> CreateBlockEntityBuilder<T, R> addVisualToSmartFluidPipe(
            CreateBlockEntityBuilder<T, R> instance,
            NonNullSupplier<T>[] nonNullSuppliers
    ) {
        return instance.visual(() -> FilterVisual::new, true);
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
    private static <T extends RedstoneLinkBlockEntity, R extends CreateRegistrate> CreateBlockEntityBuilder<T, R> addVisualToRedstoneLink(
            CreateBlockEntityBuilder<T, R> instance,
            NonNullSupplier<T>[] nonNullSuppliers
    ) {
        return instance.visual(() -> LinkVisual::new, true);
    }
}
