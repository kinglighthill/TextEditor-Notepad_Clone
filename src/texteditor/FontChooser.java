package texteditor;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Position;
import java.awt.*;
import java.awt.event.*;
import java.util.Locale;

/**
 * Created by KCA on 6/5/2518.
 */
public class FontChooser extends JDialog {

    private final String[] fontSizes = {"8", "9", "10", "11", "12", "14", "16", "18", "20", "22", "24", "26", "28", "36", "48", "72"};
    private String[] fontFamilyNames = null;
    private final String[] fontStyleNames = {"Regular", "Italic", "Bold", "Bold Italic"};
    private final String[] scriptNames = {"Western", "Greek", "Turkish", "Baltic", "Central Europe", "Cyrillic"};
    private final int[] fontStyles = {Font.PLAIN, Font.ITALIC, Font.BOLD, Font.BOLD | Font.ITALIC};

    private Font defaultFont = new Font(Font.MONOSPACED, Font.PLAIN, 12);
    private Font font = null;

    private JLabel fontLabel = new JLabel("Font:");
    private JLabel fontStyleLabel = new JLabel("Font Style:");
    private JLabel sizeLabel = new JLabel("Size:");
    private JLabel sampleLabel = new JLabel("AaBbYyZz");
    private JLabel scriptLabel = new JLabel("Script");

    private JTextField fontTextField = new JTextField();
    private JTextField fontStyleTextField = new JTextField();
    private JTextField sizeTextField = new JTextField();

    private JList<String> fontJList = new JList<>();
    private JList<String> fontStyleJList = new JList<>();
    private JList<String> sizeJList = new JList<>();

    private JButton okButton = new JButton();
    private JButton cancelButton = new JButton();

    private JComboBox<String> scriptComboBox = new JComboBox<>();

    private JPanel fontPanel = new JPanel();
    private JPanel fontStylePanel = new JPanel();
    private JPanel sizePanel = new JPanel();
    private JPanel buttonsPanel = new JPanel();
    private JPanel panel = new JPanel();

    private BorderLayout fontLayout = new BorderLayout();
    private BorderLayout fontStyleLayout = new BorderLayout();
    private BorderLayout fontSizeLayout = new BorderLayout();

    private GroupLayout layout = new GroupLayout(panel);

    private TextEditor textEditor;


    public FontChooser (Frame owner, String title, boolean modal) {
        super(owner, title, modal);

        textEditor = (TextEditor) this.getParent();
        font = textEditor.getFont();

        setUpFontPanel();
        setUpFontStylePanel();
        setUpFontSizePanel();
        setUpSampleScriptPanels();

        okButton.setText("OK");
        okButton.setAction(okAction);
        cancelButton.setText("Cancel");
        cancelButton.setAction(cancelAction);

        panel.setLayout(layout);
        layout.setAutoCreateGaps(true);
        layout.setAutoCreateContainerGaps(true);

        layout.setHorizontalGroup(
                layout.createSequentialGroup().
                        addComponent(fontPanel).
                        addGroup(
                                layout.createParallelGroup(GroupLayout.Alignment.TRAILING).
                                        addGroup(
                                            layout.createParallelGroup(GroupLayout.Alignment.LEADING).
                                                    addGroup(
                                                            layout.createSequentialGroup().
                                                                    addComponent(fontStylePanel).
                                                                    addComponent(sizePanel)
                                                    ).
                                                    addComponent(sampleLabel, 200, 200, 200).
                                                    addComponent(scriptLabel).
                                                    addComponent(scriptComboBox).
                                                    addComponent(buttonsPanel)
                                        ).
                                        addGroup(
                                                layout.createSequentialGroup().
                                                        addComponent(okButton, 80, 80, 80).
                                                        addComponent(cancelButton, 80, 80, 80)
                                        )
                        )
        );

        layout.setVerticalGroup(
                layout.createSequentialGroup().
                        addGroup(
                                layout.createParallelGroup(GroupLayout.Alignment.LEADING).
                                        addComponent(fontPanel).
                                        addComponent(fontStylePanel).
                                        addComponent(sizePanel)
                        ).
                        addComponent(sampleLabel,80,80,80).
                        addComponent(scriptLabel).
                        addComponent(scriptComboBox, 25, 25, 25).
                        addComponent(buttonsPanel, 70, 70, 70).
                        addGroup(
                                layout.createParallelGroup(GroupLayout.Alignment.LEADING).
                                        addComponent(okButton,25, 25, 25).
                                        addComponent(cancelButton, 25, 25, 25)
                        )
        );

        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE,0), "Cancel"
        );
        getRootPane().getActionMap().put("Cancel",cancelAction);

        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                KeyStroke.getKeyStroke(KeyEvent.VK_ENTER,0), "OK"
        );
        getRootPane().getActionMap().put("OK", okAction);

        this.setSelectedFont(font);

        this.add(panel);
        this.setSize(425, 470);
        this.setLocation(owner.getX()+60,owner.getY()+90);
        this.setResizable(false);
        this.setVisible(true);
        this.close();
    }

    Action okAction = new AbstractAction("OK") {
        @Override
        public void actionPerformed(ActionEvent e) {
            Font font = sampleLabel.getFont();
            textEditor.setFont(font);
            textEditor.getArea().setFont(font);
            exit();
        }
    };

    Action cancelAction = new AbstractAction("Cancel") {
        @Override
        public void actionPerformed(ActionEvent e) {
            exit();
        }
    };

    private class TextFieldFocusListener implements FocusListener {

        private JTextField textField;

        public TextFieldFocusListener(JTextField textField) {
            this.textField = textField;
        }

        @Override
        public void focusGained(FocusEvent e) {
            textField.selectAll();
        }

        @Override
        public void focusLost(FocusEvent e) {
            textField.select(0,0);
            updateSampleFont();
        }
    }

    private class TextFieldKeyListener extends KeyAdapter {

        private JList jList;

        public TextFieldKeyListener(JList jList) {
            this.jList = jList;
        }

        @Override
        public void keyPressed(KeyEvent e) {
            int i;
            switch (e.getKeyCode())
            {
                case KeyEvent.VK_UP:
                    i = jList.getSelectedIndex() - 1;
                    if (i < 0)
                    {
                        i = 0;
                    }
                    jList.setSelectedIndex(i);
                    jList.ensureIndexIsVisible(i);
                    break;
                case KeyEvent.VK_DOWN:
                    int listSize = jList.getModel().getSize();
                    i = jList.getSelectedIndex() + 1;
                    if (i >= listSize)
                    {
                        i = listSize - 1;
                    }
                    jList.setSelectedIndex(i);
                    jList.ensureIndexIsVisible(i);
                    break;
                default:
                    break;
            }
        }
    }

    private class TextFieldDocumentListener implements DocumentListener {

        private JList jList;

        public TextFieldDocumentListener(JList jList) {
            this.jList = jList;
        }

        @Override
        public void insertUpdate(DocumentEvent e) {
            update(e);
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            update(e);
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
            update(e);
        }

        private void update(DocumentEvent event)
        {
            String newValue = "";
            try
            {
                Document doc = event.getDocument();
                newValue = doc.getText(0, doc.getLength());
            }
            catch (BadLocationException e)
            {
                e.printStackTrace();
            }

            if (newValue.length() > 0)
            {
                int index = jList.getNextMatch(newValue, 0, Position.Bias.Forward);
                if (index < 0)
                {
                    index = 0;
                }

                jList.ensureIndexIsVisible(index);

                String matchedName = jList.getModel().getElementAt(index).toString();
                if (newValue.equalsIgnoreCase(matchedName))
                {
                    if (index != jList.getSelectedIndex())
                    {
                        SwingUtilities.invokeLater(new ListSelector(index));
                    }
                }
            }
        }

        public class ListSelector implements Runnable
        {
            private int index;

            public ListSelector(int index)
            {
                this.index = index;
            }

            public void run()
            {
                jList.setSelectedIndex(this.index);
            }
        }
    }

    private class JListSelectionListener implements ListSelectionListener {

        private JTextField textField;

        public JListSelectionListener(JTextField textField) {
            this.textField = textField;
        }

        @Override
        public void valueChanged(ListSelectionEvent e) {
            if(!e.getValueIsAdjusting()) {
                JList list = (JList) e.getSource();
                String selectedValue = (String) list.getSelectedValue();
                String oldValue = textField.getText();
                if (!oldValue.equalsIgnoreCase(selectedValue)) {
                    textField.setText(selectedValue);
                    textField.selectAll();
                    textField.requestFocus();
                }
                updateSampleFont();
            }
        }
    }

    private void setFontFamilyNames() {
        GraphicsEnvironment environment = GraphicsEnvironment.getLocalGraphicsEnvironment();
        fontFamilyNames = environment.getAvailableFontFamilyNames();
    }

    private void setUpFontPanel() {
        fontLabel.setLabelFor(fontTextField);
        fontLabel.setHorizontalAlignment(SwingConstants.LEFT);

        setFontFamilyNames();
        fontJList.setListData(fontFamilyNames);
        fontJList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        fontJList.setSelectedIndex(0);
        fontJList.addListSelectionListener(new JListSelectionListener(fontTextField));
        fontJList.setFocusable(true);
        fontJList.setFont(defaultFont);

        JScrollPane fontJScrollPane = new JScrollPane(fontJList);
        fontJScrollPane.getVerticalScrollBar().setFocusable(false);
        fontJScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

        fontTextField.addFocusListener(new TextFieldFocusListener(fontTextField));
        fontTextField.addKeyListener(new TextFieldKeyListener(fontJList));
        fontTextField.getDocument().addDocumentListener(new TextFieldDocumentListener(fontJList));
        fontTextField.setFont(defaultFont);

        fontPanel.setLayout(fontLayout);
        JPanel p = new JPanel(new BorderLayout());
        p.add(fontTextField, BorderLayout.NORTH);
        p.add(fontJScrollPane, BorderLayout.CENTER);
        fontPanel.add(fontLabel, BorderLayout.NORTH);
        fontPanel.add(p, BorderLayout.CENTER);
        fontPanel.setPreferredSize(new Dimension(200,120));
        fontPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    }

    private void setUpFontStylePanel() {
        fontStyleLabel.setLabelFor(fontStyleTextField);
        fontStyleLabel.setHorizontalAlignment(SwingConstants.LEFT);

        fontStyleJList.setListData(fontStyleNames);
        fontStyleJList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        fontStyleJList.setSelectedIndex(0);
        fontStyleJList.addListSelectionListener(new JListSelectionListener(fontStyleTextField));
        fontStyleJList.setFocusable(false);
        fontStyleJList.setFont(defaultFont);

        JScrollPane fontStyleJScrollPane = new JScrollPane(fontStyleJList);
        fontStyleJScrollPane.getVerticalScrollBar().setFocusable(false);
        fontStyleJScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

        fontStyleTextField.addFocusListener(new TextFieldFocusListener(fontStyleTextField));
        fontStyleTextField.addKeyListener(new TextFieldKeyListener(fontStyleJList));
        fontStyleTextField.getDocument().addDocumentListener(new TextFieldDocumentListener(fontStyleJList));
        fontStyleTextField.setFont(defaultFont);

        fontStylePanel.setLayout(fontStyleLayout);
        JPanel p = new JPanel(new BorderLayout());
        p.add(fontStyleTextField, BorderLayout.NORTH);
        p.add(fontStyleJScrollPane, BorderLayout.CENTER);
        fontStylePanel.add(fontStyleLabel, BorderLayout.NORTH);
        fontStylePanel.add(p, BorderLayout.CENTER);
        fontStylePanel.setPreferredSize(new Dimension(120,120));
        fontStylePanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    }

    private void setUpFontSizePanel() {
        sizeLabel.setLabelFor(sizeTextField);
        sizeLabel.setHorizontalAlignment(SwingConstants.LEFT);

        sizeJList.setListData(fontSizes);
        sizeJList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        sizeJList.setSelectedIndex(0);
        sizeJList.addListSelectionListener(new JListSelectionListener(sizeTextField));
        sizeJList.setFocusable(false);
        sizeJList.setFont(defaultFont);

        JScrollPane fontSizeJScrollPane = new JScrollPane(sizeJList);
        fontSizeJScrollPane.getVerticalScrollBar().setFocusable(false);
        fontSizeJScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

        sizeTextField.addFocusListener(new TextFieldFocusListener(sizeTextField));
        sizeTextField.addKeyListener(new TextFieldKeyListener(sizeJList));
        sizeTextField.getDocument().addDocumentListener(new TextFieldDocumentListener(sizeJList));
        sizeTextField.setFont(defaultFont);

        JPanel p = new JPanel(new BorderLayout());
        sizePanel.setLayout(fontSizeLayout);
        p.add(sizeTextField, BorderLayout.NORTH);
        p.add(fontSizeJScrollPane, BorderLayout.CENTER);
        sizePanel.add(sizeLabel, BorderLayout.NORTH);
        sizePanel.add(p, BorderLayout.CENTER);
        sizePanel.setPreferredSize(new Dimension(60,120));
        sizePanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    }

    private void setUpSampleScriptPanels() {
        sampleLabel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),"Sample"));
        sampleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        scriptLabel.setLabelFor(scriptComboBox);

        for (String scripts: scriptNames) {
            scriptComboBox.addItem(scripts);
        }
    }

    private void setFont() {
        String fontName = fontJList.getSelectedValue();
        int fontStyle = fontStyles[fontStyleJList.getSelectedIndex()];
        int fontSize;
        String fontSizeString = sizeTextField.getText();
        while (true)
        {
            try
            {
                fontSize = Integer.parseInt(fontSizeString);
                break;
            }
            catch (NumberFormatException e)
            {
                fontSizeString = sizeJList.getSelectedValue();
                sizeTextField.setText(fontSizeString);
            }
        }

        this.font = new Font(fontName, fontStyle, fontSize);
    }

    public void setSelectedFont(Font font) {
        for (int i = 0; i < fontFamilyNames.length; i++)
        {
            if (fontFamilyNames[i].toLowerCase().equals(font.getFamily().toLowerCase()))
            {
                fontJList.setSelectedIndex(i);
                break;
            }
        }

        for (int i = 0; i < fontStyles.length; i++)
        {
            if (fontStyles[i] == font.getStyle())
            {
                fontStyleJList.setSelectedIndex(i);
                break;
            }
        }

        String sizeString = String.valueOf(font.getSize());
        for (int i = 0; i < this.fontSizes.length; i++)
        {
            if (this.fontSizes[i].equals(sizeString))
            {
                sizeJList.setSelectedIndex(i);
                break;
            }
        }

        fontTextField.setText(fontJList.getSelectedValue());
        fontStyleTextField.setText(fontStyleJList.getSelectedValue());
        sizeTextField.setText(sizeString);
        updateSampleFont();
    }

    protected void updateSampleFont()
    {
        setFont();
        sampleLabel.setFont(font);
    }

    private void close(){
        this.addWindowListener(
                new WindowAdapter(){
                    @Override
                    public void windowClosing(WindowEvent event){
                        exit();
                    }
                }
        );
    }

    private void exit(){
        this.dispose();
    }
}
