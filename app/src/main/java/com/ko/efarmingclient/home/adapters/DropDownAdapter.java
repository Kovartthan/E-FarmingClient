package com.ko.efarmingclient.home.adapters;

import android.content.Context;
import android.widget.ArrayAdapter;

import java.util.List;

public class DropDownAdapter extends ArrayAdapter<String> {

    public DropDownAdapter(Context context, int textViewResourceId,
                         List<String> objects) {
        super(context, textViewResourceId, objects);
    }


}