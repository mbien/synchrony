package com.synchrony.ui;

/**
 *
 * @author Simon Bauer
 */
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/*
 * In dieser Klasse wird auf den Systemtray zugegriffen.
 */
public class TrayIconActionListener implements ActionListener {

    //Datenelemente
    private Systemtray systemtray;

    //Konstruktor mit Referenz auf den Systemtray
    public TrayIconActionListener(Systemtray systemtray) {

        this.systemtray = systemtray;
    }

    //...
    public void actionPerformed(ActionEvent e) {

        systemtray.getTrayIcon().setToolTip("Status-Anzeige z.B.");

    }
}
