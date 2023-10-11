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
package net.pistonmaster.serverwrecker.util;

import net.pistonmaster.serverwrecker.data.BlockItems;
import net.pistonmaster.serverwrecker.data.ItemType;
import net.pistonmaster.serverwrecker.data.TierType;

public class ItemTypeHelper {
    private ItemTypeHelper() {
    }

    public static boolean isFullBlockItem(ItemType type) {
        return BlockItems.getBlockType(type).isPresent();
    }

    public static boolean isTool(ItemType type) {
        return TierType.getTier(type).isPresent() || type == ItemType.SHEARS;
    }
}