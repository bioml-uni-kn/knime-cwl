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

import javax.swing.JFileChooser;

import de.unikn.knime.cwl.dynode.data.types.CWLArrayType;
import de.unikn.knime.cwl.dynode.data.types.CWLDataType;
import de.unikn.knime.cwl.dynode.data.types.CWLEnumType;
import de.unikn.knime.cwl.dynode.data.types.CWLType;
import de.unikn.knime.cwl.dynode.data.types.CWLUnionType;

/**
 * Factory for CWL input editors.
 * @author Martin Horn, Alexander Fillbrunn: University of Konstanz
 *
 */
public final class CWLEditorFactory {
    
    private CWLEditorFactory() { }
    
    // Private class that will only be initialized on first access by getInstance().
    // Implicitely synchronized by class loader.
    private static final class InstanceHolder {
      static final CWLEditorFactory INSTANCE = new CWLEditorFactory();
    }
    
    /**
     * @return the singleton instance of this factory.
     */
    public static CWLEditorFactory getInstance() {
        return InstanceHolder.INSTANCE;
    }
    
    /**
     * Creates an editor for inputs of the given type.
     * @param type the type to get an editor for.
     * @return a {@link CWLInputEditor} instance for the given type
     */
    public CWLInputEditor getEditorForType(final CWLDataType type) {
        if (type instanceof CWLUnionType) {
            CWLUnionType union = (CWLUnionType)type;
            CWLDataType nullable = union.nullable();
            if (nullable != null) {
                return getEditorForType(nullable);
            }
            return new CWLUnionEditor(union);
        } else if (type.getType() == CWLType.FILE) {
            return new CWLFileSystemEditor("File", JFileChooser.FILES_ONLY);
        } else if (type.getType() == CWLType.DIRECTORY) {
            return new CWLFileSystemEditor("Directory", JFileChooser.DIRECTORIES_ONLY);
        } else if (type.getType() == CWLType.STRING) {
            return new CWLStringEditor();
        } else if (type.getType() == CWLType.BOOLEAN) {
            return new CWLBooleanEditor();
        } else if (type.getType() == CWLType.NULL) {
            return new CWLNullEditor();
        } else if (type.getType() == CWLType.ARRAY) {
            return new CWLArrayEditor(((CWLArrayType)type).getItemType());
        } else if (type.getType() == CWLType.DOUBLE || type.getType() == CWLType.FLOAT) {
            return new CWLNumberEditor(true);
        } else if (type.getType() == CWLType.LONG || type.getType() == CWLType.INT) {
            return new CWLNumberEditor(false);
        } else if (type.getType() == CWLType.ENUM) {
            return new CWLEnumEditor(((CWLEnumType)type).getSymbols());
        }
        return new DefaultCWLInputEditor();
    }
    
    /**
     * Creates an editor for inputs of the given type.
     * @param type the type to get an editor for.
     * @return a {@link CWLInputEditor} instance for the given type
     */
    public CWLInputEditor getEditorForType(final CWLType type) {
        if (type == CWLType.FILE) {
            return new CWLFileSystemEditor("File", JFileChooser.FILES_ONLY);
        } else if (type == CWLType.DIRECTORY) {
            return new CWLFileSystemEditor("Directory", JFileChooser.DIRECTORIES_ONLY);
        } else if (type == CWLType.STRING) {
            return new CWLStringEditor();
        } else if (type == CWLType.BOOLEAN) {
            return new CWLBooleanEditor();
        } else if (type == CWLType.NULL) {
            return new CWLNullEditor();
        } else if (type == CWLType.DOUBLE || type == CWLType.FLOAT) {
            return new CWLNumberEditor(true);
        } else if (type == CWLType.LONG || type == CWLType.INT) {
            return new CWLNumberEditor(false);
        }
        return new DefaultCWLInputEditor();
    }
}
