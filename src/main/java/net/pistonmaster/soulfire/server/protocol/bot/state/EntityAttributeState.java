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
package net.pistonmaster.soulfire.server.protocol.bot.state;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import lombok.Data;
import net.pistonmaster.soulfire.server.data.AttributeType;

import java.util.ArrayList;
import java.util.Map;

@Data
public class EntityAttributeState {
    private final Map<AttributeType, AttributeState> attributeStore = new Object2ObjectOpenHashMap<>();

    public AttributeState getOrCreateAttribute(AttributeType type) {
        return attributeStore.computeIfAbsent(type, k -> new AttributeState(type.defaultValue(), new ArrayList<>()));
    }
}
