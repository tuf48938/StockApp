package edu.temple.stockapp;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;
import java.util.Random;

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
        Drawable img = context.getDrawable(getRandom());
        textView.setGravity(Gravity.CENTER_VERTICAL);
        textView.setHeight(200);
        textView.setPadding(100,5,100,5);
        textView.setTextColor(Color.BLACK);
        textView.setTypeface(null, Typeface.BOLD_ITALIC);
        textView.setTextSize(20);
        textView.setCompoundDrawablesWithIntrinsicBounds(null,null, img,null);
        return textView;
    }

    public static int getRandom() {
        int[] array = {R.drawable.ic_trending_up_black_24dp, R.drawable.ic_trending_flat_black_24dp, R.drawable.ic_trending_down_black_24dp};
        int rnd = new Random().nextInt(array.length);
        return array[rnd];
    }
}
