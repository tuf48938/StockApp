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
    public void onStart(){
        super.onStart();
        Intent serviceIntent = new Intent(this, UpdateStocksService.class);
        bindService(serviceIntent, connection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onStop(){
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
//        // Figure out how to read/write array to file
//        String list[] =
//                {"Dog", "Cat", "Mouse", "Elephant", "Rat", "Parrot"};
//
//        File file = new File(MainActivity.this.getFilesDir(), "Symbols");
//
//        FileOutputStream stream = null;
//        try {
//            stream = new FileOutputStream(file);
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        }
//        try {
//            stream.write(list.toString().getBytes());
//        } catch (IOException e) {
//            e.printStackTrace();
//        } finally {
//            try {
//                stream.close();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//
//        int length = (int) file.length();
//
//        byte[] bytes = new byte[length];
//
//        FileInputStream in = null;
//        try {
//            in = new FileInputStream(file);
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        }
//        try {
//            in.read(bytes);
//        } catch (IOException e) {
//            e.printStackTrace();
//        } finally {
//            try {
//                in.close();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//
//        String contents = new String(bytes);
//        Toast.makeText(this, contents.toString(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.search).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String searchText) {
                portfolioFragment.newStock(searchText);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String searchText) {
                return false;
            }
        });

        return super.onCreateOptionsMenu(menu);
    }

    Handler serviceHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            JSONObject responseObject = (JSONObject) msg.obj;

            try {
                updateViews(responseObject);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return false;
        }
    });

    private void updateViews(JSONObject currentStock) throws JSONException {
        name = currentStock.getString("Name");
        price = String.valueOf(currentStock.getDouble("LastPrice"));
        Log.d("Name", name);
        Log.d("Price", price);
    }

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
        }
        catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return stock_symbols;
    }

    @Override
    public void stockSearch(String stockSymbol) {
        StockFragment stockFrag;
        Toast.makeText(this, stockSymbol, Toast.LENGTH_SHORT).show();
        if (isConnected){
            updateStocksService.getStock(stockSymbol, serviceHandler);
        }
        if (twopanes) {
            stockFrag = (StockFragment) fm.findFragmentById(R.id.stock_container);
            stockFrag.updateStockDetails(name,price);
        } else {
            fm.beginTransaction().replace(R.id.portfolio_container, stockFragment).addToBackStack(null).commit();
            fm.executePendingTransactions();
            stockFrag = (StockFragment) fm.findFragmentById(R.id.portfolio_container);
            stockFrag.updateStockDetails(name,price);

        }
    }
}
