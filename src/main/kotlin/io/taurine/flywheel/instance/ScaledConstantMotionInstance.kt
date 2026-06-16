package io.taurine.flywheel.instance

import dev.engine_room.flywheel.api.instance.InstanceHandle
import dev.engine_room.flywheel.api.instance.InstanceType
import dev.engine_room.flywheel.lib.instance.ColoredLitOverlayInstance
import dev.engine_room.flywheel.lib.transform.Translate
import io.taurine.extension.toHalf
import io.taurine.flywheel.transform.IsotropicScale
import io.taurine.flywheel.transform.Move

class ScaledConstantMotionInstance(
    type: InstanceType<ScaledConstantMotionInstance>,
    handle: InstanceHandle
) : ColoredLitOverlayInstance(type, handle),
    Translate<ScaledConstantMotionInstance>,
    Move,
    IsotropicScale {

    var posx = 0f
    var posy = 0f
    var posz = 0f

    override var mx = 0f.toHalf()
    override var my = 0f.toHalf()
    override var mz = 0f.toHalf()

    override var anchorTime = 0f

    override var scale = 0f.toHalf()

    override fun translate(
        x: Float,
        y: Float,
        z: Float
    ) = this.apply {
        posx = x
        posy = y
        posz = z
    }
}