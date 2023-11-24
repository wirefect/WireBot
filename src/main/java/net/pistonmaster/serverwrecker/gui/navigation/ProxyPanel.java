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

import net.pistonmaster.serverwrecker.gui.GUIFrame;
import net.pistonmaster.serverwrecker.gui.GUIManager;
import net.pistonmaster.serverwrecker.gui.libs.JEnumComboBox;
import net.pistonmaster.serverwrecker.gui.libs.SwingTextUtils;
import net.pistonmaster.serverwrecker.gui.popups.ImportTextDialog;
import net.pistonmaster.serverwrecker.proxy.ProxyType;
import net.pistonmaster.serverwrecker.proxy.SWProxy;

import javax.inject.Inject;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.nio.file.Path;
import java.util.ArrayList;

public class ProxyPanel extends NavigationItem {
    @Inject
    public ProxyPanel(GUIManager guiManager, GUIFrame parent, CardsContainer cardsContainer) {
        setLayout(new GridLayout(2, 1, 10, 10));

        var proxyOptionsPanel = new JPanel();
        proxyOptionsPanel.setLayout(new GridLayout(2, 1, 10, 10));

        var addProxyPanel = new JPanel();
        addProxyPanel.setLayout(new GridLayout(1, 3, 10, 10));

        addProxyPanel.add(createProxyLoadButton(guiManager, parent, ProxyType.HTTP));
        addProxyPanel.add(createProxyLoadButton(guiManager, parent, ProxyType.SOCKS4));
        addProxyPanel.add(createProxyLoadButton(guiManager, parent, ProxyType.SOCKS5));

        proxyOptionsPanel.add(addProxyPanel);

        var proxySettingsPanel = new JPanel();
        proxySettingsPanel.setLayout(new GridLayout(0, 2));

        GeneratedPanel.addComponents(this, cardsContainer.getByNamespace("proxy"), guiManager.getSettingsManager());

        proxyOptionsPanel.add(proxySettingsPanel);

        add(proxyOptionsPanel);

        var proxyListPanel = new JPanel();
        proxyListPanel.setLayout(new GridLayout(1, 1));

        var columnNames = new String[]{"IP", "Port", "Username", "Password", "Type", "Enabled"};
        var model = new DefaultTableModel(columnNames, 0) {
            final Class<?>[] columnTypes = new Class<?>[]{
                    Object.class, Integer.class, Object.class, Object.class, ProxyType.class, Boolean.class
            };

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return columnTypes[columnIndex];
            }
        };

        var proxyList = new JTable(model);

        var proxyRegistry = guiManager.getProxyRegistry();
        proxyRegistry.addLoadHook(() -> {
            model.getDataVector().removeAllElements();

            var proxies = proxyRegistry.getProxies();
            var registrySize = proxies.size();
            var dataVector = new Object[registrySize][];
            for (var i = 0; i < registrySize; i++) {
                var proxy = proxies.get(i);

                dataVector[i] = new Object[]{
                        proxy.host(),
                        proxy.port(),
                        proxy.username(),
                        proxy.password(),
                        proxy.type(),
                        proxy.enabled()
                };
            }

            model.setDataVector(dataVector, columnNames);

            proxyList.getColumnModel().getColumn(4)
                    .setCellEditor(new DefaultCellEditor(new JEnumComboBox<>(ProxyType.class)));

            model.fireTableDataChanged();
        });

        proxyList.addPropertyChangeListener(evt -> {
            if ("tableCellEditor".equals(evt.getPropertyName()) && !proxyList.isEditing()) {
                var proxies = new ArrayList<SWProxy>();

                for (var i = 0; i < proxyList.getRowCount(); i++) {
                    var row = new Object[proxyList.getColumnCount()];
                    for (var j = 0; j < proxyList.getColumnCount(); j++) {
                        row[j] = proxyList.getValueAt(i, j);
                    }

                    var host = (String) row[0];
                    var port = (int) row[1];
                    var username = (String) row[2];
                    var password = (String) row[3];
                    var type = (ProxyType) row[4];
                    var enabled = (boolean) row[5];

                    proxies.add(new SWProxy(type, host, port, username, password, enabled));
                }

                proxyRegistry.setProxies(proxies);
            }
        });

        var scrollPane = new JScrollPane(proxyList);

        proxyListPanel.add(scrollPane);

        add(proxyListPanel);
    }

    private static JButton createProxyLoadButton(GUIManager guiManager, GUIFrame parent, ProxyType type) {
        var button = new JButton(SwingTextUtils.htmlCenterText(String.format("Load %s proxies", type)));

        button.addActionListener(e -> new ImportTextDialog(
                Path.of(System.getProperty("user.dir")),
                String.format("Load %s proxies", type),
                String.format("%s list file", type),
                guiManager,
                parent,
                text -> guiManager.getProxyRegistry().loadFromString(text, type)
        ));

        return button;
    }

    @Override
    public String getNavigationName() {
        return "Proxies";
    }

    @Override
    public String getNavigationId() {
        return "proxy-menu";
    }
}
