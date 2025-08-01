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

import btw.AddonHandler;
import me.lucko.spark.common.SparkPlatform;
import me.lucko.spark.common.SparkPlugin;
import me.lucko.spark.common.command.sender.CommandSender;
import me.lucko.spark.common.sampler.source.ClassSourceLookup;
import me.lucko.spark.common.sampler.source.SourceMetadata;
import me.lucko.spark.common.util.SparkThreadFactory;
import me.lucko.spark.btw.FabricClassSourceLookup;
import me.lucko.spark.btw.FabricSparkMod;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.metadata.Person;
import net.minecraft.server.MinecraftServer;
import net.minecraft.src.ICommand;
import net.minecraft.src.ICommandSender;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.logging.Level;
import java.util.stream.Collectors;

public abstract class FabricSparkPlugin implements SparkPlugin {

    private final FabricSparkMod mod;
    private final Logger logger;
    protected final ScheduledExecutorService scheduler;

    protected SparkPlatform platform;

    protected FabricSparkPlugin(FabricSparkMod mod) {
        this.mod = mod;
        this.logger = LogManager.getLogger("spark");
        this.scheduler = Executors.newScheduledThreadPool(4, new SparkThreadFactory());
    }

    public void enable() {
        this.platform = new SparkPlatform(this);
        this.platform.enable();
    }

    public void disable() {
        this.platform.disable();
        this.scheduler.shutdown();
    }

    @Override
    public String getVersion() {
        return this.mod.getVersion();
    }

    @Override
    public Path getPluginDirectory() {
        return this.mod.getConfigDirectory();
    }

    @Override
    public void executeAsync(Runnable task) {
        this.scheduler.execute(task);
    }

    @Override
    public void log(Level level, String msg) {
        if (level.intValue() >= 1000) { // severe
            this.logger.error(msg);
        } else if (level.intValue() >= 900) { // warning
            this.logger.warn(msg);
        } else {
            this.logger.info(msg);
        }
    }

    @Override
    public void log(Level level, String msg, Throwable throwable) {
        if (level.intValue() >= 1000) { // severe
            this.logger.error(msg, throwable);
        } else if (level.intValue() >= 900) { // warning
            this.logger.warn(msg, throwable);
        } else {
            this.logger.info(msg, throwable);
        }
    }

    @Override
    public ClassSourceLookup createClassSourceLookup() {
        return new FabricClassSourceLookup(createClassFinder());
    }

    @Override
    public Collection<SourceMetadata> getKnownSources() {
        return SourceMetadata.gather(
                FabricLoader.getInstance().getAllMods(),
                mod -> mod.getMetadata().getId(),
                mod -> mod.getMetadata().getVersion().getFriendlyString(),
                mod -> mod.getMetadata().getAuthors().stream()
                        .map(Person::getName)
                        .collect(Collectors.joining(", ")),
                mod -> mod.getMetadata().getDescription()
        );
    }

    protected List<String> generateSuggestions(CommandSender sender, String[] args) {
        return this.platform.tabCompleteCommand(sender, args);
    }

//    protected CompletableFuture<Suggestions> generateSuggestions(CommandSender sender, String[] args, SuggestionsBuilder builder) {
//        SuggestionsBuilder suggestions;
//
//        int lastSpaceIdx = builder.getRemaining().lastIndexOf(' ');
//        if (lastSpaceIdx != -1) {
//            suggestions = builder.createOffset(builder.getStart() + lastSpaceIdx + 1);
//        } else {
//            suggestions = builder;
//        }
//
//        return CompletableFuture.supplyAsync(() -> {
//            for (String suggestion : this.platform.tabCompleteCommand(sender, args)) {
//                suggestions.suggest(suggestion);
//            }
//            return suggestions.build();
//        });
//    }

    protected static void registerCommands(ICommand command, boolean clientOnly) {
        AddonHandler.registerCommand(command, clientOnly);
    }

//    protected static <T> void registerCommands(CommandDispatcher<T> dispatcher, Command<T> executor, SuggestionProvider<T> suggestor, String... aliases) {
//        if (aliases.length == 0) {
//            return;
//        }
//
//        String mainName = aliases[0];
//        LiteralArgumentBuilder<T> command = LiteralArgumentBuilder.<T>literal(mainName)
//                .executes(executor)
//                .then(RequiredArgumentBuilder.<T, String>argument("args", StringArgumentType.greedyString())
//                        .suggests(suggestor)
//                        .executes(executor)
//                );
//
//        LiteralCommandNode<T> node = dispatcher.register(command);
//        for (int i = 1; i < aliases.length; i++) {
//            dispatcher.register(LiteralArgumentBuilder.<T>literal(aliases[i]).redirect(node));
//        }
//    }

    protected static String[] processArgs(ICommandSender sender, boolean tabComplete, String[] args, String... aliases) {
        //todo think of a better way to filter?
        String newArgs = "";
        for (String arg : args) {
            newArgs = arg + " ";
        }
        String[] split = newArgs.split(" ", tabComplete ? -1 : 0);
        if (split.length == 0 || !Arrays.asList(aliases).contains(split[0])) {
            return null;
        }
        return Arrays.copyOfRange(split, 1, split.length);
    }

//    protected static String[] processArgs(CommandContext<?> context, boolean tabComplete, String... aliases) {
//        String[] split = context.getInput().split(" ", tabComplete ? -1 : 0);
//        if (split.length == 0 || !Arrays.asList(aliases).contains(split[0])) {
//            return null;
//        }
//
//        return Arrays.copyOfRange(split, 1, split.length);
//    }

}
