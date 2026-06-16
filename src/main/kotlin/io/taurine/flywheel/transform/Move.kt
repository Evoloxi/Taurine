package io.taurine.flywheel.transform

import io.taurine.extension.Float16
import io.taurine.extension.toHalf
import org.joml.Vector3fc
import io.taurine.extension.x
import io.taurine.extension.y
import io.taurine.extension.z

interface Move {
    var mx: Float16
    var my: Float16
    var mz: Float16
    var anchorTime: Float
}

fun <T : Move> T.setMotion(x: Float, y: Float, z: Float): T = apply {
    mx = x.toHalf()
    my = y.toHalf()
    mz = z.toHalf()
}

var <T : Move> T.motion: Vector3fc
    get() = throw NotImplementedError("set-only property")
    set(value) {
        setMotion(
            value.x,
            value.y,
            value.z
        )
    }