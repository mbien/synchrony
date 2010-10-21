package com.synchrony.ui.notification;

import java.awt.TrayIcon;

/**
 *
 * @author mbien
 */
public class WindowsBubbleService extends NotificationService {

    private final TrayIcon tray;

    private WindowsBubbleService(TrayIcon tray) {
        this.tray = tray;
    }

    static NotificationService load(TrayIcon tray) {
        if(tray == null) {
            return null;
        }
        return new WindowsBubbleService(tray);
    }

    @Override
    public void showNotification(String caption, String text) {
        tray.displayMessage(caption, text, TrayIcon.MessageType.INFO);
    }

}
