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
package de.unikn.knime.cwl.util.node.convertany;

import java.io.IOException;
import java.math.BigInteger;

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

import de.unikn.knime.cwl.dynode.data.types.CWLType;

/**
 * Factory for the nodes converting between CWL ports.
 *
 * @author Martin Horn, Alexander Fillbrunn: University of Konstanz
 */
public class AnyToSpecificNodeFactory 
        extends NodeFactory<AnyToSpecificNodeModel> {
    
    private static final NodeLogger LOGGER = NodeLogger.getLogger(AnyToSpecificNodeFactory.class);
    
    private CWLType m_type;
    
    /**
     * Creates a new lazily initialized instance of {@code JsonToPortNodeFactory}.
     */
    public AnyToSpecificNodeFactory() {
        super(true);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public AnyToSpecificNodeModel createNodeModel() {
        return new AnyToSpecificNodeModel(m_type);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getNrNodeViews() {
        return 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NodeView<AnyToSpecificNodeModel> createNodeView(final int viewIndex,
            final AnyToSpecificNodeModel nodeModel) {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasDialog() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NodeDialogPane createNodeDialogPane() {
        return null;
    }
    
    @Override
    public void loadAdditionalFactorySettings(final ConfigRO config) throws InvalidSettingsException {
        m_type = CWLType.valueOf(config.getString(AnyToSpecificNodeSetFactory.CFG_TYPE));
        super.loadAdditionalFactorySettings(config);
    }
    
    @Override
    public void saveAdditionalFactorySettings(final ConfigWO config) {
        config.addString(AnyToSpecificNodeSetFactory.CFG_TYPE, m_type.toString());
    }
    
    @Override
    protected NodeDescription createNodeDescription() throws SAXException, IOException, XmlException {
        try {
            KnimeNodeDocument doc = org.knime.node.v28.KnimeNodeDocument.Factory.newInstance();
            Document domDoc = (Document)doc.getDomNode();

            // Node
            KnimeNode node = doc.addNewKnimeNode();
            node.setDeprecated(false);

            String type = m_type.symbol();
            String niceType = type.substring(0, 1).toUpperCase() + type.substring(1).toLowerCase();
                    
            node.setName(String.format("Convert to CWL %s", niceType));
            
            node.setIcon("./anytospecial.png");
            node.setType(KnimeNode.Type.OTHER);
            
            String descr = String.format("Converts a CWL port of any type"
                    + " into a CWL Port of type %s, if possible.", niceType);
            node.setShortDescription(descr);
            FullDescription fullDescr = node.addNewFullDescription();
            
            // Intro
            Intro intro = fullDescr.addNewIntro();
            intro.addNewP().getDomNode().appendChild(domDoc.createTextNode(descr));
            
            // Ports
            Ports ports = node.addNewPorts();
            InPort ip = ports.addNewInPort();
            ip.setIndex(new BigInteger(Integer.toString(0)));
            ip.setName("Input");
            ip.getDomNode().appendChild(domDoc.createTextNode("CWL Port"));
            
            OutPort op = ports.addNewOutPort();
            op.setIndex(new BigInteger(Integer.toString(0)));
            op.setName(String.format("CWL %s", niceType));
            op.getDomNode().appendChild(domDoc.createTextNode(String.format("A CWL port of type %s.", niceType)));

            return new NodeDescription28Proxy(doc);
        } catch (Exception e) {
            LOGGER.error("Dynamic node description instantiation failed", e);
        }
        return null;
    }
}

