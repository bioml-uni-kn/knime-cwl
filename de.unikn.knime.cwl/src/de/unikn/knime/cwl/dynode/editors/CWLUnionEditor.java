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
package de.unikn.knime.cwl.dynode.editors;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.config.Config;
import org.knime.core.node.config.ConfigRO;
import org.knime.core.node.config.ConfigWO;

import de.unikn.knime.cwl.dynode.data.types.CWLDataType;
import de.unikn.knime.cwl.dynode.data.types.CWLType;
import de.unikn.knime.cwl.dynode.data.types.CWLUnionType;

/**
 * Editor component for union types. Creates a panel with multiple other editors.
 * @author Martin Horn, Alexander Fillbrunn: University of Konstanz
 *
 */
public class CWLUnionEditor implements CWLInputEditor {

    private JPanel m_panel;
    private JRadioButton[] m_radioBtns;
    private CWLInputEditor[] m_editors;
    
    /**
     * Creates a new instance of this editor.
     * @param type the union type represented by the editor.
     */
    public CWLUnionEditor(final CWLUnionType type) {
        List<CWLDataType> types = new ArrayList<>();
        // We need to filter null because instead of explicitly
        // selecting it we can just not provide a value at all
        for (CWLDataType t : type.getTypes()) {
            if (t.getType() != CWLType.NULL) {
                types.add(t);
            }
        }
        types.sort((a, b) -> Integer.compare(a.hashCode(), b.hashCode()));
        m_radioBtns = new JRadioButton[types.size()];
        m_editors = new CWLInputEditor[types.size()];
        m_panel = new JPanel(new GridBagLayout());
        m_panel.setBorder(BorderFactory.createTitledBorder("Union type"));
        ButtonGroup group = new ButtonGroup();

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        for (int i = 0; i < types.size(); i++) {
            CWLDataType t = types.get(i);
            JRadioButton btn = new JRadioButton(t.toString());
            group.add(btn);
            CWLInputEditor editor = CWLEditorFactory.getInstance().getEditorForType(t);
            m_panel.add(btn, gbc);
            gbc.gridy++;
            m_panel.add(editor.getComponent(), gbc);
            gbc.gridy++;
            m_radioBtns[i] = btn;
            m_editors[i] = editor;
        }
    }
    
    @Override
    public String getJSONStringValue() {
        for (int i = 0; i < m_radioBtns.length; i++) {
            if (m_radioBtns[i].isSelected()) {
                return m_editors[i].getJSONStringValue();
            }
        }
        return null;
    }

    @Override
    public void setStringValue(final String jsonStr) {
        for (int i = 0; i < m_radioBtns.length; i++) {
            if (m_radioBtns[i].isSelected()) {
                m_editors[i].setStringValue(jsonStr);
            }
        }
    }

    @Override
    public Component getComponent() {
        return m_panel;
    }
    
    @Override
    public void loadAdditionalSettings(final ConfigRO settings) {
        int selectedEditor = settings.getInt("selectedEditor", 0);
        m_radioBtns[selectedEditor].setSelected(true);
        Config cfg;
        try {
            cfg = settings.getConfig("editorSettings");
        } catch (InvalidSettingsException e) {
            return;
        }
        m_editors[selectedEditor].loadAdditionalSettings(cfg);
    }
    
    @Override
    public void saveAdditionalSettings(final ConfigWO settings) {
        Config cfg = settings.addConfig("editorSettings");
        for (int i = 0; i < m_radioBtns.length; i++) {
            if (m_radioBtns[i].isSelected()) {
                m_editors[i].saveAdditionalSettings(cfg);
                settings.addInt("selectedEditor", i);
                break;
            }
        }
    }
    
    @Override
    public void setEnabled(final boolean enabled) {
        for (int i = 0; i < m_radioBtns.length; i++) {
            m_radioBtns[i].setEnabled(enabled);
            m_editors[i].setEnabled(enabled);
        }
    }
}
