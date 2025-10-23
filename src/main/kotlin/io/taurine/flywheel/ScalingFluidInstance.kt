package io.taurine.visual

import dev.engine_room.flywheel.api.instance.InstanceHandle
import dev.engine_room.flywheel.api.instance.InstanceType
import dev.engine_room.flywheel.lib.instance.TransformedInstance

class ScalingFluidInstance(type: InstanceType<out TransformedInstance>, handle: InstanceHandle) : TransformedInstance(type, handle) {
	var vScale: Float = 1f
    var uScale: Float = 1f

    var v0: Float = 1f
    var u0: Float = 1f
}