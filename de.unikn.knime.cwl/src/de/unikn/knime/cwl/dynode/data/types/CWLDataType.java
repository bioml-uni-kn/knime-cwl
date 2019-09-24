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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

/**
 * A data type for data being passed between CWL tools.
 * @author Martin Horn, Alexander Fillbrunn: University of Konstanz
 *
 */
public interface CWLDataType {
    
    /** YAML/JSON key for the type property. */
    static final String TYPE_KEY = "type";
    /** YAML/JSON key for the enum symbols property. */
    static final String ENUM_SYMBOLS_KEY = "symbols";
    /** YAML/JSON key for the record fields property. */
    static final String RECORD_FIELDS_KEY = "fields";
    /** YAML/JSON key for the array items property. */
    static final String ARRAY_ITEMS_KEY = "items";
    
    /**
     * @return the data type enum value for this type.
     */
    public CWLType getType();
    
    /**
     * Creates a data type from a YAML type object.
     * @param o the object representing the type
     * @return a CWLDataType for the given object
     */
    public static CWLDataType fromYAMLObject(final Object o) {
        if (o instanceof String) {
            String s = ((String)o);
            return CWLDataType.fromString(s);
        } else if (o instanceof Map<?, ?>) {
            // Complex type or type object
            Map<?, ?> om = (Map<?, ?>) o;
            final Object type = om.get(TYPE_KEY);
            if (type instanceof String) {
                if (CWLType.get((String)type) == CWLType.ARRAY) {
                    Object items = om.get(ARRAY_ITEMS_KEY);
                    return new CWLArrayType(fromYAMLObject(items));
                } else if (CWLType.get((String)type) == CWLType.RECORD) {
                    Object fieldsObj = om.get(RECORD_FIELDS_KEY);
                    Map<String, CWLDataType> recordFields = new HashMap<>();
                    if (fieldsObj instanceof Map<?, ?>) {
                        Map<?, ?> fields = (Map<?, ?>) fieldsObj;
                        for (Entry<?, ?> e : fields.entrySet()) {
                            String key = (String)e.getKey();
                            CWLDataType tp = fromYAMLObject(e.getValue());
                            recordFields.put(key, tp);
                        }
                    } else {
                        List<?> fields = (List<?>)fieldsObj;
                        for (Object f : fields) {
                            recordFields.put(f.toString(), CWLAnyType.getInstance());
                        }
                    }
                    return new CWLRecordType(recordFields);
                } else if (CWLType.get((String)type) == CWLType.ENUM) {
                    List<?> symbols = (List<?>)om.get(ENUM_SYMBOLS_KEY);
                    return new CWLEnumType(symbols.stream().map(s -> (String)s).collect(Collectors.toList()));
                }
                return fromYAMLObject(om.get(TYPE_KEY));
            } else {
                return fromYAMLObject(type);
            }
        } else if (o instanceof List<?>) {
            // Union type
            List<?> l = (List<?>)o;
            return new CWLUnionType(l.stream().map(t -> fromYAMLObject(t)).collect(Collectors.toSet()));
        } else {
            throw new IllegalArgumentException("Unsupported type for conversion to CWL type");
        }
    }
    
    /**
     * Creates a data type from a string representation.
     * @param s the string representing the CWL data type
     * @return a data type object
     */
    public static CWLDataType fromString(final String s) {
        if (s.endsWith("?")) {
            // Nullables are represented by union sets with null type and one other type
            String type = s.substring(0, s.length() - 1);
            List<CWLDataType> types = new ArrayList<>();
            types.add(fromString(type));
            types.add(CWLNullType.getInstance());
            return new CWLUnionType(new HashSet<>(types));
        } else if (s.endsWith("[]")) {
            String items = s.substring(0, s.length() - 2);
            return new CWLArrayType(CWLDataType.fromString(items));
        } else {
            return CWLSimpleDataType.fromSymbol(s);
        }
    }
    
    /**
     * @return a string representation of the data type for usage in a UI
     */
    public String toHumanReadableString();

    /**
     * @return whether the type represents a nullable, i.e. optional type
     */
    public default boolean isOptional() {
        return false;
    }
}
