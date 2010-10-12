package com.synchrony.ui.tray;

/**
 *
 * @author Simon Bauer
 */
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/*
 * Listener-Klasse f_r das PopUpMen_-Item "exitItem" (siehe Systemtray.java)
 */
public class TrayExitActionListener implements ActionListener {

    //Datenelemente
    @SuppressWarnings("unused")
    private Systemtray systemtray;

    //Konstruktor mit Referenz auf den Systemtray
    public TrayExitActionListener(Systemtray systemtray) {

        this.systemtray = systemtray;
    }

    //wird ausgef_hrt wenn das exitItem des PopUpMen_s geklickt wird
    public void actionPerformed(ActionEvent e) {

        System.out.println("Die Applikation wird geschlossen...");
        System.exit(0);
    }
}
