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

package me.lucko.spark.btw.plugin;

import me.lucko.spark.common.monitor.ping.PlayerPingProvider;
import me.lucko.spark.common.platform.PlatformInfo;
import me.lucko.spark.common.platform.serverconfig.ServerConfigProvider;
import me.lucko.spark.common.platform.world.WorldInfoProvider;
import me.lucko.spark.common.sampler.ThreadDumper;
import me.lucko.spark.common.tick.TickHook;
import me.lucko.spark.common.tick.TickReporter;
import me.lucko.spark.btw.*;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.MinecraftServer;
import net.minecraft.src.EntityPlayerMP;
import net.minecraft.src.ICommand;
import net.minecraft.src.ICommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

public class FabricServerSparkPlugin extends FabricSparkPlugin implements ICommand {

    public static FabricServerSparkPlugin register(FabricSparkMod mod, MinecraftServer server) {
        FabricServerSparkPlugin plugin = new FabricServerSparkPlugin(mod, server);
        plugin.enable();
        return plugin;
    }

    private final MinecraftServer server;
    private final ThreadDumper gameThreadDumper;

    public FabricServerSparkPlugin(FabricSparkMod mod, MinecraftServer server) {
        super(mod);
        this.server = server;
        this.gameThreadDumper = new ThreadDumper.Specific(Thread.currentThread()/* server.getThread()*/);
    }

    @Override
    public void enable() {
        super.enable();

        // register commands
        registerCommands();
    }

    public void registerCommands() {
        registerCommands(this, false);
//        registerCommands(dispatcher, this, this, "spark");
    }

//    @Override
//    public int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
//
//        String[] args = processArgs(context, false, "/spark", "spark");
//        if (args == null) {
//            return 0;
//        }
//
//        this.platform.executeCommand(new FabricServerCommandSender(context.getSource()), args);
//        return Command.SINGLE_SUCCESS;
//    }
    @Override
    public void processCommand(ICommandSender sender, String[] strings) {
        String[] args = processArgs(sender, false, strings, "/spark", "spark");
        if (args == null) {
            return;
        }

        this.platform.executeCommand(new FabricServerCommandSender(sender), args);
    }

//    @Override
//    public CompletableFuture<Suggestions> getSuggestions(CommandContext<ServerCommandSource> context, SuggestionsBuilder builder) throws CommandSyntaxException {
//        String[] args = processArgs(context, true, "/spark", "spark");
//        if (args == null) {
//            return Suggestions.empty();
//        }
//
//        return generateSuggestions(new FabricServerCommandSender(context.getSource()), args, builder);
//    }


    @Override
    public List addTabCompletionOptions(ICommandSender iCommandSender, String[] strings) {
        String[] realArgs = processArgs(iCommandSender, true, strings, "/spark", "spark");
        if (realArgs == null) {
            return List.of();
        }

        return generateSuggestions(new FabricServerCommandSender(iCommandSender), realArgs);
    }

    @Override
    public Stream<FabricServerCommandSender> getCommandSenders() {
        return Stream.concat(
                this.server.getConfigurationManager().playerEntityList.stream(),
                Stream.of(this.server)
        ).map(o -> new FabricServerCommandSender((ICommandSender)o));
    }

    @Override
    public void executeSync(Runnable task) {
        task.run();
//        this.server.executeSync(task);
    }

    @Override
    public ThreadDumper getDefaultThreadDumper() {
        return this.gameThreadDumper;
    }

    @Override
    public TickHook createTickHook() {
        return new FabricTickHook.Server();
    }

    @Override
    public TickReporter createTickReporter() {
        return new FabricTickReporter.Server();
    }

    @Override
    public PlayerPingProvider createPlayerPingProvider() {
        return new FabricPlayerPingProvider(this.server);
    }

    @Override
    public ServerConfigProvider createServerConfigProvider() {
        return new FabricServerConfigProvider();
    }

    @Override
    public WorldInfoProvider createWorldInfoProvider() {
        return new FabricWorldInfoProvider.Server(this.server);
    }

    @Override
    public PlatformInfo getPlatformInfo() {
        return new FabricPlatformInfo(PlatformInfo.Type.SERVER);
    }

    @Override
    public String getCommandName() {
        return "spark";
    }

    @Override
    public List getCommandAliases() {
        return List.of();
    }

    @Override
    public String getCommandUsage(ICommandSender iCommandSender) {
        return "";
    }

    @Override
    public boolean canCommandSenderUseCommand(ICommandSender iCommandSender) {
        return iCommandSender.canCommandSenderUseCommand(4, "spark");
    }

    @Override
    public boolean isUsernameIndex(String[] strings, int i) {
        return false;
    }

    @Override
    public int compareTo(@NotNull Object o) {

        return 0;
    }
}
