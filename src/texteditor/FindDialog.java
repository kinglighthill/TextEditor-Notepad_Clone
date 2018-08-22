/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package texteditor;

import javafx.scene.text.Text;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.plaf.PanelUI;
import java.awt.*;
import java.awt.event.*;

/**
 *
 * @author KCA
 */
public class FindDialog extends JDialog{

    private String text1 = "Find what:";
    private boolean isDown = true;
    private boolean isCaseSensitive = false;

    private final JLabel label = new JLabel();
    private final JButton findButton = new JButton();
    private final JButton cancelButton = new JButton();
    private final JTextField textField = new JTextField();
    private final JRadioButton upRadioButton = new JRadioButton();
    private final JRadioButton downRadioButton = new JRadioButton();
    private final JCheckBox matchCaseCheckBox = new JCheckBox();
    private final ButtonGroup directionButtonGroup = new ButtonGroup();
    private final JPanel buttonPanel = new JPanel();
    private final JPanel panel = new JPanel();
    private final GroupLayout layout = new GroupLayout(panel);
    private final TextEditor textEditor;


    public FindDialog (Frame owner, String title, boolean modal) {
        super(owner, title, modal);
        textEditor = (TextEditor) this.getParent();
        textEditor.setDown(isDown);
        textEditor.setCaseSensitive(isCaseSensitive);

        label.setText(text1);
        label.setLabelFor(textField);

        findButton.setText("Find Next");
        findButton.addActionListener(findAction);
        cancelButton.setText("Cancel");
        cancelButton.addActionListener(cancelAction);

        upRadioButton.setText("Up");
        upRadioButton.addActionListener(upListener);
        downRadioButton.setText("Down");
        downRadioButton.addActionListener(downListener);

        directionButtonGroup.add(downRadioButton);
        directionButtonGroup.add(upRadioButton);
        downRadioButton.setSelected(true);

        buttonPanel.setLayout(new GridLayout(1,2));
        buttonPanel.add(upRadioButton);
        buttonPanel.add(downRadioButton);
        buttonPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
                "Direction"));

        matchCaseCheckBox.setText("Match case");
        matchCaseCheckBox.addItemListener(caseListener);

        if(textEditor.getFindText() != null) {
            textField.setText(textEditor.getFindText());
            textField.selectAll();
        }
        else {
            findButton.setEnabled(false);
        }

        textField.getDocument().addDocumentListener(new DocumentListener() {
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
        panel.setLayout(layout);
        layout.setAutoCreateGaps(true);
        layout.setAutoCreateContainerGaps(true);
        layout.setHorizontalGroup(
                layout.createSequentialGroup().
                        addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING).
                            addComponent(label).addComponent(matchCaseCheckBox)).
                        addGroup(layout.createParallelGroup(GroupLayout.Alignment.TRAILING).
                                addComponent(textField).
                                addComponent(buttonPanel,125,125,125)).
                        addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING).
                                addComponent(findButton,80,80,80).
                                addComponent(cancelButton,80,80,80))
        );
        layout.setVerticalGroup(
                layout.createSequentialGroup().
                        addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING).
                                addComponent(label).addComponent(textField,20,20,20).
                                addComponent(findButton)).
                        addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING).
                                addGroup(layout.createParallelGroup(GroupLayout.Alignment.TRAILING).
                                        addComponent(matchCaseCheckBox).addComponent(buttonPanel)).
                                addComponent(cancelButton))
        );

        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE,0), "Cancel"
        );
        getRootPane().getActionMap().put("Cancel",cancelAction);

        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                KeyStroke.getKeyStroke(KeyEvent.VK_ENTER,0), "Find"
        );
        getRootPane().getActionMap().put("Find", findAction);

        this.add(panel);
        this.setSize(360, 135);
        this.setLocation(owner.getX()+150,owner.getY()+200);
        this.setResizable(false);
        this.setVisible(true);
        this.close();
    }

    Action findAction = new AbstractAction("Find Next") {
        @Override
        public void actionPerformed(ActionEvent e) {
            textEditor.findNext(textEditor, textField.getText(), isDown, isCaseSensitive);
        }
    };

    Action cancelAction = new AbstractAction("Cancel") {
        @Override
        public void actionPerformed(ActionEvent e) {
            exit();
        }
    };

    ActionListener upListener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            isDown = false;
            textEditor.setDown(isDown);
        }
    };

    ActionListener downListener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            isDown = true;
            textEditor.setDown(isDown);
        }
    };

    ItemListener caseListener = new ItemListener() {
        @Override
        public void itemStateChanged(ItemEvent e) {
            if (matchCaseCheckBox.isSelected()) {
                isCaseSensitive = true;
                textEditor.setCaseSensitive(isCaseSensitive);
            }
            else {
                isCaseSensitive = false;
                textEditor.setCaseSensitive(isCaseSensitive);
            }
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
