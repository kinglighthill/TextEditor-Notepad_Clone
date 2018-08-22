package texteditor;

import javax.swing.*;
import java.awt.*;

/**
 * Created by KCA on 12/20/2017.
 */
public class CustomMenuItem extends JMenuItem {

    public CustomMenuItem(String text) {
        String text1 = text.substring(0,text.indexOf("_"));
        String text2 = text.substring(text.indexOf("_")+1);
        System.out.println(text1);
        System.out.println(text2);
        JLabel label1 = new JLabel(text1);
        JLabel label2 = new JLabel(text2, SwingConstants.RIGHT);
        BoxLayout boxLayout = new BoxLayout(this, BoxLayout.X_AXIS);
        this.setLayout(boxLayout);
        this.add(label1);
        this.add(label2);
        this.setPreferredSize(new Dimension(300,20));
    }
}
