/*
 * Improved Stations by shedaniel.
 * Licensed under the MIT.
 */

package me.shedaniel.istations.blocks;

import net.minecraft.block.*;
import net.minecraft.block.enums.SlabType;
import net.minecraft.entity.EntityContext;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.tag.FluidTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.IWorld;

import static net.minecraft.state.property.Properties.WATERLOGGED;

public class LoomSlabBlock extends LoomBlock implements Waterloggable {
    public static final EnumProperty<SlabType> TYPE = SlabBlock.TYPE;
    protected static final VoxelShape BOTTOM_SHAPE;
    protected static final VoxelShape TOP_SHAPE;
    
    static {
        BOTTOM_SHAPE = Block.createCuboidShape(0.0D, 0.0D, 0.0D, 16.0D, 8.0D, 16.0D);
        TOP_SHAPE = Block.createCuboidShape(0.0D, 8.0D, 0.0D, 16.0D, 16.0D, 16.0D);
    }
    
    public LoomSlabBlock(Settings settings) {
        super(settings);
        this.setDefaultState(this.stateManager.getDefaultState().with(FACING, Direction.NORTH).with(TYPE, SlabType.BOTTOM).with(WATERLOGGED, Boolean.FALSE));
    }
    
    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        BlockPos blockPos = ctx.getBlockPos();
        BlockState blockState = ctx.getWorld().getBlockState(blockPos);
        if (blockState.getBlock() == this) {
            return null;
        } else {
            FluidState fluidState = ctx.getWorld().getFluidState(blockPos);
            BlockState blockState2 = this.getDefaultState().with(TYPE, SlabType.BOTTOM).with(FACING, ctx.getPlayerFacing().getOpposite()).with(WATERLOGGED, fluidState.getFluid() == Fluids.WATER);
            Direction direction = ctx.getSide();
            return direction != Direction.DOWN && (direction == Direction.UP || ctx.getHitPos().y - (double) blockPos.getY() <= 0.5D) ? blockState2 : blockState2.with(TYPE, SlabType.TOP);
        }
    }
    
    @SuppressWarnings("deprecation")
    @Override
    public FluidState getFluidState(BlockState state) {
        return state.get(WATERLOGGED) ? Fluids.WATER.getStill(false) : super.getFluidState(state);
    }
    
    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        super.appendProperties(builder);
        builder.add(WATERLOGGED, TYPE);
    }
    
    @SuppressWarnings("deprecation")
    @Override
    public BlockState getStateForNeighborUpdate(BlockState state, Direction facing, BlockState neighborState, IWorld world, BlockPos pos, BlockPos neighborPos) {
        if (state.get(WATERLOGGED)) {
            world.getFluidTickScheduler().schedule(pos, Fluids.WATER, Fluids.WATER.getTickRate(world));
        }
        return super.getStateForNeighborUpdate(state, facing, neighborState, world, pos, neighborPos);
    }
    
    @SuppressWarnings("deprecation")
    @Override
    public boolean canPathfindThrough(BlockState world, BlockView view, BlockPos pos, BlockPlacementEnvironment env) {
        if (env == BlockPlacementEnvironment.WATER) {
            return view.getFluidState(pos).matches(FluidTags.WATER);
        }
        return false;
    }
    
    @SuppressWarnings("deprecation")
    @Override
    public VoxelShape getOutlineShape(BlockState blockState_1, BlockView blockView_1, BlockPos blockPos_1, EntityContext entityContext_1) {
        SlabType slabType = blockState_1.get(TYPE);
        switch (slabType) {
            case DOUBLE:
                return VoxelShapes.fullCube();
            case TOP:
                return TOP_SHAPE;
            default:
                return BOTTOM_SHAPE;
        }
    }
}
