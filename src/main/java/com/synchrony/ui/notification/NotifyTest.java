package com.synchrony.ui.notification;

import com.synchrony.ui.tray.JPopupTrayIcon;
import java.awt.AWTException;
import java.awt.Image;
import java.awt.SystemTray;
import java.awt.Toolkit;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;

/**
 *
 * @author mbien
 */
public class NotifyTest {
    public static void main(String[] args) {

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    SystemTray tray = SystemTray.getSystemTray();
                    Image image = Toolkit.getDefaultToolkit().getImage("synchrony-logo.gif");

                    JPopupMenu menu = new JPopupMenu("common");
                    menu.add(new JMenuItem("test"));

                    JPopupTrayIcon icon = new JPopupTrayIcon(image, "synchrony", menu);
                    icon.setImageAutoSize(true);

                    tray.add(icon);

                    NotificationService.getDefault(icon).showNotification("test", "foobar");

                } catch (AWTException ex) {
                    ex.printStackTrace();
                }
            }
        });

    }
}
