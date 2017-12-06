package edu.temple.stockapp;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

public class PortfolioFragment extends Fragment {

    List<String> stock_symbols;
    StockAdapter stockAdapter;
    private OnFragmentInteractionListener mListener;
    public static String fileName = "stock_list";

    public PortfolioFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_portfolio, container, false);
        stock_symbols = new ArrayList<String>();
        ListView listView = (ListView) view.findViewById(R.id.stock_list);
        stockAdapter = new StockAdapter(stock_symbols, getContext());
        listView.setAdapter(stockAdapter);

        File file = new File(getContext().getFilesDir(), fileName);

        Log.d("Recreating View", "RecreatingView");
        if (file.exists()) {
            ArrayList<String> stocks = readFromFile(getContext());
            int totalStocks = stocks.size();
            for (int i = 0; i < totalStocks; i++) {
                stock_symbols.add(stocks.get(i));
            }
            Log.d("File", "Does exist");
        } else {
            Log.d("File", "Does not exist");
        }

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Toast.makeText(getActivity(), ((TextView) view).getText(), Toast.LENGTH_SHORT).show();
                mListener.stockSearch((String) ((TextView) view).getText());
            }
        });
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public void saveToFile(Context context) {
        try {
            FileOutputStream fileOutputStream = context.openFileOutput(fileName, Context.MODE_PRIVATE);
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
            objectOutputStream.writeObject(stock_symbols);
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

    public interface OnFragmentInteractionListener {
        void stockSearch(String stockSymbol);
    }

    public void newStock(String stock) {
        Log.d("New Stock Added", stock);
        stock_symbols.add(stock);
        stockAdapter.notifyDataSetChanged();
        saveToFile(getContext());
        int totalStoks = readFromFile(getContext()).size();
        for (int i = 0; i < totalStoks; i++) {
            Log.d("Reading File. Index " + i, readFromFile(getContext()).get(i));
        }
    }
}
