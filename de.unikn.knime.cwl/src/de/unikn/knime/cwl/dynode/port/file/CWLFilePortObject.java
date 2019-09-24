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
package de.unikn.knime.cwl.dynode.port.file;

import java.io.IOException;
import java.util.zip.ZipEntry;

import javax.json.Json;
import javax.json.JsonException;
import javax.json.JsonObject;
import javax.json.JsonString;
import javax.json.JsonValue;

import org.knime.core.node.port.PortObjectZipInputStream;
import org.knime.core.node.port.PortType;
import org.knime.core.node.port.PortTypeRegistry;

import de.unikn.knime.cwl.dynode.data.types.CWLType;
import de.unikn.knime.cwl.dynode.port.CWLPortObject;

/**
 * Port object for files.
 * @author Martin Horn, Alexander Fillbrunn: University of Konstanz
 *
 */
public class CWLFilePortObject extends CWLPortObject {

    /** Convenience accessor for the port type. */
    public static final PortType TYPE = PortTypeRegistry.getInstance().getPortType(CWLFilePortObject.class);
    
    /** Convenience accessor for the optional port type. */
    public static final PortType OPTIONAL_TYPE =
            PortTypeRegistry.getInstance().getPortType(CWLFilePortObject.class, true);
    
    /**
     * Creates a new instance of {@code CWLFilePortObject}.
     * @param content the JSON content, an object with path and class: "File" properties.
     */
    protected CWLFilePortObject(final JsonObject content) {
        super(CWLType.FILE, content);
    }
    
    /**
     * Loads the CWLPortObject from an input stream.
     * 
     * @param in the InputStream to load the JSON from
     * @return a CWLPortObject that is initialized from the JSON in the given stream
     * @throws IOException when the document cannot be loaded
     * @throws JsonException when the document's JSON cannot be parsed
     */
    public static CWLFilePortObject loadFromJson(final PortObjectZipInputStream in) throws IOException, JsonException {
        ZipEntry content = in.getNextEntry();
        assert content.getName().equals(CONTENT_KEY);
        return new CWLFilePortObject((JsonObject)Json.createReader(in).readObject().get("content"));
    }
    
    /**
     * Creates a new instance of {@code CWLFilePortObject} from a file path.
     * @param path the path to the file
     * @return a {@code CWLFilePortObject} pointing to the given path
     */
    public static CWLFilePortObject fromPath(final String path) {
        return new CWLFilePortObject(Json.createObjectBuilder()
                .add("path", path)
                .add("class", "File").build());
    }
    
    /**
     * Creates a new instance of {@code CWLFilePortObject} from a {@code JsonValue}.
     * Converts strings to a file object and otherwise checks if the passed JsonValue is a JsonObject
     * with "class": "File".
     * @param val the value representing the port
     * @return a new {@code CWLFilePortObject} with the data
     * @throws IllegalArgumentException when the given JsonValue cannot be parsed
     */
    public static CWLFilePortObject fromJsonValue(final JsonValue val) {
        if (val instanceof JsonString) {
            return CWLFilePortObject.fromPath(((JsonString) val).getString());
        } else if (val instanceof JsonObject) {
            JsonObject o = (JsonObject)val;
            if (!o.containsKey("class") || !o.getString("class").equals("File")) {
                throw new IllegalArgumentException("A file object must have a field named"
                                                    + " \"class\" with value \"File\".");
            }
            return new CWLFilePortObject(o);
        } else {
            throw new IllegalArgumentException("Can only create a CWLDirectoryPortObject from a string or object.");
        }
    }
}
