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
package net.pistonmaster.soulfire.client.gui.navigation;

import java.awt.GridBagLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import javax.inject.Inject;
import javax.swing.BorderFactory;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JToolBar;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableModel;
import net.lenni0451.commons.swing.GBC;
import net.pistonmaster.soulfire.account.AuthType;
import net.pistonmaster.soulfire.account.MinecraftAccount;
import net.pistonmaster.soulfire.client.gui.GUIFrame;
import net.pistonmaster.soulfire.client.gui.GUIManager;
import net.pistonmaster.soulfire.client.gui.libs.JEnumComboBox;
import net.pistonmaster.soulfire.client.gui.popups.ImportTextDialog;
import net.pistonmaster.soulfire.util.BuiltinSettingsConstants;
import net.pistonmaster.soulfire.util.SFPathConstants;

public class AccountPanel extends NavigationItem {
  @Inject
  public AccountPanel(GUIManager guiManager, GUIFrame parent, CardsContainer cardsContainer) {
    setLayout(new GridBagLayout());

    var accountSettingsPanel = new JPanel();
    accountSettingsPanel.setLayout(new GridBagLayout());

    GeneratedPanel.addComponents(
        accountSettingsPanel,
        cardsContainer.getByNamespace(BuiltinSettingsConstants.ACCOUNT_SETTINGS_ID),
        guiManager.settingsManager());

    GBC.create(this).grid(0, 0).fill(GBC.HORIZONTAL).weightx(1).add(accountSettingsPanel);

    var toolBar = new JToolBar();
    toolBar.setFloatable(false);
    var addButton = new JButton("+");
    addButton.setToolTipText("Add accounts");
    addButton.addMouseListener(new MouseAdapter() {
      public void mousePressed(MouseEvent e) {
        var menu = new JPopupMenu();
        menu.add(createAccountLoadButton(guiManager, parent, AuthType.OFFLINE));
        menu.add(createAccountLoadButton(guiManager, parent, AuthType.MICROSOFT_JAVA));
        menu.add(createAccountLoadButton(guiManager, parent, AuthType.MICROSOFT_BEDROCK));
        menu.add(createAccountLoadButton(guiManager, parent, AuthType.THE_ALTENING));
        menu.add(createAccountLoadButton(guiManager, parent, AuthType.EASYMC));
        menu.show(e.getComponent(), e.getX(), e.getY());
      }
    });

    toolBar.add(addButton);
    toolBar.setBorder(BorderFactory.createLineBorder(UIManager.getColor("Component.borderColor")));
    toolBar.setBackground(UIManager.getColor("Table.background"));

    GBC.create(this).grid(0, 1).insets(10, 4, -5, 4).fill(GBC.HORIZONTAL).weightx(0).add(toolBar);

    var columnNames = new String[] {"Username", "Type", "Enabled"};
    var model =
        new DefaultTableModel(columnNames, 0) {
          final Class<?>[] columnTypes =
              new Class<?>[] {String.class, AuthType.class, Boolean.class};

          @Override
          public Class<?> getColumnClass(int columnIndex) {
            return columnTypes[columnIndex];
          }
        };

    var accountList = new JTable(model);

    var accountRegistry = guiManager.settingsManager().accountRegistry();
    accountRegistry.addLoadHook(
        () -> {
          model.getDataVector().removeAllElements();

          var accounts = accountRegistry.getAccounts();
          var registrySize = accounts.size();
          var dataVector = new Object[registrySize][];
          for (var i = 0; i < registrySize; i++) {
            var account = accounts.get(i);

            dataVector[i] =
                new Object[] {account.username(), account.authType(), account.enabled()};
          }

          model.setDataVector(dataVector, columnNames);

          accountList
              .getColumnModel()
              .getColumn(1)
              .setCellEditor(new DefaultCellEditor(new JEnumComboBox<>(AuthType.class)));

          model.fireTableDataChanged();
        });

    accountList.addPropertyChangeListener(
        evt -> {
          if ("tableCellEditor".equals(evt.getPropertyName()) && !accountList.isEditing()) {
            var accounts = new ArrayList<MinecraftAccount>();

            for (var i = 0; i < accountList.getRowCount(); i++) {
              var row = new Object[accountList.getColumnCount()];
              for (var j = 0; j < accountList.getColumnCount(); j++) {
                row[j] = accountList.getValueAt(i, j);
              }

              var username = (String) row[0];
              var authType = (AuthType) row[1];
              var enabled = (boolean) row[2];

              var account = accountRegistry.getAccount(username, authType);

              accounts.add(
                  new MinecraftAccount(authType, username, account.accountData(), enabled));
            }

            accountRegistry.setAccounts(accounts);
          }
        });

    var scrollPane = new JScrollPane(accountList);

    GBC.create(this).grid(0, 2).fill(GBC.BOTH).weight(1, 1).add(scrollPane);
  }

  private static JMenuItem createAccountLoadButton(
      GUIManager guiManager, GUIFrame parent, AuthType type) {
    var button = new JMenuItem(type.toString());

    button.addActionListener(
        e ->
            new ImportTextDialog(
                SFPathConstants.WORKING_DIRECTORY,
                String.format("Add %s accounts", type),
                String.format("%s list file", type),
                guiManager,
                parent,
                text -> guiManager.settingsManager().accountRegistry().loadFromString(text, type)));

    return button;
  }

  @Override
  public String getNavigationName() {
    return "Accounts";
  }

  @Override
  public String getNavigationId() {
    return "account-menu";
  }
}
