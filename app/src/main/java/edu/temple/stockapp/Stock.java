package edu.temple.stockapp;

import org.json.JSONException;
import org.json.JSONObject;

public class Stock {
    private String name, symbol;
    private String price;

    public Stock(String name, String symbol, String price) {
        this.name = name;
        this.symbol = symbol;
        this.price = price;
    }

    public Stock (JSONObject stockObject) throws JSONException{
        this(stockObject.getString("Name"), stockObject.getString("Symbol"), stockObject.getString("LastPrice"));
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    @Override
    public boolean equals(Object object){
        Stock otherStock = (Stock) object;
        return this.symbol.equalsIgnoreCase(otherStock.symbol);
    }

    public JSONObject getStockAsJSON(){
        JSONObject stockObject = new JSONObject();
        try {
            stockObject.put("Name", name);
            stockObject.put("Symbol", symbol);
            stockObject.put("LastPrice", price);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return stockObject;
    }

}