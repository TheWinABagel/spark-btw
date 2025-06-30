package me.lucko.spark.fabricapi;

import me.lucko.spark.fabricapi.eventapi.api.event.Event;
import me.lucko.spark.fabricapi.eventapi.api.event.EventFactory;
import net.minecraft.server.MinecraftServer;

public class ServerEvents {
    public static final Event<ServerStarting> SERVER_STARTING = EventFactory.createArrayBacked(ServerStarting.class, (listeners) -> (server) -> {
        for (ServerStarting event : listeners) {
            event.onServerStarting(server);
        }
    });

    public static final Event<ServerStopping> SERVER_STOPPING = EventFactory.createArrayBacked(ServerStopping.class, (listeners) -> (server) -> {
        for (ServerStopping event : listeners) {
            event.onServerStopping(server);
        }
    });

    public static final Event<TickEvents.TickStart> START_SERVER_TICK = EventFactory.createArrayBacked(TickEvents.TickStart.class, (listeners) -> (server) -> {
        for (TickEvents.TickStart event : listeners) {
            event.onStartTick(server);
        }
    });

    public static final Event<TickEvents.TickEnd> END_SERVER_TICK = EventFactory.createArrayBacked(TickEvents.TickEnd.class, (listeners) -> (server) -> {
        for (TickEvents.TickEnd event : listeners) {
            event.onEndTick(server);
        }
    });
    
    public static class TickEvents {
        @FunctionalInterface
        public interface TickEnd {
            void onEndTick(MinecraftServer server);
        }
        @FunctionalInterface
        public interface TickStart {
            void onStartTick(MinecraftServer server);
        }
    }
    @FunctionalInterface
    public interface ServerStopping {
        void onServerStopping(MinecraftServer server);
    }

    @FunctionalInterface
    public interface ServerStarting {
        void onServerStarting(MinecraftServer server);
    }
}
