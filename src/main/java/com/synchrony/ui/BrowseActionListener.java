package com.synchrony.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class BrowseActionListener implements ActionListener {

    private String string;
    private StartupFrame startupFrame;

    public BrowseActionListener(String string, StartupFrame startupFrame) {
        this.string = string;
        this.startupFrame = startupFrame;
    }

    @Override
    public void actionPerformed(ActionEvent arg0) {
        FileChooser dialog = new FileChooser(new javax.swing.JFrame(),
                true, startupFrame, string);
        dialog.addWindowListener(new java.awt.event.WindowAdapter() {

            public void windowClosing(java.awt.event.WindowEvent e) {
            }
        });


    }
}
