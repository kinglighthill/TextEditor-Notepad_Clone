package texteditor;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.*;

/**
 * Created by KCA on 12/20/2017.
 */
public class ReplaceDialog extends JDialog {

    private String findText = "Find what:";
    private String replaceText = "Replace with:";
    private boolean isCaseSensitive = false;

    private final JLabel findLabel = new JLabel();
    private final JLabel replaceLabel = new JLabel();
    private final JTextField findTextField = new JTextField();
    private final JTextField replaceTextField = new JTextField();
    private final JButton findButton = new JButton();
    private final JButton replaceButton = new JButton();
    private final JButton replaceAllButton = new JButton();
    private final JButton cancelButton = new JButton();
    private final JCheckBox matchCaseCheckBox = new JCheckBox();
    private final JPanel panel = new JPanel();
    private final GroupLayout layout = new GroupLayout(panel);
    private final TextEditor textEditor;

    public ReplaceDialog(Frame owner, String title, boolean modal) {
        super(owner,title,true);
        textEditor = (TextEditor) this.getParent();

        findLabel.setText(findText);
        replaceLabel.setText(replaceText);

        findButton.setText("Find Next");
        findButton.addActionListener(findAction);
        replaceButton.setText("Replace");
        replaceButton.addActionListener(replaceListener);
        replaceAllButton.setText("Replace All");
        replaceAllButton.addActionListener(replaceAllListener);
        cancelButton.setText("Cancel");
        cancelButton.addActionListener(cancelAction);

        matchCaseCheckBox.setText("Match case");
        matchCaseCheckBox.addItemListener(caseListener);

        panel.setLayout(layout);
        layout.setAutoCreateGaps(true);
        layout.setAutoCreateContainerGaps(true);
        layout.setHorizontalGroup(
                layout.createSequentialGroup().
                        addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING).
                                addComponent(findLabel).addComponent(replaceLabel).
                                addComponent(matchCaseCheckBox)).
                        addGroup(layout.createParallelGroup(GroupLayout.Alignment.TRAILING).
                                addComponent(findTextField).addComponent(replaceTextField)).
                        addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING).
                                addComponent(findButton,100,100,100).
                                addComponent(replaceButton,100,100,100).
                                addComponent(replaceAllButton,100,100,100).
                                addComponent(cancelButton,100,100,100))
        );
        layout.setVerticalGroup(
                layout.createSequentialGroup().
                        addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING).
                                addComponent(findLabel).addComponent(findTextField,20,20,20).
                                addComponent(findButton)).
                        addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING).
                                addComponent(replaceLabel).addComponent(replaceTextField,20,20,20).
                                addComponent(replaceButton)).
                        addGroup(layout.createParallelGroup(GroupLayout.Alignment.TRAILING).
                                addComponent(matchCaseCheckBox).
                                addGroup(layout.createSequentialGroup().
                                        addComponent(replaceAllButton).
                                        addComponent(cancelButton)))
        );

        if(textEditor.getFindText() != null) {
            findTextField.setText(textEditor.getFindText());
            findTextField.selectAll();
        }
        else {
            findButton.setEnabled(false);
        }

        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE,0), "Cancel"
        );
        getRootPane().getActionMap().put("Cancel", cancelAction);

        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                KeyStroke.getKeyStroke(KeyEvent.VK_ENTER,0), "Find"
        );
        getRootPane().getActionMap().put("Find", findAction);

        findTextField.getDocument().addDocumentListener(new DocumentListener() {
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

            private void update(DocumentEvent event) {
                if (event.getDocument().getLength() == 0) {
                    findButton.setEnabled(false);
                }
                else {
                    findButton.setEnabled(true);
                }
            }
        });
        this.add(panel);
        this.setSize(350, 190);
        this.setLocation(owner.getX()+150,owner.getY()+200);
        this.setResizable(false);
        this.setVisible(true);
        this.close();
    }

    Action findAction = new AbstractAction("Find Next") {
        @Override
        public void actionPerformed(ActionEvent e) {

        }
    };

    ActionListener replaceListener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            JTextArea area = textEditor.getArea();
            textEditor.findNext(textEditor, findTextField.getText(), true, isCaseSensitive);
            int position = area.getCaretPosition();
            String text = area.getText();
            String start = text.substring(0, area.getSelectionStart());
            String end = text.substring(area.getSelectionEnd());
            area.setText(start + replaceTextField.getText() + end);
            area.setCaretPosition(position);
        }
    };

    ActionListener replaceAllListener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            JTextArea area = textEditor.getArea();
            area.setText(area.getText().replaceAll(area.getSelectedText(), replaceTextField.getText()));
        }
    };

    Action cancelAction = new AbstractAction("Cancel") {
        @Override
        public void actionPerformed(ActionEvent e) {
            exit();
        }
    };

    ItemListener caseListener = (e) -> {
            if (matchCaseCheckBox.isSelected()) {
                isCaseSensitive = true;
            }
            else {
                isCaseSensitive = false;
            }
    };

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
