package edu.temple.stockapp;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;



public class UpdateStocksService extends Service {
    public UpdateStocksService() {
    }

    IBinder updateStockBinder = new UpdateStocksBinder();

    public class UpdateStocksBinder extends Binder {
        UpdateStocksService getService () {
            return UpdateStocksService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return updateStockBinder;
    }

    public void getStock(final String symbol, final Handler handler) {
        Thread t = new Thread() {
            @Override
            public void run() {
                String TAG = "test";
                Log.d(TAG, "Im in new thread");

                HttpURLConnection connection = null;
                BufferedReader reader = null;
                URL stockQuoteUrl;
                try {

                    stockQuoteUrl = new URL("http://dev.markitondemand.com/MODApis/Api/v2/Quote/json/?symbol=" + symbol);
                    connection = (HttpURLConnection) stockQuoteUrl.openConnection();
                    connection.setRequestMethod("GET");
                    connection.setConnectTimeout(5000);
                    InputStream stream = connection.getInputStream();
                    reader = new BufferedReader(new InputStreamReader(stream));
                    StringBuilder sB = new StringBuilder();

                    String line = "";
                    while ((line = reader.readLine()) != null) {
                        sB.append(line);
                    }

                    connection.disconnect();
                    String jsonValue = sB.toString();
                    JSONObject stockObject = new JSONObject(jsonValue);

                    String companyName = stockObject.getString("Name");
                    Double price = stockObject.getDouble("LastPrice");

                    Log.d("name: ", companyName);
                    Log.d("price: ", String.valueOf(price));

                    Message msg = Message.obtain();
                    msg.obj = stockObject;
                    handler.sendMessage(msg);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        t.start();
    }
}