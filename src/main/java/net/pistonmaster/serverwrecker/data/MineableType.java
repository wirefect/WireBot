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
package net.pistonmaster.serverwrecker.data;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Locale;
import java.util.Optional;
import java.util.Set;

@RequiredArgsConstructor
public enum MineableType {
    PICKAXE(Set.of(ItemType.WOODEN_PICKAXE, ItemType.STONE_PICKAXE, ItemType.IRON_PICKAXE, ItemType.GOLDEN_PICKAXE, ItemType.DIAMOND_PICKAXE, ItemType.NETHERITE_PICKAXE)),
    SHOVEL(Set.of(ItemType.WOODEN_SHOVEL, ItemType.STONE_SHOVEL, ItemType.IRON_SHOVEL, ItemType.GOLDEN_SHOVEL, ItemType.DIAMOND_SHOVEL, ItemType.NETHERITE_SHOVEL)),
    AXE(Set.of(ItemType.WOODEN_AXE, ItemType.STONE_AXE, ItemType.IRON_AXE, ItemType.GOLDEN_AXE, ItemType.DIAMOND_AXE, ItemType.NETHERITE_AXE)),
    HOE(Set.of(ItemType.WOODEN_HOE, ItemType.STONE_HOE, ItemType.IRON_HOE, ItemType.GOLDEN_HOE, ItemType.DIAMOND_HOE, ItemType.NETHERITE_HOE));

    private final Set<ItemType> tools;
    @Getter
    private final String tagName = String.format("mineable/%s", this.name().toLowerCase(Locale.ROOT));

    public static Optional<MineableType> getFromTool(ItemType itemType) {
        for (var mineableType : MineableType.values()) {
            if (mineableType.tools.contains(itemType)) {
                return Optional.of(mineableType);
            }
        }

        return Optional.empty();
    }
}