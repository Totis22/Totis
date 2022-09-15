package com.totis.totismod.server.blockentity.custom;

import com.totis.totismod.Constants;
import com.totis.totismod.server.blockentity.ModBlockEntities;
import com.totis.totismod.server.blockentity.menu.XpChestMenu;
import com.totis.totismod.server.network.ModMessages;
import com.totis.totismod.server.network.packets.PacketXPSync2C;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.common.world.ForgeChunkManager;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;

@SuppressWarnings("all")
public class XpChestBlockEntity extends BlockEntity implements MenuProvider {

    public int xpStored = 0;
    protected final ContainerData data;

	private final ItemStackHandler itemHandler = new ItemStackHandler(Constants.XPCHEST_TOTALSLOTS) {
		@Override
		protected void onContentsChanged(int slot) { setChanged(); };
	};

	private LazyOptional<IItemHandler> lazyItemHandler = LazyOptional.empty();

	public XpChestBlockEntity(BlockPos pos, BlockState state) {
		super(ModBlockEntities.XPSTORER_BLOCKENTITY.get(), pos, state);
        this.data = new ContainerData() {
            @Override
            public int get(int index) {
                return switch(index) {
                    case 0 -> XpChestBlockEntity.this.xpStored;
                    default -> throw new IllegalStateException("Unexpected value: " + index);
                };
            }

            @Override
            public void set(int index, int value) {
                switch(index) {
                    case 0 -> XpChestBlockEntity.this.xpStored = value;
                    default -> throw new IllegalStateException("Unexpected value: " + index);
                };
            }

            @Override
            public int getCount() {
                return 1;
            }
        };
	}

    public void setXpStored(int xp) {
        xpStored = xp;
    }
    public int getXpStored() {
        return xpStored;
    }

    @Override
	public AbstractContainerMenu createMenu(int id, Inventory inv, Player player) {
        ModMessages.sendToClients(new PacketXPSync2C(this.xpStored, worldPosition));
		return new XpChestMenu(id, inv, this, this.data);
	}

	@Override
	public Component getDisplayName() {
		return new TextComponent("");
	}
	
	@Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @javax.annotation.Nullable Direction side) {
        if (cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return lazyItemHandler.cast();
        }
        return super.getCapability(cap, side);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        lazyItemHandler = LazyOptional.of(() -> itemHandler);
        ModMessages.sendToClients(new PacketXPSync2C(xpStored, worldPosition));
    }

    @Override
    public void invalidateCaps()  {
        super.invalidateCaps();
        lazyItemHandler.invalidate();
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag) {
        tag.put("inventory", itemHandler.serializeNBT());
        tag.putInt("storedxp", xpStored);
        super.saveAdditional(tag);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        itemHandler.deserializeNBT(tag.getCompound("inventory"));
        xpStored = tag.getInt("storedxp");
    }

    public void drops() {
        SimpleContainer inventory = new SimpleContainer(itemHandler.getSlots());
        for (int i = 0; i < itemHandler.getSlots(); i++) {
            inventory.setItem(i, itemHandler.getStackInSlot(i));
        }

        Containers.dropContents(this.level, this.worldPosition, inventory);
    }
    
    //STARTING FROM 0 (if your block entity has 3 slots, the max number you can use is 2)
    public static void tick(Level level, BlockPos pos, BlockState state, XpChestBlockEntity blockEntity) {
        if(!level.isClientSide) {
            ChunkPos chunkPos = new ChunkPos(pos);
            ResourceKey<Level> currentDimension = level.dimension();
            ServerLevel serverLevel = (ServerLevel)level.getServer().getLevel(currentDimension);
            ForgeChunkManager.forceChunk(serverLevel, Constants.MOD_ID, pos, chunkPos.x,chunkPos.z,true,true);
        }
    }
    public ItemStack getItemFromSlot(int slot) { return this.itemHandler.getStackInSlot(slot); }
//
//    private static void craftItem(PiggyBankBlockEntity entity) {
//        entity.itemHandler.extractItem(0, 1, false);
//        entity.itemHandler.extractItem(1, 1, false);
//        entity.itemHandler.getStackInSlot(2).hurt(1, new Random(), null);
//
//        entity.itemHandler.setStackInSlot(3, new ItemStack(ModItems.CITRINE.get(),
//                entity.itemHandler.getStackInSlot(3).getCount() + 1));
//    }
//
//    private static boolean hasRecipe(PiggyBankBlockEntity entity) {
//        boolean hasItemInWaterSlot = PotionUtils.getPotion(entity.itemHandler.getStackInSlot(0)) == Potions.WATER;
//        boolean hasItemInFirstSlot = entity.itemHandler.getStackInSlot(1).getItem() == ModItems.RAW_CITRINE.get();
//        boolean hasItemInSecondSlot = entity.itemHandler.getStackInSlot(2).getItem() == ModItems.GEM_CUTTER_TOOL.get();
//
//        return hasItemInWaterSlot && hasItemInFirstSlot && hasItemInSecondSlot;
//    }

    /*Synchronization to the client*/
    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
    @Override
    public CompoundTag getUpdateTag() {
        return this.saveWithoutMetadata();
    }
    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
        super.onDataPacket(net, pkt);
        load(pkt.getTag());
    }
    @Override
    public void handleUpdateTag(CompoundTag tag) {
        super.handleUpdateTag(tag);
    }
}
