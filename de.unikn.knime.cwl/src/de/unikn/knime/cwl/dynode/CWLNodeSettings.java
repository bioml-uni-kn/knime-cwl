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

import java.util.Arrays;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettings;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.config.Config;

/**
 * Settings for CWL nodes.
 * @author Martin Horn, Alexander Fillbrunn: University of Konstanz
 *
 */
public class CWLNodeSettings {
    
    private static final String CFG_INPUTS = "inputs";
    private static final String CFG_ENABLED_INPUTS = "enabledInputs";
    private static final String CFG_EDITOR_CONFIGS = "editorConfigs";
    private static final String CFG_EXTRA_CWL_ARGS = "extraArgs";
    
    private String[] m_inputs;
    private boolean[] m_enabledInputs;
    private Config m_editorConfigs = new NodeSettings(CFG_EDITOR_CONFIGS);
    private String m_extraCWLArgs = "";
    
    /**
     * Creates a new instance of {@code CWLNodeSettings}.
     * @param numInputs the number of inputs the node has
     */
    public CWLNodeSettings(final int numInputs) {
        m_inputs = new String[numInputs];
        Arrays.fill(m_inputs, "");
        m_enabledInputs = new boolean[numInputs];
    }
    
    /**
     * @return extra arguments for the cwl-runner
     */
    public String getExtraCWLArgs() {
        return m_extraCWLArgs;
    }
    
    /**
     * @param extraCWLArgs extra arguments for the cwl-runner
     */
    public void setExtraCWLArgs(final String extraCWLArgs) {
        m_extraCWLArgs = extraCWLArgs;
    }
    
    /**
     * @return the input values configured in the settings dialog
     */
    public String[] getInputs() {
        return m_inputs;
    }
    
    /**
     * @param inputs the input values configured in the settings dialog
     */
    public void setInputs(final String[] inputs) {
        m_inputs = inputs;
    }
    
    /**
     * @return the inputs that are enabled, i.e. not given by an input port
     */
    public boolean[] getEnabledInputs() {
        return m_enabledInputs;
    }
    
    /**
     * @param enabledInputs the inputs that are enabled, i.e. not given by an input port
     */
    public void setEnabledInputs(final boolean[] enabledInputs) {
        m_enabledInputs = enabledInputs;
    }
    
    /**
     * @return the configuration of the input editors
     */
    public Config getEditorConfigs() {
        return m_editorConfigs;
    }
    
    /**
     * @param editorConfigs the configuration of the input editors
     */
    public void setEditorConfigs(final Config editorConfigs) {
        m_editorConfigs = editorConfigs;
    }
    
    /**
     * Saves this settings object to node settings.
     * @param settings the node settings to save to
     */
    public void saveSettings(final NodeSettingsWO settings) {
        settings.addStringArray(CFG_INPUTS, m_inputs);
        settings.addBooleanArray(CFG_ENABLED_INPUTS, m_enabledInputs);
        settings.addString(CFG_EXTRA_CWL_ARGS, m_extraCWLArgs);
        Config cfg = settings.addConfig(CFG_EDITOR_CONFIGS);
        m_editorConfigs.copyTo(cfg);
    }

    /**
     * Loads settings from node settings.
     * @param settings the node settings to load from
     * @throws InvalidSettingsException if the settings cannot be loaded
     */
    public void loadSettings(final NodeSettingsRO settings) throws InvalidSettingsException {
        m_inputs = settings.getStringArray(CFG_INPUTS);
        m_enabledInputs = settings.getBooleanArray(CFG_ENABLED_INPUTS);
        m_editorConfigs = settings.getConfig(CFG_EDITOR_CONFIGS);
        m_extraCWLArgs = settings.getString(CFG_EXTRA_CWL_ARGS);
    }

    /**
     * Loads settings with defaults from node settings.
     * @param settings the node settings to load from
     */
    public void loadSettingsForDialog(final NodeSettingsRO settings) {
        m_inputs = settings.getStringArray(CFG_INPUTS, new String[0]);
        m_enabledInputs = settings.getBooleanArray(CFG_ENABLED_INPUTS, new boolean[0]);
        m_extraCWLArgs = settings.getString(CFG_EXTRA_CWL_ARGS, "");
        try {
            m_editorConfigs = settings.getConfig(CFG_EDITOR_CONFIGS);
        } catch (InvalidSettingsException e) {
            m_editorConfigs = new NodeSettings(CFG_EDITOR_CONFIGS);
        }
    }
}
