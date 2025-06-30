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

import com.google.gson.JsonParseException;
import me.lucko.spark.common.command.sender.AbstractCommandSender;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.minecraft.src.ChatMessageComponent;
import net.minecraft.src.CommandBase;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.ICommandSender;

import java.util.UUID;

public class FabricClientCommandSender extends AbstractCommandSender<ICommandSender> {
    ICommandSender sender;
    public FabricClientCommandSender(ICommandSender commandSource) {
        super(commandSource);
    }

    @Override
    public String getName() {
        return this.delegate.getCommandSenderName();
    }

    @Override
    public UUID getUniqueId() {
        return ((EntityPlayer) this.delegate).getUniqueID();
    }

    @Override
    public void sendMessage(Component message) {
        var text = new ChatMessageComponent();
        text.addText(GsonComponentSerializer.gson().serializeToTree(message).getAsString()); //todo Client message?
//        Text component = TextCodecs.CODEC.decode(
//                DynamicRegistryManager.EMPTY.getOps(JsonOps.INSTANCE),
//                GsonComponentSerializer.gson().serializeToTree(message)
//        ).getOrThrow(JsonParseException::new).getFirst();
//        this.delegate.sendFeedback(component);
        this.delegate.sendChatToPlayer(text);
    }

    @Override
    public boolean hasPermission(String permission) {
        return true;
    }

    @Override
    protected Object getObjectForComparison() {
        return CommandBase.getCommandSenderAsPlayer(this.delegate);
    }
}
