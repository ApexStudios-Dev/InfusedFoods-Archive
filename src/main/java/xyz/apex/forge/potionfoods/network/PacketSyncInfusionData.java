package xyz.apex.forge.potionfoods.network;

import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.network.NetworkEvent;

import xyz.apex.forge.apexcore.lib.net.AbstractPacket;
import xyz.apex.forge.apexcore.lib.net.NetworkManager;
import xyz.apex.forge.potionfoods.container.InfusionStationContainer;
import xyz.apex.forge.potionfoods.init.PFElements;

import javax.annotation.Nullable;

public final class PacketSyncInfusionData extends AbstractPacket
{
	private final BlockPos pos;
	@Nullable private final CompoundNBT updateTag;

	public PacketSyncInfusionData(BlockPos pos, CompoundNBT updateTag)
	{
		super();

		this.pos = pos;
		this.updateTag = updateTag;
	}

	public PacketSyncInfusionData(PacketBuffer buffer)
	{
		super(buffer);

		pos = buffer.readBlockPos();
		updateTag = buffer.readNbt();
	}

	@Override
	protected void encode(PacketBuffer buffer)
	{
		buffer.writeBlockPos(pos);
		buffer.writeNbt(updateTag);
	}

	@Override
	protected void process(NetworkManager network, NetworkEvent.Context ctx)
	{
		DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> this::handleClient);
	}

	@OnlyIn(Dist.CLIENT)
	private void handleClient()
	{
		if(updateTag != null)
		{
			Minecraft mc = Minecraft.getInstance();

			if(mc.level != null)
			{
				PFElements.INFUSION_STATION_BLOCK_ENTITY.getBlockEntityOptional(mc.level, pos).ifPresent(blockEntity -> {
					BlockState blockState = mc.level.getBlockState(pos);
					blockEntity.handleUpdateTag(blockState, updateTag);
				});
			}

			if(mc.player != null && mc.player.containerMenu instanceof InfusionStationContainer)
				((InfusionStationContainer) mc.player.containerMenu).updateFromNetwork(updateTag);
		}
	}
}
