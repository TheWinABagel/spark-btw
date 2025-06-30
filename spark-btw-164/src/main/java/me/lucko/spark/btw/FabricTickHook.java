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

import me.lucko.spark.common.tick.AbstractTickHook;
import me.lucko.spark.common.tick.TickHook;
import me.lucko.spark.fabricapi.ClientEvents;
import me.lucko.spark.fabricapi.ServerEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.src.Minecraft;

public abstract class FabricTickHook extends AbstractTickHook implements TickHook {
    protected boolean closed = false;

    @Override
    protected void onTick() {
        if (!this.closed) {
            super.onTick();
        }
    }

    @Override
    public void close() {
        this.closed = true;
    }

    public static final class Server extends FabricTickHook implements ServerEvents.TickEvents.TickStart {
        @Override
        public void onStartTick(MinecraftServer minecraftServer) {
            onTick();
        }

        @Override
        public void start() {
            ServerEvents.START_SERVER_TICK.register(this);
        }
    }

    public static final class Client extends FabricTickHook implements ClientEvents.TickEvents.TickStart {
        @Override
        public void onStartTick(Minecraft minecraftClient) {
            onTick();
        }

        @Override
        public void start() {
            ClientEvents.START_CLIENT_TICK.register(this);
        }
    }
}
