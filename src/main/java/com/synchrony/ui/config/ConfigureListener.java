package com.synchrony.ui.config;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 *
 * @author Simon Bauer
 */
public class ConfigureListener implements ActionListener {

    @Override
    public void actionPerformed(ActionEvent arg0) {
        java.awt.EventQueue.invokeLater(new Runnable() {

            public void run() {
                new StartupFrame().setVisible(true);
            }
        });

    }
}
