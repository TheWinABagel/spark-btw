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


import me.lucko.spark.common.command.sender.AbstractCommandSender;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.src.ChatMessageComponent;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.ICommandSender;
import net.minecraft.src.TileEntityCommandBlock;

import java.util.UUID;

public class FabricServerCommandSender extends AbstractCommandSender<ICommandSender> {
    public FabricServerCommandSender(ICommandSender commandSource) {
        super(commandSource);
    }

    @Override
    public String getName() {
        String name = this.delegate.getCommandSenderName();
        /*this.delegate.getEntity() != null*/
        if (!(this.delegate instanceof TileEntityCommandBlock) && (name.equals("Rcon") || name.equals("Server"))) {
            return "Console";
        }
        return name;
    }

    @Override
    public UUID getUniqueId() {
        if (this.delegate instanceof EntityPlayer ep) {
            return ep.getUniqueID();
        }
        return null;
//        Entity entity = this.delegate.getEntity();
//        return entity != null ? entity.getUuid() : null;
    }

    @Override
    public void sendMessage(Component message) {
        var text = new ChatMessageComponent();
        text.addText(GsonComponentSerializer.gson().serializeToTree(message).getAsString()); //todo Server message?
//        Text component = TextCodecs.CODEC.decode(
//                DynamicRegistryManager.EMPTY.getOps(JsonOps.INSTANCE),
//                GsonComponentSerializer.gson().serializeToTree(message)
//        ).getOrThrow(JsonParseException::new).getFirst();
//        this.delegate.sendFeedback(component);
        this.delegate.sendChatToPlayer(text);
    }

    @Override
    public boolean hasPermission(String permission) {
        if (MinecraftServer.getIsServer() && !MinecraftServer.getServer().isDedicatedServer()) {
            return true;
        }
        return this.delegate.canCommandSenderUseCommand(4, "spark");
//        return Permissions.getPermissionValue(this.delegate, permission).orElseGet(() -> {
//            ServerPlayerEntity player = this.delegate.getPlayer();
//            MinecraftServer server = this.delegate.getServer();
//            if (player != null) {
//                if (server != null && server.isHost(player.getGameProfile())) {
//                    return true;
//                }
//                return player.hasPermissionLevel(4);
//            }
//            return true;
//        });
    }

    @Override
    protected Object getObjectForComparison() {
        UUID uniqueId = getUniqueId();
        if (uniqueId != null) {
            return uniqueId;
        }
//        Entity entity = this.delegate.getEntity();
//        if (entity != null) {
//            return entity;
//        }
        return getName();
    }
}
