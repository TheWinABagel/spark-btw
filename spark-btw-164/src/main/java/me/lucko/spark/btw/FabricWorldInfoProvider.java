/*
 * This file is part of spark.
 *
 *  Copyright (c) lucko (Luck) <luck@lucko.me>
 *  Copyright (c) contributors
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package me.lucko.spark.btw;

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import me.lucko.spark.btw.util.BTWSparkUtil;
import me.lucko.spark.common.platform.world.AbstractChunkInfo;
import me.lucko.spark.common.platform.world.CountMap;
import me.lucko.spark.common.platform.world.WorldInfoProvider;
import me.lucko.spark.btw.mixin.*;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.MinecraftServer;
import net.minecraft.src.*;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;

public abstract class FabricWorldInfoProvider implements WorldInfoProvider {

//    protected abstract ResourcePackManager getResourcePackManager();

    @Override
    public Collection<DataPackInfo> pollDataPacks() {
//        return getResourcePackManager().getEnabledProfiles().stream()
//                .map(pack -> new DataPackInfo(
//                        pack.getId(),
//                        pack.getDescription().getString(),
//                        resourcePackSource(pack.getSource())
//                ))
//                .collect(Collectors.toList());
        return List.of();
    }

//    private static String resourcePackSource(ResourcePackSource source) {
//        if (source == ResourcePackSource.NONE) {
//            return "none";
//        } else if (source == ResourcePackSource.BUILTIN) {
//            return "builtin";
//        } else if (source == ResourcePackSource.WORLD) {
//            return "world";
//        } else if (source == ResourcePackSource.SERVER) {
//            return "server";
//        } else {
//            return "unknown";
//        }
//    }

    public static final class Server extends FabricWorldInfoProvider {
        private final MinecraftServer server;

        public Server(MinecraftServer server) {
            this.server = server;
        }

        @Override
        public CountsResult pollCounts() {
            int players = this.server.getCurrentPlayerCount();
            int entities = 0;
            int chunks = 0;

            for (WorldServer world : this.server.worldServers) {

                if (FabricLoader.getInstance().isModLoaded("moonrise")) {
//                    entities += MoonriseMethods.getEntityCount(world((WorldAccessor) world).spark$getEntityLookup());
                } else {
                    entities += world.loadedEntityList.size();
//                    ServerEntityManager<Entity> entityManager = ((ServerWorldAccessor) world).getEntityManager();
//                    EntityIndex<?> entityIndex = ((ServerEntityManagerAccessor) entityManager).getIndex();
//                    entities += entityIndex.size();
                }

                chunks += world.getChunkProvider().getLoadedChunkCount();
            }

            return new CountsResult(players, entities, -1, chunks);
        }

        @Override
        public ChunksResult<FabricChunkInfo> pollChunks() {
            ChunksResult<FabricChunkInfo> data = new ChunksResult<>();

            for (int i = 0; i < this.server.worldServers.length; i++) {
                WorldServer world = this.server.worldServers[i];
                Long2ObjectOpenHashMap<FabricChunkInfo> worldInfos = new Long2ObjectOpenHashMap<>();

                for (Entity entity : (List<Entity>) world.loadedEntityList) {
                    FabricChunkInfo info = worldInfos.computeIfAbsent(
                            BTWSparkUtil.entityChunkPosAsLong(entity), FabricChunkInfo::new);
                    info.entityCounts.increment(EntityList.getClassFromID(entity.entityId)/*entity.getType()*/);
                }

                data.put(BTWSparkUtil.getWorldName(i), List.copyOf(worldInfos.values()));
//                data.put(world.getRegistryKey().getValue().getPath(), List.copyOf(worldInfos.values()));
            }

            return data;
        }

        @Override
        public GameRulesResult pollGameRules() {
            GameRulesResult data = new GameRulesResult();
            GameRules defaults = new GameRules();
            for (int i = 0; i < this.server.worldServers.length; i++) {
                WorldServer world = this.server.worldServers[i];

                String worldName = BTWSparkUtil.getWorldName(i);
                GameRules gameRules = world.getGameRules();
                for(String ruleName : world.getGameRules().getRules()) {
                    String defaultValue = defaults.getGameRuleStringValue(ruleName);
                    if (defaultValue.isEmpty()) {
                        defaultValue = "Unknown Default";
                    }
                    data.putDefault(ruleName, defaultValue);

                    data.put(ruleName, worldName, gameRules.getGameRuleStringValue(ruleName));
                }

//                world.getGameRules().accept(new GameRules.Visitor() {
//                    @Override
//                    public <T extends GameRules.Rule<T>> void visit(GameRules.Key<T> key, GameRules.Type<T> type) {
//                        String defaultValue = type.createRule().serialize();
//                        data.putDefault(key.getName(), defaultValue);
//
//                        String value = world.getGameRules().get(key).serialize();
//                        data.put(key.getName(), worldName, value);
//                    }
//                });
            }
            return data;
        }

//        @Override
//        protected ResourcePackManager getResourcePackManager() {
//            return this.server.getDataPackManager();
//        }
    }

    public static final class Client extends FabricWorldInfoProvider {
        private final Minecraft client;

        public Client(Minecraft client) {
            this.client = client;
        }

        @Override
        public CountsResult pollCounts() {
            WorldClient world = this.client.theWorld;
            if (world == null) {
                return null;
            }

            int entities = world.loadedEntityList.size();

            if (FabricLoader.getInstance().isModLoaded("moonrise")) {
//                entities = MoonriseMethods.getEntityCount(((WorldAccessor) world).spark$getEntityLookup());
            } else {
//                ClientEntityManager<Entity> entityManager = ((ClientWorldAccessor) world).getEntityManager();
//                EntityIndex<?> entityIndex = ((ClientEntityManagerAccessor) entityManager).getIndex();
//                entities = entityIndex.size();
            }

            int chunks = world.getChunkProvider().getLoadedChunkCount();

            return new CountsResult(-1, entities, -1, chunks);
        }

        @Override
        public ChunksResult<FabricChunkInfo> pollChunks() {
            WorldClient world = this.client.theWorld;
            if (world == null) {
                return null;
            }

            ChunksResult<FabricChunkInfo> data = new ChunksResult<>();

            Long2ObjectOpenHashMap<FabricChunkInfo> worldInfos = new Long2ObjectOpenHashMap<>();

            for (Entity entity : (List<Entity>) world.loadedEntityList) {
                FabricChunkInfo info = worldInfos.computeIfAbsent(
                        BTWSparkUtil.entityChunkPosAsLong(entity), FabricChunkInfo::new);
                info.entityCounts.increment(EntityList.getClassFromID(entity.entityId)/*entity.getType()*/);
            }
//            for (Entity entity : ((WorldAccessor) world).spark$getEntityLookup().iterate()) {
//                FabricChunkInfo info = worldInfos.computeIfAbsent(entity.getChunkPos().toLong(), FabricChunkInfo::new);
//                info.entityCounts.increment(entity.getType());
//            }

            data.put(/*world.getProviderName()*/BTWSparkUtil.getWorldName(0), List.copyOf(worldInfos.values()));

//            data.put(world.getRegistryKey().getValue().getPath(), List.copyOf(worldInfos.values()));

            return data;
        }

        @Override
        public GameRulesResult pollGameRules() {
            // Not available on client since 24w39a
            return null;
        }

//        @Override
//        protected ResourcePackManager getResourcePackManager() {
//            return this.client.getResourcePackManager();
//        }
    }

    static final class FabricChunkInfo extends AbstractChunkInfo<Class<? extends Entity>> {
        private final CountMap<Class<? extends Entity>> entityCounts;

        FabricChunkInfo(long chunkPos) {
            super(BTWSparkUtil.getPackedX(chunkPos), BTWSparkUtil.getPackedZ(chunkPos));

            this.entityCounts = new CountMap.Simple<>(new HashMap<>());
        }

        @Override
        public CountMap<Class<? extends Entity>> getEntityCounts() {
            return this.entityCounts;
        }

        @Override
        public String entityTypeName(Class<? extends Entity> type) {
            return EntityList.getStringFromID(EntityList.getEntityIDFromClass(type));
        }
    }

//    private static final class MoonriseMethods {
//        private static Method getEntityCount;
//
//        private static Method getEntityCountMethod(EntityLookup<Entity> getter) {
//            if (getEntityCount == null) {
//                try {
//                    getEntityCount = getter.getClass().getMethod("getEntityCount");
//                } catch (final ReflectiveOperationException e) {
//                    throw new RuntimeException("Cannot find Moonrise getEntityCount method", e);
//                }
//            }
//            return getEntityCount;
//        }
//
//        private static int getEntityCount(EntityLookup<Entity> getter) {
//            try {
//                return (int) getEntityCountMethod(getter).invoke(getter);
//            } catch (final ReflectiveOperationException e) {
//                throw new RuntimeException("Failed to invoke Moonrise getEntityCount method", e);
//            }
//        }
//    }

}

