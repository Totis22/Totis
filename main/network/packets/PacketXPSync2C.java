package com.totis.totismod.server.network.packets;

import com.totis.totismod.server.blockentity.custom.TeleporterBlockEntity;
import com.totis.totismod.server.blockentity.custom.XpChestBlockEntity;
import com.totis.totismod.server.blockentity.menu.TeleporterMenu;
import com.totis.totismod.server.blockentity.menu.XpChestMenu;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

@SuppressWarnings("all")
public class PacketXPSync2C {

    private final int xp;
    private final BlockPos pos;

    public PacketXPSync2C(int xp, BlockPos pos) {
        this.xp = xp;
        this.pos = pos;
    }

    // Read
    public PacketXPSync2C(FriendlyByteBuf buf) {
        xp = buf.readInt();
        pos = buf.readBlockPos();
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeInt(this.xp);
        buf.writeBlockPos(this.pos);
    }

    public boolean handle(Supplier<NetworkEvent.Context> ctx) {
        NetworkEvent.Context context = ctx.get();
        context.enqueueWork(() -> {
            // This is the client
            if(Minecraft.getInstance().level.getBlockEntity(pos) instanceof XpChestBlockEntity blockEntity) {
                blockEntity.setXpStored(xp);
                blockEntity.setChanged();

                if(Minecraft.getInstance().player.containerMenu instanceof XpChestMenu menu &&
                        menu.getBlockEntity().getBlockPos().equals(pos)) {
                    blockEntity.setXpStored(xp);
                    blockEntity.setChanged();
                }
            }
        });
        return true;
    }
}
