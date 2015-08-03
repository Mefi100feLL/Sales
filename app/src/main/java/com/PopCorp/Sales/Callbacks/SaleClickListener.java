package com.PopCorp.Sales.Callbacks;

import android.view.View;

import com.PopCorp.Sales.Data.Sale;

public interface SaleClickListener {

    void onSaleClicked(View v, Sale sale);
    void changeFavorite(Sale sale);
    void shareSale(Sale sale);
    void buySale(Sale sale);
}
