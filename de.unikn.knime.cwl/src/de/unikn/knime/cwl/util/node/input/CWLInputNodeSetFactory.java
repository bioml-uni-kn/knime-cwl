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
package de.unikn.knime.cwl.util.node.input;

import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSetFactory;
import org.knime.core.node.NodeSettings;
import org.knime.core.node.config.Config;
import org.knime.core.node.config.ConfigRO;

import de.unikn.knime.cwl.dynode.data.types.CWLType;

/**
 * Node set factory for CWL Input nodes.
 * @author Martin Horn, Alexander Fillbrunn: University of Konstanz
 *
 */
public class CWLInputNodeSetFactory implements NodeSetFactory {

    /** Config key for the type setting. */
    public static final String CFG_TYPE = "type";

    private static final String ID_PREFIX = "org.knime.cwl.input.";
    
    private static final CWLType[] GENERATED_TYPES = new CWLType[] {
            CWLType.BOOLEAN,
            CWLType.INT,
            CWLType.LONG,
            CWLType.FLOAT,
            CWLType.DOUBLE,
            CWLType.STRING,
            CWLType.FILE,
            CWLType.DIRECTORY
    };
    
    @Override
    public Collection<String> getNodeFactoryIds() {
        return Arrays.stream(GENERATED_TYPES)
                .map(t -> String.format("%s%s", ID_PREFIX, t))
                .collect(Collectors.toList());
    }

    @Override
    public Class<? extends NodeFactory<? extends NodeModel>> getNodeFactory(final String id) {
        return CWLInputNodeFactory.class;
    }

    @Override
    public String getCategoryPath(final String id) {
        return "/cwl/util/input";
    }

    @Override
    public String getAfterID(final String id) {
        return null;
    }

    @Override
    public ConfigRO getAdditionalSettings(final String id) {
        Config cfg = new NodeSettings("");
        cfg.addString(CFG_TYPE, id.substring(ID_PREFIX.length()));
        return cfg;
    }

}
