package com.g5.tdp2.cashmaps.view;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.g5.tdp2.cashmaps.R;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

public class CustomInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {

    private static final String TAG = "CustomInfoWindowAdapter";
    private LayoutInflater inflater;

    public CustomInfoWindowAdapter(LayoutInflater inflater){
        this.inflater = inflater;
    }

    @Override
    public View getInfoContents(final Marker m) {
        View v = inflater.inflate(R.layout.info_atm_window, null);
        String[] info = m.getTitle().split("&");
        String bank, address, net, terms;
        bank = info[0];
        Log.d("current-location", bank);
        if (!bank.equals("current-location")) {
            address = info[1];
            net = info[2];
            terms = info[3];
        } else {
            bank="";
            address = "";
            net = "";
            terms = "";
            ((ImageView)v.findViewById(R.id.info_window_imagen)).setImageDrawable(null);
        }
        ((TextView)v.findViewById(R.id.info_window_banco)).setText(bank);
        ((TextView)v.findViewById(R.id.info_window_direccion)).setText(address);
        ((TextView)v.findViewById(R.id.info_window_red)).setText(net);
        ((TextView)v.findViewById(R.id.info_window_terminales)).setText(terms);
        return v;
    }

    @Override
    public View getInfoWindow(Marker m) {
        return null;
    }

}
