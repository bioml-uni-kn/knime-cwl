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
package de.unikn.knime.cwl.preferences;

import java.io.File;

import org.eclipse.core.runtime.Platform;

import de.unikn.knime.cwl.Activator;

/**
 * Convenience accessors and constants for the preferences of the CWL plugin.
 * @author Martin Horn, Alexander Fillbrunn: University of Konstanz
 *
 */
public final class Preferences {
    
    private Preferences() { }
    
    /** Config key for the path to the CWL runner executable. */
    public static final String CWL_RUNNER_PATH = "CWL_RUNNER_PATH";
    
    /** Config key for the path to the CWL directory. */
    public static final String CWL_DIR_PATH = "CWL_DIR_PATH";
    
    /** Config key for extra arguments to be passed to the cwl-runner. */
    public static final String CWL_EXTRA_ARGS = "CWL_EXTRA_ARGS";
    
    /**
     * Reads the currently configured CWL executable path from the preferences.
     * @return the configured path to the cwl-runner executable
     */
    public static String getCWLRunnerPath() {
        return Platform.getPreferencesService()
                .getString(Activator.PLUGIN_ID, CWL_RUNNER_PATH, "cwl-runner", null);
    }
    
    /**
     * Reads the currently configured CWL arguments from the preferences. 
     * @return extra arguments for the cwl-runner executable
     */
    public static String getExtraArgs() {
        return Platform.getPreferencesService()
                .getString(Activator.PLUGIN_ID, CWL_EXTRA_ARGS, "", null);
    }
    
    /**
     * Reads the currently configured CWL paths from the preferences.
     * @return an array of paths that are configured for reading CWL files from
     */
    public static String[] getCWLRootPaths() {
        return Platform.getPreferencesService()
                .getString(Activator.PLUGIN_ID, CWL_DIR_PATH, "", null)
                .split(File.pathSeparator);
    }
}
