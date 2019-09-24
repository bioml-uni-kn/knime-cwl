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

import java.awt.BorderLayout;
import java.awt.Component;
import java.io.File;
import java.io.StringReader;
import java.nio.file.Paths;

import javax.json.Json;
import javax.json.JsonObject;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * Editor for CWL file inputs.
 * @author Martin Horn, Alexander Fillbrunn: University of Konstanz
 *
 */
public class CWLFileSystemEditor implements CWLInputEditor {

    private JPanel m_panel;
    private JTextField m_path;
    private JButton m_browseBtn;
    private String m_class;
    /**
     * Creates a new instance of a {@code CWLFileEditor}.
     * @param cl the class for the output JSON (either "File" or "Directory"
     * @param fileSelectionMode the file selection mode,
     *  either {@code JFileChooser.FILES_ONLY} or {@code JFileChooser.DIRECTORIES_ONLY}
     */
    public CWLFileSystemEditor(final String cl, final int fileSelectionMode) {
        m_class = cl;
        m_panel = new JPanel(new BorderLayout());
        m_path = new JTextField();
        m_browseBtn = new JButton("Browse");
        m_browseBtn.addActionListener(e -> {
            String current = m_path.getText().trim();
            JFileChooser j;
            if (current.length() > 0) {
                File dir = Paths.get(current).getParent().toFile();
                j = new JFileChooser(dir);
            } else {
                j = new JFileChooser();
            }
            j.setFileSelectionMode(fileSelectionMode);
            if (j.showDialog(null, "Choose") == JFileChooser.APPROVE_OPTION) {
                m_path.setText(j.getSelectedFile().getAbsolutePath());
            }
        });
        m_panel.add(m_path, BorderLayout.CENTER);
        m_panel.add(m_browseBtn, BorderLayout.EAST);
    }
    
    @Override
    public String getJSONStringValue() {
        return Json.createObjectBuilder()
                .add("class", m_class)
                .add("path", m_path.getText())
                .build().toString();
    }

    @Override
    public void setStringValue(final String jsonStr) {
        JsonObject obj = Json.createReader(new StringReader(jsonStr)).readObject();
        m_path.setText(obj.getString("path"));
    }

    @Override
    public Component getComponent() {
        return m_panel;
    }

    @Override
    public void setEnabled(final boolean enabled) {
        m_path.setEnabled(enabled);
        m_browseBtn.setEnabled(enabled);
    }
}
