/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package texteditor;

import javafx.scene.input.KeyEvent;

import javax.swing.*;

/**
 *
 * @author KCA
 */
public class Menu {
    private JMenuItem[] item;
    private boolean t;
    String[] items;

    public void setActions(JMenu[] menu, JMenuItem[] menuItem, Action[] action, int position){
        int i = 0;
        for (i=0; i<menuItem.length; i++){
            menu[position].add(menuItem[i]);
            menuItem[i].addActionListener(action[i]);
            if(i == menuItem.length - 1) {
                break;
            }
        }
    }

    public void setItems(boolean t, String ...i){
        this.t = t;
        this.items = i;
        int x = 0;
        for(String j : items)
           x++;
        String[] s = items;
        JMenuItem[] n = new JMenuItem[x];
        if(!t){
            for(int y=0; y<x; y++){
                JMenuItem I = new JMenuItem(s[y]);
                n[y] = I;
            }
        }
        else{
            for(int y=0; y<x; y++){
                if (y == 0){
                    JMenuItem I = new JCheckBoxMenuItem(s[y]);
                    n[y] = I;
                }
                else{
                    JMenuItem I = new JMenuItem(s[y]);
                    n[y] = I;
                }
            }
        }
        item = n;
    }
    public JMenuItem[] getItems(){
        return item;
    }
    
    public void setEnable(JMenu menu, boolean b, int ...position){
        int[] pos = position;
        int i = 0;
        for(int j=0; j<menu.getItemCount(); j++){
            if (i < pos.length && j == pos[i]) {
                menu.getItem(j).setEnabled(b);
                i++;
            }
        }
    }

    public void setSeperators(JMenu menu, int ... position){
        int[] pos = position;
        int i = 0;
        JMenuItem[] menuItems = new JMenuItem[menu.getItemCount()];
        for (int j = 0; j < menuItems.length; j++){
            menuItems[j] = menu.getItem(j);
        }
        menu.removeAll();
        for (int k = 0; k < menuItems.length; k++) {
            if( i < pos.length && k == pos[i]) {
                menu.addSeparator();
                menu.add(menuItems[k]);
                i++;
            }
            else {
                menu.add(menuItems[k]);
            }
        }
    }

    public void setAccelerator(JMenu menu, String[] str, int ... position) {
        int[] pos = position;
        int i = 0;
        for (int k = 0; k < menu.getItemCount(); k++) {
            if (i < pos.length && k == pos[i]) {
                menu.getItem(k).setAccelerator(KeyStroke.getKeyStroke(str[i]));
                i++;
            }
        }
    }
}
