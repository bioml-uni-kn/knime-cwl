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
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

import javax.json.Json;
import javax.json.JsonValue;

import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;

import de.unikn.knime.cwl.dynode.port.CWLPortDescription;
import de.unikn.knime.cwl.dynode.port.CWLPortObject;
import de.unikn.knime.cwl.dynode.port.CWLPortObjectSpec;
import de.unikn.knime.cwl.execution.CWLExecutor;
import de.unikn.knime.cwl.execution.CWLExecutor.CWLExecutionResult;

/**
 * Generic node model for CWL tools.
 * @author Martin Horn, Alexander Fillbrunn: University of Konstanz
 *
 */
public class CWLNodeModel extends NodeModel {

    private CWLNodeSettings m_settings;
    private String m_file;
    private CWLPortDescription[] m_inputs;
    private CWLPortDescription[] m_outputs;
    
    /**
     * Creates a new {@code CWLNodeModel} with the given number of inputs and outputs.
     * @param file the file to execute
     * @param inputs the inputs of the node
     * @param outputs the outputs of the node
     */
    public CWLNodeModel(final String file, final CWLPortDescription[] inputs, final CWLPortDescription[] outputs) {
        super(createPortTypesArray(inputs, true), createPortTypesArray(outputs, false));
        m_file = file;
        m_inputs = inputs;
        m_outputs = outputs;
        m_settings = new CWLNodeSettings(m_inputs.length);
    }
    
    @Override
    protected PortObject[] execute(final PortObject[] inObjects, final ExecutionContext exec) throws Exception {
        // Prepare inputs for the tool execution
        Map<String, JsonValue> inputs = new HashMap<>();
        for (int i = 0; i < inObjects.length; i++) {
            CWLPortObject po = (CWLPortObject)inObjects[i];
            if (po != null) {
                inputs.put(m_inputs[i].getName(), po.getJsonContent());
            } else if (m_settings.getEnabledInputs().length > 0 && m_settings.getEnabledInputs()[i]) {
                String val = m_settings.getInputs()[i];
                // TODO: How to get a JsonValue from a string?
                JsonValue jsonVal = Json.createReader(
                        new StringReader("{ \"content\": " + val + "}")).readObject().get("content");
                inputs.put(m_inputs[i].getName(), jsonVal);
            }
        }
        
        // Execute with configured runner
        CWLExecutionResult res = CWLExecutor.execute(m_file, inputs, m_settings.getExtraCWLArgs());
        
        PortObject[] outputs = new PortObject[m_outputs.length];
        for (int i = 0; i < m_outputs.length; i++) {
            outputs[i] = CWLPortObject.createOutput(m_outputs[i].getType().getType(),
                    res.getOutputJson().get(m_outputs[i].getName()));
        }
        
        return outputs;
    }

    @Override
    protected PortObjectSpec[] configure(final PortObjectSpec[] inSpecs) throws InvalidSettingsException {
        PortObjectSpec[] outSpecs = new PortObjectSpec[m_outputs.length];
        for (int i = 0; i < m_outputs.length; i++) {
            outSpecs[i] = new CWLPortObjectSpec(m_outputs[i].getType().getType());
        }
        
        // Check if there is a value for every input, either from a port or from the settings.
        for (int i = 0; i < inSpecs.length; i++) {
            if (!m_inputs[i].getType().isOptional() && inSpecs[i] == null && !m_settings.getEnabledInputs()[i]) {
                throw new InvalidSettingsException(
                        String.format("No value given for input %s (input %d)", m_inputs[i].getName(), i));
            }
        }
        return outSpecs;
    }
    
    @Override
    protected void loadInternals(final File nodeInternDir, final ExecutionMonitor exec)
            throws IOException, CanceledExecutionException {
        
    }

    @Override
    protected void saveInternals(final File nodeInternDir, final ExecutionMonitor exec)
            throws IOException, CanceledExecutionException {
        
    }

    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings) {
        m_settings.saveSettings(settings);
    }

    @Override
    protected void validateSettings(final NodeSettingsRO settings) throws InvalidSettingsException {
        CWLNodeSettings s = new CWLNodeSettings(m_inputs.length);
        s.loadSettings(settings);
    }

    @Override
    protected void loadValidatedSettingsFrom(final NodeSettingsRO settings) throws InvalidSettingsException {
        m_settings.loadSettings(settings);
    }

    @Override
    protected void reset() {
    }

    private static final PortType[] createPortTypesArray(final CWLPortDescription[] ports, final boolean optional) {
        PortType[] types = new PortType[ports.length];
        for (int i = 0; i < ports.length; i++) {
            types[i] = CWLPortObject.getPortType(ports[i].getType(), optional);
        }
        return types;
    }
}
