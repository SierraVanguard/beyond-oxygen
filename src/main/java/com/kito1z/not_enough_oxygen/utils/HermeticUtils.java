package com.kito1z.not_enough_oxygen.utils;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.joml.Vector3i;

public class HermeticUtils {

    public static boolean isFullHermetic(ServerLevel level, BlockPos pos){
        BlockState state = level.getBlockState(pos);
        return state.isCollisionShapeFullBlock(level,pos);
    }
    public static boolean isFullHermetic(ServerLevel level, Vector3i pos){
        return isFullHermetic(level, new BlockPos(pos.x, pos.y, pos.z));
    }

    public static boolean isHermetic(ServerLevel level, BlockPos pos, Direction dir) {
        BlockState state = level.getBlockState(pos);
        return state.isFaceSturdy(level, pos, dir);
    }
    public static boolean canFlowTrough(ServerLevel level, BlockPos pos, Direction from, Direction to){
        if(from.getOpposite()==to){
            BlockState state = level.getBlockState(pos);
            VoxelShape shape = state.getShape(level, pos);
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
