package edu.temple.stockapp;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.SearchManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Array;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class MainActivity extends Activity implements PortfolioFragment.OnFragmentInteractionListener {

    boolean twopanes;
    StockFragment stockFragment;
    PortfolioFragment portfolioFragment;
    FragmentManager fm;
    boolean isConnected;
    UpdateStocksService updateStocksService;
    String name, price;
    public static String fileName = "stock_list";
    public static String dataFileName = "stock_list_data";
    SearchView searchView;

    ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            UpdateStocksService.UpdateStocksBinder binder = (UpdateStocksService.UpdateStocksBinder) iBinder;
            updateStocksService = binder.getService();
            isConnected = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            isConnected = false;
        }
    };

    @Override
    public void onStart() {
        super.onStart();
        Intent serviceIntent = new Intent(this, UpdateStocksService.class);
        bindService(serviceIntent, connection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onStop() {
        super.onStop();
        unbindService(connection);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        portfolioFragment = new PortfolioFragment();

        stockFragment = new StockFragment();

        fm = getFragmentManager();

        if (findViewById(R.id.stock_container) != null) {
            twopanes = true;
            fm.beginTransaction().add(R.id.portfolio_container, portfolioFragment).commit();
            fm.beginTransaction().add(R.id.stock_container, stockFragment).commit();
        } else {
            fm.beginTransaction().add(R.id.portfolio_container, portfolioFragment).commit();
        }
    }

    // stuff
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu, menu);
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        final SearchView searchView = (SearchView) menu.findItem(R.id.search).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        this.searchView = searchView;
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String searchText) {
                portfolioFragment.newStock(searchText);
                searchView.setQuery("", false);
                searchView.clearFocus();
                return false;
            }

            @Override
            public boolean onQueryTextChange(String searchText) {
                return false;
            }
        });

        // Starts the looping service that updates the stock
        if (isConnected) {
            updateStocksService.getStocks();
            Log.d("File exists", "connected");
        } else {
            Log.d("File exists", "But not connected");
        }
        return super.onCreateOptionsMenu(menu);
    }

//    Handler serviceHandler = new Handler(new Handler.Callback() {
//        @Override
//        public boolean handleMessage(Message msg) {
//            JSONObject responseObject = (JSONObject) msg.obj;
//
//            try {
//                updateViews(responseObject);
//            } catch (JSONException e) {
//                e.printStackTrace();
//            }
//
//            return false;
//        }
//    });
//
//    private void updateViews(JSONObject currentStock) throws JSONException {
//        name = currentStock.getString("Name");
//        price = String.valueOf(currentStock.getDouble("LastPrice"));
//        Log.d("Name", name);
//        Log.d("Price", price);
//    }

    public void saveToFile(Context context) {
        try {
            FileOutputStream fileOutputStream = context.openFileOutput(fileName, Context.MODE_PRIVATE);
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
            objectOutputStream.writeObject(portfolioFragment.stock_symbols);
            objectOutputStream.close();
            fileOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Creates an object by reading it from a file
    public static ArrayList<String> readFromFile(Context context) {
        ArrayList<String> stock_symbols = null;
        try {
            FileInputStream fileInputStream = context.openFileInput(fileName);
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

    // Accessory Method that creates an object by reading it from a file
    public static String readFromDataFile(Context context) {
        String stock_data = null;
        try {
            FileInputStream fileInputStream = context.openFileInput(dataFileName);
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
    @Override
    public void stockSearch(String stockSymbol) {
        String placeholder = readFromDataFile(getApplicationContext());
        JSONArray jsonArr = null;
        try {
            jsonArr = new JSONArray(placeholder);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        for(int i = 0; i < jsonArr.length(); i++) {
            try {
                JSONObject explrObject = jsonArr.getJSONObject(i);
                if(explrObject.getString("Symbol").equals(stockSymbol)) {
                    name = explrObject.getString("Name");
                    price = explrObject.getString("LastPrice");
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        StockFragment stockFrag;
//        updateViews(stockSymbol);
        Toast.makeText(this, stockSymbol, Toast.LENGTH_SHORT).show();
        if (twopanes) {
            stockFrag = (StockFragment) fm.findFragmentById(R.id.stock_container);
            stockFrag.updateStockDetails(name, price);
        } else {
            fm.beginTransaction().replace(R.id.portfolio_container, stockFragment).addToBackStack(null).commit();
            fm.executePendingTransactions();
            stockFrag = (StockFragment) fm.findFragmentById(R.id.portfolio_container);
            stockFrag.updateStockDetails(name, price);

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

            return false;
        }
    });

//    private void updateViews(String stockSymbol) {
//        Stock currentStock = null;
//        Log.d("UPdateViews list", String.valueOf(updateStocksService.stock_list_data.size()));
//        for (int k = 0; k < updateStocksService.stock_list_data.size(); k++) {
//            Log.d("UPdateViews run" + k, String.valueOf(updateStocksService.stock_list_data.size()));
//
//            String holder = updateStocksService.stock_list_data.get(k).getSymbol();
//            if (stockSymbol.equals(holder)) {
//                Log.d("UPdateViews run" + k, "equals");
//
//                currentStock = updateStocksService.stock_list_data.get(k);
//                name = currentStock.getName();
//                price = currentStock.getPrice();
//            }
//            Log.d("UpdateViews for", updateStocksService.stock_list_data.get(k).getSymbol());
//        }
//
//
//    }
}
