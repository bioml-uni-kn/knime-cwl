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
package de.unikn.knime.cwl.dynode.data.types;

/**
 * A simple CWL data type.
 * @author Martin Horn, Alexander Fillbrunn: University of Konstanz
 *
 */
public class CWLSimpleDataType implements CWLDataType {
    
    private static final String STDOUT = "stdout";
    private static final String STDERR = "stderr";
    
    private CWLType m_type;
    
    /**
     * Default constructor for implementing classes.
     * @param type the type represented by the class
     */
    protected CWLSimpleDataType(final CWLType type) {
        m_type = type;
    }
    
    /**
     * @return the type represented by the class
     */
    public CWLType getType() {
        return m_type;
    }
    
    @Override
    public boolean equals(final Object obj) {
        if (!(obj instanceof CWLSimpleDataType)) {
            return false;
        }
        return m_type == ((CWLSimpleDataType)obj).getType();
    }
    
    @Override
    public int hashCode() {
        return m_type.hashCode();
    }
    
    @Override
    public String toString() {
        return m_type.toString();
    }
    
    /**
     * Resolves a type from a string representation.
     * @param symbol the string representation of the type
     * @return the type object
     */
    public static CWLSimpleDataType fromSymbol(final String symbol) {
        // stdout is a special type. It ends up in a file.
        if (symbol.equals(STDOUT) || symbol.equals(STDERR)) {
            return CWLFileType.getInstance();
        }
        CWLType type = CWLType.get(symbol);
        /* This is not really correct here.
         * Inserted because of type imports, which cannot be handled right now.
         */
        if (type == null) {
            return CWLAnyType.getInstance();
        }
        switch (type) {
        case ANY:
            return CWLAnyType.getInstance();
        case NULL:
            return CWLNullType.getInstance();
        case BOOLEAN:
            return CWLBooleanType.getInstance();
        case INT:
            return CWLIntegerType.getInstance();
        case LONG:
            return CWLLongType.getInstance();
        case FLOAT:
            return CWLFloatType.getInstance();
        case DOUBLE:
            return CWLDoubleType.getInstance();
        case STRING:
            return CWLStringType.getInstance();
        case FILE:
            return CWLFileType.getInstance();
        case DIRECTORY:
            return CWLDirectoryType.getInstance();
            default:
                return null;
        }
    }
    
    @Override
    public String toHumanReadableString() {
        String s = toString();
        return s.substring(0, 1).toUpperCase() + s.substring(1).toLowerCase();
    }
}
