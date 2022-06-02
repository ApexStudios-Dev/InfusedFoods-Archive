package xyz.apex.forge.infusedfoods.network;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fmllegacy.network.NetworkEvent;

import xyz.apex.forge.apexcore.lib.net.AbstractPacket;
import xyz.apex.forge.apexcore.lib.net.NetworkManager;
import xyz.apex.forge.infusedfoods.container.InfusionStationMenu;
import xyz.apex.forge.infusedfoods.init.IFElements;

import javax.annotation.Nullable;

public final class PacketSyncInfusionData extends AbstractPacket
{
	private final BlockPos pos;
	@Nullable private final CompoundTag updateTag;

	public PacketSyncInfusionData(BlockPos pos, CompoundTag updateTag)
	{
		super();

		this.pos = pos;
		this.updateTag = updateTag;
	}

	public PacketSyncInfusionData(FriendlyByteBuf buffer)
	{
		super(buffer);

		pos = buffer.readBlockPos();
		updateTag = buffer.readNbt();
	}

	@Override
	protected void encode(FriendlyByteBuf buffer)
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
			var mc = Minecraft.getInstance();

			if(mc.level != null)
				IFElements.INFUSION_STATION_BLOCK_ENTITY.getBlockEntityOptional(mc.level, pos).ifPresent(blockEntity -> blockEntity.handleUpdateTag(updateTag));
			if(mc.player != null && mc.player.containerMenu instanceof InfusionStationMenu menu)
				menu.updateFromNetwork(updateTag);
		}
	}
}
