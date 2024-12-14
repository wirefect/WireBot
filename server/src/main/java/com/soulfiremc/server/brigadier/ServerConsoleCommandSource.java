/*
 * SoulFire
 * Copyright (C) 2024  AlexProgrammerDE
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.soulfiremc.server.brigadier;

import com.soulfiremc.brigadier.ClientConsoleCommandSource;
import com.soulfiremc.server.user.Permission;
import com.soulfiremc.server.user.ServerCommandSource;
import net.kyori.adventure.util.TriState;

import java.util.UUID;

public class ServerConsoleCommandSource extends ClientConsoleCommandSource implements ServerCommandSource {
  public static final ServerConsoleCommandSource INSTANCE = new ServerConsoleCommandSource();
  private static final UUID CONSOLE_UUID = UUID.fromString("00000000-0000-0000-0000-000000000000");
  private static final String CONSOLE_NAME = "CONSOLE";

  @Override
  public UUID getUniqueId() {
    return CONSOLE_UUID;
  }

  @Override
  public String getUsername() {
    return CONSOLE_NAME;
  }

  @Override
  public TriState getPermission(Permission.Context permission) {
    return TriState.TRUE;
  }
}
