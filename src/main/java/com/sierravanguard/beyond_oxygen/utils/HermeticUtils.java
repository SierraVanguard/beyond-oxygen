package com.sierravanguard.beyond_oxygen.utils;

import com.sierravanguard.beyond_oxygen.blocks.OxygenHarvesterBlock;
import com.sierravanguard.beyond_oxygen.blocks.VentBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.VoxelShape;

public class HermeticUtils {

    public static boolean isHermetic(ServerLevel level, BlockPos pos, Direction dir) {
        BlockState state = level.getBlockState(pos);
        if (state.getBlock() instanceof OxygenHarvesterBlock) {
            return false;
        }
        if (state.getBlock() instanceof VentBlock ventBlock) {
            Direction facing = state.getValue(VentBlock.FACING);
            return dir != facing;
        }

        return state.isFaceSturdy(level, pos, dir);
    }
    public static boolean canFlowTrough(ServerLevel level, BlockPos pos, Direction from, Direction to){
        if(from.getOpposite()==to){
            Direction.Axis axis = from.getAxis();
            switch (axis){
                case X -> {
                    return !isFlatHermetic(level, pos, Direction.Axis.Y, Direction.Axis.Z);
                }
                case Y -> {
                    return !isFlatHermetic(level, pos, Direction.Axis.X, Direction.Axis.Z);
                }
                case Z -> {
                    return !isFlatHermetic(level, pos, Direction.Axis.X, Direction.Axis.Y);
                }
            }
        }
        return !isHermetic(level, pos, from) && !isHermetic(level, pos, to);
    }
    public static boolean isFlatHermetic(ServerLevel level, BlockPos pos, Direction.Axis a, Direction.Axis b){
        BlockState state = level.getBlockState(pos);
        VoxelShape shape = state.getShape(level, pos);
        return (shape.max(a)>=1 && shape.min(a)<=0) && (shape.max(b)>=1 && shape.min(b)<=0);
    }
}
