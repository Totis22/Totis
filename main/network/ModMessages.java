package com.totis.totismod.server.network;

import com.totis.totismod.Constants;
import com.totis.totismod.server.network.packets.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

public class ModMessages {

    public static final String PROTOCOL_VERSION = "1";
    public static SimpleChannel INSTANCE;

    private static int packetID = 0;
    private static int id() {
        return packetID++;
    }

    public static void register() {
        SimpleChannel net = NetworkRegistry.ChannelBuilder
                .named(new ResourceLocation(Constants.MOD_ID, "messages"))
                .networkProtocolVersion(() -> "1.0")
                .clientAcceptedVersions(s -> true)
                .serverAcceptedVersions(s -> true)
                .simpleChannel();

        INSTANCE = net;

        INSTANCE.messageBuilder(PacketSyncPriceToClient.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(PacketSyncPriceToClient::new)
                .encoder(PacketSyncPriceToClient::toBytes)
                .consumer(PacketSyncPriceToClient::handle)
                .add();
        INSTANCE.messageBuilder(PacketSyncTeleporterPosToClient.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(PacketSyncTeleporterPosToClient::new)
                .encoder(PacketSyncTeleporterPosToClient::toBytes)
                .consumer(PacketSyncTeleporterPosToClient::handle)
                .add();
        INSTANCE.messageBuilder(PacketDisplayTotem.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(PacketDisplayTotem::new)
                .encoder(PacketDisplayTotem::write)
                .consumer(PacketDisplayTotem::handle)
                .add();
        INSTANCE.messageBuilder(PacketAddGhost.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(PacketAddGhost::read)
                .encoder(PacketAddGhost::write)
                .consumer(PacketAddGhost::handle)
                .add();
        INSTANCE.messageBuilder(PacketEnergySync.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(PacketEnergySync::new)
                .encoder(PacketEnergySync::write)
                .consumer(PacketEnergySync::handle)
                .add();
        INSTANCE.messageBuilder(PacketXPSync2C.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(PacketXPSync2C::new)
                .encoder(PacketXPSync2C::write)
                .consumer(PacketXPSync2C::handle)
                .add();
        INSTANCE.messageBuilder(PacketXPSync2S.class, id(), NetworkDirection.PLAY_TO_SERVER)
                .decoder(PacketXPSync2S::new)
                .encoder(PacketXPSync2S::write)
                .consumer(PacketXPSync2S::handle)
                .add();
        INSTANCE.messageBuilder(PacketGivePlayerXpSync2S.class, id(), NetworkDirection.PLAY_TO_SERVER)
                .decoder(PacketGivePlayerXpSync2S::new)
                .encoder(PacketGivePlayerXpSync2S::write)
                .consumer(PacketGivePlayerXpSync2S::handle)
                .add();
    }

    public static <MSG> void sendToAllTrackingAndSelf(MSG message, Entity entity) {
        INSTANCE.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> entity), message);
    }
    // Send to the server
    public static <MSG> void sendToServer(MSG message) {
        INSTANCE.sendToServer(message);
    }
    // Send to one player
    public static <MSG> void sendToClient(MSG message, ServerPlayer player) {
        INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), message);
    }
    // Send to all connected players
    public static <MSG> void sendToClients(MSG message) {
        INSTANCE.send(PacketDistributor.ALL.noArg(), message);
    }
}
