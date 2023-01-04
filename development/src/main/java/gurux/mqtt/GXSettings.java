//
// --------------------------------------------------------------------------
//  Gurux Ltd
// 
//
//
// Filename:        $HeadURL$
//
// Version:         $Revision$,
//                  $Date$
//                  $Author$
//
// Copyright (c) Gurux Ltd
//
//---------------------------------------------------------------------------
//
//  DESCRIPTION
//
// This file is a part of Gurux Device Framework.
//
// Gurux Device Framework is Open Source software; you can redistribute it
// and/or modify it under the terms of the GNU General Public License 
// as published by the Free Software Foundation; version 2 of the License.
// Gurux Device Framework is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of 
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
// See the GNU General Public License for more details.
//
// This code is licensed under the GNU General Public License v2. 
// Full text may be retrieved at http://www.gnu.org/licenses/gpl-2.0.txt
//---------------------------------------------------------------------------

package gurux.mqtt;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JRootPane;
import javax.swing.KeyStroke;
import javax.swing.LayoutStyle.ComponentPlacement;

/**
 * SMS settings dialog.
 */
class GXSettings extends javax.swing.JDialog implements ActionListener {
    /**
     * Serialization version.
     */
    private static final long serialVersionUID = 1L;
    /**
     * Has user accept changes.
     */
    private boolean accepted;
    /**
     * Parent media component.
     */
    private GXMqtt target;

    /**
     * Cancel button.
     */
    private javax.swing.JButton cancelBtn;
    /**
     * IP Address label.
     */
    private javax.swing.JLabel ipAddressLbl;
    /**
     * IP Address panel includes label and text box.
     */
    private javax.swing.JPanel ipAddressPanel;
    /**
     * IP address text box.
     */
    private javax.swing.JTextField ipAddressTB;
    /**
     * OK button.
     */
    private javax.swing.JButton okBtn;
    /**
     * Port label.
     */
    private javax.swing.JLabel portLbl;
    /**
     * Port panel includes label and text box.
     */
    private javax.swing.JPanel portPanel;
    /**
     * Port text box.
     */
    private javax.swing.JTextField portTB;
    /**
     * Main panel.
     */
    private javax.swing.JPanel jPanel1;

    /**
     * Creates new form GXSettings.
     * 
     * @param parent
     *            Parent frame.
     * @param modal
     *            Is Dialog shown as modal.
     * @param comp
     *            Media component where settings are get and set.
     * @param locale
     *            Used locale.
     */
    GXSettings(final java.awt.Frame parent, final boolean modal,
            final GXMqtt comp, final Locale locale) {
        super(parent, modal);
        super.setLocationRelativeTo(parent);
        initComponents();
        target = comp;
        ipAddressTB.setText(target.getServerAddress());
        portTB.setText(String.valueOf(target.getPort()));
        // Localize strings.
        ResourceBundle bundle = ResourceBundle.getBundle("resources", locale);
        this.setTitle(bundle.getString("SettingsTxt"));
        ipAddressLbl.setText(bundle.getString("ServerAddressTxt"));
        portLbl.setText(bundle.getString("PortTxt"));
        okBtn.setText(bundle.getString("OK"));
        cancelBtn.setText(bundle.getString("Cancel"));
    }

    /**
     * Has user accept changes.
     * 
     * @return True, if user has accept changes.
     */
    public boolean isAccepted() {
        return accepted;
    }

    @Override
    public final void actionPerformed(final ActionEvent e) {
        this.dispose();
    }

    /**
     * Initialize components.
     */
    /// CHECKSTYLE:OFF
    private void initComponents() {
        jPanel1 = new javax.swing.JPanel();
        ipAddressPanel = new javax.swing.JPanel();
        ipAddressLbl = new javax.swing.JLabel();
        ipAddressTB = new javax.swing.JTextField();
        portPanel = new javax.swing.JPanel();
        portLbl = new javax.swing.JLabel();
        portTB = new javax.swing.JTextField();
        okBtn = new javax.swing.JButton();
        cancelBtn = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        ipAddressPanel.setPreferredSize(new java.awt.Dimension(298, 33));
        ipAddressLbl.setText("IP Address:");

        javax.swing.GroupLayout ipAddressPanelLayout =
                new javax.swing.GroupLayout(ipAddressPanel);
        ipAddressPanel.setLayout(ipAddressPanelLayout);
        ipAddressPanelLayout.setHorizontalGroup(ipAddressPanelLayout
                .createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(ipAddressPanelLayout.createSequentialGroup()
                        .addContainerGap().addComponent(ipAddressLbl)
                        .addPreferredGap(
                                javax.swing.LayoutStyle.ComponentPlacement.RELATED,
                                34, Short.MAX_VALUE)
                        .addComponent(ipAddressTB,
                                javax.swing.GroupLayout.PREFERRED_SIZE, 211,
                                javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap()));
        ipAddressPanelLayout.setVerticalGroup(ipAddressPanelLayout
                .createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING,
                        ipAddressPanelLayout.createSequentialGroup()
                                .addContainerGap(
                                        javax.swing.GroupLayout.DEFAULT_SIZE,
                                        Short.MAX_VALUE)
                                .addGroup(ipAddressPanelLayout
                                        .createParallelGroup(
                                                javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(ipAddressLbl)
                                        .addComponent(ipAddressTB,
                                                javax.swing.GroupLayout.PREFERRED_SIZE,
                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(367, 367, 367)));

        portPanel.setPreferredSize(new java.awt.Dimension(298, 35));

        portLbl.setText("Port:");

        javax.swing.GroupLayout portPanelLayout =
                new javax.swing.GroupLayout(portPanel);
        portPanel.setLayout(portPanelLayout);
        portPanelLayout.setHorizontalGroup(portPanelLayout
                .createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(portPanelLayout.createSequentialGroup()
                        .addContainerGap().addComponent(portLbl)
                        .addPreferredGap(
                                javax.swing.LayoutStyle.ComponentPlacement.RELATED,
                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                Short.MAX_VALUE)
                        .addComponent(portTB,
                                javax.swing.GroupLayout.PREFERRED_SIZE, 213,
                                javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap()));
        portPanelLayout.setVerticalGroup(portPanelLayout
                .createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING,
                        portPanelLayout.createSequentialGroup()
                                .addContainerGap(
                                        javax.swing.GroupLayout.DEFAULT_SIZE,
                                        Short.MAX_VALUE)
                                .addGroup(portPanelLayout.createParallelGroup(
                                        javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(portLbl)
                                        .addComponent(portTB,
                                                javax.swing.GroupLayout.PREFERRED_SIZE,
                                                javax.swing.GroupLayout.DEFAULT_SIZE,
                                                javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addContainerGap()));

        javax.swing.GroupLayout jPanel1Layout =
                new javax.swing.GroupLayout(jPanel1);
        jPanel1Layout.setHorizontalGroup(
                jPanel1Layout.createParallelGroup(Alignment.LEADING)
                        .addComponent(ipAddressPanel, GroupLayout.DEFAULT_SIZE,
                                341, Short.MAX_VALUE)
                        .addComponent(portPanel, GroupLayout.DEFAULT_SIZE, 341,
                                Short.MAX_VALUE));
        okBtn.setText("OK");
        okBtn.addActionListener(new java.awt.event.ActionListener() {
            public final void
                    actionPerformed(final java.awt.event.ActionEvent evt) {
                okBtnActionPerformed(evt);
            }
        });

        cancelBtn.setText("Cancel");
        cancelBtn.addActionListener(new java.awt.event.ActionListener() {
            public final void
                    actionPerformed(final java.awt.event.ActionEvent evt) {
                cancelBtnActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout =
                new javax.swing.GroupLayout(getContentPane());
        layout.setHorizontalGroup(layout.createParallelGroup(Alignment.TRAILING)
                .addGroup(layout.createSequentialGroup()
                        .addContainerGap(220, Short.MAX_VALUE)
                        .addComponent(okBtn)
                        .addPreferredGap(ComponentPlacement.UNRELATED)
                        .addComponent(cancelBtn).addContainerGap())
                .addGroup(Alignment.LEADING, layout.createSequentialGroup()
                        .addComponent(jPanel1, GroupLayout.PREFERRED_SIZE,
                                GroupLayout.DEFAULT_SIZE,
                                GroupLayout.PREFERRED_SIZE)
                        .addContainerGap(11, Short.MAX_VALUE)));
        layout.setVerticalGroup(layout.createParallelGroup(Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                        .addComponent(jPanel1, GroupLayout.PREFERRED_SIZE, 234,
                                GroupLayout.PREFERRED_SIZE)
                        .addGap(26)
                        .addGroup(layout.createParallelGroup(Alignment.BASELINE)
                                .addComponent(cancelBtn).addComponent(okBtn))
                        .addContainerGap()));
        getContentPane().setLayout(layout);

        pack();
    }
    // CHECKSTYLE:ON

    @Override
    protected JRootPane createRootPane() {
        KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
        JRootPane rootPane = new JRootPane();
        rootPane.registerKeyboardAction(this, stroke,
                JComponent.WHEN_IN_FOCUSED_WINDOW);
        return rootPane;
    }

    /**
     * Accept changes.
     * 
     * @param evt
     *            Action events.
     */
    private void okBtnActionPerformed(final ActionEvent evt) {
        try {
            target.setServerAddress(ipAddressTB.getText());
            target.setPort(Integer.parseInt(portTB.getText()));
            target.validate();
            accepted = true;
            this.dispose();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage());
        }
    }

    /**
     * Discard changes.
     * 
     * @param evt
     *            Action event.
     */
    private void cancelBtnActionPerformed(final ActionEvent evt) {
        this.dispose();
    }
}
