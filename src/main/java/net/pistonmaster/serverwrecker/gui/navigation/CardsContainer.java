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
package net.pistonmaster.serverwrecker.gui.navigation;

import ch.jalu.injector.Injector;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import javax.inject.Inject;
import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor(onConstructor_ = @Inject)
public class CardsContainer extends JPanel {
    public static final String NAVIGATION_MENU = "navigation-menu";
    @Getter
    private final List<NavigationItem> panels = new ArrayList<>();
    private final Injector injector;

    public void create() {
        setLayout(new CardLayout());

        panels.add(injector.getSingleton(SettingsPanel.class));
        var pluginPanel = injector.getSingleton(PluginListPanel.class);
        panels.add(pluginPanel);
        panels.add(injector.getSingleton(AccountPanel.class));
        panels.add(injector.getSingleton(ProxyPanel.class));
        panels.add(injector.getSingleton(DeveloperPanel.class));

        var navigationPanel = injector.getSingleton(NavigationPanel.class);
        add(navigationPanel, NAVIGATION_MENU);

        for (var item : panels) {
            add(NavigationWrapper.createBackWrapper(this, NAVIGATION_MENU, item), item.getNavigationId());
        }

        for (var item : pluginPanel.getPluginPages()) {
            add(NavigationWrapper.createBackWrapper(this, pluginPanel.getNavigationId(), new PluginSettingsPanel(item)), item.getPageId());
        }

        setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 10));
    }
}
