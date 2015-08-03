package com.PopCorp.Sales.Callbacks;

import java.util.TreeMap;

public interface SalesFilterListener {
    void onHideFilterMenuItem();
    void onShowFilterMenuItem(TreeMap<String, String> filterItems, String selectedItem);
}