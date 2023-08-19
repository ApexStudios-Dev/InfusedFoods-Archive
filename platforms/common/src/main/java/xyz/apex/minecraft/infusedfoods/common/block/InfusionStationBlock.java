package xyz.apex.minecraft.infusedfoods.common.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import xyz.apex.minecraft.apexcore.common.lib.component.block.BaseBlockComponentHolder;
import xyz.apex.minecraft.apexcore.common.lib.component.block.BlockComponentRegistrar;
import xyz.apex.minecraft.apexcore.common.lib.component.block.types.BlockComponentTypes;
import xyz.apex.minecraft.apexcore.common.lib.component.block.types.HorizontalFacingBlockComponent;
import xyz.apex.minecraft.apexcore.common.lib.helper.VoxelShapeHelper;
import xyz.apex.minecraft.infusedfoods.common.InfusedFoods;
import xyz.apex.minecraft.infusedfoods.common.block.entity.InfusionStationBlockEntity;

public final class InfusionStationBlock extends BaseBlockComponentHolder
{
    private static final VoxelShape SHAPE = VoxelShapeHelper.combine(
            box(1D, 0D, 5D, 13D, 1D, 11D),
            box(13D, 0D, 7D, 15D, 14D, 9D)
    );

    private static final VoxelShape SHAPE_EAST = VoxelShapeHelper.rotateHorizontal(SHAPE, Direction.EAST);
    private static final VoxelShape SHAPE_SOUTH = VoxelShapeHelper.rotateHorizontal(SHAPE, Direction.SOUTH);
    private static final VoxelShape SHAPE_WEST = VoxelShapeHelper.rotateHorizontal(SHAPE, Direction.WEST);

    public InfusionStationBlock(Properties properties)
    {
        super(properties);
    }

    @Override
    protected void registerComponents(BlockComponentRegistrar registrar)
    {
        registrar.register(BlockComponentTypes.HORIZONTAL_FACING);
        registrar.register(BlockComponentTypes.WATERLOGGED);
        registrar.register(BlockComponentTypes.MENU_PROVIDER, component -> component.withExtraData((level, pos, blockState, buffer) -> buffer.writeBlockPos(pos)));
    }

    @Override
    protected BlockEntityType<?> getBlockEntityType()
    {
        return InfusedFoods.BLOCK_ENTITY.value();
    }

    @Override
    public void animateTick(BlockState blockState, Level level, BlockPos pos, RandomSource random)
    {
        if(blockState.getValue(BlockStateProperties.WATERLOGGED))
            return;

        var facing = blockState.getValue(HorizontalFacingBlockComponent.FACING).getClockWise();

        var x = (double) pos.getX() + .4D + (double) random.nextFloat() * .2D + (facing.getStepX() * .4D);
        var y = (double) pos.getY() + .7D + (double) random.nextFloat() * .3D;
        var z = (double) pos.getZ() + .4D + (double) random.nextFloat() * .2D + (facing.getStepZ() * .4D);

        level.addParticle(ParticleTypes.SMOKE, x, y, z, 0D, 0D, 0D);
    }

    @Override
    public VoxelShape getShape(BlockState blockState, BlockGetter level, BlockPos pos, CollisionContext context)
    {
        return switch(blockState.getValue(HorizontalFacingBlockComponent.FACING)) {
            case EAST -> SHAPE_EAST;
            case SOUTH -> SHAPE_SOUTH;
            case WEST -> SHAPE_WEST;
            default -> SHAPE;
        };
    }

    @Override
    public boolean isPathfindable(BlockState blockState, BlockGetter level, BlockPos pos, PathComputationType type)
    {
        return false;
    }

    @Nullable
    @Override
    protected <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState blockState)
    {
        return level.isClientSide ? null : (level1, blockPos, blockState1, blockEntity) -> InfusionStationBlockEntity.serverTick(level1, blockPos, blockState1, (InfusionStationBlockEntity) blockEntity);
    }
}
