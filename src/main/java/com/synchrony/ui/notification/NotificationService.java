package com.synchrony.ui.notification;

import java.awt.TrayIcon;

/**
 *
 * @author Michael Bien
 */
public abstract class NotificationService {
    
    private static NotificationService service;

    public static synchronized NotificationService getDefault(TrayIcon tray) {
        if(service != null) {
            return service;
        }
        if ((service = LibNotifyService.load()) != null) {
            return service;
        }
        if ((service = WindowsBubbleService.load(tray)) != null) {
            return service;
        }

        return null;
    }

    public abstract void showNotification(String caption, String text);


}
