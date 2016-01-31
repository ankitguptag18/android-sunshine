package com.ankitgupta.android.sunshine;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v7.widget.AppCompatTextView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment {

    public MainActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        String[] weekForecastarray = {
                "Today - Sunny - 88/63","Tomorrow - Sunny - 88/63"
        };
        List <String> weekForecast = new ArrayList<String>(
                Arrays.asList(weekForecastarray)
        );
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(),
                R.layout.list_item_forecast,R.id.list_item_forecast_textview, weekForecast);

        ListView lv = (ListView) rootView.findViewById(R.id.listview_forecast);
        lv.setAdapter(adapter);
        return rootView;
    }
}
