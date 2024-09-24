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
package com.soulfiremc.server.util;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class RandomUtil {
  private RandomUtil() {}

  public static int getRandomInt(int min, int max) {
    if (min > max) {
      throw new IllegalArgumentException("min must not be greater than max");
    }

    if (min == max) {
      return min;
    }

    return ThreadLocalRandom.current().nextInt(min, max);
  }

  public static <E> E getRandomEntry(List<E> list) {
    return list.get(ThreadLocalRandom.current().nextInt(list.size()));
  }
}
