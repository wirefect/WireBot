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

import com.soulfiremc.server.data.BlockItems;
import com.soulfiremc.server.data.BlockType;
import com.soulfiremc.server.data.ItemType;
import com.soulfiremc.server.pathfinding.Costs;
import com.soulfiremc.server.pathfinding.SFVec3i;
import com.soulfiremc.server.protocol.bot.SessionDataManager;
import com.soulfiremc.server.protocol.bot.container.ContainerSlot;
import com.soulfiremc.server.protocol.bot.container.InventoryManager;
import com.soulfiremc.server.protocol.bot.container.PlayerInventoryContainer;
import com.soulfiremc.server.protocol.bot.container.SFItemStack;
import com.soulfiremc.server.util.TimeUtil;
import java.util.concurrent.TimeUnit;

public class ItemPlaceHelper {
  public static boolean placeBestBlockInHand(SessionDataManager sessionDataManager) {
    var inventoryManager = sessionDataManager.inventoryManager();
    var playerInventory = inventoryManager.playerInventory();

    ItemType leastHardItemType = null;
    var leastDestroyTime = 0F;
    for (var slot : playerInventory.storage()) {
      var slotItem = slot.item();
      if (slotItem == null) {
        continue;
      }

      var slotItemType = slotItem.type();
      var blockType = BlockItems.getBlockType(slotItemType);
      if (blockType.isEmpty()) {
        continue;
      }

      var destroyTime = blockType.get().destroyTime();
      if (leastHardItemType == null || destroyTime < leastDestroyTime) {
        leastHardItemType = slotItemType;
        leastDestroyTime = destroyTime;
      }
    }

    if (leastHardItemType == null) {
      throw new IllegalStateException("Failed to find item stack to place");
    }

    var finalLeastHardItemType = leastHardItemType;
    return placeInHand(inventoryManager, playerInventory,
      playerInventory.findMatchingSlotForAction(
          slot -> slot.item() != null && slot.item().type() == finalLeastHardItemType)
        .orElseThrow(() -> new IllegalStateException("Failed to find item stack to use")));
  }

  public static boolean placeBestToolInHand(SessionDataManager sessionDataManager, SFVec3i blockPosition) {
    var inventoryManager = sessionDataManager.inventoryManager();
    var playerInventory = inventoryManager.playerInventory();

    SFItemStack bestItemStack = null;
    var bestCost = Integer.MAX_VALUE;
    var sawEmpty = false;
    for (var slot : playerInventory.storage()) {
      var slotItem = slot.item();
      if (slotItem == null) {
        if (sawEmpty) {
          continue;
        }

        sawEmpty = true;
      }

      var optionalBlockType = sessionDataManager.currentLevel().getBlockStateAt(blockPosition).blockType();
      if (optionalBlockType == BlockType.VOID_AIR) {
        throw new IllegalStateException("Block at " + blockPosition + " is not in view range");
      }

      var cost =
        Costs.getRequiredMiningTicks(
            sessionDataManager.tagsState(),
            sessionDataManager.clientEntity(),
            sessionDataManager.inventoryManager(),
            sessionDataManager.clientEntity().onGround(),
            slotItem,
            optionalBlockType)
          .ticks();

      if (cost < bestCost || (slotItem == null && cost == bestCost)) {
        bestCost = cost;
        bestItemStack = slotItem;
      }
    }

    // Our hand is the best tool
    if (bestItemStack == null) {
      return true;
    }

    var finalBestItemStack = bestItemStack;
    return placeInHand(inventoryManager, playerInventory,
      playerInventory.findMatchingSlotForAction(
          slot -> slot.item() != null && slot.item().equalsShape(finalBestItemStack))
        .orElseThrow(() -> new IllegalStateException("Failed to find item stack to use")));
  }

  private static boolean placeInHand(InventoryManager inventoryManager, PlayerInventoryContainer playerInventory,
                                     ContainerSlot slot) {
    if (!inventoryManager.tryInventoryControl()) {
      return false;
    }

    try {
      if (playerInventory.isHeldItem(slot)) {
        return true;
      } else if (playerInventory.isHotbar(slot)) {
        inventoryManager.heldItemSlot(playerInventory.toHotbarIndex(slot));
        inventoryManager.sendHeldItemChange();
        return true;
      } else if (playerInventory.isMainInventory(slot)) {
        inventoryManager.leftClickSlot(slot);
        TimeUtil.waitTime(50, TimeUnit.MILLISECONDS);
        inventoryManager.leftClickSlot(playerInventory.getHeldItem());
        TimeUtil.waitTime(50, TimeUnit.MILLISECONDS);

        if (inventoryManager.cursorItem() != null) {
          inventoryManager.leftClickSlot(slot);
          TimeUtil.waitTime(50, TimeUnit.MILLISECONDS);
        }

        return true;
      } else {
        throw new IllegalStateException("Unexpected container slot type");
      }
    } finally {
      inventoryManager.unlockInventoryControl();
    }
  }
}
