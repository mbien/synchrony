/*
 * Created on Sep 15, 2008  5:51:33 PM
 */
package com.synchrony.ui.tray;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Point;
import java.awt.PopupMenu;
import java.awt.TrayIcon;
import java.awt.Window;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JDialog;
import javax.swing.JPopupMenu;
import javax.swing.JWindow;
import javax.swing.RootPaneContainer;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

// stolen from project FishFarm. (found some issues in XP / VirtualBox)
/**
 * JPopupMenu compatible TrayIcon based on Alexander Potochkin's JXTrayIcon
 * (http://weblogs.java.net/blog/alexfromsun/archive/2008/02/jtrayicon_updat.html)
 * but uses a JWindow instead of a JDialog to workaround some bugs on linux.
 *
 * @author Michael Bien
 */
public class JPopupTrayIcon extends TrayIcon {

    private JPopupMenu menu;
    private Window window;
    private PopupMenuListener popupListener;
    private final static boolean IS_WINDOWS = System.getProperty("os.name").toLowerCase().contains("windows");

    public JPopupTrayIcon(Image image) {
        super(image);
        init();
    }

    public JPopupTrayIcon(Image image, String tooltip) {
        super(image, tooltip);
        init();
    }

    public JPopupTrayIcon(Image image, String tooltip, PopupMenu popup) {
        super(image, tooltip, popup);
        init();
    }

    public JPopupTrayIcon(Image image, String tooltip, JPopupMenu popup) {
        super(image, tooltip);
        init();
        setJPopupMenu(popup);
    }

    private void init() {

        popupListener = new PopupMenuListener() {

            @Override
            public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
//                System.out.println("popupMenuWillBecomeVisible");
            }

            @Override
            public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
//                System.out.println("popupMenuWillBecomeInvisible");
                if (window != null) {
                    window.dispose();
                    window = null;
                }
            }

            @Override
            public void popupMenuCanceled(PopupMenuEvent e) {
//                System.out.println("popupMenuCanceled");
                if (window != null) {
                    window.dispose();
                    window = null;
                }
            }
        };

        addMouseListener(new MouseAdapter() {

            @Override
            public void mousePressed(MouseEvent e) {
//                System.out.println(e.getPoint());
                showJPopupMenu(e);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
//                System.out.println(e.getPoint());
                showJPopupMenu(e);
            }
        });

    }

    private void showJPopupMenu(MouseEvent e) {
        if (e.isPopupTrigger() && menu != null) {
            if (window == null) {

                if (IS_WINDOWS) {
                    window = new JDialog((Frame) null);
                    ((JDialog) window).setUndecorated(true);
                } else {
                    window = new JWindow((Frame) null);
                }
                window.setAlwaysOnTop(true);
                Dimension size = menu.getPreferredSize();

                Point centerPoint = GraphicsEnvironment.getLocalGraphicsEnvironment().getCenterPoint();
                if (e.getY() > centerPoint.getY()) {
                    window.setLocation(e.getX(), e.getY() - size.height);
                } else {
                    window.setLocation(e.getX(), e.getY());
                }

                window.setVisible(true);

                menu.show(((RootPaneContainer) window).getContentPane(), 0, 0);

                // popup works only for focused windows
                window.toFront();

            }
        }
    }

    public final JPopupMenu getJPopupMenu() {
        return menu;
    }

    public final void setJPopupMenu(JPopupMenu menu) {
        if (this.menu != null) {
            this.menu.removePopupMenuListener(popupListener);
        }
        this.menu = menu;
        menu.addPopupMenuListener(popupListener);
    }
}
