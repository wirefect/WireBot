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
package com.soulfiremc.server.pathfinding.execution;

import com.github.steveice10.mc.protocol.data.game.entity.player.Hand;
import com.soulfiremc.server.pathfinding.SFVec3i;
import com.soulfiremc.server.protocol.BotConnection;
import com.soulfiremc.server.protocol.bot.BotActionManager;
import com.soulfiremc.server.util.BlockTypeHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public final class BlockPlaceAction implements WorldAction {
  private final SFVec3i blockPosition;
  private final BotActionManager.BlockPlaceAgainstData blockPlaceAgainstData;
  private boolean putOnHotbar = false;
  private boolean finishedPlacing = false;

  @Override
  public boolean isCompleted(BotConnection connection) {
    var level = connection.sessionDataManager().currentLevel();

    return BlockTypeHelper.isFullBlock(level.getBlockStateAt(blockPosition));
  }

  @Override
  public void tick(BotConnection connection) {
    var sessionDataManager = connection.sessionDataManager();
    sessionDataManager.controlState().resetAll();

    if (!putOnHotbar) {
      if (ItemPlaceHelper.placeBestBlockInHand(sessionDataManager)) {
        putOnHotbar = true;
      }

      return;
    }

    if (finishedPlacing) {
      return;
    }

    connection.sessionDataManager().botActionManager().placeBlock(Hand.MAIN_HAND, blockPlaceAgainstData);
    finishedPlacing = true;
  }

  @Override
  public int getAllowedTicks() {
    // 3-seconds max to place a block
    return 3 * 20;
  }

  @Override
  public String toString() {
    return "BlockPlaceAction -> " + blockPosition.formatXYZ();
  }
}
