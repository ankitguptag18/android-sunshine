package com.ankitgupta.android.sunshine;

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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment {
    private final String defaultLanguage = "English";
    private Spinner languageSpinner;
    private final String className = this.getClass().getSimpleName();
    private ArrayAdapter<String> weatherAdapter;
    private Map<String, String> languages;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        languages = new HashMap<>();
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

        List<String> languagesList = new ArrayList<>();
        languagesList.addAll(languages.keySet());
        Collections.sort(languagesList, String.CASE_INSENSITIVE_ORDER);
        weatherAdapter = new ArrayAdapter<>(
                getActivity(),
                R.layout.list_item_forecast,
                R.id.list_item_forecast_textview,
                new ArrayList<String>());
        ListView lv = (ListView) rootView.findViewById(R.id.listview_forecast);
        lv.setAdapter(weatherAdapter);

        languageSpinner = (Spinner) rootView.findViewById(R.id.language_spinner);
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, languagesList);
        languageSpinner.setAdapter(spinnerAdapter);
        languageSpinner.setSelection(languagesList.indexOf(defaultLanguage));

        languageSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            public void onItemSelected(AdapterView<?> arg0, View view, int position, long id) {
                String newLanguage = (String )languageSpinner.getSelectedItem();
                updateWeather(languages.get(newLanguage));
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
        final String apiId = "44db6a862fba0b067b1930da0d769e98";
        String postalCode = "560078,India";
        String mode = "json";
        String units = "metric";
        String cnt = "7";
        weatherTask.execute(new OpenWeatherAPIParams( apiId, postalCode,  mode,  units,  cnt,  language));
    }

    private void updateWeather(){
        String lang = languages.get(defaultLanguage);
        updateWeather(lang);
    }

    public class FetchWeatherTask extends AsyncTask{
        private final String className = this.getClass().getSimpleName();
        @Override
        protected String[] doInBackground(Object[] params) {
            OpenWeatherAPIParams httpParams = (OpenWeatherAPIParams) params[0];
            //Log.i(className, "Input Params Item Count " + httpParams.size());

            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Will contain the raw JSON response as a string.
            String forecastJsonStr = null;

            Uri.Builder builder = new Uri.Builder();
            builder.scheme("http")
                    .authority("api.openweathermap.org")
                    .appendPath("data/2.5/forecast/daily")
                    .appendQueryParameter("q", httpParams.getPostalCode())
                    .appendQueryParameter("mode", httpParams.getMode())
                    .appendQueryParameter("units", httpParams.getUnits())
                    .appendQueryParameter("cnt", httpParams.getCnt())
                    .appendQueryParameter("appid", httpParams.getApiId())
                    .appendQueryParameter("lang", httpParams.getLang())
            ;
            Uri uri = builder.build();
            String weatherAPI = uri.toString();
            Log.v(className, "Fetch weatherAPI from " + uri.toString());
            try {
                // Construct the URL for the OpenWeatherMap query
                // Possible parameters are available at OWM's forecast API page, at
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
                // If the code didn't successfully get the weather data, there's no point in attempting
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
            //Log.v(className, forecastJsonStr);
            return getWeatherArrayFromJSON(forecastJsonStr);
        }

        @Override
        protected void onPostExecute(Object o) {
            String[] forecastList = (String[]) o;
            weatherAdapter.clear();
            weatherAdapter.addAll(forecastList);
        }

        private String[] getWeatherArrayFromJSON(String forecastJsonStr){
            ArrayList<String> weatherArray = new ArrayList<>();
            try {
                    JSONObject forecastJson = new JSONObject(forecastJsonStr);
                    JSONArray forecastList = forecastJson.getJSONArray("list");
                    JSONObject dayForecast;
                    for (int i=0;i<forecastList.length();i++){
                        dayForecast = forecastList.getJSONObject(i);
                        weatherArray.add(dayForecast(dayForecast, i));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                return weatherArray.toArray(new String[weatherArray.size()]);
        }
        private String dayForecast(JSONObject dayForecast, int dayNbrFromToday)throws JSONException{
            //"Mon, Jun 1 - Clear - 18/13"
            String dayForecastText = "";
            switch (dayNbrFromToday){
                case 0:
                    dayForecastText += "Today : ";
                    break;
                case 1:
                    dayForecastText += "Tomorrow : ";
                    break;
                default:
                    dayForecastText = getDate(dayNbrFromToday) + " : ";
            }
            dayForecastText += getWeatherDesc(dayForecast) + " - " + getMaxTemp(dayForecast) + " / " + getMinTemp(dayForecast);
            return dayForecastText;
        }
        private String getDate(int dayNbrFromToday){
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DATE, dayNbrFromToday);
            return new SimpleDateFormat("dd MMM yyyy").format(cal.getTime());
        }
        private int getMaxTemp(JSONObject dayForecast) throws JSONException {
            JSONObject dayTemp = (JSONObject) dayForecast.get("temp");
            Object maxtmp = dayTemp.get("max");
            int maxTemp = 0;
            if (maxtmp instanceof Integer){
                maxTemp = (int) maxtmp;
            } else if (maxtmp instanceof Double) {
                maxTemp = ((Double) dayTemp.get("max")).intValue();
            }
            return  (maxTemp) ;
        }

        private int getMinTemp(JSONObject dayForecast) throws JSONException {
            JSONObject dayTemp = (JSONObject) dayForecast.get("temp");
            return   ((Double) dayTemp.get("min")).intValue();
        }
        private String getWeatherDesc(JSONObject dayForecast) throws JSONException {
            JSONArray dayTempArray = (JSONArray) dayForecast.get("weather");
            JSONObject dayTemp =(JSONObject) dayTempArray.get(0);
            return (String) dayTemp.get("description");
        }
    }

    class OpenWeatherAPIParams{

        String apiId;
        String postalCode;
        String mode;
        String units;
        String cnt;
        String lang;

        public OpenWeatherAPIParams(String apiId, String postalCode, String mode, String units, String cnt, String lang) {
            this.apiId = apiId;
            this.postalCode = postalCode;
            this.mode = mode;
            this.units = units;
            this.cnt = cnt;
            this.lang = lang;
        }

        public String getApiId() {
            return apiId;
        }

        public void setApiId(String apiId) {
            this.apiId = apiId;
        }

        public String getPostalCode() {
            return postalCode;
        }

        public void setPostalCode(String postalCode) {
            this.postalCode = postalCode;
        }

        public String getMode() {
            return mode;
        }

        public void setMode(String mode) {
            this.mode = mode;
        }

        public String getUnits() {
            return units;
        }

        public void setUnits(String units) {
            this.units = units;
        }

        public String getCnt() {
            return cnt;
        }

        public void setCnt(String cnt) {
            this.cnt = cnt;
        }

        public String getLang() {
            return lang;
        }

        public void setLang(String lang) {
            this.lang = lang;
        }
    }

}
