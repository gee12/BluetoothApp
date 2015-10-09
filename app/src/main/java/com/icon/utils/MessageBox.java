package com.icon.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.widget.Toast;

/**
 * Created by Ivan on 02.10.2015.
 */
public class MessageBox {

    public static void shoter(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
    }
    public static void longer(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
    }

    public static void yesNoDialog(Context context, DialogInterface.OnClickListener listener, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(message).setPositiveButton("Да", listener)
                .setNegativeButton("Нет", listener).show();
    }
}
