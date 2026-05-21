package com.kill_line.critical_core.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;

public class ThresholdCrystalClusterBlock extends Block {

    public static final int MAX_AGE = 3;
    public static final IntegerProperty AGE = BlockStateProperties.AGE_3;

    private static final VoxelShape[] SHAPES = {
            Block.box(5.0, 0.0, 5.0, 11.0, 6.0, 11.0),
            Block.box(4.0, 0.0, 4.0, 12.0, 8.0, 12.0),
            Block.box(3.0, 0.0, 3.0, 13.0, 10.0, 13.0),
            Block.box(2.0, 0.0, 2.0, 14.0, 12.0, 14.0)
    };

    public ThresholdCrystalClusterBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(AGE, 0));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(AGE);
    }

    @Override
    public @NotNull VoxelShape getShape(@NotNull BlockState state, @NotNull BlockGetter level,
                                         @NotNull BlockPos pos, @NotNull CollisionContext context) {
        return SHAPES[Math.min(state.getValue(AGE), MAX_AGE)];
    }
}
