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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.knime.core.node.NodeLogger;
import org.yaml.snakeyaml.Yaml;

import de.unikn.knime.cwl.preferences.Preferences;

/**
 * Registry for CWL files in the folders specified in the preferences.
 * @author Martin Horn, Alexander Fillbrunn: University of Konstanz
 *
 */
public final class CWLRegistry {
    
    private static final NodeLogger LOGGER = NodeLogger.getLogger(CWLRegistry.class);
    
    private CWLRegistry() { }
    
    // Private class that will only be initialized on first access by getInstance().
    // Implicitely synchronized by class loader.
    private static final class InstanceHolder {
      static final CWLRegistry INSTANCE = new CWLRegistry();
    }
    
    /**
     * @return the singleton instance for the CWL registry
     */
    public static CWLRegistry getInstance() {
        return InstanceHolder.INSTANCE;
    }
    
    /**
     * @return the folder containing the CWL files to load
     */
    public static String[] getCWLFolders() {
        return Preferences.getCWLRootPaths();
    }
    
    private Map<String, String> m_files = null;
    
    /**
     * Reads all registered CWL directories and builds a map with MD5 hashes as keys and file paths as values.
     * @return a map with MD5 hashes of the files as keys and the file paths as values.
     */
    private Map<String, String> getNamedFiles() {
        if (m_files == null) {
            m_files = new HashMap<>();
            for (String folder : getCWLFolders()) {
                File root = new File(folder);
                if (root.exists()) {
                    File[] cwls = root.listFiles((dir, name) -> name.endsWith(".cwl"));
                    for (File cwl : cwls) {
                        try {
                            String md5 = getFileID(cwl);
                            m_files.put(md5, cwl.getAbsolutePath());
                        } catch (IOException | NoSuchAlgorithmException e) {
                            LOGGER.error("Could not load CWL file " + cwl.getAbsolutePath());
                            LOGGER.error(e);
                        }
                    }
                } else {
                    LOGGER.warn(String.format("Configured CWL folder \"%s\" does not exist.", root.getAbsolutePath()));
                }
            }
        }
        return m_files;
    }
    
    private String getFileID(final File f) throws IOException, NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("MD5");
        String id = null;
        try (InputStream is = Files.newInputStream(Paths.get(f.toURI()));
             DigestInputStream dis = new DigestInputStream(is, md)) {
            /* Read decorated stream (dis) to EOF as normal... */
            Yaml cwlYaml = new Yaml();
            Map<String, Object> cwl = cwlYaml.load(dis);
            id = (String)cwl.get("id");
        }
        if (id == null) {
            byte[] digest = md.digest();
            BigInteger bigInt = new BigInteger(1, digest);
            id = bigInt.toString(16);
        }
        return id;
    }
    
    /**
     * @return a set of hashes of the registered CWL files.
     */
    public Set<String> getHashes() {
        return Collections.unmodifiableSet(getNamedFiles().keySet());
    }
    
    /**
     * Retrieves a file path for a file's MD5 hash.
     * @param hash the hash of the file to retrieve
     * @return the file path matching the hash
     */
    public String getPathForHash(final String hash) {
        return getNamedFiles().get(hash);
    }
}
