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
package com.soulfiremc.server.grpc;

import com.soulfiremc.server.user.AuthSystem;
import com.soulfiremc.server.user.AuthenticatedUser;
import com.soulfiremc.server.user.Permission;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;
import net.kyori.adventure.util.TriState;

public class DefaultAuthSystem implements AuthSystem {
  @Override
  public AuthenticatedUser authenticate(String subject, Date issuedAt) {
    var uuid = UUID.nameUUIDFromBytes("RemoteUser:%s".formatted(subject).getBytes(StandardCharsets.UTF_8));
    return new AuthenticatedUser() {
      @Override
      public void sendMessage(String message) {
        LogServiceImpl.sendMessage(uuid, message);
      }

      @Override
      public UUID getUniqueId() {
        return uuid;
      }

      @Override
      public String getUsername() {
        return subject;
      }

      @Override
      public TriState getPermission(Permission permission) {
        return TriState.TRUE;
      }
    };
  }
}
