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

import me.lucko.spark.btw.plugin.FabricClientSparkPlugin;
import me.lucko.spark.btw.plugin.FabricServerSparkPlugin;
import me.lucko.spark.fabricapi.ServerEvents;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.src.Minecraft;

import java.nio.file.Path;
import java.util.Objects;

public class FabricSparkMod implements ModInitializer {
    private static FabricSparkMod mod;

    private ModContainer container;
    private Path configDirectory;

    private FabricServerSparkPlugin activeServerPlugin = null;

    @Override
    public void onInitialize() {
        FabricSparkMod.mod = this;

        FabricLoader loader = FabricLoader.getInstance();
        this.container = loader.getModContainer("spark")
                .orElseThrow(() -> new IllegalStateException("Unable to get container for spark"));
        this.configDirectory = loader.getConfigDir().resolve("spark");

        // server event hooks
        ServerEvents.SERVER_STARTING.register(this::initializeServer);
        ServerEvents.SERVER_STOPPING.register(this::onServerStopping);

//        CommandRegistrationCallback.EVENT.register(this::onServerCommandRegister); todo check if this should be registered later?
    }

    // client (called by entrypoint defined in fabric.mod.json)
    public static void initializeClient() {
        Objects.requireNonNull(FabricSparkMod.mod, "mod");
        FabricClientSparkPlugin.register(FabricSparkMod.mod, Minecraft.getMinecraft());
    }

    // server
    public void initializeServer(MinecraftServer server) {
        this.activeServerPlugin = FabricServerSparkPlugin.register(this, server);

        if (this.activeServerPlugin != null) {
            this.activeServerPlugin.registerCommands();
        }
    }

    public void onServerStopping(MinecraftServer stoppingServer) {
        if (this.activeServerPlugin != null) {
            this.activeServerPlugin.disable();
            this.activeServerPlugin = null;
        }
    }

//    public void onServerCommandRegister(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess access, RegistrationEnvironment env) {
//        if (this.activeServerPlugin != null) {
//            this.activeServerPlugin.registerCommands();
//        }
//    }

    public String getVersion() {
        return this.container.getMetadata().getVersion().getFriendlyString();
    }

    public Path getConfigDirectory() {
        if (this.configDirectory == null) {
            throw new IllegalStateException("Config directory not set");
        }
        return this.configDirectory;
    }
}
