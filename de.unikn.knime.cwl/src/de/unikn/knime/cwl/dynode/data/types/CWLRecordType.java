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
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * A CWL data type representing records.
 * @author Martin Horn, Alexander Fillbrunn: University of Konstanz
 *
 */
public class CWLRecordType implements CWLDataType {
    
    private Map<String, CWLDataType> m_fields;
    
    /**
     * Constructs a new instance of a CWL record type with a set of fields.
     * @param fields the fields of the record
     */
    public CWLRecordType(final Map<String, CWLDataType> fields) {
        m_fields = Collections.unmodifiableMap(fields);
    }
    
    /**
     * @return the fields of the record
     */
    public Map<String, CWLDataType> getFields() {
        return m_fields;
    }
    
    @Override
    public boolean equals(final Object obj) {
        if (!(obj instanceof CWLRecordType)) {
            return false;
        }
        CWLRecordType other = (CWLRecordType)obj;
        return other.getFields().equals(getFields());
    }
    
    @Override
    public int hashCode() {
        return 31 * m_fields.hashCode();
    }
    
    @Override
    public String toString() {
        List<String> fields = StreamSupport.stream(
                m_fields.entrySet().spliterator(), false)
                .limit(5)
                .map(e -> String.format("%s : %s", e.getKey(), e.getValue()))
                .collect(Collectors.toList());
        if (m_fields.size() > 5) {
            fields.add("...");
        }
        return String.format("RECORD{%s}", String.join(", ", fields));
    }
    
    @Override
    public CWLType getType() {
        return CWLType.RECORD;
    }
    
    @Override
    public String toHumanReadableString() {
        List<String> fields = StreamSupport.stream(
                m_fields.entrySet().spliterator(), false)
                .limit(5)
                .map(e -> String.format("%s (%s)", e.getKey(), e.getValue()))
                .collect(Collectors.toList());

        return String.format("Record with keys: %s", String.join(", ", fields));
    }
}
