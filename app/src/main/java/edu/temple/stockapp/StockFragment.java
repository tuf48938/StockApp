package edu.temple.stockapp;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

public class StockFragment extends Fragment {
    public StockFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View stockFragment = inflater.inflate(R.layout.fragment_stock, container, false);
        return stockFragment;
    }

    public void updateStockDetails(String name, String price, Bitmap bm) {
        TextView companyName = (TextView) getActivity().findViewById(R.id.companyName);
        TextView retrievedPrice = (TextView) getActivity().findViewById(R.id.companyPrice);
        ImageView imageChartView = (ImageView) getActivity().findViewById(R.id.imageChartView);

        companyName.setText(name);
        retrievedPrice.setText("$" + price);
        imageChartView.setImageBitmap(bm);
    }
}