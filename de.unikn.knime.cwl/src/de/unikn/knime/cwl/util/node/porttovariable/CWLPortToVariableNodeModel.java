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
package de.unikn.knime.cwl.util.node.porttovariable;

import java.io.File;
import java.io.IOException;
import java.util.Map.Entry;

import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonString;
import javax.json.JsonValue;

import org.apache.commons.lang3.StringUtils;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;
import org.knime.core.node.port.flowvariable.FlowVariablePortObject;
import org.knime.core.node.port.flowvariable.FlowVariablePortObjectSpec;

import de.unikn.knime.cwl.dynode.data.types.CWLType;
import de.unikn.knime.cwl.dynode.port.CWLPortObject;

/**
 * Node model for the CWL Port to Variable node.
 * 
 * @author Martin Horn, Alexander Fillbrunn: University of Konstanz
 */
public class CWLPortToVariableNodeModel extends NodeModel {

    /**
     * Creates a new instance of {@code CreateRecordNodeModel}.
     */
    public CWLPortToVariableNodeModel() {
        super(new PortType[] {CWLPortObject.TYPE},
                new PortType[] {FlowVariablePortObject.TYPE});
    }
    
    /**
     * @return a settings model describing the setting for the output column name
     */
    public static final SettingsModelString createVariableNameSettingsModel() {
        return new SettingsModelString("variableName", "cwl-value");
    }
    
    private SettingsModelString m_varName = createVariableNameSettingsModel();

    /**
     * {@inheritDoc}
     */
    @Override
    protected PortObject[] execute(final PortObject[] inData, final ExecutionContext exec) throws Exception {
        CWLPortObject cwl = (CWLPortObject)inData[0];
        JsonValue content = cwl.getJsonContent();
        String varName = m_varName.getStringValue();
        // Flow vars can only be Double, Integer, or String
        if (cwl.getType() == CWLType.DOUBLE || cwl.getType() == CWLType.LONG || cwl.getType() == CWLType.FLOAT) {
            pushFlowVariableDouble(varName, ((JsonNumber)content).doubleValue());
        } else if (cwl.getType() == CWLType.INT) {
            pushFlowVariableInt(varName, ((JsonNumber)content).intValue());
        } else if (cwl.getType() == CWLType.STRING) {
            pushFlowVariableString(varName, ((JsonString)content).getString());
        } else if (cwl.getType() == CWLType.DIRECTORY || cwl.getType() == CWLType.FILE
                || cwl.getType() == CWLType.RECORD) {
            JsonObject o = (JsonObject)content;
            for (Entry<String, JsonValue> e : o.entrySet()) {
                String key = String.format("%s.%s", varName, e.getKey());
                JsonValue val = e.getValue();
                // Ignore class field for directory and file ports
                if (cwl.getType() != CWLType.RECORD && key.equals("class")) {
                    continue;
                }
                if (val instanceof JsonNumber) {
                    pushFlowVariableDouble(key, ((JsonNumber)val).doubleValue());
                } else if (val instanceof JsonString) {
                    pushFlowVariableString(key, ((JsonString)val).getString());
                } else {
                    pushFlowVariableString(key, val.toString());
                }
            }
        } else {
            pushFlowVariableString(varName, content.toString());
        }
        return new PortObject[] {FlowVariablePortObject.INSTANCE};
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void reset() {
        // No-op
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected PortObjectSpec[] configure(final PortObjectSpec[] inSpecs) throws InvalidSettingsException {
        if (StringUtils.isBlank(m_varName.getStringValue())) {
            throw new InvalidSettingsException("No variable name given");
        }
        return new PortObjectSpec[] {FlowVariablePortObjectSpec.INSTANCE};
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings) {
        m_varName.saveSettingsTo(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadValidatedSettingsFrom(final NodeSettingsRO settings) throws InvalidSettingsException {
        m_varName.loadSettingsFrom(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateSettings(final NodeSettingsRO settings) throws InvalidSettingsException {
        m_varName.validateSettings(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadInternals(final File internDir, final ExecutionMonitor exec)
            throws IOException, CanceledExecutionException {
        // No-op
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveInternals(final File internDir, final ExecutionMonitor exec)
            throws IOException, CanceledExecutionException {
        // No-op
    }

}
