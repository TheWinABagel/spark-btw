package me.lucko.spark.btw.util;

import net.minecraft.src.Entity;

public class BTWSparkUtil {
    public static long entityChunkPosAsLong(Entity entity) {
        return entity.chunkCoordX & 4294967295L | (entity.chunkCoordZ & 4294967295L) << 32;
    }

    public static int getPackedX(long pos) {
        return (int)(pos & 4294967295L);
    }

    public static int getPackedZ(long pos) {
        return (int)(pos >>> 32 & 4294967295L);
    }

    //todo change to compare from world's WorldProvider?
    public static String getWorldName(int worldId) {
        return switch (worldId) {
            case 0 -> "Overworld";
            case 1 -> "Nether";
            case 2 -> "End";
            default -> "Unknown";
        };
    }
}
