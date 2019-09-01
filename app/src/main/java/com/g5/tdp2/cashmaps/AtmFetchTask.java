package com.g5.tdp2.cashmaps;

import android.os.AsyncTask;
import android.util.Log;

import com.g5.tdp2.cashmaps.domain.Atm;
import com.g5.tdp2.cashmaps.gateway.AtmGateway;
import com.g5.tdp2.cashmaps.gateway.AtmRequest;
import com.g5.tdp2.cashmaps.gateway.GatewayException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

public class AtmFetchTask extends AsyncTask<AtmRequest, Void, List<Atm>> {
    private AtmGateway atmGateway;
    private Consumer<List<Atm>> onSuccess;

    public AtmFetchTask(AtmGateway atmGateway, Consumer<List<Atm>> onSuccess) {
        this.atmGateway = atmGateway;
        this.onSuccess = onSuccess;
    }

    @Override
    protected List<Atm> doInBackground(AtmRequest... requests) {
        try {
            return requests.length > 0 ? atmGateway.getAtms(requests[0]) : new ArrayList<>();
        } catch (GatewayException e) {
            Log.e("atm-error", "Conexion interrumpida", e);
            return Collections.emptyList();
        }
    }

    @Override
    protected void onPostExecute(List<Atm> atms) {
        onSuccess.accept(atms);
    }
}
