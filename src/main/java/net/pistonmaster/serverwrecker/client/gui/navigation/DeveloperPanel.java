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
package net.pistonmaster.serverwrecker.client.gui.navigation;

import javafx.stage.FileChooser;
import net.pistonmaster.serverwrecker.client.gui.GUIManager;
import net.pistonmaster.serverwrecker.client.gui.LogPanel;
import net.pistonmaster.serverwrecker.client.gui.libs.JFXFileHelper;
import net.pistonmaster.serverwrecker.util.SWPathConstants;

import javax.inject.Inject;
import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.nio.file.Files;

public class DeveloperPanel extends NavigationItem {
    @Inject
    public DeveloperPanel(GUIManager guiManager, LogPanel logPanel, CardsContainer cardsContainer) {
        setLayout(new GridLayout(0, 2));

        GeneratedPanel.addComponents(this, cardsContainer.getByNamespace("dev"), guiManager.settingsManager());

        add(new JLabel("Save Log"));
        var saveLog = new JButton("Save Log");
        add(saveLog);

        saveLog.addActionListener(listener -> {
            var chooser = new FileChooser();
            chooser.setInitialDirectory(SWPathConstants.DATA_FOLDER.toFile());
            chooser.setTitle("Save Log");
            chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Log Files", "*.log"));
            JFXFileHelper.showSaveDialog(chooser).thenAcceptAsync(file -> {
                if (file == null) {
                    return;
                }

                try (var writer = Files.newBufferedWriter(file)) {
                    writer.write(logPanel.messageLogPanel().getLogs());
                    guiManager.logger().info("Saved log to: {}", file);
                } catch (IOException e) {
                    guiManager.logger().error("Failed to save log!", e);
                }
            }, guiManager.threadPool());
        });
    }

    @Override
    public String getNavigationName() {
        return "Developer Tools";
    }

    @Override
    public String getNavigationId() {
        return "dev-menu";
    }
}