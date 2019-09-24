package de.unikn.knime.cwl.dynode.editors;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonValue;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import de.unikn.knime.cwl.dynode.data.types.CWLDataType;

/**
 * Editor component for array types.
 * @author Martin Horn, Alexander Fillbrunn: University of Konstanz
 *
 */
public class CWLArrayEditor implements CWLInputEditor {

    private List<CWLInputEditor> m_editors;
    private CWLDataType m_itemType;
    private JPanel m_editorPanel;
    private JPanel m_panel;
    private JButton m_addBtn;
    
    /**
     * Create a new instance of {@code CWLArrayEditor}.
     * @param itemType the type of items in the array
     */
    public CWLArrayEditor(final CWLDataType itemType) {
        m_editors = new ArrayList<>();
        m_itemType = itemType;
        m_panel = new JPanel(new BorderLayout());
        m_panel.setMinimumSize(new Dimension(100, 150));
        m_editorPanel = new JPanel(new GridBagLayout());
        JScrollPane scroller = new JScrollPane(m_editorPanel);
        scroller.setMinimumSize(new Dimension(100, 100));
        scroller.setPreferredSize(new Dimension(scroller.getPreferredSize().width, 100));
        m_panel.add(scroller, BorderLayout.CENTER);
        
        JPanel buttons = new JPanel();
        m_panel.add(buttons, BorderLayout.SOUTH);
        
        m_addBtn = new JButton("Add item");
        m_addBtn.addActionListener(e -> {
            GridBagConstraints gbc = createGBC();
            gbc.gridy = m_editors.size();
            CWLInputEditor editor = CWLEditorFactory.getInstance().getEditorForType(m_itemType);
            m_editors.add(editor);
            m_editorPanel.add(createEditorPanel(editor), gbc);
            m_editorPanel.revalidate();
            m_editorPanel.repaint();
            //SwingUtilities.getWindowAncestor(m_editorPanel).pack();
        });
        
        buttons.add(m_addBtn);
    }
    
    private JPanel createEditorPanel(final CWLInputEditor editor) {
        JPanel ePnl = new JPanel(new BorderLayout());
        ePnl.add(editor.getComponent(), BorderLayout.CENTER);
        JButton remove = new JButton("-");
        remove.addActionListener(e -> {
            m_editorPanel.remove(ePnl);
            m_editorPanel.revalidate();
            m_editorPanel.repaint();
        });
        ePnl.add(remove, BorderLayout.EAST);
        return ePnl;
    }
    
    @Override
    public String getJSONStringValue() {
        return String.format("[%s]", String.join(",",
                m_editors.stream().map(e -> e.getJSONStringValue()).collect(Collectors.toList())));
    }

    @Override
    public void setStringValue(final String jsonStr) {
        m_editors.clear();
        m_editorPanel.removeAll();
        GridBagConstraints gbc = createGBC();
        if (jsonStr != null && jsonStr.trim().length() != 0) {
            JsonArray arr = Json.createReader(new StringReader(jsonStr)).readArray();
            for (JsonValue val : arr) {
                CWLInputEditor editor = CWLEditorFactory.getInstance().getEditorForType(m_itemType);
                editor.setStringValue(val.toString());
                m_editors.add(editor);
                m_editorPanel.add(editor.getComponent(), gbc);
                gbc.gridy++;
            }
        }
        if (m_editors.size() == 0) {
            m_editors.add(CWLEditorFactory.getInstance().getEditorForType(m_itemType));
        }
    }
    
    private GridBagConstraints createGBC() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        return gbc;
    }

    @Override
    public Component getComponent() {
        return m_panel;
    }
    
    @Override
    public void setEnabled(final boolean enabled) {
        for (CWLInputEditor e : m_editors) {
            e.setEnabled(enabled);
        }
        m_addBtn.setEnabled(enabled);
    }
}
