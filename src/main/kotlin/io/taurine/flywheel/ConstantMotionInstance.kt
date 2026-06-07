package io.taurine.flywheel

import dev.engine_room.flywheel.api.instance.InstanceHandle
import dev.engine_room.flywheel.api.instance.InstanceType
import dev.engine_room.flywheel.lib.instance.TransformedInstance
import org.joml.Vector3f

class ConstantMotionInstance(type: InstanceType<ConstantMotionInstance>, handle: InstanceHandle) : TransformedInstance(type, handle) {
    var mx = 0f
    var my = 0f
    var mz = 0f
    var anchorTime = 0f

    var motion: Vector3f
        get() = Vector3f(mx, my, mz)
        set(value) {
            setMotion(value)
        }

    fun setMotion(vector: Vector3f) = setMotion(vector.x, vector.y, vector.z)

    fun setMotion(x: Float, y: Float, z: Float): ConstantMotionInstance {
        mx = x
        my = y
        mz = z
        return this
    }
}