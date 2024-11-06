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
package com.soulfiremc.server.protocol.bot.state;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.geysermc.mcprotocollib.protocol.packet.ingame.serverbound.level.ServerboundPlayerInputPacket;

@Setter
@Getter
@ToString
public class ControlState {
  private int activelyControlling;
  private boolean forward;
  private boolean backward;
  private boolean left;
  private boolean right;
  private boolean sprinting;
  private boolean jumping;
  private boolean sneaking;
  private boolean flying;

  public void incrementActivelyControlling() {
    activelyControlling++;
  }

  public void decrementActivelyControlling() {
    activelyControlling--;
  }

  public boolean isActivelyControlling() {
    return activelyControlling > 0;
  }

  public void resetWasd() {
    forward = false;
    backward = false;
    left = false;
    right = false;
  }

  public void resetAll() {
    resetWasd();
    sprinting = false;
    jumping = false;
    sneaking = false;
    flying = false;
  }

  public ServerboundPlayerInputPacket toServerboundPlayerInputPacket() {
    return new ServerboundPlayerInputPacket(forward, backward, left, right, jumping, sneaking, sprinting);
  }
}
