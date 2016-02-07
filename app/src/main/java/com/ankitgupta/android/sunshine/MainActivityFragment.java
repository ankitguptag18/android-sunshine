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
import android.widget.ArrayAdapter;
import android.widget.ListView;

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
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment {
    private final String apiId = "44db6a862fba0b067b1930da0d769e98";
    private final String className = this.getClass().getSimpleName();
    private ArrayAdapter<String> adapter;
    private OpenWeatherAPIParams openWeatherAPIParams;
    public MainActivityFragment() {
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
        String[] weekForecastarray = {
                "Today - Sunny - 88/63","Tomorrow - Sunny - 88/63"
        };
        List <String> weekForecast = new ArrayList<String>(
                Arrays.asList(weekForecastarray)
        );
        adapter = new ArrayAdapter<String>(
                getActivity(),
                R.layout.list_item_forecast,
                R.id.list_item_forecast_textview,
                weekForecast);
        ListView lv = (ListView) rootView.findViewById(R.id.listview_forecast);
        lv.setAdapter(adapter);
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
    private void updateWeather(){
        FetchWeatherTask weatherTask =  new FetchWeatherTask();
        Map<String, String> map = new HashMap<String, String>();
        String postalCode = "560078,India";
        String mode = "json";
        String units = "metric";
        String cnt = "14";
        String lang = "en";


        openWeatherAPIParams = new OpenWeatherAPIParams( apiId,postalCode,  mode,  units,  cnt,  lang);
        map.put("apiId",apiId);
        map.put("postalCode",postalCode);
        map.put("mode",mode);
        map.put("units",units);
        map.put("cnt", cnt);
        map.put("lang", lang);
        weatherTask.execute(openWeatherAPIParams);
    }

    public class FetchWeatherTask extends AsyncTask{
        @Override
        protected String[] doInBackground(Object[] params) {
           // Map<String, String> httpParams = (Map<String, String>) params[0];
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
              /*
                    .appendQueryParameter("q", httpParams.get("postalCode"))
                    .appendQueryParameter("mode", httpParams.get("mode"))
                    .appendQueryParameter("units", httpParams.get("units"))
                    .appendQueryParameter("cnt", httpParams.get("cnt"))
                    .appendQueryParameter("appid", httpParams.get("apiId"))
                    .appendQueryParameter("lang", httpParams.get("lang"))
*/
                    .appendQueryParameter("q", httpParams.getPostalCode())
                    .appendQueryParameter("mode", httpParams.getMode())
                    .appendQueryParameter("units", httpParams.getUnits())
                    .appendQueryParameter("cnt", httpParams.getCnt())
                    .appendQueryParameter("appid", httpParams.getApiId())
                    .appendQueryParameter("lang", httpParams.getLang())

            ;
            Uri uri = builder.build();
            String weatherAPI = uri.toString();
            Log.v(className, "Featch weatherAPI from " + uri.toString());
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
            //Log.v(className, forecastJsonStr);
            return getWeatherArrayFromJSON(forecastJsonStr,5);
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

            //for (String dayforcase: weatherArray)
                //Log.v(className, dayforcase);
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
            SimpleDateFormat dt = new SimpleDateFormat("dd MMM yyyy");
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

        @Override
        protected void onPostExecute(Object o) {
            String[] forcastlist = (String[]) o;
            adapter.clear();
            adapter.addAll(forcastlist);

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
