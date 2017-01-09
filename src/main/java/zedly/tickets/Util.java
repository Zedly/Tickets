package zedly.tickets;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

public class Util {

    // Returns the exact center of a block of a given location
    public static Location getCenter(Location loc) {
        return getCenter(loc, false);
    }

    // Returns the exact center of a block of a given location
    public static Location getCenter(Location loc, boolean centerVertical) {
        double x = loc.getX();
        double y = loc.getY();
        double z = loc.getZ();
        if (x >= 0) {
            x += .5;
        } else {
            x += .5;
        }
        if (centerVertical) {
            y = (int) y + .5;
        }
        if (z >= 0) {
            z += .5;
        } else {
            z += .5;
        }
        Location lo = loc.clone();
        lo.setX(x);
        lo.setY(y);
        lo.setZ(z);
        return lo;
    }

    // Returns the exact center of a block of a given block
    public static Location getCenter(Block blk) {
        return getCenter(blk.getLocation());
    }

    // Returns the exact center of a block of a given block
    public static Location getCenter(Block blk, boolean centerVertical) {
        return getCenter(blk.getLocation(), centerVertical);
    }

    public static Location getLiftDestination(Location originalPlayerLoc, Location destinationLiftLoc) {
        Block block = originalPlayerLoc.getWorld().getBlockAt(originalPlayerLoc.getBlockX(),
                destinationLiftLoc.getBlockY(), originalPlayerLoc.getBlockZ());
        for (int i = 0; i++ < 3 && block.getY() > 0; block = block.getRelative(BlockFace.DOWN)) {
            if (isBlockSafeToStand(block)) {
                return block.getLocation().add(.5, 1, .5).setDirection(originalPlayerLoc.getDirection());
            }
        }
        if(destinationLiftLoc.getBlockY() == 0) {
            return null;
        }
        block = originalPlayerLoc.getWorld().getBlockAt(destinationLiftLoc);
        for (int i = 0; i++ < 3 && block.getY() > 0; block = block.getRelative(BlockFace.DOWN)) {
            if (isBlockSafeToStand(block)) {
                return block.getLocation().add(.5, 1, .5).setDirection(originalPlayerLoc.getDirection());
            }
        }
        return null;
    }

    public static boolean isBlockSafeToStand(Block block) {
        if (block.getType().isSolid()) {
            for (int i = 0; i++ < 2 && block.getY() < 255;) {
                if ((block = block.getRelative(BlockFace.UP)).getType().isSolid()) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    // Displays a particle with the given data
    public static void display(Location loc, Particle particle, int amount, float speed, float xO, float yO, float zO) {
        loc.getWorld().spawnParticle(particle, loc.getX(), loc.getY(), loc.getZ(), amount, xO, yO, zO, speed);
    }
}
