/*
 * Created on Sunday, November 28 2010
 */
package com.synchrony.ui;

import com.synchrony.config.Config;
import com.synchrony.ui.config.ConfigurationFrame;
import com.synchrony.ui.notification.NotificationService;
import com.synchrony.ui.tray.JPopupTrayIcon;
import java.awt.AWTException;
import java.awt.Image;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.InvocationTargetException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

/**
 * Responsible for boot strapping the ui.
 * @author mbien
 */
public class SynchronyUIManager {
    
    private static final Logger LOG = Logger.getLogger(SynchronyUIManager.class.getName());
    
    static {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (final ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
            LOG.log(Level.INFO, "system look and feal init failed.", ex);
        }
    }
    
    private final Config config;
    private NotificationService notserve;

    public SynchronyUIManager(Config config) {
        this.config = config;
    }

    public void init() {
        try {
            SwingUtilities.invokeAndWait(new Runnable() {
                @Override
                public void run() {
                    initUI();
                }
            });
        } catch (final InterruptedException | InvocationTargetException ex) {
            throw new RuntimeException("ui not initialized.", ex);
        }
        
    }
    
    private void initUI() {
        try {
            SystemTray tray = SystemTray.getSystemTray();
            Image image = Toolkit.getDefaultToolkit().getImage(getClass().getResource("synchrony-logo.png"));

            JPopupMenu menu = new JPopupMenu("common");
            
            JMenuItem options = new JMenuItem("configure");
            JMenuItem pause = new JMenuItem("pause");
            JMenuItem quit = new JMenuItem("quit");
            
            menu.add(options);
//            menu.add(pause); todo
            menu.add(quit);
            
            quit.addActionListener(exitAction());
            options.addActionListener(configureAction());

            JPopupTrayIcon icon = new JPopupTrayIcon(image, "synchrony", menu);
            icon.setImageAutoSize(true);
            tray.add(icon);

            notserve = NotificationService.getDefault(icon);
//            notserve.showNotification("test", "foobar");

        } catch (AWTException ex) {
            LOG.log(Level.SEVERE, "", ex);
        }
    }

    public NotificationService getNotificationService() {
        return notserve;
    }
    

    private ActionListener exitAction() {
        return new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                System.exit(0);
            }
        };
    }

    private ActionListener configureAction() {
        return new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                ConfigurationFrame frame = new ConfigurationFrame(config);
                frame.setVisible(true);
            }
        };
    }

}
