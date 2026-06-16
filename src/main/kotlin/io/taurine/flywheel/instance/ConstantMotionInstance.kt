package io.taurine.flywheel.instance

import dev.engine_room.flywheel.api.instance.InstanceHandle
import dev.engine_room.flywheel.api.instance.InstanceType
import dev.engine_room.flywheel.lib.instance.TransformedInstance
import io.taurine.extension.toHalf
import io.taurine.flywheel.transform.Move

class ConstantMotionInstance(
    type: InstanceType<ConstantMotionInstance>,
    handle: InstanceHandle
) : TransformedInstance(type, handle), Move {
    override var mx = 0f.toHalf()
    override var my = 0f.toHalf()
    override var mz = 0f.toHalf()

    override var anchorTime = 0f
}

