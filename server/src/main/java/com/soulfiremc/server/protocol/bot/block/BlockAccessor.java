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
package com.soulfiremc.server.protocol.bot.block;

import com.soulfiremc.server.data.BlockState;
import com.soulfiremc.server.pathfinding.SFVec3i;
import org.cloudburstmc.math.vector.Vector3i;

public interface BlockAccessor {
  BlockState getBlockState(int x, int y, int z);

  default BlockState getBlockState(SFVec3i position) {
    return getBlockState(position.x, position.y, position.z);
  }

  default BlockState getBlockState(Vector3i pos) {
    return getBlockState(pos.getX(), pos.getY(), pos.getZ());
  }
}
