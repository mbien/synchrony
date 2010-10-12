package com.synchrony.ui;

/**
 *
 * @author Simon Bauer
 */
import java.awt.TrayIcon;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

/*
 * Mouse-Listener f_r das Icon in Symbolleiste
 */
public class TrayMouseListener implements MouseListener {

    //Datenelemente
    private Systemtray systemtray;

    //Konstruktor mit Referenz auf den Systemtray
    public TrayMouseListener(Systemtray systemtray) {

        this.systemtray = systemtray;
    }

    //mouseClicked-Event
    public void mouseClicked(MouseEvent e) {
        System.out.println("mouseClicked");

        //eine Info ausgeben
        systemtray.getTrayIcon().displayMessage("Info",
                "Hier kann dann eine Info ausgegeben werden!",
                TrayIcon.MessageType.INFO);
    }

    //mouseEntered-Event
    public void mouseEntered(MouseEvent e) {
        System.out.println("mouseEntered");
    }

    //mouseExited-Event
    public void mouseExited(MouseEvent e) {
        System.out.println("mouseExited");
    }

    //mousePressed-Event
    public void mousePressed(MouseEvent e) {
        System.out.println("mousePressed");
    }

    //mouseReleased-Event
    public void mouseReleased(MouseEvent e) {
        System.out.println("mouseReleased");
    }
}
