/*
 * ServerWrecker
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
package net.pistonmaster.serverwrecker.server.pathfinding.execution;

import lombok.ToString;
import net.pistonmaster.serverwrecker.server.protocol.BotConnection;

@ToString
public final class RecalculatePathAction implements WorldAction {
    @Override
    public boolean isCompleted(BotConnection connection) {
        throw new UnsupportedOperationException("Should be handled separately!");
    }

    @Override
    public void tick(BotConnection connection) {
        throw new UnsupportedOperationException("Should be handled separately!");
    }

    @Override
    public int getAllowedTicks() {
        throw new UnsupportedOperationException("Should be handled separately!");
    }
}
