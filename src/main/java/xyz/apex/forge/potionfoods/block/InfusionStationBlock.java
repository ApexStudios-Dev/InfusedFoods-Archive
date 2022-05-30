package xyz.apex.forge.potionfoods.block;

import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.items.IItemHandler;

import xyz.apex.forge.apexcore.lib.block.ContainerBlock;
import xyz.apex.forge.apexcore.lib.util.ContainerHelper;
import xyz.apex.forge.potionfoods.PotionFoods;
import xyz.apex.forge.potionfoods.block.entity.InfusionStationBlockEntity;
import xyz.apex.forge.potionfoods.init.PFElements;
import xyz.apex.forge.potionfoods.network.PacketSyncInfusionData;

import javax.annotation.Nullable;

public final class InfusionStationBlock extends ContainerBlock<InfusionStationBlockEntity>
{
	public InfusionStationBlock(Properties properties)
	{
		super(properties);
	}

	@Override
	protected TileEntityType<InfusionStationBlockEntity> getBlockEntityType()
	{
		return PFElements.INFUSION_STATION_BLOCK_ENTITY.asBlockEntityType();
	}

	@Override
	public void setPlacedBy(World level, BlockPos pos, BlockState blockState, @Nullable LivingEntity placer, ItemStack stack)
	{
		super.setPlacedBy(level, pos, blockState, placer, stack);

		InfusionStationBlockEntity blockEntity = getBlockEntity(level, pos);

		if(blockEntity != null && stack.hasCustomHoverName())
		{
			ITextComponent customName = stack.getHoverName();
			blockEntity.setCustomName(customName);
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
			PotionFoods.NETWORK.sendTo(new PacketSyncInfusionData(pos, updateTag), PacketDistributor.ALL.noArg());
		}

		return true;
	}
}
