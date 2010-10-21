package com.synchrony.ui.notification;

import com.sun.jna.Library;
import com.sun.jna.Native;

/**
 *
 * @author mbien
 */
public class LibNotifyService extends NotificationService {

    private static LibNotifyService instance = null;

    private static LibNotify lib = null;

    private LibNotifyService() {
        if(lib == null) {
            lib = (LibNotify) Native.loadLibrary("libnotify", LibNotify.class);
            lib.notify_init(getClass().getName());
        }
    }

    @Override
    public void showNotification(String caption, String text) {
        long pointer = lib.notify_notification_new(caption, text, null, null);
        lib.notify_notification_show(pointer, null);
        lib.g_object_unref(pointer);
    }

    static NotificationService load() {
        try{
            if(instance == null) {
                instance = new LibNotifyService();
            }
        }catch(Exception ex) {
            // ignore
        }catch(UnsatisfiedLinkError err) {
            // ignore
        }
        return instance;
    }

    /**
     * Binding to libnotify.
     *
     * @author mbien
     */
    public interface LibNotify extends Library {


    //     typedef void  (*notify_init_t)(char *);
    //     typedef void *(*notify_notification_new_t)( char *, char *, char *, char *);
    //     typedef void  (*notify_notification_set_timeout_t)( void *, int );
    //     typedef void (*notify_notification_show_t)(void *, char *);

        public void notify_init(String foo);

        public long notify_notification_new(String a, String b, String c, String d);

        public boolean notify_notification_show(long n, String foo);

        public void g_object_unref(long n);
    }

}
