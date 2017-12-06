package edu.temple.stockapp;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.List;

public class StockAdapter extends BaseAdapter {

    private List<String> stock_symbols;
    private Context context;

    public StockAdapter (List<String> stock_symbols, Context context) {
        this.stock_symbols = stock_symbols;
        this.context = context;
    }
    @Override
    public int getCount() {
        return stock_symbols.size();
    }

    @Override
    public Object getItem(int i) {
        return stock_symbols.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {

        TextView textView = new TextView(context);
        textView.setText(stock_symbols.get(i));
        return textView;
    }
}
