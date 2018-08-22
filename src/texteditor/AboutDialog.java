package texteditor;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * Created by KCA on 12/20/2017.
 */
public class AboutDialog extends JDialog {

    private String logoPath = "/images/logo.jpg";
    private String iconPath = "/images/icon.png";
    private String text = "<html>KingEdit <br> Version 1.0 (Build 1024) " +
            "<br> &copy; 2018 Kingsley Ugwudinso. All rights reserved. " +
            "<br> KingEdit is personal project influenced by the Microsoft Windows Notepad. " +
            "<br> In other words, it's a simple Notepad clone with some of its basic features.</html>";

    JLabel label = new JLabel();
    JLabel logoLabel = new JLabel();
    JLabel iconLabel = new JLabel();
    JLabel buttonLabel = new JLabel();
    JButton okButton = new JButton();
    ImageIcon logo = new ImageIcon(getClass().getResource(logoPath));
    ImageIcon icon = new ImageIcon(getClass().getResource(iconPath));
    JSeparator separator = new JSeparator();
    JPanel panel = new JPanel();
    GroupLayout layout = new GroupLayout(panel);

    public AboutDialog (Frame owner, String title, boolean modal) {
        super (owner, title, modal);

        okButton.setText("Ok");
        okButton.setAction(okAction);
        label.setText(text);

        logoLabel.setIcon(logo);
        logoLabel.setForeground(Color.LIGHT_GRAY);
        iconLabel.setIcon(icon);

        panel.setLayout(layout);
        layout.setAutoCreateGaps(true);
        layout.setAutoCreateContainerGaps(true);

        layout.setHorizontalGroup(
                layout.createSequentialGroup().
                        addGroup(layout.createParallelGroup(GroupLayout.Alignment.TRAILING).
                                addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER).
                                        addComponent(logoLabel).
                                        addComponent(separator).
                                        addGroup(layout.createSequentialGroup().
                                                addComponent(iconLabel).
                                                addComponent(label))
                                ).
                                addComponent(buttonLabel).
                                addComponent(okButton, 80, 80, 80))
        );
        layout.setVerticalGroup(
                layout.createSequentialGroup().
                        addComponent(logoLabel).
                        addComponent(separator,10,10,10).
                        addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING).
                                addComponent(iconLabel).
                                addComponent(label)
                        ).
                        addComponent(buttonLabel,90,90,90).
                        addComponent(okButton,25, 25, 25)
        );

        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE,0), "Cancel"
        );
        getRootPane().getActionMap().put("Cancel",okAction);

        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                KeyStroke.getKeyStroke(KeyEvent.VK_ENTER,0), "Cancel"
        );
        getRootPane().getActionMap().put("Cancel", okAction);

        this.add(panel);
        this.setSize(460, 405);
        this.setLocation(owner.getX()+100,owner.getY()+150);
        this.setResizable(false);
        this.setVisible(true);
        this.close();
    }

    Action okAction = new AbstractAction("OK") {
        @Override
        public void actionPerformed(ActionEvent e) {
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
}
