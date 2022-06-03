package xyz.apex.forge.infusedfoods.block;

import net.minecraft.block.BlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.LivingEntity;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.pathfinding.PathType;
import net.minecraft.state.Property;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

import xyz.apex.forge.apexcore.lib.block.VoxelShaper;
import xyz.apex.forge.apexcore.revamp.block.BaseBlock;
import xyz.apex.forge.infusedfoods.InfusedFoods;
import xyz.apex.forge.infusedfoods.block.entity.InfusionStationBlockEntity;
import xyz.apex.forge.infusedfoods.container.InfusionStationContainer;
import xyz.apex.forge.infusedfoods.init.IFElements;
import xyz.apex.java.utility.nullness.NonnullConsumer;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;

import static xyz.apex.forge.apexcore.revamp.block.entity.BaseBlockEntity.NBT_APEX;

public final class InfusionStationBlock extends BaseBlock.WithContainer<InfusionStationBlockEntity, InfusionStationContainer>
{
	public static final VoxelShape SHAPE = VoxelShaper.or(
			box(1D, 0D, 5D, 13D, 1D, 11D),
			box(13D, 0D, 7D, 15D, 14D, 9D)
	);

	public static final VoxelShaper SHAPER = VoxelShaper.forHorizontal(SHAPE, Direction.NORTH);

	public InfusionStationBlock(Properties properties)
	{
		super(properties);

		registerDefaultState(defaultBlockState().setValue(FACING_4_WAY, Direction.NORTH).setValue(WATERLOGGED, false));
	}

	@Override
	protected void registerProperties(NonnullConsumer<Property<?>> consumer)
	{
		consumer.accept(FACING_4_WAY);
		consumer.accept(WATERLOGGED);
	}

	@Override
	public void animateTick(BlockState blockState, World level, BlockPos pos, Random rng)
	{
		if(blockState.hasProperty(WATERLOGGED) && !blockState.getValue(WATERLOGGED))
		{
			Direction facing = blockState.getOptionalValue(FACING_4_WAY).orElse(Direction.NORTH).getClockWise();

			double x = (double) pos.getX() + .4D + (double) rng.nextFloat() * .2D + (facing.getStepX() * .4D);
			double y = (double) pos.getY() + .7D + (double) rng.nextFloat() * .3D;
			double z = (double) pos.getZ() + .4D + (double) rng.nextFloat() * .2D + (facing.getStepZ() * .4D);

			level.addParticle(ParticleTypes.SMOKE, x, y, z, 0D, 0D, 0D);
		}
	}

	@Override
	public void setPlacedBy(World level, BlockPos pos, BlockState blockState, @Nullable LivingEntity placer, ItemStack stack)
	{
		super.setPlacedBy(level, pos, blockState, placer, stack);

		InfusionStationBlockEntity blockEntity = getBlockEntity(level, pos);
		CompoundNBT apexTag = stack.getTagElement(NBT_APEX);

		if(blockEntity != null && apexTag != null)
			blockEntity.deserializeData(apexTag);
	}

	@Override
	public VoxelShape getShape(BlockState blockState, IBlockReader level, BlockPos pos, ISelectionContext ctx)
	{
		if(blockState.hasProperty(FACING_4_WAY))
		{
			Direction facing = blockState.getValue(FACING_4_WAY);
			return SHAPER.get(facing);
		}

		return VoxelShapes.block();
	}

	@Override
	public boolean isPathfindable(BlockState blockState, IBlockReader level, BlockPos pos, PathType pathType)
	{
		return false;
	}

	@Override
	public boolean propagatesSkylightDown(BlockState blockState, IBlockReader level, BlockPos pos)
	{
		return !blockState.getValue(WATERLOGGED);
	}

	@Override
	protected TileEntityType<InfusionStationBlockEntity> getBlockEntityType()
	{
		return IFElements.INFUSION_STATION_BLOCK_ENTITY.asBlockEntityType();
	}

	@Override
	protected ContainerType<InfusionStationContainer> getContainerType()
	{
		return IFElements.INFUSION_STATION_CONTAINER.asContainerType();
	}

	@Override
	public void appendHoverText(ItemStack stack, @Nullable IBlockReader level, List<ITextComponent> tooltip, ITooltipFlag flag)
	{
		super.appendHoverText(stack, level, tooltip, flag);
		InfusedFoods.appendPotionEffectTooltips(stack, tooltip);
	}
}
