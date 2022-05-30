package xyz.apex.forge.infusedfoods.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalBlock;
import net.minecraft.block.IWaterLoggable;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.Attribute;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.pathfinding.PathType;
import net.minecraft.potion.Effect;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tags.FluidTags;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.text.*;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.items.IItemHandler;

import xyz.apex.forge.apexcore.lib.block.ContainerBlock;
import xyz.apex.forge.apexcore.lib.block.VoxelShaper;
import xyz.apex.forge.apexcore.lib.util.ContainerHelper;
import xyz.apex.forge.infusedfoods.InfusedFoods;
import xyz.apex.forge.infusedfoods.block.entity.InfusionStationBlockEntity;
import xyz.apex.forge.infusedfoods.block.entity.InfusionStationInventory;
import xyz.apex.forge.infusedfoods.init.IFElements;
import xyz.apex.forge.infusedfoods.network.PacketSyncInfusionData;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.Random;

public final class InfusionStationBlock extends ContainerBlock<InfusionStationBlockEntity> implements IWaterLoggable
{
	public static final VoxelShape SHAPE = VoxelShaper.or(
			box(1D, 0D, 5D, 13D, 1D, 11D),
			box(13D, 0D, 7D, 15D, 14D, 9D)
	);

	public static final VoxelShaper SHAPER = VoxelShaper.forHorizontal(SHAPE, Direction.NORTH);

	public static final DirectionProperty FACING = HorizontalBlock.FACING;
	public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

	public InfusionStationBlock(Properties properties)
	{
		super(properties);

		registerDefaultState(defaultBlockState().setValue(WATERLOGGED, false).setValue(FACING, Direction.NORTH));
	}

	@Override
	public void animateTick(BlockState blockState, World level, BlockPos pos, Random rng)
	{
		if(!blockState.getValue(WATERLOGGED))
		{
			Direction facing = blockState.getValue(FACING).getClockWise();

			double x = (double) pos.getX() + .4D + (double) rng.nextFloat() * .2D + (facing.getStepX() * .4D);
			double y = (double) pos.getY() + .7D + (double) rng.nextFloat() * .3D;
			double z = (double) pos.getZ() + .4D + (double) rng.nextFloat() * .2D + (facing.getStepZ() * .4D);

			level.addParticle(ParticleTypes.SMOKE, x, y, z, 0D, 0D, 0D);
		}
	}

	@Override
	public VoxelShape getShape(BlockState blockState, IBlockReader level, BlockPos pos, ISelectionContext ctx)
	{
		Direction facing = blockState.getValue(FACING);
		return SHAPER.get(facing);
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
	public FluidState getFluidState(BlockState blockState)
	{
		return blockState.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(blockState);
	}

	@Override
	public BlockState updateShape(BlockState blockState, Direction facing, BlockState facingBlockState, IWorld level, BlockPos pos, BlockPos facingPos)
	{
		if(blockState.getValue(WATERLOGGED))
			level.getLiquidTicks().scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));

		return super.updateShape(blockState, facing, facingBlockState, level, pos, facingPos);
	}

	@Override
	public BlockState getStateForPlacement(BlockItemUseContext ctx)
	{
		BlockState stateForPlacement = super.getStateForPlacement(ctx);

		if(stateForPlacement != null)
		{
			FluidState fluidState = ctx.getLevel().getFluidState(ctx.getClickedPos());
			boolean waterLogged = fluidState.is(FluidTags.WATER);
			return stateForPlacement.setValue(WATERLOGGED, waterLogged).setValue(FACING, ctx.getHorizontalDirection().getOpposite());
		}

		return null;
	}

	@Override
	protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder)
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
	protected TileEntityType<InfusionStationBlockEntity> getBlockEntityType()
	{
		return IFElements.INFUSION_STATION_BLOCK_ENTITY.asBlockEntityType();
	}

	@Override
	public void setPlacedBy(World level, BlockPos pos, BlockState blockState, @Nullable LivingEntity placer, ItemStack stack)
	{
		super.setPlacedBy(level, pos, blockState, placer, stack);

		InfusionStationBlockEntity blockEntity = getBlockEntity(level, pos);

		if(blockEntity != null)
		{
			CompoundNBT stackTag = stack.getTag();

			if(stackTag != null)
				blockEntity.loadFromItemStack(stackTag);

			if(stack.hasCustomHoverName())
			{
				ITextComponent customName = stack.getHoverName();
				blockEntity.setCustomName(customName);
			}
		}
	}

	@Override
	public void onRemove(BlockState blockState, World level, BlockPos pos, BlockState newBlockState, boolean isMoving)
	{
		InfusionStationBlockEntity blockEntity = getBlockEntity(level, pos);

		if(blockEntity != null)
		{
			IItemHandler itemHandler = blockEntity.getItemHandler();

			for(int i = 0; i < itemHandler.getSlots(); i++)
			{
				ItemStack stack = itemHandler.getStackInSlot(i);
				InventoryHelper.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), stack);
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
	public int getAnalogOutputSignal(BlockState blockState, World level, BlockPos pos)
	{
		return ContainerHelper.getRedstoneSignalFromContainer(level, pos);
	}

	@Override
	protected boolean openContainerScreen(BlockState blockState, World level, BlockPos pos, ServerPlayerEntity player, Hand hand, ItemStack stack, ITextComponent titleComponent)
	{
		super.openContainerScreen(blockState, level, pos, player, hand, stack, titleComponent);

		InfusionStationBlockEntity blockEntity = getBlockEntity(level, pos);

		if(blockEntity != null)
		{
			CompoundNBT updateTag = blockEntity.getUpdateTag();
			InfusedFoods.NETWORK.sendTo(new PacketSyncInfusionData(pos, updateTag), PacketDistributor.ALL.noArg());
		}

		return true;
	}

	@Override
	public void appendHoverText(ItemStack stack, @Nullable IBlockReader level, List<ITextComponent> tooltip, ITooltipFlag flag)
	{
		super.appendHoverText(stack, level, tooltip, flag);

		CompoundNBT stackTag = stack.getTag();

		if(stackTag != null && stackTag.contains(InfusionStationBlockEntity.NBT_INVENTORY, Constants.NBT.TAG_COMPOUND))
		{
			CompoundNBT inventoryTag = stackTag.getCompound(InfusionStationBlockEntity.NBT_INVENTORY);

			if(inventoryTag.contains(InfusionStationInventory.NBT_INFUSION_FLUID, Constants.NBT.TAG_COMPOUND))
			{
				CompoundNBT fluidTag = inventoryTag.getCompound(InfusionStationInventory.NBT_INFUSION_FLUID);
				InfusionStationInventory.InfusionFluid fluid = new InfusionStationInventory.InfusionFluid(fluidTag);

				Effect effect = fluid.getEffect();

				if(effect != null)
				{
					IFormattableTextComponent potionName = new TranslationTextComponent(effect.getDescriptionId());

					int amplifier = fluid.getAmplifier();
					int duration = fluid.getDuration();

					if(amplifier > 0)
						potionName = new TranslationTextComponent("potion.withAmplifier", potionName, new TranslationTextComponent("potion.potency." + amplifier));
					if(duration > 20)
					{
						int i = MathHelper.floor(duration * 1F);
						String durationFormat = StringUtils.formatTickDuration(i);
						potionName = new TranslationTextComponent("potion.withDuration", potionName, durationFormat);
					}

					tooltip.add(potionName.withStyle(effect.getCategory().getTooltipFormatting()));

					Map<Attribute, AttributeModifier> attributeModifiers = effect.getAttributeModifiers();

					if(!attributeModifiers.isEmpty())
					{
						tooltip.add(StringTextComponent.EMPTY);
						tooltip.add(new TranslationTextComponent("potion.whenDrank").withStyle(TextFormatting.DARK_PURPLE));

						attributeModifiers.forEach((attribute, attributeModifier) -> {
							AttributeModifier mod = attributeModifier;
							AttributeModifier mod1 = new AttributeModifier(mod.getName(), effect.getAttributeModifierValue(amplifier, mod), mod.getOperation());

							double d0 = mod1.getAmount();
							double d1;

							AttributeModifier.Operation operation = mod1.getOperation();

							if(operation != AttributeModifier.Operation.MULTIPLY_BASE && operation != AttributeModifier.Operation.MULTIPLY_TOTAL)
								d1 = d0;
							else
								d1 = d0 * 100D;

							if(d0 > 0D)
								tooltip.add(new TranslationTextComponent("attribute.modifier.plus." + operation.toValue(), ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(d1), new TranslationTextComponent(attribute.getDescriptionId())).withStyle(TextFormatting.BLUE));
							else
								tooltip.add(new TranslationTextComponent("attribute.modifier.take." + operation.toValue(), ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(d1), new TranslationTextComponent(attribute.getDescriptionId())).withStyle(TextFormatting.BLUE));
						});
					}
				}
			}
		}
	}
}
