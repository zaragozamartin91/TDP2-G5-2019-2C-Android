package com.g5.tdp2.cashmaps;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.Toast;

import java.util.concurrent.atomic.AtomicBoolean;

public class ConnectionTask extends BroadcastReceiver {
    Context context;

    public ConnectionTask(Context context) {
        this.context = context;
    }

    AtomicBoolean connOk = new AtomicBoolean(true);

    @Override
    public void onReceive(Context context, Intent intent) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();

        if (isConnected && connOk.compareAndSet(false, true)) {
            Toast.makeText(context, R.string.internet_recover, Toast.LENGTH_SHORT).show();
        } else if (!isConnected) {
            connOk.set(false);
            Toast.makeText(context, R.string.no_access_internet, Toast.LENGTH_SHORT).show();
        }
    }
}
