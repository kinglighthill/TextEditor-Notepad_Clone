/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package texteditor;

import com.sun.deploy.util.WinRegistry;
import com.sun.java.swing.plaf.windows.WindowsClassicLookAndFeel;

import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.*;
import java.awt.print.PageFormat;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import java.util.prefs.PreferencesFactory;
import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;
import javax.swing.event.*;
import javax.swing.text.*;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;
import javax.swing.undo.UndoableEdit;

/**
 *
 * @author KCA
 */
public class TextEditor extends JFrame {
    private String title = "Untitled"; 
    private final String name = " - KingEdit";
    private final String registryPath = "SOFTWARE\\Classes\\txtfile\\shell\\KingEdit\\command";
    private final String url = "www.google.com";
    private String titleBar = title + name;
    private String path;
    private final String iconPath = "/images/icon.png";
    private String clipboard;
    private String findText = null;
    private String gotoText = null;
    private String statusString = "Ln 1, Col 1";

    private int lineCount;

    private boolean changed = false;
    private boolean wordWrapListener;
    private boolean statusBar;
    private boolean savedAs = false;
    private boolean isDown = true;
    private boolean isCaseSensitive = false;

    private JTextArea area;
    private FileDialog fDialog; 
    private JScrollPane scroll;
    private JPanel statusPanel;
    private JLabel statusLabel;
    private JMenuBar menuBar;
    private JPopupMenu popupMenu = new JPopupMenu();
    private final JMenu fileMenu = new JMenu("File");
    private final JMenu editMenu = new JMenu("Edit");
    private final JMenu formatMenu = new JMenu("Format");
    private final JMenu viewMenu = new JMenu("View");
    private final JMenu helpMenu = new JMenu("Help");
    private ImageIcon imageIcon;
    private Image image;
    private Dimension dimension;
    private AbstractDocument document;
    private JMenuItem[] file = null;
    private JMenuItem[] edit = null;
    private JMenuItem[] format = null;
    private JMenuItem[] view = null;
    private JMenuItem[] help = null;
    private UndoManager undoManager;
    private final Menu m = new Menu();
    private final ActionMap actionMap;
    private final Action Cut;
    private final Action Copy;
    private final Action Paste;
    private final Action SelectAll;

    private Preferences preferences;
    private WinRegistry winRegistry;

    private final Font defaultFont = new Font("Monospaced",Font.PLAIN,12);
    private Font font = null;

    private TextEditor context;
    
    public TextEditor() throws HeadlessException {
        context = this;
        this.setLayout(new BorderLayout());
        path = System.getProperty("user.dir");
        area = new JTextArea();
        scroll = new JScrollPane(area,JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        statusPanel = new JPanel();
        statusLabel = new JLabel();
        actionMap = area.getActionMap();
        Cut = actionMap.get(DefaultEditorKit.cutAction);
        Copy = actionMap.get(DefaultEditorKit.copyAction);
        Paste = actionMap.get(DefaultEditorKit.pasteAction);
        SelectAll = actionMap.get(DefaultEditorKit.selectAllAction);
        dimension = Toolkit.getDefaultToolkit().getScreenSize();
        imageIcon = new ImageIcon(getClass().getResource(iconPath));
        image = imageIcon.getImage();
        undoManager = new UndoManager();
        document = (AbstractDocument) area.getDocument();
        preferences = Preferences.userNodeForPackage(this.getClass());



        statusBar = preferences.getBoolean("StatusBar", false);
        wordWrapListener = preferences.getBoolean("WordWrap", true);
        String fontName = preferences.get("fontName", defaultFont.getName());
        int fontStyle = preferences.getInt("fontStyle", defaultFont.getStyle());
        int fontSize = preferences.getInt("fontSize", defaultFont.getSize());
        font = new Font(fontName, fontStyle, fontSize);

        /*try {
            texteditor.WinRegistry.createKey(texteditor.WinRegistry.HKEY_CURRENT_USER, registryPath);
            texteditor.WinRegistry.writeStringValue(texteditor.WinRegistry.HKEY_CURRENT_USER, registryPath,
                    "(Default)", System.getProperty("java.home")
                            + File.separator + "bin" + File.separator + "java");
        }
        catch (IllegalAccessException | InvocationTargetException exp) {

        }
*/

        if(font != null) {
            area.setFont(font);
        }
        else {
            area.setFont(defaultFont);
        }
        area.setDocument(new LimitedColumnsDocument(1024, 1024));
        this.add(scroll,BorderLayout.CENTER);
        this.setUpStatusBar();
        menuBar = new JMenuBar();
        menuBar.setBorder(null);
        this.setJMenuBar(menuBar);
        JMenu[] menu = {fileMenu, editMenu, formatMenu, viewMenu, helpMenu};
        for (JMenu menu1 : menu) {
            menuBar.add(menu1); 
        }

        String[] fileShortCuts = {"control N", "control O", "control S", "control P"};
        m.setItems(false,"New", "Open...", "Save", "Save As...", "Page Setup...","Print...", "Exit");
        file = m.getItems();
        Action[] a = {New,Open,Save,SaveAs,PageSetup,Print,Exit};
        m.setActions(menu,file,a,0);
        m.setAccelerator(fileMenu, fileShortCuts, 0, 1, 2, 5);
        m.setSeperators(fileMenu, 4, 6);

        String[] editShortCuts = {"control Z", "control X", "control C", "control V", "DELETE", "control F", "F3",
                                    "control H", "control G", "control A", "F5"};
        m.setItems(false, "Undo","Cut","Copy","Paste","Delete","Find...","Find Next","Replace...","Go To...",
                   "Select All","Time/Date");
        edit = m.getItems();
        Action[] b = {Undo,Cut,Copy,Paste,Delete,Find,Find_Next,Replace,GoTo,SelectAll,Time_Date};
        m.setActions(menu,edit,b,1);
        m.setAccelerator(editMenu, editShortCuts, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
        m.setSeperators(editMenu, 1, 5, 9);
        m.setEnable(editMenu,false,0);

        m.setItems(true,"Word Wrap","Font...");
        format = m.getItems();
        if (wordWrapListener) {
            format[0].setSelected(true);
        }
        else {
            format[0].setSelected(false);
        }

        format[0].addItemListener( (event)-> {
                wordWrapListener = format[0].isSelected();
                if(wordWrapListener) {
                    statusPanel.setVisible(false);
                }
                else if(!wordWrapListener && statusBar) {
                    statusPanel.setVisible(true);
                }
        });
        format[0].addChangeListener( (e) -> {
                enableItems();
        });
        Action[] c = {WordWrap,Fonts};
        m.setActions(menu,format,c,2);
        
        m.setItems(true, "Status Bar");
        view = m.getItems();
        Action[] d = {StatusBar};
        m.setActions(menu, view, d, 3);
        view[0].addItemListener( (event) ->
            statusBar = view[0].isSelected()
        );
        if (statusBar) {
            view[0].setSelected(true);
        }
        else {
            view[0].setSelected(false);
        }

        m.setItems(false, "View Help", "About KingEdit");
        help = m.getItems();
        Action[] e = {ViewHelp, About};
        m.setActions(menu,help,e,4);
        m.setSeperators(helpMenu, 1);

        //document.addDocumentListener(documentListener);
        //document.setDocumentFilter(documentFilter);
//        registry();
        area.getDocument().addUndoableEditListener(undoableEditListener);
        area.addKeyListener(keyListener);
        area.addMouseListener(popUpAdapter);
        area.addCaretListener(caretListener);
        editMenu.addMouseListener(mouseAdapter);
        viewMenu.addMouseListener(mouseAdapter);
        this.addMouseListener(popUpAdapter);
        this.enableItems();
        this.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        this.initialisePopMenu();
        this.close();
        this.setIconImage(image);
        this.setTitle(titleBar);
        this.setSize(600, 510);
        this.setLocation(dimension.width/2 - this.getSize().width/2, dimension.height/2 - this.getSize().height/2);
        this.setVisible(true);
    }
    
    private void close(){
        this.addWindowListener(
                new WindowAdapter(){
                    @Override
                    public void windowClosing(WindowEvent event){int i = area.getDocument().getLength();
                        if(i != 0){
                            changed = true;
                        }
                        else{
                            changed = false;
                        }
                        exit();
                    }
                }
        );

        this.addWindowFocusListener(
                new WindowFocusListener() {
                    @Override
                    public void windowGainedFocus(WindowEvent e) {
                        scroll.setBorder(BorderFactory.createLineBorder(Color.BLUE));
                    }

                    @Override
                    public void windowLostFocus(WindowEvent e) {
                        scroll.setBorder(BorderFactory.createLineBorder(Color.BLACK));
                    }
                }
        );
    }

    private void setUpStatusBar() {
        statusLabel.setText(statusString);
        statusLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        statusLabel.setBorder(BorderFactory.createEmptyBorder(0,0,0,25));
        statusPanel.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
        statusPanel.setLayout(new BorderLayout());
        statusPanel.add(statusLabel);
        this.add(statusPanel,BorderLayout.SOUTH);
        statusPanel.setPreferredSize(new Dimension(statusPanel.getParent().getWidth(), 20));
        statusLabel.setPreferredSize(new Dimension(statusLabel.getParent().getWidth(),20));
        if(!statusBar || wordWrapListener) {
            statusPanel.setVisible(false);
        }
    }

    private class LimitedColumnsDocument extends DefaultStyledDocument {

        private final int maxChar;
        private final int maxLine;
        private static final String EOL = "\n";

        private LimitedColumnsDocument(int maxLine, int maxChar) {
            this.maxChar = maxChar;
            this.maxLine = maxLine;
        }

        @Override
        public void insertString(int offs, String str, AttributeSet a) throws BadLocationException {
            boolean ok = true;
            String currentText = getText(0, getLength());
            String formattedText = "";
            if (getLength() > maxChar) {
                String[] lines = currentText.split("");
                int tempLength = 0;
                for (int i = 0; i < lines.length; i++) {
                    if (tempLength < maxChar ) {
                        tempLength += lines[i].length();
                        formattedText.concat(lines[i]);
                        System.out.println("Charles");
                    }
                    else {
                        formattedText.concat("\r");
                        tempLength = 0;
                        System.out.println("Charlie");
                    }
                }
                super.insertString(0, formattedText, a);
            }
            super.insertString(offs, str, a);
        }
    }

    private void registry() {
        Preferences preferences = Preferences.systemRoot();
        //preferences.putBoolean("status",true);
    }

    private final KeyListener keyListener = new KeyAdapter() {
        @Override
        public void keyPressed(KeyEvent e) {
            enableItems();
        }

        @Override
        public void keyTyped(KeyEvent e) {
            if(!editMenu.getItem(0).isEnabled()) {
                m.setEnable(editMenu,true,0);
            }
            if(!popupMenu.getComponent(0).isEnabled()) {
                popupMenu.getComponent(0).setEnabled(true);
            }
        }
    };

    private final MouseAdapter mouseAdapter = new MouseAdapter(){
        @Override
        public void mouseEntered(MouseEvent e) {
            enableItems();
        }
    };

    private final MouseAdapter popUpAdapter = new MouseAdapter() {
        @Override
        public void mousePressed(MouseEvent e) {
            showPopup(e);
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            showPopup(e);
        }

        private void showPopup(MouseEvent event) {
            if(event.isPopupTrigger()) {
                enablePopupMenuItems();
                popupMenu.show(event.getComponent(),event.getX(),event.getY());
            }
        }
    };

    private final UndoableEditListener undoableEditListener = new UndoableEditListener() {
        @Override
        public void undoableEditHappened(UndoableEditEvent e) {
            undoManager.addEdit(e.getEdit());
        }
    };

    private final CaretListener caretListener = new CaretListener() {
        @Override
        public void caretUpdate(CaretEvent e) {
            SwingUtilities.invokeLater(new CaretRunnable(e));
        }
    };

    private class CaretRunnable implements Runnable {

        CaretEvent event;

        public  CaretRunnable (CaretEvent event) {
            this.event = event;
        }

        @Override
        public void run() {
            int column = area.getCaretPosition();
            int line = 0;

            try {
                line = area.getLineOfOffset(column);
                column -= area.getLineStartOffset(line);
                line++;
            }
            catch (BadLocationException exp) {}

            statusLabel.setText(String.format("Ln %s, Col %s", line, column));
        }
    }

    Action New = new AbstractAction("New"){
        @Override
        public void actionPerformed(ActionEvent event){
               New();
        }
    };
    
    Action Open = new AbstractAction("Open"){
        @Override
        public void actionPerformed(ActionEvent event){
            open();
        }
    };
    
    Action Save = new AbstractAction("Save"){
        @Override
        public void actionPerformed(ActionEvent event){
            if(title.equals("Untitled")){
                saveFileAs();
            }
            else{
                saveFile(path, title);
            }
        }
    };
    
    Action SaveAs = new AbstractAction("Save as..."){
        @Override
        public void actionPerformed(ActionEvent event){
            saveFileAs();
        }
    };
    
    Action Exit = new AbstractAction ("Exit") {
        @Override
        public void actionPerformed(ActionEvent event){
            exit();
        }
    };
    
    Action Print = new AbstractAction("Print") {
        @Override
        public void actionPerformed(ActionEvent event){
            PrinterJob job = PrinterJob.getPrinterJob();
            if (job.printDialog()){
                try{
                    job.print();
                }
                catch(PrinterException err){}
            }
        }
    };
    
    Action PageSetup = new AbstractAction("Page Setup...") {
        @Override
        public void actionPerformed(ActionEvent event){
            PrinterJob job = PrinterJob.getPrinterJob();
            PageFormat format = job.pageDialog(job.defaultPage());
        }
    };
    
    Action Delete = new AbstractAction("Delete"){
        @Override
        public void actionPerformed(ActionEvent event){
           if(area.getSelectedText()!= null){
               area.replaceSelection("");
           }
        }
    };
    
    Action Undo = new AbstractAction("Undo"){
        @Override
        public void actionPerformed(ActionEvent event){
            undo();
        }
    };
    
    Action Find = new AbstractAction("Find"){
        public void actionPerformed(ActionEvent event){
            find();
        }
    };
    
    Action Find_Next = new AbstractAction("Find Next"){
        public void actionPerformed(ActionEvent event){
            if(findText == null) {
                find();
            }
            else {
                findNext(context, findText, isDown, isCaseSensitive);
            }
        }
    };
    
    Action Replace = new AbstractAction("Replace"){
        @Override
        public void actionPerformed(ActionEvent event){
            replace();
        }
    };
    
    Action GoTo = new AbstractAction("GoTo"){
        @Override
        public void actionPerformed(ActionEvent event){
            Goto();
        }
    };
    
    Action Time_Date = new AbstractAction("Time/Date"){
        @Override
        public void actionPerformed(ActionEvent event){
            time_date();
        }
    };
    
    Action WordWrap = new AbstractAction("Word Wrap"){
        @Override
        public void actionPerformed(ActionEvent event){
            if(wordWrapListener){
                area.setLineWrap(true);
                area.setWrapStyleWord(true);
                scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
            }
            else{
                area.setLineWrap(false);
                scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
            }
            
        }
    };

    Action Fonts = new AbstractAction("Fonts") {
        @Override
        public void actionPerformed(ActionEvent e) {
            font();
        }
    };
    
    Action StatusBar = new AbstractAction("StatusBar"){
        @Override
        public void actionPerformed(ActionEvent event){
            if(statusBar){
                statusPanel.setVisible(true);
            }
            else {
                statusPanel.setVisible(false);
            }
        }
    };

    Action ViewHelp = new AbstractAction("ViewHelp"){
        @Override
        public void actionPerformed(ActionEvent event){
            viewHelp();
        }
    };

    Action About = new AbstractAction("About"){
        @Override
        public void actionPerformed(ActionEvent event){
            about();
        }
    };

    Action RightToLeft = new AbstractAction("RightToLeft"){
        @Override
        public void actionPerformed(ActionEvent event){
        }
    };

    Action LeftToRight = new AbstractAction("LeftToRight"){
        @Override
        public void actionPerformed(ActionEvent event){
        }
    };

    Action IME = new AbstractAction("IME"){
        @Override
        public void actionPerformed(ActionEvent event){
        }
    };
    
    private void New() {
        if(changed || "Untitled".equals(title)){
            this.exit();
            JFrame _new = new TextEditor();
            _new.setVisible(true);
        }
        else {
            scroll = new JScrollPane(new JTextArea(),JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                        JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
            add(scroll, BorderLayout.CENTER);
        }
    }
    
    private void openFile(){
        fDialog = new FileDialog(this,"Open",FileDialog.LOAD);
        fDialog.setIconImage(image);
        fDialog.setVisible(true);
        String filepath = fDialog.getDirectory()+fDialog.getFile();
        String filename = fDialog.getFile();
        if(filename != null){
            readInFile(filepath, filename);
        }
    }
    private void open(){
        if(changed){
            int i = JOptionPane.showConfirmDialog(this, "Would you like to save " + title + " ?",
                    "Save", JOptionPane.YES_NO_CANCEL_OPTION);
            if(i == JOptionPane.YES_OPTION) {
                if(title.equals("Untitled")){
                    saveFileAs();
                    if(savedAs)
                        openFile();
                }
                else {    
                    saveFile(path,title);
                    openFile();
                }
            }
            else if(i == JOptionPane.NO_OPTION){
                openFile();
            }
        }
        else {
            openFile();
        }
    }
    
    private void saveFile(String filepath, String filename){
        try{
            //FileWriter w = new FileWriter(filepath);
            //area.write(w);
            OutputStream w = new FileOutputStream(filepath);
            byte[] b = fDialog.getFile().getBytes();
            w.write(b);
            for(byte c: b){
                System.out.println(c);
            }
            title = filename;
            titleBar = title + name;
            setTitle(titleBar);
            changed = false;
            Save.setEnabled(false);
        }
        catch(IOException e){}
    };
    
    private void saveFileAs(){
        fDialog = new FileDialog(this,"Save As",FileDialog.SAVE);
        fDialog.setIconImage(image);
        fDialog.setVisible(true);
        path = fDialog.getDirectory() + fDialog.getFile();
        String filename = fDialog.getFile();
        if(filename != null){
            String name = extension(filename);
            saveFile(path, name);
            savedAs = true;
        }
    }
    
    private String extension(String name){
        if(name.contains(".")){
            String fname = name.substring(0,name.lastIndexOf("."));
            String s = name.substring(name.lastIndexOf(".")+1);
            if(s == "" || s == "txt"){
                path += ".txt";
                return fname;
            }
            else{
                return name;
            }
        }
        else{
            path += ".txt";
            return name;
        }
    } 
    
    private void readInFile(String filepath, String filename) {
        try{
            FileReader r = new FileReader(filepath);
            area.read(r,null);
            r.close();
            title = filename;
            setTitle(title);
            changed = false;
        }
        catch(IOException e){
            Toolkit.getDefaultToolkit().beep();
            JOptionPane.showMessageDialog(this, "Editor can't find the file called " + filename);
        }
    }
    
    private void exit(){
        if(changed) {
            int i = JOptionPane.showConfirmDialog(this, "Would you like to save " + title + " ?", "Save", 
                    JOptionPane.YES_NO_CANCEL_OPTION) ;
            if(i == JOptionPane.YES_OPTION) {
                if(!title.equals("Untitled")) {
                    saveFile(path,title);
                    this.dispose();
                }
                else {
                    saveFileAs();
                    if(savedAs)
                        this.dispose();
                }
            }
            else if(i == JOptionPane.NO_OPTION ) 
                this.dispose();
        }
        else 
            this.dispose();

        preferences.putBoolean("WordWrap", wordWrapListener);
        preferences.putBoolean("StatusBar", statusBar);
        preferences.put("fontName", font.getName());
        preferences.putInt("fontStyle", font.getStyle());
        preferences.putInt("fontSize", font.getSize());
    }
    
    private void undo(){
        try {
            undoManager.undo();
        }
        catch (CannotUndoException ex) {}
    }
    
    private void Goto(){
        JDialog jDialog = new Goto(this,"Go To Line",true);
        jDialog.setIconImage(null);
    }

    public void font() {
        JDialog jDialog = new FontChooser(this, "Font", true);
        jDialog.setIconImage(null);
    }

    private void find() {
        JDialog jDialog = new FindDialog(this, "Find", false);
        jDialog.setIconImage(null);
    }

    public void findNext(TextEditor textEditor, String findText, boolean isDown, boolean isCaseSensitive) {
        textEditor.setFindText(findText);
        JTextArea area = textEditor.getArea();
        String text = area.getText();
        int start;

        if (!isCaseSensitive) {
            text = text.toLowerCase();
            findText = findText.toLowerCase();
        }

        if (isDown) {
            start = text.indexOf(findText, area.getSelectionEnd());
        }
        else {
            start = text.lastIndexOf(findText, area.getSelectionStart()-1);
        }

        if (start != -1) {
            area.setCaretPosition(start);
            area.moveCaretPosition(start + findText.length());
            area.getCaret().setSelectionVisible(true);
        }
        else {
            JOptionPane.showMessageDialog(this, "Cannot find " + findText, name, JOptionPane.INFORMATION_MESSAGE);
        }
    }

    public void replace() {
        JDialog jDialog = new ReplaceDialog(this, "Replace", false);
        jDialog.setIconImage(null);
    }

    private void about() {
        JDialog jDialog = new AboutDialog(this, "About KingEdit", true);
        jDialog.setIconImage(null);
    }

    private void viewHelp() {
           if (Desktop.isDesktopSupported()) {
               Desktop desktop = Desktop.getDesktop();
               try {
                   desktop.browse(new URI(url));
               }
               catch (IOException | URISyntaxException exp) {}
           }
           else {
               Runtime runtime = Runtime.getRuntime();
               try {
                   runtime.exec("xdg-open " + url);
               }
               catch (IOException exp) {}
           }
    }

    private void time_date(){
        Formatter timeformatter = new Formatter();
        Formatter dateformatter = new Formatter();
        String temp;
        String temp1;
        String temp2;
        String time;
        String date;
        String time_date;
        Calendar c = Calendar.getInstance();
        timeformatter.format("%tr", c);
        temp = timeformatter.toString();
        timeformatter.close();
        temp1 = temp.substring(0,5);
        temp2 = temp.substring(8);
        time = temp1 + temp2;
        dateformatter.format("%tD", c);
        date = dateformatter.toString();
        dateformatter.close();
        time_date = time + " " + date;
        area.insert(time_date, area.getCaretPosition());
    }
    
    /*public void checkClipboard(){
        try {
            clipboard = (String) Toolkit.getDefaultToolkit().getSystemClipboard().getData(DataFlavor.stringFlavor);
        } catch (UnsupportedFlavorException | IOException ex) {
            Logger.getLogger(TextEditor.class.getName()).log(Level.SEVERE, null, ex);
        }
    }*/
    
    private void enableItems(){
        if(area.getSelectedText() != null){
            m.setEnable(editMenu, true, 2, 3, 5);
        }
        else if(area.getSelectedText() == null) {
            m.setEnable(editMenu, false, 2, 3, 5);
        }
        if(!"".equals(clipboard)){
            m.setEnable(editMenu, true, 4);
        }
        else if("".equals(clipboard)) {
            m.setEnable(editMenu, false, 4);
        }
        if(!wordWrapListener){
            m.setEnable(editMenu, true, 10);
            m.setEnable(viewMenu, true, 0);
        }
        else if(wordWrapListener){
            m.setEnable(editMenu, false, 10);
            m.setEnable(viewMenu, false, 0);
        }
        int i = area.getDocument().getLength();
        if(i == 0){
            m.setEnable(editMenu, false, 7, 8);
        }
        if(i > 0) {
            m.setEnable(editMenu, true, 7, 8);
        }
        if(changed){
            m.setEnable(editMenu, true, 0);
        }
    }

    public JTextArea getArea(){
        return area;
    }
    
    public int getLineCount(){
        lineCount = area.getLineCount();
        return lineCount;
    }

    public String getFindText() {
        return findText;
    }

    public void setFindText(String findText) {
        this.findText = findText;
    }

    public String getGotoText() {
        return gotoText;
    }

    public void setGotoText(String gotoText) {
        this.gotoText = gotoText;
    }

    public boolean isDown() {
        return isDown;
    }

    public void setDown(boolean down) {
        isDown = down;
    }

    public boolean isCaseSensitive() {
        return isCaseSensitive;
    }

    public void setCaseSensitive(boolean aCase) {
        isCaseSensitive = aCase;
    }

    @Override
    public Font getFont() {
        return font;
    }

    @Override
    public void setFont(Font font) {
        this.font = font;
    }

    private void initialisePopMenu(){
        popupMenu.setPopupSize(260,280);
        String[] popupItems = {"Undo", "Cut", "Copy", "Paste","Delete", "SelectAll", "Right to left Reading order",
                            "Show Unicode control characters", "Open IME",
                            "Reconversion"};
        JMenu subMenu = new JMenu("Insert Unicode control characters");
        String[] subMenuItems = {"Left-to-rigth mark", "Right-to-left mark", "Zero width joiner", "Zero width non-joiner",
                                "Start of left-to-right embedding", "Start of right-to-left embedding",
                                "Start of left-to-right override", "Start of right-to-left override",
                                "Pop directional formatting", "Natonal digit shapes substitution",
                                "Nominal (European) digit shapes", "Activate symmetric swapping",
                                "Inhibit symmetric swapping", "Activate Arabic form shaping",
                                "Inhibit Arabic form shaping", "Record Separator (Block separator)",
                                "Unit Separator (Segment separator)"};
        subMenu.setLayout(new GridLayout());
        for (int i = 0; i < subMenuItems.length; i++) {
            JMenuItem menuItem = new JMenuItem(subMenuItems[i]);
            menuItem.setHorizontalAlignment(SwingConstants.RIGHT);
            subMenu.add(menuItem);
        }

        JMenuItem[] menuItems = new JMenuItem[popupItems.length];

        for(int i = 0; i < popupItems.length; i++) {
            if(popupItems[i].equals("Right to left Reading order") || popupItems[i].equals("Show Unicode control characters")) {
                menuItems[i] = new JCheckBoxMenuItem(popupItems[i]);
            }
            else if(popupItems[i].equals("Undo")) {
                menuItems[i] = new JMenuItem(popupItems[i]);
                menuItems[i].setEnabled(false);
            }
            else if(popupItems[i].equals("Reconversion")) {
                menuItems[i] = new JMenuItem(popupItems[i]);
                menuItems[i].setEnabled(false);
            }
            else {
                menuItems[i] = new JMenuItem(popupItems[i]);
            }
        }

        Action[] actions = {Undo, Cut, Copy, Paste, Delete, SelectAll, RightToLeft, LeftToRight, IME};

        int x = 0;
        for (int i = 0; i < menuItems.length; i++) {
            if(!(i == 8 || i == 10)) {
                menuItems[i].addActionListener(actions[x]);
                x++;
            }
        }

        for (int i = 0; i < menuItems.length; i++) {
            switch (i) {
                case 1 : {
                    popupMenu.addSeparator();
                    popupMenu.add(menuItems[i]);
                    break;
                }
                case 5 : {
                    popupMenu.addSeparator();
                    popupMenu.add(menuItems[i]);
                    break;
                }
                case 6 : {
                    popupMenu.addSeparator();
                    popupMenu.add(menuItems[i]);
                    break;
                }
                case 8 : {
                    popupMenu.add(subMenu);
                    popupMenu.addSeparator();
                    popupMenu.add(menuItems[i]);
                    break;
                }
                default:
                    popupMenu.add(menuItems[i]);
            }
        }
    }

    private void enablePopupMenuItems() {
        if(area.getSelectedText() != null){
            popupMenu.getComponent(2).setEnabled(true);
            popupMenu.getComponent(3).setEnabled(true);
            popupMenu.getComponent(5).setEnabled(true);
        }
        else if(area.getSelectedText() == null) {
            popupMenu.getComponent(2).setEnabled(false);
            popupMenu.getComponent(3).setEnabled(false);
            popupMenu.getComponent(5).setEnabled(false);
        }
        else if(!"".equals(clipboard)){
            System.out.println("c");
            popupMenu.getComponent(4).setEnabled(true);
        }
        else if("".equals(clipboard)) {
            System.out.println("d");
            popupMenu.getComponent(4).setEnabled(false);
        }
        int i = area.getDocument().getLength();
        if(i > 0){
            popupMenu.getComponent(7).setEnabled(true);
        }
        else if(i == 0) {
            popupMenu.getComponent(7).setEnabled(false);
        }
    }

    private static void start() {
        JFrame frame = new TextEditor();
        frame.setVisible(true);
    }

    public static void main(String[] args) {

        try{
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            start();
        }

        catch (IllegalAccessException e) {
            start();
        }

        catch (InstantiationException e) {
            start();
        }

        catch (ClassNotFoundException e) {
            start();
        }

        catch (UnsupportedLookAndFeelException e){
            start();
        }
    }
    
}
