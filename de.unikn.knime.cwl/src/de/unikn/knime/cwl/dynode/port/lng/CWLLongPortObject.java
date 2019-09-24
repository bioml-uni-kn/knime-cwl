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
package de.unikn.knime.cwl.dynode.port.lng;

import java.io.IOException;
import java.util.zip.ZipEntry;

import javax.json.Json;
import javax.json.JsonException;
import javax.json.JsonNumber;
import javax.json.JsonString;
import javax.json.JsonValue;

import org.knime.core.node.port.PortObjectZipInputStream;
import org.knime.core.node.port.PortType;
import org.knime.core.node.port.PortTypeRegistry;

import de.unikn.knime.cwl.dynode.data.types.CWLType;
import de.unikn.knime.cwl.dynode.port.integer.CWLIntPortObject;

/**
 * Port object for files.
 * @author Martin Horn, Alexander Fillbrunn: University of Konstanz
 *
 */
public class CWLLongPortObject extends CWLIntPortObject {

    /** Convenience accessor for the port type. */
    public static final PortType TYPE = PortTypeRegistry.getInstance().getPortType(CWLLongPortObject.class);
    
    /** Convenience accessor for the optional port type. */
    public static final PortType OPTIONAL_TYPE =
            PortTypeRegistry.getInstance().getPortType(CWLLongPortObject.class, true);
    
    /**
     * Creates a new instance of {@code CWLLongPortObject}.
     * @param content the JSON content
     */
    protected CWLLongPortObject(final JsonNumber content) {
        super(CWLType.LONG, content);
    }

    /**
     * Loads the CWLPortObject from an input stream.
     * 
     * @param in the InputStream to load the JSON from
     * @return a CWLPortObject that is initialized from the JSON in the given stream
     * @throws IOException when the document cannot be loaded
     * @throws JsonException when the document's JSON cannot be parsed
     */
    public static CWLLongPortObject loadFromJson(final PortObjectZipInputStream in) throws IOException, JsonException {
        ZipEntry content = in.getNextEntry();
        assert content.getName().equals(CONTENT_KEY);
        return new CWLLongPortObject((JsonNumber)Json.createReader(in).readObject().get("content"));
    }
    
    /**
     * Creates a {@code CWLDoublePortObject} from a {@code JsonValue}.
     * Can handle {@code JsonNumber} and {@code JsonString}, if it can be parsed as long.
     * @param val the value to interpret as long number
     * @return A {@code CWLDoublePortObject} from the given JsonValue
     * @throws IllegalArgumentException when the given JsonValue cannot be parsed
     * @throws NumberFormatException when the given JsonValue is a string that cannot be parsed to long
     */
    public static CWLLongPortObject fromJsonValue(final JsonValue val) {
        if (val instanceof JsonNumber) {
            return new CWLLongPortObject((JsonNumber)val);
        } else if (val instanceof JsonString) {
            double d  = Long.parseLong(((JsonString)val).getString());
            JsonValue doubleVal = Json.createObjectBuilder().add("val", d).build().get("val");
            return new CWLLongPortObject((JsonNumber)doubleVal);
        }
        throw new IllegalArgumentException("Can only create a CWLLongPortObject from a string or number.");
    }
}
