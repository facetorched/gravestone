package de.maxhenkel.gravestone;

import de.maxhenkel.gravestone.tileentity.GraveStoneTileEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;

import javax.annotation.Nullable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

public class GraveUtils {

    public static final UUID EMPTY_UUID = new UUID(0L, 0L);

    @Nullable
    public static BlockPos getGraveStoneLocation(Level world, BlockPos pos) {
        BlockPos.MutableBlockPos location = new BlockPos.MutableBlockPos(pos.getX(), pos.getY(), pos.getZ());

        if (world.isOutsideBuildHeight(location) && location.getY() <= 0) {
            location.set(location.getX(), 1, location.getZ());
        }

        while (!world.isOutsideBuildHeight(location)) {
            if (isReplaceable(world, location)) {
                return location;
            }

            location.move(0, 1, 0);
        }

        return null;
    }

    public static boolean isReplaceable(Level world, BlockPos pos) {
        Block b = world.getBlockState(pos).getBlock();

        if (world.isEmptyBlock(pos)) {
            return true;
        }

        return Main.SERVER_CONFIG.replaceableBlocks.stream().anyMatch(blockTag -> blockTag.contains(b));
    }

    @Nullable
    public static MutableComponent getDate(long timestamp) {
        if (timestamp <= 0L) {
            return null;
        }
        SimpleDateFormat dateFormat = new SimpleDateFormat(new TranslatableComponent("gui.gravestone.date_format").getString());
        return new TextComponent(dateFormat.format(new Date(timestamp)));
    }

    public static boolean canBreakGrave(Level world, Player player, BlockPos pos) {
        if (!Main.SERVER_CONFIG.onlyOwnersCanBreak.get()) {
            return true;
        }

        BlockEntity te = world.getBlockEntity(pos);

        if (!(te instanceof GraveStoneTileEntity)) {
            return true;
        }

        GraveStoneTileEntity grave = (GraveStoneTileEntity) te;

        if (player instanceof ServerPlayer) {
            ServerPlayer p = (ServerPlayer) player;
            if (p.hasPermissions(p.server.getOperatorUserPermissionLevel())) {
                return true;
            }
        }
        UUID uuid = grave.getDeath().getPlayerUUID();
        if (uuid.equals(GraveUtils.EMPTY_UUID)) {
            return true;
        }

        return player.getUUID().equals(uuid);
    }

}
