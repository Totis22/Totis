package com.totis.totismod.server.network.packets;

import com.totis.totismod.utils.TotisPlayerUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

@SuppressWarnings("all")
public class PacketGivePlayerXpSync2S {

    public int xp;

    public PacketGivePlayerXpSync2S(int xp) {
        this.xp = xp;
    }

    // Read
    public PacketGivePlayerXpSync2S(FriendlyByteBuf buf) {
        xp = buf.readInt();
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeInt(xp);
    }

    public boolean handle(Supplier<NetworkEvent.Context> ctx) {
        NetworkEvent.Context context = ctx.get();
        context.enqueueWork(() -> {
            // This is the client

            // Client Player
            Player clientPlayer = Minecraft.getInstance().player;
            // Server Player
            ServerPlayer serverPlayer = context.getSender();
            TotisPlayerUtils.addPlayerXP(serverPlayer, xp);
        });
        return true;
    }
}
