package com.synchrony.ui;

import java.io.File;
import javax.swing.JFileChooser;

/**
 *
 * @author Simon Bauer
 */
@SuppressWarnings("serial")
public class FileChooser extends javax.swing.JDialog {

    private StartupFrame startupFrame;
    String identifier;

    public FileChooser(java.awt.Frame parent, boolean modal, StartupFrame startupFrame, String identifier) {
        super(parent, modal);
        this.startupFrame = startupFrame;
        this.identifier = identifier;
        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        initComponents();
    }

    @SuppressWarnings({"unused"})
    private void initComponents() {
        File defaultDir = null;

        String title = null;
        SecurityManager sm = null;
        JFileChooser chooser = null;
        File choice = null;

        sm = System.getSecurityManager();
        System.setSecurityManager(null);
        chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        if ((defaultDir != null) && defaultDir.exists()
                && defaultDir.isDirectory()) {
            chooser.setCurrentDirectory(defaultDir);
            chooser.setSelectedFile(defaultDir);
        }
        chooser.setDialogTitle(title);
        chooser.setApproveButtonText("OK");
        int v = chooser.showOpenDialog(null);
        switch (v) {
            case JFileChooser.APPROVE_OPTION:
                if (chooser.getSelectedFile() != null) {
                    if (chooser.getSelectedFile().exists()) {
                        choice = chooser.getSelectedFile();
                    } else {
                        File parentFile =
                                new File(chooser.getSelectedFile().getParent());

                        choice = parentFile;

                    }
                }
                break;
            case JFileChooser.CANCEL_OPTION:
            case JFileChooser.ERROR_OPTION:
        }
        chooser.removeAll();
        chooser = null;
        System.setSecurityManager(sm);

        //Auswahl im Textfeld setzen
        System.out.println(identifier);
        if (identifier.equals("jbutton2")) {
            startupFrame.getjTextField3().setText(choice.toString());
        }
        if (identifier.equals("jbutton5")) {
            startupFrame.getjTextField4().setText(choice.toString());
        }
        System.out.println(choice);
    }
}
