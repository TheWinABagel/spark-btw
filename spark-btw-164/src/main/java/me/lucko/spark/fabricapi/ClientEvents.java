package me.lucko.spark.fabricapi;

import me.lucko.spark.fabricapi.eventapi.api.event.Event;
import me.lucko.spark.fabricapi.eventapi.api.event.EventFactory;
import net.minecraft.src.Minecraft;
//todo currently NOOP
public class ClientEvents {
    public static final Event<ClientStopping> CLIENT_STOPPING = EventFactory.createArrayBacked(ClientStopping.class, (listeners) -> (client) -> {
        for (ClientStopping event : listeners) {
            event.onClientStopping(client);
        }
    });

    public static final Event<TickEvents.TickStart> START_CLIENT_TICK = EventFactory.createArrayBacked(TickEvents.TickStart.class, (listeners) -> (client) -> {
        for (TickEvents.TickStart event : listeners) {
            event.onStartTick(client);
        }
    });

    public static final Event<TickEvents.TickEnd> END_CLIENT_TICK = EventFactory.createArrayBacked(TickEvents.TickEnd.class, (listeners) -> (client) -> {
        for (TickEvents.TickEnd event : listeners) {
            event.onEndTick(client);
        }
    });

    public static class TickEvents {
        @FunctionalInterface
        public interface TickEnd {
            void onEndTick(Minecraft minecraft);
        }
        @FunctionalInterface
        public interface TickStart {
            void onStartTick(Minecraft minecraft);
        }
    }

    @FunctionalInterface
    public interface ClientStopping {
        void onClientStopping(Minecraft minecraft);
    }
}
