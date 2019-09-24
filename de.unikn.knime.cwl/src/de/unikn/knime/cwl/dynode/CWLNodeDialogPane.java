/*
 * ------------------------------------------------------------------------
 *
 *  Copyright by KNIME AG, Zurich, Switzerland
 *  Website: http://www.knime.com; Email: contact@knime.com
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License, Version 3, as
 *  published by the Free Software Foundation.
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, see <http://www.gnu.org/licenses>.
 *
 *  Additional permission under GNU GPL version 3 section 7:
 *
 *  KNIME interoperates with ECLIPSE solely via ECLIPSE's plug-in APIs.
 *  Hence, KNIME and ECLIPSE are both independent programs and are not
 *  derived from each other. Should, however, the interpretation of the
 *  GNU GPL Version 3 ("License") under any applicable laws result in
 *  KNIME and ECLIPSE being a combined program, KNIME AG herewith grants
 *  you the additional permission to use and propagate KNIME together with
 *  ECLIPSE with only the license terms in place for ECLIPSE applying to
 *  ECLIPSE and the GNU GPL Version 3 applying for KNIME, provided the
 *  license terms of ECLIPSE themselves allow for the respective use and
 *  propagation of ECLIPSE together with KNIME.
 *
 *  Additional permission relating to nodes for KNIME that extend the Node
 *  Extension (and in particular that are based on subclasses of NodeModel,
 *  NodeDialog, and NodeView) and that only interoperate with KNIME through
 *  standard APIs ("Nodes"):
 *  Nodes are deemed to be separate and independent programs and to not be
 *  covered works.  Notwithstanding anything to the contrary in the
 *  License, the License does not apply to Nodes, you are not required to
 *  license Nodes under the License, and you are granted a license to
 *  prepare and propagate Nodes, in each case even if such Nodes are
 *  propagated with or for interoperation with KNIME.  The owner of a Node
 *  may freely choose the license terms applicable to such Node, including
 *  when such Node is propagated with or for interoperation with KNIME.
 * ---------------------------------------------------------------------
 *
 * History
 *   1 Aug 2019 : created
 */
package de.unikn.knime.cwl.dynode;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.config.Config;
import org.knime.core.node.port.PortObjectSpec;

import de.unikn.knime.cwl.dynode.editors.CWLEditorFactory;
import de.unikn.knime.cwl.dynode.editors.CWLInputEditor;
import de.unikn.knime.cwl.dynode.port.CWLPortDescription;

/**
 * Generic settings panel for CWL nodes.
 * @author Martin Horn, Alexander Fillbrunn: University of Konstanz
 *
 */
public class CWLNodeDialogPane extends NodeDialogPane {

    private static final String INPUT_CFG_KEY_TEMPLATE = "config_%s[%d]";
    
    private CWLPortDescription[] m_inputs;
    private CWLInputEditor[] m_inputFields;
    private JCheckBox[] m_inputEnabledCheckboxes;
    
    private JTextField m_extraArguments = new JTextField();
    
    private CWLNodeSettings m_settings;
    
    /**
     * Creates a new instance of {@code CWLNodeDialogPane}.
     * @param inputs the inputs to the CWL file represented by this panel
     */
    public CWLNodeDialogPane(final CWLPortDescription[] inputs) {
        m_inputs = inputs;
        m_settings = new CWLNodeSettings(inputs.length);
        addTab("Inputs", createInputTab());
        addTab("Advanced", createAdvancedTab());
    }
    
    private JPanel createAdvancedTab() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        panel.add(new JLabel("Extra cwl-runner arguments:"), gbc);
        gbc.gridx++;
        gbc.weightx = 2;
        panel.add(m_extraArguments, gbc);
        
        return panel;
    }
    
    private JPanel createInputTab() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        m_inputFields = new CWLInputEditor[m_inputs.length];
        m_inputEnabledCheckboxes = new JCheckBox[m_inputs.length];
        
        for (CWLPortDescription port : m_inputs) {
            JPanel ePnl = createEditorPanel(port);
            panel.add(ePnl, gbc);
            gbc.gridy++;
        }
        return panel;
    }
    
    private JPanel createEditorPanel(final CWLPortDescription port) {
        m_inputEnabledCheckboxes[port.getIndex()] = new JCheckBox("Provide a value for this port");
        m_inputEnabledCheckboxes[port.getIndex()].addActionListener(
                e -> m_inputFields[port.getIndex()]
                        .setEnabled(m_inputEnabledCheckboxes[port.getIndex()].isSelected()));
        m_inputFields[port.getIndex()] = CWLEditorFactory.getInstance().getEditorForType(port.getType());
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder(
                String.format("Port %d: %s%s",
                        port.getIndex() + 1, port.getName(), port.getType().isOptional() ? " (optional)" : "")));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(m_inputEnabledCheckboxes[port.getIndex()], gbc);
        gbc.gridy++;
        Component component = m_inputFields[port.getIndex()].getComponent();
        panel.add(component, gbc);
        return panel;
    }
    
    @Override
    protected void loadSettingsFrom(final NodeSettingsRO settings, final PortObjectSpec[] specs)
            throws NotConfigurableException {
        m_settings.loadSettingsForDialog(settings);
        m_extraArguments.setText(m_settings.getExtraCWLArgs());
        for (int i = 0; i < m_inputs.length; i++) {
            if (specs[i] == null) {
                try {
                    Config cfg = m_settings.getEditorConfigs()
                            .getConfig(String.format(INPUT_CFG_KEY_TEMPLATE, m_inputs[i].getName(), i));
                    m_inputFields[i].loadAdditionalSettings(cfg);
                } catch (InvalidSettingsException e) {
                    // Nothing to do here
                }
                m_inputFields[i].setStringValue(m_settings.getInputs()[i]);
                m_inputEnabledCheckboxes[i].setSelected(m_settings.getEnabledInputs()[i]);
                m_inputEnabledCheckboxes[i].setEnabled(true);
                m_inputFields[i].setEnabled(m_settings.getEnabledInputs()[i]);
            } else {
                m_inputFields[i].setEnabled(false); 
                m_inputEnabledCheckboxes[i].setEnabled(false); 
            }
        }
    }
    
    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings) throws InvalidSettingsException {
        String[] inputs = new String[m_inputs.length];
        boolean[] enabledInputs = new boolean[m_inputs.length];
        for (int i = 0; i < m_inputs.length; i++) {
            inputs[i] = m_inputFields[i].getJSONStringValue();
            enabledInputs[i] = m_inputEnabledCheckboxes[i].isSelected();
            Config cfg = m_settings.getEditorConfigs()
                    .addConfig(String.format(INPUT_CFG_KEY_TEMPLATE, m_inputs[i].getName(), i));
            m_inputFields[i].saveAdditionalSettings(cfg);
        }
        m_settings.setExtraCWLArgs(m_extraArguments.getText());
        m_settings.setInputs(inputs);
        m_settings.setEnabledInputs(enabledInputs);
        m_settings.saveSettings(settings);
    }
    
}
