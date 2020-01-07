package com.posstation.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.WindowManager;

/**
 * Created by yyzz on 2018/5/18.
 */

public class DialogUtils {

    private DialogUtils() {
    }

    private static class Holder {
        private static DialogUtils INSTANCE = new DialogUtils();
    }

    public static DialogUtils getInstace() {
        return Holder.INSTANCE;
    }

    public static void destoryDialog() {

    }

    public static Dialog showProgress(Context context, String title, String message) {
        return ProgressDialog.show(context, title, message);
    }

    public static Dialog showProgress(Context context, String title, String message, DialogInterface.OnCancelListener cancelListener) {
        return ProgressDialog.show(context, title, message, false, true, cancelListener);
    }


    public static Dialog show(Context context, String message) {
        return show(context, null, message, true, "OK", null, null, null, null, null, null, null, null);
    }

    public static Dialog show(Context context, String title, String message) {
        return show(context, title, message, true, "OK", null, null, null, null, null, null, null, null);
    }

    public static Dialog show(Context context, String title, String message, String confirmButton) {
        return show(context, title, message, true, confirmButton, null, null, null, null, null, null, null, null);
    }

    public static Dialog show(Context context, String title, String message, String confirmButton, String cancelButton, DialogInterface.OnClickListener confirmListener, DialogInterface.OnClickListener cancelListener) {
        return show(context, title, message, true, confirmButton, confirmListener, null, null, cancelButton, cancelListener, null, null, null);
    }


    public static Dialog show(Context context, String title, String message, String cancelButton, DialogInterface.OnClickListener canListener, DialogInterface.OnCancelListener cancelListener, int a) {
        return show(context, title, message, true, null, null, null, null, cancelButton, canListener, null, cancelListener, null);
    }


    public static Dialog show(Context context, String title, String message, String confirmButton, DialogInterface.OnClickListener confirmListener, DialogInterface.OnCancelListener onCancelListener) {
        return show(context, title, message, true, confirmButton, confirmListener, null, null, null, null, null, onCancelListener, null);
    }

    public static Dialog show(Context context, String title, String message, boolean cancelable,
                              String confirmButton, DialogInterface.OnClickListener confirmListener,
                              String centerButton, DialogInterface.OnClickListener centerListener,
                              String cancelButton, DialogInterface.OnClickListener cancelListener,
                              DialogInterface.OnShowListener onShowListener,
                              DialogInterface.OnCancelListener onCancelListener,
                              DialogInterface.OnDismissListener onDismissListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context).setCancelable(cancelable);

        if (title != null) {
            builder.setTitle(title);
        }
        if (message != null) {
            builder.setMessage(message);
        }
        if (confirmButton != null) {
            builder.setPositiveButton(confirmButton, confirmListener);
        }
        if (centerButton != null) {
            builder.setNeutralButton(centerButton, centerListener);
        }
        if (cancelButton != null) {
            builder.setNegativeButton(cancelButton, cancelListener);
        }
        if (cancelable) {
            builder.setOnCancelListener(onCancelListener);
        }
        AlertDialog alertDialog = builder.create();
        alertDialog.setOnShowListener(onShowListener);
        alertDialog.setOnDismissListener(onDismissListener);
        if (!(context instanceof Activity)) {
            if (alertDialog.getWindow() != null) {
                alertDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
            }
        }
        alertDialog.show();
        return alertDialog;
    }
}
