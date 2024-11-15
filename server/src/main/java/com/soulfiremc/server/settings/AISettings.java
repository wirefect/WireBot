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
package com.soulfiremc.server.settings;

import com.soulfiremc.server.settings.lib.SettingsObject;
import com.soulfiremc.server.settings.lib.SettingsSource;
import com.soulfiremc.server.settings.property.BooleanProperty;
import com.soulfiremc.server.settings.property.IntProperty;
import com.soulfiremc.server.settings.property.Property;
import com.soulfiremc.server.settings.property.StringProperty;
import com.soulfiremc.server.util.BuiltinSettingsConstants;
import io.github.ollama4j.OllamaAPI;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class AISettings implements SettingsObject {
  public static final Property.Builder BUILDER =
    Property.builder(BuiltinSettingsConstants.AI_SETTINGS_ID);
  public static final StringProperty API_ENDPOINT =
    BUILDER.ofString(
      "api-endpoint",
      "API Endpoint",
      "Ollama API server endpoint",
      "http://127.0.0.1:11434");
  public static final StringProperty API_USERNAME =
    BUILDER.ofString(
      "api-username",
      "API Username",
      "Ollama API server username (if required)",
      "");
  public static final StringProperty API_PASSWORD =
    BUILDER.ofString(
      "api-password",
      "API Password",
      "Ollama API server password (if required)",
      "");
  public static final IntProperty REQUEST_TIMEOUT =
    BUILDER.ofInt(
      "api-request-timeout",
      "API Request Timeout",
      "Ollama API request timeout (seconds)",
      5,
      1,
      6,
      1);
  public static final BooleanProperty PULL_MODELS =
    BUILDER.ofBoolean(
      "pull-models",
      "Pull Models",
      "Whether to pull models if not found already installed",
      true);
  public static final BooleanProperty VERBOSE =
    BUILDER.ofBoolean(
      "verbose",
      "Verbose",
      "Enable verbose extra logging",
      false);

  public static OllamaAPI create(SettingsSource source) {
    var api = new OllamaAPI(source.get(AISettings.API_ENDPOINT));
    api.setBasicAuth(
      source.get(AISettings.API_USERNAME),
      source.get(AISettings.API_PASSWORD));
    api.setRequestTimeoutSeconds(source.get(AISettings.REQUEST_TIMEOUT));
    api.setVerbose(source.get(AISettings.VERBOSE));

    return api;
  }

  public static void pullIfNecessary(OllamaAPI api, String modelId, SettingsSource source) {
    var pull = source.get(AISettings.PULL_MODELS);
    if (!pull) {
      return;
    }

    try {
      log.debug("Checking if model {} is installed", modelId);
      api.getModelDetails(modelId);
    } catch (Exception e) {
      if (e.getMessage().startsWith("404")) {
        try {
          log.info("Pulling model {}. This may take a while...", modelId);
          api.pullModel(modelId);
          log.info("Model {} pulled successfully", modelId);
        } catch (Exception e2) {
          throw new RuntimeException("Failed to pull model", e2);
        }
      } else {
        throw new RuntimeException("Failed to get model details", e);
      }
    }
  }
}
