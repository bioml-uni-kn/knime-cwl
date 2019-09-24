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
package de.unikn.knime.cwl.util.node.createrecord;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;

/**
 * Settings for the Build CWL Record node.
 * @author Martin Horn, Alexander Fillbrunn: University of Konstanz
 *
 */
public class BuildRecordSettings {
    
    private static final String CFG_KEY1 = "key1";
    private static final String CFG_KEY2 = "key2";
    private static final String CFG_KEY3 = "key3";
    
    private String m_key1;
    private String m_key2;
    private String m_key3;
    
    /**
     * @return the key for the first new entry in the created record
     */
    public String getKey1() {
        return m_key1;
    }
    
    /**
     * @param key1 the key for the first new entry in the created record
     */
    public void setKey1(final String key1) {
        m_key1 = key1;
    }
    
    /**
     * @return the key for the second new entry in the created record
     */
    public String getKey2() {
        return m_key2;
    }
    
    /**
     * @param key2 the key for the second new entry in the created record
     */
    public void setKey2(final String key2) {
        m_key2 = key2;
    }
    
    /**
     * @return the key for the third new entry in the created record
     */
    public String getKey3() {
        return m_key3;
    }
    
    /**
     * @param key3 the key for the third new entry in the created record
     */
    public void setKey3(final String key3) {
        m_key3 = key3;
    }
    
    /**
     * Saves this settings object to node settings.
     * @param settings the node settings to save to
     */
    public void saveSettings(final NodeSettingsWO settings) {
        settings.addString(CFG_KEY1, m_key1);
        settings.addString(CFG_KEY2, m_key2);
        settings.addString(CFG_KEY3, m_key3);
    }
    
    /**
     * Loads settings from node settings.
     * @param settings the node settings to load from
     * @throws InvalidSettingsException if the settings cannot be loaded
     */
    public void loadSettings(final NodeSettingsRO settings) throws InvalidSettingsException {
        m_key1 = settings.getString(CFG_KEY1);
        m_key2 = settings.getString(CFG_KEY2);
        m_key3 = settings.getString(CFG_KEY3);
    }

    /**
     * Loads settings with defaults from node settings.
     * @param settings the node settings to load from
     */
    public void loadSettingsForDialog(final NodeSettingsRO settings) {
        m_key1 = settings.getString(CFG_KEY1, "");
        m_key2 = settings.getString(CFG_KEY2, "");
        m_key3 = settings.getString(CFG_KEY3, "");
    }
}
