package com.g5.tdp2.cashmaps;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.Toast;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Verifica cambios en la conectividad
 */
public class ConnectionTask extends BroadcastReceiver {
    private AtomicBoolean firstRcv = new AtomicBoolean(true); // switch de "primer cambio de estado"
    private AtomicBoolean connOk = new AtomicBoolean(true);

    @Override
    public void onReceive(Context context, Intent intent) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        boolean isConnected = Optional.ofNullable(cm.getActiveNetworkInfo())
                .map(NetworkInfo::isConnectedOrConnecting)
                .orElse(false);

        if (!isConnected) {
            handleNoConnection(context);
        } else if (connOk.compareAndSet(false, true)) {
            // si se recupero la conexion muestro un mensaje indicando ello
            Toast.makeText(context, R.string.internet_recover, Toast.LENGTH_SHORT).show();
        }

        firstRcv.set(false);
    }

    private void handleNoConnection(Context context) {
        if (firstRcv.compareAndSet(true, false)) {
            // si no tengo conexion y es la primera interaccion de la app -> muestro dialogo con boton de cierre
            new AlertDialog.Builder(context)
                    .setTitle(R.string.init_conn_error_title)
                    .setMessage(R.string.init_conn_error_msg)
                    .setPositiveButton(R.string.init_conn_error_close, (dialog, which) -> {
                        context.unregisterReceiver(ConnectionTask.this);
                        ((Activity) context).finish();
                    })
                    .setCancelable(false)
                    .create()
                    .show();
        } else {
            // si se perdio la conexion muestro un mensaje informando ello
            connOk.set(false);
            Toast.makeText(context, R.string.no_access_internet, Toast.LENGTH_SHORT).show();
        }
    }
}
