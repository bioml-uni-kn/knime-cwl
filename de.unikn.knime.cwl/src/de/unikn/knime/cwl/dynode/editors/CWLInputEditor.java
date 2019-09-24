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

import org.knime.core.node.config.ConfigRO;
import org.knime.core.node.config.ConfigWO;

/**
 * An editor for an input value for a CWL file.
 * @author Martin Horn, Alexander Fillbrunn: University of Konstanz
 *
 */
public interface CWLInputEditor {

    /**
     * @return the value as a JSON string that can be pasted into the input YAML.
     */
    public String getJSONStringValue();
    
    /**
     * @param jsonStr the value as a JSON string
     */
    public void setStringValue(final String jsonStr);
    
    /**
     * @return the component for the UI.
     */
    public Component getComponent();
    
    /**
     * Enables or disables the component.
     * @param enabled if true, the control is enabled, otherwise disabled
     */
    public default void setEnabled(boolean enabled) {
        getComponent().setEnabled(enabled);
    }
    
    /**
     * Save additional settings for the editor.
     * @param settings the settings to save to
     */
    public default void saveAdditionalSettings(final ConfigWO settings) {
        // Default: Nothing
    }
    
    /**
     * Load additional settings for the editor.
     * @param settings the settings to load from
     */
    public default void loadAdditionalSettings(final ConfigRO settings) {
        // Default: Nothing
    }
}
