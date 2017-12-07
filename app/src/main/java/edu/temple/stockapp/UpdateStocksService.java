package edu.temple.stockapp;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
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
import java.util.Timer;
import java.util.TimerTask;


public class UpdateStocksService extends Service {

    public static String fileName = "stock_list_data";
    public static String stockListFileName = "stock_list";
    JSONArray stock_list_data_array = new JSONArray();

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

    public void saveToFile(Context context) {
        try {
            FileOutputStream fileOutputStream = context.openFileOutput(fileName, Context.MODE_PRIVATE);
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
            objectOutputStream.writeObject(stock_list_data_array.toString());
            objectOutputStream.close();
            fileOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Accessory Method that creates an object by reading it from a file
    public static String readFromFile(Context context) {
        String stock_data = null;
        try {
            FileInputStream fileInputStream = context.openFileInput(fileName);
            ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
            stock_data = (String) objectInputStream.readObject();
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

    public static ArrayList<String> readFromListFile(Context context) {
        ArrayList<String> stock_symbols = null;
        try {
            FileInputStream fileInputStream = context.openFileInput(stockListFileName);
            ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
            stock_symbols = (ArrayList) objectInputStream.readObject();
            objectInputStream.close();
            fileInputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return stock_symbols;
    }

    public void getStocks() {
        Thread t = new Thread() {
            @Override
            public void run() {
                while (true) {
                    ArrayList<String> stocks = readFromListFile(getApplicationContext());
//                    Log.d("GetStocks", String.valueOf(stocks.size()));
                    stock_list_data_array = new JSONArray();
                    HttpURLConnection connection = null;
                    BufferedReader reader = null;
                    URL stockQuoteUrl;
                    try {
                        for (int i = 0; i < stocks.size(); i++) {
                            stockQuoteUrl = new URL("http://dev.markitondemand.com/MODApis/Api/v2/Quote/json/?symbol=" + stocks.get(i));
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

                            stock_list_data_array.put(stockObject);
                            Log.d("Test2", String.valueOf(stock_list_data_array.length()));

                            Bitmap stockImage = null;
                            try {
                                stockImage = Picasso.with(getApplicationContext()).load("https://finance.google.com/finance/getchart?p=7d&q=" + stocks.get(i)).get();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            FileOutputStream out = null;
                            try {
                                out = getApplicationContext().openFileOutput(stocks.get(i) + ".png", Context.MODE_PRIVATE);
                                stockImage.compress(Bitmap.CompressFormat.PNG, 100, out);
                            } catch (Exception e) {
                                e.printStackTrace();
                            } finally {
                                try {
                                    if (out != null) {
                                        out.close();
                                    }
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                        saveToFile(getApplicationContext());
                        String placeholder = readFromFile(getApplicationContext());
                        JSONArray jsonArr = null;
                        jsonArr = new JSONArray(placeholder);
                        Log.d("FINALFINAL", String.valueOf(jsonArr));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    try {
                        Thread.sleep(30000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        t.start();
    }
}