/*
 * ServerWrecker
 *
 * Copyright (C) 2023 ServerWrecker
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 */
package net.pistonmaster.serverwrecker.protocol;

import com.github.steveice10.mc.protocol.codec.MinecraftPacket;
import com.github.steveice10.packetlib.Session;
import com.github.steveice10.packetlib.event.session.DisconnectedEvent;
import com.github.steveice10.packetlib.event.session.PacketSendingEvent;
import com.github.steveice10.packetlib.event.session.SessionAdapter;
import com.github.steveice10.packetlib.packet.Packet;
import lombok.RequiredArgsConstructor;
import net.pistonmaster.serverwrecker.api.ServerWreckerAPI;
import net.pistonmaster.serverwrecker.api.event.bot.BotDisconnectedEvent;
import net.pistonmaster.serverwrecker.api.event.bot.SWPacketReceiveEvent;
import net.pistonmaster.serverwrecker.api.event.bot.SWPacketSendingEvent;
import net.pistonmaster.serverwrecker.protocol.bot.SessionDataManager;
import net.pistonmaster.serverwrecker.util.BusHelper;

@RequiredArgsConstructor
public class SWSessionListener extends SessionAdapter {
    private final SessionDataManager bus;
    private final BotConnection botConnection;

    @Override
    public void packetReceived(Session session, Packet packet) {
        SWPacketReceiveEvent event1 = new SWPacketReceiveEvent(botConnection, (MinecraftPacket) packet);
        ServerWreckerAPI.postEvent(event1);
        if (event1.cancelled()) {
            return;
        }

        botConnection.logger().trace("Received packet: " + packet.toString());

        BusHelper.handlePacket(event1.getPacket(), bus);
    }

    @Override
    public void packetSending(PacketSendingEvent event) {
        SWPacketSendingEvent event1 = new SWPacketSendingEvent(botConnection, event.getPacket());
        ServerWreckerAPI.postEvent(event1);
        event.setPacket(event1.getPacket());
        event.setCancelled(event1.cancelled());

        if (event1.cancelled()) {
            return;
        }

        botConnection.logger().debug("Sending packet: " + event.getPacket().toString());
    }

    @Override
    public void disconnected(DisconnectedEvent event) {
        try {
            bus.onDisconnectEvent(event);
        } catch (Throwable t) {
            t.printStackTrace();
        }

        ServerWreckerAPI.postEvent(new BotDisconnectedEvent(botConnection));
    }
}
