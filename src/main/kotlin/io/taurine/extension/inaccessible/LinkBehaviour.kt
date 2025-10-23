package io.taurine.extension.inaccessible

import com.simibubi.create.content.redstone.link.LinkBehaviour
import com.simibubi.create.content.redstone.link.RedstoneLinkNetworkHandler
import com.simibubi.create.foundation.blockEntity.behaviour.ValueBoxTransform
import io.taurine.mixin.create.accessor.LinkBehaviourAccessor


val LinkBehaviour.firstSlot: ValueBoxTransform
    get() = (this as LinkBehaviourAccessor).firstSlot

val LinkBehaviour.secondSlot: ValueBoxTransform
    get() = (this as LinkBehaviourAccessor).secondSlot

val LinkBehaviour.frequencyFirst: RedstoneLinkNetworkHandler.Frequency
    get() = (this as LinkBehaviourAccessor).frequencyFirst

val LinkBehaviour.frequencyLast: RedstoneLinkNetworkHandler.Frequency
    get() = (this as LinkBehaviourAccessor).frequencyLast
