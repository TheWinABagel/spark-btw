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

import me.lucko.spark.common.platform.PlatformInfo;
import me.lucko.spark.common.platform.world.WorldInfoProvider;
import me.lucko.spark.common.sampler.ThreadDumper;
import me.lucko.spark.common.tick.TickHook;
import me.lucko.spark.common.tick.TickReporter;
import me.lucko.spark.btw.*;
import me.lucko.spark.fabricapi.ClientEvents;
import net.minecraft.src.*;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Stream;

public class FabricClientSparkPlugin extends FabricSparkPlugin implements ICommand /*Command<FabricClientCommandSource>, SuggestionProvider<FabricClientCommandSource>*/ {

    public static void register(FabricSparkMod mod, Minecraft client) {
        FabricClientSparkPlugin plugin = new FabricClientSparkPlugin(mod, client);
        plugin.enable();
    }

    private final Minecraft minecraft;
    private final ThreadDumper.GameThread gameThreadDumper;

    public FabricClientSparkPlugin(FabricSparkMod mod, Minecraft minecraft) {
        super(mod);
        this.minecraft = minecraft;
        this.gameThreadDumper = new ThreadDumper.GameThread(() -> Thread.currentThread() /*((MinecraftClientAccessor) minecraft).getThread()*/);
    }

    @Override
    public void enable() {
        super.enable();

        // events
        ClientEvents.CLIENT_STOPPING.register(this::onDisable);
//        ClientCommandRegistrationCallback.EVENT.register(this::onCommandRegister);

        registerCommands(this, true);
    }

    private void onDisable(Minecraft stoppingClient) {
        if (stoppingClient == this.minecraft) {
            disable();
        }
    }

//    public void onCommandRegister(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandRegistryAccess registryAccess) {
//        registerCommands(this, true);
////        registerCommands(dispatcher, this, this, "sparkc", "sparkclient");
//    }

    @Override
    public String getCommandUsage(ICommandSender iCommandSender) {
        return "";
    }

    @Override
    public List getCommandAliases() {
        return List.of("sparkclient");
    }

    @Override
    public void processCommand(ICommandSender senderContext, String[] args) {
        String[] realArgs = processArgs(senderContext, false, args, "sparkc", "sparkclient");
        if (realArgs == null) {
            return;
        }

        this.platform.executeCommand(new FabricClientCommandSender(senderContext), realArgs);
    }

    @Override
    public boolean canCommandSenderUseCommand(ICommandSender iCommandSender) {
        return true;
    }

    @Override
    public boolean isUsernameIndex(String[] strings, int i) {
        return false;
    }

    @Override
    public int compareTo(@NotNull Object o) {
        return 0;
    }

//    @Override
//    public int run(CommandContext<FabricClientCommandSource> context) throws CommandException {
//        String[] args = processArgs(context, false, "sparkc", "sparkclient");
//        if (args == null) {
//            return 0;
//        }
//
//        this.platform.executeCommand(new FabricClientCommandSender(context.getSource()), args);
//        return Command.SINGLE_SUCCESS;
//    }

    @Override
    public List<String> addTabCompletionOptions(ICommandSender iCommandSender, String[] args) {
        String[] realArgs = processArgs(iCommandSender, true, args, "/sparkc", "/sparkclient");
        if (realArgs == null) {
            return List.of();
        }

//        this.platform.executeCommand(new FabricClientCommandSender(iCommandSender), realArgs);

        return generateSuggestions(new FabricClientCommandSender(iCommandSender), args);
    }

//    @Override
//    public CompletableFuture<Suggestions> getSuggestions(CommandContext<FabricClientCommandSource> context, SuggestionsBuilder builder) throws CommandSyntaxException {
//        String[] args = processArgs(context, true, "/sparkc", "/sparkclient");
//        if (args == null) {
//            return Suggestions.empty();
//        }
//
//        return generateSuggestions(new FabricClientCommandSender(context.getSource()), args, builder);
//    }

    @Override
    public Stream<FabricClientCommandSender> getCommandSenders() {
        EntityClientPlayerMP player = this.minecraft.thePlayer;
        if (player == null) {
            return Stream.empty();
        }
        return Stream.of(new FabricClientCommandSender(player));
    }

    @Override
    public void executeSync(Runnable task) {
        task.run();
//        this.minecraft.executeSync(task);
    }

    @Override
    public ThreadDumper getDefaultThreadDumper() {
        return this.gameThreadDumper.get();
    }

    @Override
    public TickHook createTickHook() {
        return new FabricTickHook.Client();
    }

    @Override
    public TickReporter createTickReporter() {
        return new FabricTickReporter.Client();
    }

    @Override
    public WorldInfoProvider createWorldInfoProvider() {
        return new FabricWorldInfoProvider.Client(this.minecraft);
    }

    @Override
    public PlatformInfo getPlatformInfo() {
        return new FabricPlatformInfo(PlatformInfo.Type.CLIENT);
    }

    @Override
    public String getCommandName() {
        return "sparkc";
    }


}
