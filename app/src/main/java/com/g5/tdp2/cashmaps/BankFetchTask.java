package com.g5.tdp2.cashmaps;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.g5.tdp2.cashmaps.domain.Atm;
import com.g5.tdp2.cashmaps.domain.AtmNet;
import com.g5.tdp2.cashmaps.gateway.AtmGateway;
import com.g5.tdp2.cashmaps.gateway.AtmRequest;
import com.g5.tdp2.cashmaps.gateway.BankGateway;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

public class BankFetchTask extends AsyncTask<AtmNet, Void, List<String>> {
    private BankGateway bankGateway;
    private Consumer<List<String>> onSuccess;

    public BankFetchTask(BankGateway bankGateway, Consumer<List<String>> onSuccess) {
        this.bankGateway = bankGateway;
        this.onSuccess = onSuccess;
    }

    @Override
    protected List<String> doInBackground(AtmNet... atmNets) {
        try {
            if (atmNets.length == 2) {
                List<String> banks = new ArrayList<>();
                banks.addAll(bankGateway.getBanks(atmNets[0]));
                banks.addAll(bankGateway.getBanks(atmNets[1]));
                Collections.sort(banks);
                return banks;
            }
            return atmNets.length > 0 ? bankGateway.getBanks(atmNets[0]) : Collections.emptyList();
        } catch (Exception e) {
            Log.e("bank-error", "Conexion interrumpida", e);
            return Collections.emptyList();
        }
    }

    @Override
    protected void onPostExecute(List<String> banks) {
        onSuccess.accept(banks);
    }
}
