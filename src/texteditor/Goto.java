/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package texteditor;

import javafx.scene.Scene;
import org.omg.IOP.ExceptionDetailMessage;


import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.ActionEvent;
import java.util.Scanner;
import javax.swing.*;
import javax.swing.text.*;

/**
 *
 * @author KCA
 */
public class Goto extends JDialog{
    
    private final String text = "Line Number: ";
    private final JPanel panel = new JPanel();
    private final JLabel label = new JLabel();
    private final JTextField textfield = new JTextField();
    private final JButton button1 = new JButton();
    private final JButton button2 = new JButton(); 
    private final GridBagLayout grid = new GridBagLayout();
    private final AbstractDocument document;
    private final TextEditor textEditor;

    private String message = "The Line is beyond the total number of lines";
    private String title = "KingEdit - GoTo Line";

    public Goto(Frame owner, String title,boolean modal) {
        super(owner,title,modal);
        textEditor = (TextEditor) this.getParent();
        label.setText(text);
        label.setVisible(true);
        label.setLabelFor(textfield);
        button1.setAction(GoTo);
        button2.setAction(Cancel);
        GridBagConstraints c1 = new GridBagConstraints();
        c1.fill = GridBagConstraints.HORIZONTAL;
        c1.gridx = 0;
        c1.gridy = 0; 
        c1.insets = new Insets(0,0,5,0);
        GridBagConstraints c2 = new GridBagConstraints();
        c2.fill = GridBagConstraints.HORIZONTAL;
        c2.gridx = 0;
        c2.gridy = 1;
        c2.gridwidth = 3;
        c2.ipady = 8;
        c2.insets = new Insets(0,0,15,0); 
        GridBagConstraints c3 = new GridBagConstraints();
        c3.gridx = 1;
        c3.gridy = 2;
        c3.gridwidth = 1;
        c3.insets = new Insets(0,0,0,5); 
        GridBagConstraints c4 = new GridBagConstraints();
        c4.gridx = 2;
        c4.gridy = 2;
        c4.gridwidth = 1;
        c4.insets = new Insets(0,5,0,0);

        if (textEditor.getGotoText() != null) {
            textfield.setText(textEditor.getGotoText());
            textfield.selectAll();
        }

        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE,0), "Cancel"
        );
        getRootPane().getActionMap().put("Cancel",Cancel);

        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                KeyStroke.getKeyStroke(KeyEvent.VK_ENTER,0), "GoTo"
        );
        getRootPane().getActionMap().put("GoTo", GoTo);


        document = (AbstractDocument)textfield.getDocument();
        document.setDocumentFilter(filter);
        panel.setLayout(grid);
        panel.add(label, c1);
        panel.add(textfield, c2);
        panel.add(button1, c3);
        panel.add(button2, c4);
        this.add(panel);

        this.setSize(260, 135);
        this.setLocation(owner.getX()+10,owner.getY()+50);
        this.setResizable(false);
        this.setVisible(true);
        this.close();
        
    }
    
    Action GoTo = new AbstractAction("GoTo"){
        @Override
        public void actionPerformed(ActionEvent event){
            Goto();
        }
    };
    
    Action Cancel = new AbstractAction("Cancel"){
        @Override
        public void actionPerformed(ActionEvent event){
            exit();
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
    
    private void Goto(){
        textEditor.setGotoText(textfield.getText());
        JTextArea area = textEditor.getArea();
        int i = textEditor.getLineCount();
        try {
            int input = Integer.parseInt(textfield.getText());
            if (i < input) {
                JOptionPane.showMessageDialog(this.getParent(), message, title, JOptionPane.PLAIN_MESSAGE);
            }
            else {
                exit();
                area.setCaretPosition(area.getDocument().getDefaultRootElement().getElement(input-1).getStartOffset());
            }
        }
        catch (NumberFormatException exp) {
            Toolkit.getDefaultToolkit().beep();
        }
    }

    DocumentFilter filter = new DocumentFilter() {

        @Override
        public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
            boolean ok = true;
            char chars[] = new char[text.length()];
            for (int i = 0; i < text.length(); i++) {
                chars[i] = text.charAt(i);
            }
            for (char c: chars) {
                int i = (int) c;
                if (i < 48 || i > 57) {
                    ok = false;
                    Toolkit.getDefaultToolkit().beep();
                }
            }
            if (ok) {
                super.replace(fb, offset, length, text, attrs);
            }
        }
    };

}
