package io.taurine.visual

import com.simibubi.create.content.kinetics.belt.BeltBlock
import com.simibubi.create.content.kinetics.belt.BeltBlockEntity
import com.simibubi.create.content.kinetics.belt.BeltSlope
import net.minecraft.core.Direction
import net.minecraft.core.Vec3i
import net.minecraft.world.phys.Vec3

data class BeltParams(
    val beltFacing: Direction,
    val directionVec: Vec3i,
    val slope: BeltSlope,
    val slopeAlongX: Boolean,
    val alongX: Boolean,
    val verticality: Int,
    val beltStartOffset: Vec3,
    val beltLength: Int,
) {
    companion object {
        fun from(belt: BeltBlockEntity): BeltParams {
            val facing = belt.blockState.getValue(BeltBlock.HORIZONTAL_FACING)
            val slope = belt.blockState.getValue(BeltBlock.SLOPE)
            val verticality = when (slope) {
                BeltSlope.DOWNWARD -> -1
                BeltSlope.UPWARD -> 1
                else -> 0
            }
            return BeltParams(
                beltFacing             = facing,
                directionVec           = facing.normal,
                slope                  = slope,
                slopeAlongX            = facing.axis == Direction.Axis.X,
                alongX                 = facing.clockWise.axis == Direction.Axis.X,
                verticality            = verticality,
                beltStartOffset        = Vec3.atLowerCornerOf(facing.normal)
                                             .scale(-.5)
                                             .add(.5, 15 / 16.0, .5),
                beltLength             = belt.beltLength,
            )
        }
    }
}