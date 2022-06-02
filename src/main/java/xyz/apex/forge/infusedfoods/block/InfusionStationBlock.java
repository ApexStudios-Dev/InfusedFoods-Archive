package xyz.apex.forge.infusedfoods.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.network.PacketDistributor;

import xyz.apex.forge.apexcore.lib.block.ContainerBlock;
import xyz.apex.forge.apexcore.lib.block.VoxelShaper;
import xyz.apex.forge.apexcore.lib.util.ContainerHelper;
import xyz.apex.forge.infusedfoods.InfusedFoods;
import xyz.apex.forge.infusedfoods.block.entity.InfusionStationBlockEntity;
import xyz.apex.forge.infusedfoods.init.IFElements;
import xyz.apex.forge.infusedfoods.network.PacketSyncInfusionData;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;

public final class InfusionStationBlock extends ContainerBlock<InfusionStationBlockEntity> implements SimpleWaterloggedBlock
{
	public static final VoxelShape SHAPE = VoxelShaper.or(
			box(1D, 0D, 5D, 13D, 1D, 11D),
			box(13D, 0D, 7D, 15D, 14D, 9D)
	);

	public static final VoxelShaper SHAPER = VoxelShaper.forHorizontal(SHAPE, Direction.NORTH);

	public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
	public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

	public InfusionStationBlock(Properties properties)
	{
		super(properties);

		registerDefaultState(defaultBlockState().setValue(WATERLOGGED, false).setValue(FACING, Direction.NORTH));
	}

	@Override
	public void animateTick(BlockState blockState, Level level, BlockPos pos, Random rng)
	{
		if(!blockState.getValue(WATERLOGGED))
		{
			var facing = blockState.getValue(FACING).getClockWise();

			var x = (double) pos.getX() + .4D + (double) rng.nextFloat() * .2D + (facing.getStepX() * .4D);
			var y = (double) pos.getY() + .7D + (double) rng.nextFloat() * .3D;
			var z = (double) pos.getZ() + .4D + (double) rng.nextFloat() * .2D + (facing.getStepZ() * .4D);

			level.addParticle(ParticleTypes.SMOKE, x, y, z, 0D, 0D, 0D);
		}
	}

	@Override
	public VoxelShape getShape(BlockState blockState, BlockGetter level, BlockPos pos, CollisionContext ctx)
	{
		var facing = blockState.getValue(FACING);
		return SHAPER.get(facing);
	}

	@Override
	public boolean isPathfindable(BlockState blockState, BlockGetter level, BlockPos pos, PathComputationType pathType)
	{
		return false;
	}

	@Override
	public boolean propagatesSkylightDown(BlockState blockState, BlockGetter level, BlockPos pos)
	{
		return !blockState.getValue(WATERLOGGED);
	}

	@Override
	public FluidState getFluidState(BlockState blockState)
	{
		return blockState.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(blockState);
	}

	@Override
	public BlockState updateShape(BlockState blockState, Direction facing, BlockState facingBlockState, LevelAccessor level, BlockPos pos, BlockPos facingPos)
	{
		if(blockState.getValue(WATERLOGGED))
			level.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));

		return super.updateShape(blockState, facing, facingBlockState, level, pos, facingPos);
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext ctx)
	{
		var stateForPlacement = super.getStateForPlacement(ctx);

		if(stateForPlacement != null)
		{
			var fluidState = ctx.getLevel().getFluidState(ctx.getClickedPos());
			var waterLogged = fluidState.is(FluidTags.WATER);
			return stateForPlacement.setValue(WATERLOGGED, waterLogged).setValue(FACING, ctx.getHorizontalDirection().getOpposite());
		}

		return null;
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
	{
		builder.add(WATERLOGGED, FACING);
		super.createBlockStateDefinition(builder);
	}

	@Override
	public BlockState rotate(BlockState blockState, Rotation rotation)
	{
		return blockState.setValue(FACING, rotation.rotate(blockState.getValue(FACING)));
	}

	@Override
	public BlockState mirror(BlockState blockState, Mirror mirror)
	{
		return blockState.rotate(mirror.getRotation(blockState.getValue(FACING)));
	}

	@Override
	protected BlockEntityType<InfusionStationBlockEntity> getBlockEntityType()
	{
		return IFElements.INFUSION_STATION_BLOCK_ENTITY.asBlockEntityType();
	}

	@Override
	public void setPlacedBy(Level level, BlockPos pos, BlockState blockState, @Nullable LivingEntity placer, ItemStack stack)
	{
		super.setPlacedBy(level, pos, blockState, placer, stack);

		var blockEntity = getBlockEntity(level, pos);

		if(blockEntity != null)
		{
			var stackTag = stack.getTag();

			if(stackTag != null)
				blockEntity.loadFromItemStack(stackTag);

			if(stack.hasCustomHoverName())
			{
				var customName = stack.getHoverName();
				blockEntity.setCustomName(customName); // TODO:
			}
		}
	}

	@Override
	public void onRemove(BlockState blockState, Level level, BlockPos pos, BlockState newBlockState, boolean isMoving)
	{
		var blockEntity = getBlockEntity(level, pos);

		if(blockEntity != null)
		{
			var itemHandler = blockEntity.getItemHandler();

			for(int i = 0; i < itemHandler.getSlots(); i++)
			{
				var stack = itemHandler.getStackInSlot(i);
				Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), stack);
			}
		}

		super.onRemove(blockState, level, pos, newBlockState, isMoving);
	}

	@Override
	public boolean hasAnalogOutputSignal(BlockState blockState)
	{
		return true;
	}

	@Override
	public int getAnalogOutputSignal(BlockState blockState, Level level, BlockPos pos)
	{
		return ContainerHelper.getRedstoneSignalFromContainer(level, pos);
	}

	@Override
	protected boolean openContainerScreen(BlockState blockState, Level level, BlockPos pos, ServerPlayer player, InteractionHand hand, ItemStack stack, Component titleComponent)
	{
		super.openContainerScreen(blockState, level, pos, player, hand, stack, titleComponent);

		var blockEntity = getBlockEntity(level, pos);

		if(blockEntity != null)
		{
			var updateTag = blockEntity.getUpdateTag();
			InfusedFoods.NETWORK.sendTo(new PacketSyncInfusionData(pos, updateTag), PacketDistributor.ALL.noArg());
		}

		return true;
	}

	@Override
	public void appendHoverText(ItemStack stack, @Nullable BlockGetter level, List<Component> tooltip, TooltipFlag flag)
	{
		super.appendHoverText(stack, level, tooltip, flag);
		InfusedFoods.appendPotionEffectTooltips(stack, tooltip);
	}

	@Nullable
	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState blockState, BlockEntityType<T> blockEntityType)
	{
		return level.isClientSide ? null : createTickerHelper(blockEntityType, getBlockEntityType(), InfusionStationBlockEntity::serverTick);
	}

	@Nullable
	public static <E extends BlockEntity, A extends BlockEntity> BlockEntityTicker<A> createTickerHelper(BlockEntityType<A> blockEntityTypeA, BlockEntityType<E> blockEntityTypeE, BlockEntityTicker<? super E> blockEntityTicker)
	{
		return blockEntityTypeE == blockEntityTypeA ? (BlockEntityTicker<A>) blockEntityTicker : null;
	}
}
