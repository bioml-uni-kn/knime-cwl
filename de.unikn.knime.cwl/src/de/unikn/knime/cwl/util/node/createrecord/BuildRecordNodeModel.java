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
package de.unikn.knime.cwl.util.node.createrecord;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

import org.apache.commons.lang3.StringUtils;
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

import de.unikn.knime.cwl.dynode.data.types.CWLType;
import de.unikn.knime.cwl.dynode.port.CWLPortObject;
import de.unikn.knime.cwl.dynode.port.CWLPortObjectSpec;
import de.unikn.knime.cwl.dynode.port.record.CWLRecordPortObject;

/**
 * Node model for the Build CWL Record node.
 * 
 * @author Martin Horn, Alexander Fillbrunn: University of Konstanz
 */
public class BuildRecordNodeModel extends NodeModel {

    private BuildRecordSettings m_settings = new BuildRecordSettings();
    
    /**
     * Creates a new instance of {@code CreateRecordNodeModel}.
     */
    public BuildRecordNodeModel() {
        super(new PortType[] {CWLPortObject.TYPE_OPTIONAL, CWLPortObject.TYPE_OPTIONAL, CWLPortObject.TYPE_OPTIONAL},
                new PortType[] {CWLRecordPortObject.TYPE});
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected PortObject[] execute(final PortObject[] inData, final ExecutionContext exec) throws Exception {
        CWLPortObject po1 = (CWLPortObject)inData[0];
        CWLPortObject po2 = (CWLPortObject)inData[1];
        CWLPortObject po3 = (CWLPortObject)inData[2];
        
        boolean append = po3 != null && po3.getType() == CWLType.RECORD;
        
        List<CWLPortObject> toMerge = new ArrayList<>();
        List<String> keys = new ArrayList<>();
        
        if (po1 != null) {
            toMerge.add(po1);
            keys.add(m_settings.getKey1());
        }
        if (po2 != null) {
            toMerge.add(po2);
            keys.add(m_settings.getKey2());
        }
        if (append) {
            if (toMerge.isEmpty()) {
                return new PortObject[] {po3};
            }
            JsonObject record = (JsonObject)po3.getJsonContent();
            for (int i = 0; i < toMerge.size(); i++) {
                record.put(keys.get(i), toMerge.get(i).getJsonContent());
            }
            return new PortObject[] {CWLRecordPortObject.fromJsonValue(record)};
        } else {
            if (po3 != null) {
                toMerge.add(po3);
                keys.add(m_settings.getKey3());
            }
            JsonObjectBuilder builder = Json.createObjectBuilder();
            for (int i = 0; i < toMerge.size(); i++) {
                builder.add(keys.get(i), toMerge.get(i).getJsonContent());
            }
            return new PortObject[] {CWLRecordPortObject.fromJsonValue(builder.build())};
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
        CWLPortObjectSpec po1 = (CWLPortObjectSpec)inSpecs[0];
        CWLPortObjectSpec po2 = (CWLPortObjectSpec)inSpecs[1];
        CWLPortObjectSpec po3 = (CWLPortObjectSpec)inSpecs[2];
        
        boolean append = po3 != null && po3.getType() == CWLType.RECORD;
        
        if (po1 != null && StringUtils.isBlank(m_settings.getKey1())) {
            throw new InvalidSettingsException("No key for input 1 given.");
        }
        if (po2 != null && StringUtils.isBlank(m_settings.getKey2())) {
            throw new InvalidSettingsException("No key for input 2 given.");
        }
        if (!append && po3 != null && StringUtils.isBlank(m_settings.getKey3())) {
            throw new InvalidSettingsException("No key for input 3 given.");
        }
        
        return new PortObjectSpec[] {new CWLPortObjectSpec(CWLType.RECORD)};
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings) {
        m_settings.saveSettings(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadValidatedSettingsFrom(final NodeSettingsRO settings) throws InvalidSettingsException {
        m_settings.loadSettings(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateSettings(final NodeSettingsRO settings) throws InvalidSettingsException {
        BuildRecordSettings s = new BuildRecordSettings();
        s.loadSettings(settings);
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
