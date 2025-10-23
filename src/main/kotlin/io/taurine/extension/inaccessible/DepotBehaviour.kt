package io.taurine.extension.inaccessible

import com.simibubi.create.content.kinetics.belt.transport.TransportedItemStack
import com.simibubi.create.content.logistics.depot.DepotBehaviour
import com.simibubi.create.content.logistics.depot.DepotBlockEntity
import io.taurine.mixin.create.accessor.DepotBehaviourAccessor
import io.taurine.mixin.create.accessor.DepotBlockEntityAccessor
import net.neoforged.neoforge.items.ItemStackHandler

val DepotBlockEntity.depotBehaviour: DepotBehaviour
    get() = (this as DepotBlockEntityAccessor).depotBehaviour

val DepotBehaviour.heldItem: TransportedItemStack?
    get() = (this as DepotBehaviourAccessor).heldItem

val DepotBehaviour.processingOutputBuffer: ItemStackHandler
    get() = (this as DepotBehaviourAccessor).processingOutputBuffer

val DepotBehaviour.incoming: List<TransportedItemStack>
    get() = (this as DepotBehaviourAccessor).incoming
