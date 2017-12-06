package edu.temple.stockapp;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;


public class UpdateStocksService extends Service {

    public static String fileName = "stock_list_data";
    public static List<Stock> stock_list_data = new ArrayList<Stock>();
    ;

    public UpdateStocksService() {
    }

    IBinder updateStockBinder = new UpdateStocksBinder();

    public class UpdateStocksBinder extends Binder {
        UpdateStocksService getService() {
            return UpdateStocksService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return updateStockBinder;
    }

    // Accessory Method that will handle the update of current stock list data every min
    public void updateStocks(ArrayList<String> currentStocks) {
        int totalStocks = 0;
        if (currentStocks != null) {
            totalStocks = currentStocks.size();
        }
        Log.d("UpdateStocks", String.valueOf(currentStocks));
        for (int i = 0; i < totalStocks; i++) {
            getStock(currentStocks.get(i), serviceHandler);
        }
    }

    Handler serviceHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {

            JSONObject responseObject = (JSONObject) msg.obj;
            Stock newStock = null;
            try {
                newStock = new Stock(responseObject);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            Log.d("Response", newStock.getName());

            stock_list_data.add(newStock);

//            saveToFile(getApplicationContext());
//
//            File file = new File(getApplicationContext().getFilesDir(), fileName);
//
//            if (file.exists()) {
//                ArrayList<Stock> stocks = readFromFile(getApplicationContext());
//                int totalStocks = stocks.size();
//                for (int i = 0; i < totalStocks; i++) {
//                    Log.d("Reading File. Index " + i, String.valueOf(readFromFile(getApplicationContext()).get(i)));
//                }
//                Log.d("Service: File", "Does exist");
//            } else {
//                Log.d("Service: File", "Does not exist");
//            }

            return false;
        }
    });

    public void saveToFile(Context context) {
        try {
            FileOutputStream fileOutputStream = context.openFileOutput(fileName, Context.MODE_PRIVATE);
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
            objectOutputStream.writeObject(stock_list_data);
            objectOutputStream.close();
            fileOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Accessory Method that creates an object by reading it from a file
    public static ArrayList<Stock> readFromFile(Context context) {
        ArrayList<Stock> stock_data = null;
        try {
            FileInputStream fileInputStream = context.openFileInput(fileName);
            ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
            stock_data = (ArrayList<Stock>) objectInputStream.readObject();
            objectInputStream.close();
            fileInputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return stock_data;
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