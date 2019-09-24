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
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.xmlbeans.XmlException;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeDescription;
import org.knime.core.node.NodeDescription28Proxy;
import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.NodeView;
import org.knime.core.node.config.ConfigRO;
import org.knime.core.node.config.ConfigWO;
import org.knime.node.v28.FullDescriptionDocument.FullDescription;
import org.knime.node.v28.InPortDocument.InPort;
import org.knime.node.v28.IntroDocument.Intro;
import org.knime.node.v28.KnimeNodeDocument;
import org.knime.node.v28.KnimeNodeDocument.KnimeNode;
import org.knime.node.v28.OutPortDocument.OutPort;
import org.knime.node.v28.PortsDocument.Ports;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;
import org.yaml.snakeyaml.Yaml;

import de.unikn.knime.cwl.dynode.data.types.CWLDataType;
import de.unikn.knime.cwl.dynode.port.CWLPortDescription;

/**
 * A node factory for nodes from CWL files.
 * @author Martin Horn, Alexander Fillbrunn: University of Konstanz
 *
 */
public class CWLNodeFactory extends NodeFactory<CWLNodeModel> {

    private static final NodeLogger LOGGER = NodeLogger.getLogger(CWLNodeFactory.class);
    
    private String m_id;
    private String m_name;
    private String m_file;
    
    private CWLPortDescription[] m_inputs;
    private CWLPortDescription[] m_outputs;
    
    /**
     * Default constructor that enables lazy loading.
     */
    public CWLNodeFactory() {
        super(true);
    }
    
    @Override
    public CWLNodeModel createNodeModel() {
        return new CWLNodeModel(
                m_file,
                m_inputs, m_outputs);
    }

    @Override
    protected int getNrNodeViews() {
        return 0;
    }

    @Override
    public NodeView<CWLNodeModel> createNodeView(final int viewIndex, final CWLNodeModel nodeModel) {
        return null;
    }

    @Override
    protected boolean hasDialog() {
        return true;
    }

    @Override
    protected NodeDialogPane createNodeDialogPane() {
        return new CWLNodeDialogPane(m_inputs);
    }
    
    @Override
    public void loadAdditionalFactorySettings(final ConfigRO config) throws InvalidSettingsException {
        m_id = config.getString(CWLNodeSetFactory.ID_CFG_KEY);
        m_file = CWLRegistry.getInstance()
                .getPathForHash(m_id.substring(CWLNodeSetFactory.ID_PREFIX.length()));
        Yaml yaml = new Yaml();
        File input = new File(m_file);
        m_name = input.getName();
        
        Map<String, Object> cwl;
        try (FileInputStream fis = new FileInputStream(input)) {
            cwl = yaml.load(fis);
        } catch (IOException e) {
            throw new InvalidSettingsException(
                    String.format("The CWL file \"%s\" cannot be read.", input.getAbsolutePath()), e);
        }
        m_inputs = createPorts(cwl.get("inputs"));
        m_outputs = createPorts(cwl.get("outputs"));

        super.loadAdditionalFactorySettings(config);
    }
    
    private CWLPortDescription[] createPorts(final Object ports) {
        List<CWLPortDescription> types = new ArrayList<>();
        int index = 0;
        if (ports == null) {
            // No input/output property means no ports
            return new CWLPortDescription[0];
        } else if (ports instanceof List) {
            // A list of objects with ID and type properties
            List<?> l = (List<?>)ports;
            for (Object o : l) {
                Map<?, ?> om = (Map<?, ?>)o;
                String id = (String)om.get("id");
                Object type = om.get("type");
                types.add(new CWLPortDescription(id, index++, CWLDataType.fromYAMLObject(type)));
            }
        } else {
            // A map from ID to type or a single input with ID and type properties
            Map<?, ?> om = (Map<?, ?>)ports;
            if (om.containsKey("id")) {
                // A single input
                String id = (String)om.get("id");
                Object type = om.get("type");
                types.add(new CWLPortDescription(id, index++, CWLDataType.fromYAMLObject(type)));
            } else {
                for (Entry<?, ?> e : om.entrySet()) {
                    types.add(new CWLPortDescription(
                            (String)e.getKey(), index++, CWLDataType.fromYAMLObject(e.getValue())));
                }
            }
        }
        return types.toArray(new CWLPortDescription[types.size()]);
    }
    
    @Override
    public void saveAdditionalFactorySettings(final ConfigWO config) {
        config.addString(CWLNodeSetFactory.ID_CFG_KEY, m_id);
        super.saveAdditionalFactorySettings(config);
    }
    
    @Override
    protected NodeDescription createNodeDescription() throws SAXException, IOException, XmlException {
        try {
            KnimeNodeDocument doc = org.knime.node.v28.KnimeNodeDocument.Factory.newInstance();
            Document domDoc = (Document)doc.getDomNode();

            // Node
            KnimeNode node = doc.addNewKnimeNode();
            node.setDeprecated(false);

            node.setName(m_name);
            
            node.setIcon("./cwl.png");
            node.setType(KnimeNode.Type.OTHER);
            
            node.setShortDescription("CWL Tool");
            FullDescription fullDescr = node.addNewFullDescription();
            
            // Intro
            Intro intro = fullDescr.addNewIntro();
            intro.addNewP().getDomNode()
                .appendChild(domDoc.createTextNode("No description available"));
            // Ports
            Ports ports = node.addNewPorts();
            int i = 0;
            for (CWLPortDescription inPort : m_inputs) {
                InPort ip = ports.addNewInPort();
                ip.setIndex(new BigInteger(Integer.toString(i++)));
                ip.setName(inPort.getName());
                ip.getDomNode().appendChild(domDoc.createTextNode(
                        String.format("A CWL port of type %s.", inPort.getType().toHumanReadableString())));
            }
            
            i = 0;
            for (CWLPortDescription outPort : m_outputs) {
                OutPort op = ports.addNewOutPort();
                op.setIndex(new BigInteger(Integer.toString(i++)));
                op.setName(outPort.getName());
                op.getDomNode().appendChild(domDoc.createTextNode(
                        String.format("A CWL port of type %s.", outPort.getType().toHumanReadableString())));
            }

            return new NodeDescription28Proxy(doc);
        } catch (Exception e) {
            LOGGER.error("Dynamic node description instantiation failed", e);
        }
        return null;
    }
}
