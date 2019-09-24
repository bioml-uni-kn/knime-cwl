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

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * A CWL data type representing unions of types.
 * @author Martin Horn, Alexander Fillbrunn: University of Konstanz
 *
 */
public class CWLUnionType implements CWLDataType {
    
    private Set<CWLDataType> m_types;
    
    /**
     * Constructs a new instance of a CWL union type with a set of types.
     * @param types the types contained in the union
     */
    public CWLUnionType(final Set<CWLDataType> types) {
        m_types = Collections.unmodifiableSet(types);
    }
    
    /**
     * @return the types contained in the union
     */
    public Set<CWLDataType> getTypes() {
        return m_types;
    }
    
    /**
     * @return true if the union is nullable, i.e. contains the null type
     */
    public boolean containsNull() {
        return m_types.contains(CWLNullType.getInstance());
    }
    
    /**
     * Checks whether the union represents a nullable type, i.e. null and one other type.
     * @return the nullable type or null if this union does not represent a nullable type
     */
    public CWLDataType nullable() {
        if (m_types.size() == 2 && containsNull()) {
            for (CWLDataType dt : m_types) {
                if (dt.getType() != CWLType.NULL) {
                    return dt;
                }
            }
        }
        return null;
    }
    
    /**
     * Checks if the union contains a certain scalar type.
     * @param type the type to check
     * @return true if the type is contained in the union
     */
    public boolean containsType(final CWLType type) {
        for (CWLDataType dt : m_types) {
            if (dt.getType() == type) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Checks if the union contains a certain type.
     * @param type the type to check
     * @return true if the type is contained in the union
     */
    public boolean containsType(final CWLDataType type) {
        for (CWLDataType dt : m_types) {
            if (dt.equals(type)) {
                return true;
            }
        }
        return false;
    }
    
    @Override
    public boolean equals(final Object obj) {
        if (!(obj instanceof CWLUnionType)) {
            return false;
        }
        CWLUnionType other = (CWLUnionType)obj;
        return other.getTypes().equals(getTypes());
    }
    
    @Override
    public int hashCode() {
        return 31 * m_types.hashCode();
    }
    
    @Override
    public String toString() {
        return String.format("UNION(%s)",
                String.join(", ", m_types.stream().map(t -> t.toString()).collect(Collectors.toList())));
    }
    
    @Override
    public CWLType getType() {
        return CWLType.UNION;
    }
    
    @Override
    public String toHumanReadableString() {
        CWLDataType nullable = nullable();
        if (nullable != null) {
            return String.format("Optional %s", nullable.toHumanReadableString());
        } else {
            return String.format("One of %s",
                String.join(", ", m_types.stream().map(t -> t.toHumanReadableString()).collect(Collectors.toList())));
        }
    }
    
    @Override
    public boolean isOptional() {
        return containsNull();
    }
}
