package com.ankitgupta.android.sunshine;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import com.ankitgupta.android.sunshine.model.OpenWeatherAPIParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A placeholder fragment containing a simple view.
 */
public class ForecastFragment extends Fragment {
    private final String apiId = "ea574594b9d36ab688642d5fbeab847e";
    private final String defaultLanguage = "English";
    Spinner languageSpinner;
    private final String className = this.getClass().getSimpleName();
    private ArrayAdapter<String> weatherAdapter;
    private OpenWeatherAPIParams openWeatherAPIParams;
    private Map<String, String> languages;
    public ForecastFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        languages = new HashMap<String, String>();
        languages.put("English", "en");
        languages.put("Russian", "ru");
        languages.put("Italian", "it");
        languages.put("Spanish", "es");
        languages.put("Ukrainian", "uk");
        languages.put("German", "de");
        languages.put("Portuguese", "pt");
        languages.put("Polish", "pl");
        languages.put("Finnish", "fi");
        languages.put("Dutch", "nl");
        languages.put("French", "fr");
        languages.put("Bulgarian", "bg");
        languages.put("Swedish", "sv");
        languages.put("Chinese Traditional", "zh_tw");
        languages.put("Chinese Simplified", "zh_cn");
        languages.put("Turkish", "tr");
        languages.put("Croatian", "hr");
        languages.put("Catalan", "ca");

        List<String> languagesList = new ArrayList<String>();
        languagesList.addAll(languages.keySet());
        Collections.sort(languagesList, String.CASE_INSENSITIVE_ORDER);
        weatherAdapter = new ArrayAdapter<String>(
                getActivity(),
                R.layout.list_item_forecast,
                R.id.list_item_forecast_textview,
                new ArrayList<String>());
        ListView lv = (ListView) rootView.findViewById(R.id.listview_forecast);
        lv.setAdapter(weatherAdapter);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                CharSequence text = "Button Clicked";
                int duration = Toast.LENGTH_SHORT;
                Toast toast = Toast.makeText(getActivity(), text, duration);
                toast.show();
            }
        });
        languageSpinner = (Spinner) rootView.findViewById(R.id.language_spinner);
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, languagesList);
        languageSpinner.setAdapter(spinnerAdapter);
        languageSpinner.setSelection(languagesList.indexOf(defaultLanguage));

        languageSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            public void onItemSelected(AdapterView<?> arg0, View view, int position, long id) {
                String newLanguge = (String )languageSpinner.getSelectedItem();
                updateWeather(languages.get(newLanguge));
            }
            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });

        updateWeather();
        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.forecast_fragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id==R.id.action_refresh){
            updateWeather();
        }
        return super.onOptionsItemSelected(item);
    }
    private void updateWeather(String language){
        FetchWeatherTask weatherTask =  new FetchWeatherTask();
        String postalCode = "560078,India";
        String mode = "json";
        String units = "metric";
        String cnt = "7";
//        openWeatherAPIParams = new OpenWeatherAPIParams( apiId, postalCode,  mode,  units,  cnt,  language);
        openWeatherAPIParams = new OpenWeatherAPIParams( apiId, postalCode);
        openWeatherAPIParams.setMode(mode);
        openWeatherAPIParams.setUnits(units);
        openWeatherAPIParams.setCnt(cnt);
        openWeatherAPIParams.setLang(language);

        final String PARAM_QUERY = "q";
        final String PARAM_MODE = "mode";
        final String PARAM_UNITS = "units";
        final String PARAM_COUNT = "cnt";
        final String PARAM_APPID = "appid";
        final String PARAM_LANGUAGE = "lang";

        Map<String, String> params = new HashMap<String, String>();
        params.put(PARAM_QUERY,postalCode);
        params.put(PARAM_MODE,mode);
        params.put(PARAM_UNITS,units);
        params.put(PARAM_COUNT,cnt);
        params.put(PARAM_APPID,apiId);
        params.put(PARAM_LANGUAGE,language);

        weatherTask.execute(params);
    }

    private void updateWeather(){
        String lang = languages.get(defaultLanguage);
        updateWeather(lang);
    }

    public class FetchWeatherTask extends AsyncTask{
        private final String className = this.getClass().getSimpleName();
        @Override
        protected String[] doInBackground(Object[] params) {
            Map<String, String> httpParams = (Map<String, String>) params[0];
//            OpenWeatherAPIParams httpParams = (OpenWeatherAPIParams) params[0];

            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Will contain the raw JSON response as a string.
            String forecastJsonStr = null;

            final String FORECAST_DOMAIN = "api.openweathermap.org";
            final String FORECAST_PATH = "data/2.5/forecast/daily";

            Uri.Builder builder = new Uri.Builder();
            builder.scheme("http")
                    .authority(FORECAST_DOMAIN)
                    .appendPath(FORECAST_PATH)
            ;
            for (String key : httpParams.keySet()){
                builder.appendQueryParameter(key,httpParams.get(key));
            }

            Uri uri = builder.build();
            String weatherAPI = uri.toString();
            Log.d(className, "Fetch weatherAPI from " + uri.toString());
            try {
                // Construct the URL for the OpenWeatherMap query
                // Possible parameters are avaiable at OWM's forecast API page, at
                // http://openweathermap.org/API#forecast
                URL url = new URL(weatherAPI);

                // Create the request to OpenWeatherMap, and open the connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier if you print out the completed
                    // buffer for debugging.
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    return null;
                }
                forecastJsonStr = buffer.toString();
            } catch (IOException e) {
                Log.e(this.getClass().getName(), "Error ", e);
                // If the code didn't successfully get the weather data, there's no point in attemping
                // to parse it.
                return null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(className, "Error closing stream", e);
                    }
                }
            }
            return getWeatherArrayFromJSON(forecastJsonStr,5);
        }

        @Override
        protected void onPostExecute(Object o) {
            String[] forcastlist = (String[]) o;
            weatherAdapter.clear();
            weatherAdapter.addAll(forcastlist);

        }

        private String[] getWeatherArrayFromJSON(String forecastJsonStr, int NbrDays){
            ArrayList<String> weatherArray = new ArrayList<String>();
            try {
                JSONObject forecastJson = new JSONObject(forecastJsonStr);
                JSONArray forecastList = forecastJson.getJSONArray("list");
                JSONObject dayForcast;
                for (int i=0;i<forecastList.length();i++){
                    dayForcast = forecastList.getJSONObject(i);
                    weatherArray.add(dayForcast(dayForcast,i));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return (String[]) weatherArray.toArray(new String[weatherArray.size()]);
        }
        private String dayForcast(JSONObject dayForcast, int dayNbrFromToday)throws JSONException{
            //"Mon, Jun 1 - Clear - 18/13"
            String dayforcastText = "";
            if (dayNbrFromToday==0) {
                dayforcastText += "Today : ";
            }
            else if (dayNbrFromToday==1)
                dayforcastText += "Tomorrow : ";
            else
                dayforcastText = getDate(dayNbrFromToday) + " : ";

            dayforcastText += getWeatherDesc(dayForcast) + " - " + getMaxTemp(dayForcast) + "/" + getMinTemp(dayForcast);
            return dayforcastText;
        }

        private String getDate(int dayNbrFromToday){
            Calendar cal = Calendar.getInstance();
            //cal.setTime( dateFormat.parse( inputString ) );
            cal.add( Calendar.DATE, dayNbrFromToday );
            Date date = cal.getTime();
            SimpleDateFormat dt = new SimpleDateFormat("EEE, dd MMM yyyy");
            return dt.format(date);
        }

        private int getMaxTemp(JSONObject dayForcast) throws JSONException {
            JSONObject dayTemp = (JSONObject) dayForcast.get("temp");
            Object maxtmp = dayTemp.get("max");
            int maxTemp = 0;
            if (maxtmp instanceof Integer){
                maxTemp = (int) maxtmp;
            } else if (maxtmp instanceof Double) {
                Double maxTempdouble = ((Double) dayTemp.get("max"));
                maxTemp = maxTempdouble.intValue();
            }
            return  (maxTemp) ;
        }

        private double getMinTemp(JSONObject dayForcast) throws JSONException {
            JSONObject dayTemp = (JSONObject) dayForcast.get("temp");
            return  (double) dayTemp.get("min");
        }
        private String getWeatherDesc(JSONObject dayForcast) throws JSONException {
            JSONArray dayTemp = (JSONArray) dayForcast.get("weather");
            JSONObject daytmp =(JSONObject) dayTemp.get(0);
            return (String) daytmp.get("description");
        }
        private String getDate(JSONObject dayForcast) throws JSONException {
            JSONObject dayTemp = (JSONObject) dayForcast.get("weather");
            return (String) dayTemp.get("main");
        }
    }
}
