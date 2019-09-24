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
package de.unikn.knime.cwl.dynode.port;

import java.io.IOException;
import java.util.zip.ZipEntry;

import javax.json.Json;
import javax.json.JsonException;
import javax.json.JsonValue;
import javax.swing.JComponent;

import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortObjectZipInputStream;
import org.knime.core.node.port.PortObjectZipOutputStream;
import org.knime.core.node.port.PortType;
import org.knime.core.node.port.PortTypeRegistry;

import de.unikn.knime.cwl.dynode.data.types.CWLDataType;
import de.unikn.knime.cwl.dynode.data.types.CWLType;
import de.unikn.knime.cwl.dynode.data.types.CWLUnionType;
import de.unikn.knime.cwl.dynode.port.array.CWLArrayPortObject;
import de.unikn.knime.cwl.dynode.port.bool.CWLBoolPortObject;
import de.unikn.knime.cwl.dynode.port.dbl.CWLDoublePortObject;
import de.unikn.knime.cwl.dynode.port.dir.CWLDirectoryPortObject;
import de.unikn.knime.cwl.dynode.port.enm.CWLEnumPortObject;
import de.unikn.knime.cwl.dynode.port.file.CWLFilePortObject;
import de.unikn.knime.cwl.dynode.port.flt.CWLFloatPortObject;
import de.unikn.knime.cwl.dynode.port.integer.CWLIntPortObject;
import de.unikn.knime.cwl.dynode.port.lng.CWLLongPortObject;
import de.unikn.knime.cwl.dynode.port.record.CWLRecordPortObject;
import de.unikn.knime.cwl.dynode.port.string.CWLStringPortObject;

/**
 * A port object for data being passed between CWL tools.
 * @author Martin Horn, Alexander Fillbrunn: University of Konstanz
 *
 */
public class CWLPortObject implements PortObject {
    
    /**
     * Finds the port type for a certain CWL data type.
     * @param type the CWL data type for which to find a port type
     * @param optional whether the port should be optional
     * @return a PortType for ports containing content of the specified CWL type
     */
    public static final PortType getPortType(final CWLDataType type, final boolean optional) {
        CWLType t = type.getType();
        switch(t) {
        case FILE:
            return optional ? CWLFilePortObject.OPTIONAL_TYPE : CWLFilePortObject.TYPE;
        case DIRECTORY:
            return optional ? CWLDirectoryPortObject.OPTIONAL_TYPE : CWLDirectoryPortObject.TYPE;
        case BOOLEAN:
            return optional ? CWLBoolPortObject.OPTIONAL_TYPE : CWLBoolPortObject.TYPE;
        case INT:
            return optional ? CWLIntPortObject.OPTIONAL_TYPE : CWLIntPortObject.TYPE;
        case LONG:
            return optional ? CWLLongPortObject.OPTIONAL_TYPE : CWLLongPortObject.TYPE;
        case FLOAT:
            return optional ? CWLFloatPortObject.OPTIONAL_TYPE : CWLFloatPortObject.TYPE;
        case DOUBLE:
            return optional ? CWLDoublePortObject.OPTIONAL_TYPE : CWLDoublePortObject.TYPE;
        case STRING:
            return optional ? CWLStringPortObject.OPTIONAL_TYPE : CWLStringPortObject.TYPE;
        case RECORD:
            return optional ? CWLRecordPortObject.OPTIONAL_TYPE : CWLRecordPortObject.TYPE;
        case ARRAY:
            return optional ? CWLArrayPortObject.OPTIONAL_TYPE : CWLArrayPortObject.TYPE;
        case ENUM:
            return optional ? CWLEnumPortObject.OPTIONAL_TYPE : CWLEnumPortObject.TYPE;
        case UNION:
            CWLUnionType ut = (CWLUnionType)type;
            CWLDataType nullable = ut.nullable();
            if (optional && nullable != null) {
                // Since all inputs are optional anyways, we can deconstruct nullable types
                // and use the actual type as input port.
                return getPortType(nullable, optional);
            } else {
                return optional ? CWLPortObject.OPTIONAL_TYPE : CWLPortObject.TYPE;
            }
        default:
            return optional ? CWLPortObject.OPTIONAL_TYPE : CWLPortObject.TYPE;
        }
    }
    
    /**
     * Finds the port type for a certain CWL type enum.
     * @param type the CWL type for which to find a port type
     * @param optional whether the port should be optional
     * @return a PortType for ports containing content of the specified CWL type
     */
    public static final PortType getPortType(final CWLType type, final boolean optional) {
        switch(type) {
        case FILE:
            return optional ? CWLFilePortObject.OPTIONAL_TYPE : CWLFilePortObject.TYPE;
        case DIRECTORY:
            return optional ? CWLDirectoryPortObject.OPTIONAL_TYPE : CWLDirectoryPortObject.TYPE;
        case BOOLEAN:
            return optional ? CWLBoolPortObject.OPTIONAL_TYPE : CWLBoolPortObject.TYPE;
        case INT:
            return optional ? CWLIntPortObject.OPTIONAL_TYPE : CWLIntPortObject.TYPE;
        case LONG:
            return optional ? CWLLongPortObject.OPTIONAL_TYPE : CWLLongPortObject.TYPE;
        case FLOAT:
            return optional ? CWLFloatPortObject.OPTIONAL_TYPE : CWLFloatPortObject.TYPE;
        case DOUBLE:
            return optional ? CWLDoublePortObject.OPTIONAL_TYPE : CWLDoublePortObject.TYPE;
        case STRING:
            return optional ? CWLStringPortObject.OPTIONAL_TYPE : CWLStringPortObject.TYPE;
        case RECORD:
            return optional ? CWLRecordPortObject.OPTIONAL_TYPE : CWLRecordPortObject.TYPE;
        case ARRAY:
            return optional ? CWLArrayPortObject.OPTIONAL_TYPE : CWLArrayPortObject.TYPE;
        case ENUM:
            return optional ? CWLEnumPortObject.OPTIONAL_TYPE : CWLEnumPortObject.TYPE;
        default:
            return optional ? CWLPortObject.OPTIONAL_TYPE : CWLPortObject.TYPE;
        }
    }
    
    /**
     * Creates a port object wrapping the given CWL value of the given type.
     * @param t the object's type
     * @param val the object's value
     * @return a port object wrapping the value
     */
    public static final PortObject createOutput(final CWLType t, final JsonValue val) {
        switch(t) {
        case FILE:
            return CWLFilePortObject.fromJsonValue(val);
        case DIRECTORY:
            return CWLDirectoryPortObject.fromJsonValue(val);
        case BOOLEAN:
            return CWLBoolPortObject.fromJsonValue(val);
        case INT:
            return CWLIntPortObject.fromJsonValue(val);
        case LONG:
            return CWLLongPortObject.fromJsonValue(val);
        case FLOAT:
            return CWLFloatPortObject.fromJsonValue(val);
        case DOUBLE:
            return CWLDoublePortObject.fromJsonValue(val);
        case STRING:
            return CWLStringPortObject.fromJsonValue(val);
        case RECORD:
            return CWLRecordPortObject.fromJsonValue(val);
        case ARRAY:
            return CWLArrayPortObject.fromJsonValue(val);
        case ENUM:
            return CWLEnumPortObject.fromJsonValue(val);
        default:
            return new CWLPortObject(val);
        }
    }
    
    /** The key for the storage of the content. */
    protected static final String CONTENT_KEY = "content";
    
    /** Convenience accessor for the port type. */
    public static final PortType TYPE = PortTypeRegistry.getInstance().getPortType(CWLPortObject.class);
    
    /** Convenience accessor for the optional port type. */
    public static final PortType OPTIONAL_TYPE = PortTypeRegistry.getInstance().getPortType(CWLPortObject.class, true);

    private JsonValue m_content;
    private CWLPortObjectSpec m_spec;

    /**
     * Creates a new {@code CWLPortObject}.
     * @param content the JSON object representing the port's payload.
     */
    protected CWLPortObject(final JsonValue content) {
        m_content = content;
        // spec of the document
        m_spec = new CWLPortObjectSpec(CWLType.ANY);
    }
    
    /**
     * Creates a new {@code CWLPortObject}.
     * @param type the type of content stored in this port object
     * @param content the JSON object representing the port's payload.
     */
    protected CWLPortObject(final CWLType type, final JsonValue content) {
        m_content = content;
        // spec of the document
        m_spec = new CWLPortObjectSpec(type);
    }
    
    @Override
    public String getSummary() {
        String s = m_content.toString();
        if (s.length() > 20) {
            s = s.substring(0, 20) + "...";
        }
        return s;
    }
    
    /**
     * @return the type of content in this port object
     */
    public CWLType getType() {
        return m_spec.getType();
    }

    /**
     * @return a string representation of the JSON content
     */
    public String getJson() {
        return m_content.toString();
    }

    /**
     * Get the PFA document as a JsonObject.
     * 
     * @return The PFA object.
     */
    public JsonValue getJsonContent() {
        return m_content;
    }

    @Override
    public PortObjectSpec getSpec() {
        return m_spec;
    }

    @Override
    public JComponent[] getViews() {
        return new JComponent[] {new CWLPortObjectView(this)}; // null
    }

    /**
     * Writes the content to the output stream.
     * 
     * @param out the stream to save the document into
     * @throws IOException when the document cannot be saved.
     */
    public void saveTo(final PortObjectZipOutputStream out) throws IOException {
        ZipEntry content = new ZipEntry(CONTENT_KEY);
        out.putNextEntry(content);
        out.write(Json.createObjectBuilder().add("content", m_content).build().toString().getBytes());
    }
    
    /**
     * Loads the CWLPortObject from an input stream.
     * 
     * @param in the InputStream to load the JSON from
     * @return a CWLPortObject that is initialized from the JSON in the given stream
     * @throws IOException when the document cannot be loaded
     * @throws JsonException when the document's JSON cannot be parsed
     */
    public static CWLPortObject loadFromJson(final PortObjectZipInputStream in)
            throws IOException, JsonException {
        ZipEntry content = in.getNextEntry();
        assert content.getName().equals(CONTENT_KEY);
        return new CWLPortObject(Json.createReader(in).readObject().get("content"));
    }
}
