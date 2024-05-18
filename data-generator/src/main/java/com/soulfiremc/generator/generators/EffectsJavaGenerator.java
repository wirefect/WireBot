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
package com.soulfiremc.generator.generators;

import com.soulfiremc.generator.util.GeneratorConstants;
import com.soulfiremc.generator.util.ResourceHelper;
import java.util.Locale;
import java.util.Objects;
import net.minecraft.core.registries.BuiltInRegistries;

public class EffectsJavaGenerator implements IDataGenerator {
  @Override
  public String getDataName() {
    return "java/EffectType.java";
  }

  @Override
  public String generateDataJson() {
    var base = ResourceHelper.getResource("/templates/EffectType.java");
    return base.replace(
      GeneratorConstants.VALUES_REPLACE,
      String.join(
        "\n  ",
        BuiltInRegistries.MOB_EFFECT.stream()
          .map(
            s -> {
              var key = Objects.requireNonNull(BuiltInRegistries.MOB_EFFECT.getKey(s));
              return "public static final EffectType %s = register(\"%s\");".formatted(key.getPath().toUpperCase(Locale.ROOT), key);
            })
          .toArray(String[]::new)));
  }
}