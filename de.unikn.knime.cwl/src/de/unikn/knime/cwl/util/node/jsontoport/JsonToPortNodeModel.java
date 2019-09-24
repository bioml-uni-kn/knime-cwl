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
package de.unikn.knime.cwl.util.node.jsontoport;

import java.io.File;
import java.io.IOException;

import org.apache.commons.lang3.StringUtils;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataType;
import org.knime.core.data.container.CloseableRowIterator;
import org.knime.core.data.json.JSONCell;
import org.knime.core.data.json.JSONValue;
import org.knime.core.node.BufferedDataTable;
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

import de.unikn.knime.cwl.dynode.data.types.CWLType;
import de.unikn.knime.cwl.dynode.port.CWLPortObject;
import de.unikn.knime.cwl.dynode.port.CWLPortObjectSpec;

/**
 * Node model for the Json to CWL Port node.
 * 
 * @author Martin Horn, Alexander Fillbrunn: University of Konstanz
 */
public class JsonToPortNodeModel extends NodeModel {

    private CWLType m_type;
    
    /**
     * Creates a new instance of {@code CreateRecordNodeModel}.
     * @param type the type of data handled by this node.
     */
    public JsonToPortNodeModel(final CWLType type) {
        super(new PortType[] {BufferedDataTable.TYPE},
                new PortType[] {CWLPortObject.getPortType(type, false)});
        m_type = type;
    }
    
    /**
     * @return a new settings model for the configured JSON column name.
     */
    public static SettingsModelString createColumnNameSettingsModel() {
        return new SettingsModelString("columnName", null);
    }
    
    private SettingsModelString m_colName = createColumnNameSettingsModel();

    /**
     * {@inheritDoc}
     */
    @Override
    protected PortObject[] execute(final PortObject[] inData, final ExecutionContext exec) throws Exception {
        BufferedDataTable table = (BufferedDataTable)inData[0];
        try (CloseableRowIterator iter = table.iterator()) {
            if (!iter.hasNext()) {
                throw new InvalidSettingsException("The given table is empty.");
            }
            DataRow row = iter.next();
            int cellIdx = table.getSpec().findColumnIndex(m_colName.getStringValue());
            JSONCell cell = (JSONCell)row.getCell(cellIdx);
            if (iter.hasNext()) {
                setWarningMessage("Table contains more than one row. Only value in first row is converted.");
            }
            return new PortObject[] {CWLPortObject.createOutput(m_type, cell.getJsonValue())};
        }
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
        if (StringUtils.isEmpty(m_colName.getStringValue())) {
            // Find a suitable column
            boolean found = false;
            DataTableSpec spec = (DataTableSpec)inSpecs[0];
            for (int i = 0; i < spec.getNumColumns(); i++) {
                DataColumnSpec cspec = spec.getColumnSpec(i);
                DataType type = cspec.getType();
                if (type.isCompatible(JSONValue.class)) {
                    m_colName.setStringValue(cspec.getName());
                    setWarningMessage(String.format("No JSON column configured. Using %s.", cspec.getName()));
                    found = true;
                    break;
                }
            }
            if (!found) {
                throw new InvalidSettingsException("No compatible JSON column found in input table.");
            }
        }
        return new PortObjectSpec[] {new CWLPortObjectSpec(m_type)};
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings) {
        m_colName.saveSettingsTo(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadValidatedSettingsFrom(final NodeSettingsRO settings) throws InvalidSettingsException {
        m_colName.loadSettingsFrom(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateSettings(final NodeSettingsRO settings) throws InvalidSettingsException {
        m_colName.validateSettings(settings);
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
