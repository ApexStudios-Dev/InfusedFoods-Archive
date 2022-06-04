package xyz.apex.forge.infusedfoods.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import xyz.apex.forge.apexcore.lib.block.VoxelShaper;
import xyz.apex.forge.apexcore.revamp.block.BaseBlock;
import xyz.apex.forge.infusedfoods.InfusedFoods;
import xyz.apex.forge.infusedfoods.block.entity.InfusionStationBlockEntity;
import xyz.apex.forge.infusedfoods.container.InfusionStationMenu;
import xyz.apex.forge.infusedfoods.init.IFElements;
import xyz.apex.java.utility.nullness.NonnullConsumer;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;

import static xyz.apex.forge.apexcore.revamp.block.entity.BaseBlockEntity.NBT_APEX;

public final class InfusionStationBlock extends BaseBlock.WithContainer<InfusionStationBlockEntity, InfusionStationMenu>
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
	public void animateTick(BlockState blockState, Level level, BlockPos pos, Random rng)
	{
		if(blockState.hasProperty(WATERLOGGED) && !blockState.getValue(WATERLOGGED))
		{
			var facing = blockState.getOptionalValue(FACING_4_WAY).orElse(Direction.NORTH).getClockWise();

			var x = (double) pos.getX() + .4D + (double) rng.nextFloat() * .2D + (facing.getStepX() * .4D);
			var y = (double) pos.getY() + .7D + (double) rng.nextFloat() * .3D;
			var z = (double) pos.getZ() + .4D + (double) rng.nextFloat() * .2D + (facing.getStepZ() * .4D);

			level.addParticle(ParticleTypes.SMOKE, x, y, z, 0D, 0D, 0D);
		}
	}

	@Override
	public void setPlacedBy(Level level, BlockPos pos, BlockState blockState, @Nullable LivingEntity placer, ItemStack stack)
	{
		super.setPlacedBy(level, pos, blockState, placer, stack);

		var blockEntity = getBlockEntity(level, pos);
		var apexTag = stack.getTagElement(NBT_APEX);

		if(blockEntity != null && apexTag != null)
			blockEntity.deserializeData(apexTag);
	}

	@Override
	public VoxelShape getShape(BlockState blockState, BlockGetter level, BlockPos pos, CollisionContext ctx)
	{
		if(blockState.hasProperty(FACING_4_WAY))
		{
			var facing = blockState.getValue(FACING_4_WAY);
			return SHAPER.get(facing);
		}

		return Shapes.block();
	}

	@Override
	public boolean isPathfindable(BlockState blockState, BlockGetter level, BlockPos pos, PathComputationType pathType)
	{
		return false;
	}

	@Override
	protected BlockEntityType<InfusionStationBlockEntity> getBlockEntityType()
	{
		return IFElements.INFUSION_STATION_BLOCK_ENTITY.asBlockEntityType();
	}

	@Override
	protected MenuType<InfusionStationMenu> getContainerType()
	{
		return IFElements.INFUSION_STATION_MENU.asMenuType();
	}

	@Override
	public void appendHoverText(ItemStack stack, @Nullable BlockGetter level, List<Component> tooltip, TooltipFlag flag)
	{
		super.appendHoverText(stack, level, tooltip, flag);
		InfusedFoods.appendPotionEffectTooltips(stack, tooltip);
	}

	@Nullable
	@Override
	protected BlockEntityTicker<InfusionStationBlockEntity> getBlockEntityTicker(boolean clientSide)
	{
		return !clientSide ? InfusionStationBlockEntity::tick : null;
	}
}
