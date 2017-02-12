package com.example.alba.colorweatherproyect;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.TimeZone;

import butterknife.BindDrawable;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {

    public static final String TAG = MainActivity.class.getSimpleName();
    public static final String DATA = "data";
    public static final String SUMMARY = "summary";
    public static final String ICON = "icon";
    public static final String TEMPERATURE = "temperature";
    public static final String TEMPERATURE_MAX = "temperatureMax";
    public static final String TEMPERATURE_MIN = "temperatureMin";
    public static final String PRECIP_PROBABILITY = "precipProbability";
    public static final String TIME = "time";
    public static final String CURRENTLY = "currently";
    public static final String DAILY = "daily";
    public static final String HOURLY = "hourly";
    public static final String MINUTELY = "minutely";
    public static final String TIMEZONE = "timezone";
    public static final String DAYS_ARRAY_LIST = "DAYS_ARRAY_LIST";

    @BindView(R.id.iconImageView) ImageView iconImageView;
    @BindView(R.id.descriptionTextView) TextView descriptionTextView;
    @BindView(R.id.currentTempTextView) TextView currentTempTextView;
    @BindView(R.id.highestTemptextView) TextView highestTempTextView;
    @BindView(R.id.lowestTemptextView) TextView lowestTempTextView;

    @BindDrawable(R.drawable.clear_night) Drawable clearNight;

    ArrayList<Day> days;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(this);

        String url ="https://api.darksky.net/forecast/555f4b7ea7d6d56e50b3edb687f9034f/37.8267,-122.4233?units=si";

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {

                try {

                    CurrentWeather currentWeather = getCurrentWeatherFromJSON(response);

                    days = getDailyWeatherFromJson(response);
                    ArrayList<Hour> hours = getHourlyWeatherFromJson(response);
                    ArrayList<Minute> minutes = getMinutelyWeatherFromJson(response);

                    iconImageView.setImageDrawable(currentWeather.getIconDrawableResource());
                    descriptionTextView.setText(currentWeather.getDescription());
                    currentTempTextView.setText(currentWeather.getCurrentTemperature());
                    highestTempTextView.setText(String.format("H: %sº", currentWeather.getHighestTemperature()));
                    lowestTempTextView.setText(String.format("L: %sº", currentWeather.getLowestTemperature()));

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(TAG, "That didn't work");
            }
        });

    // Add the request to the RequestQueue.
        queue.add(stringRequest);

    }

    @OnClick(R.id.dailyWeatherTextView)
    public void dailyWeatherClick(){

        Intent dailyActivityIntent = new Intent(MainActivity.this, DailyWeatherActivity.class);
        dailyActivityIntent.putParcelableArrayListExtra(DAYS_ARRAY_LIST, days);

        startActivity(dailyActivityIntent);
    }

    @OnClick(R.id.hourlyWeatherTextView)
    public void hourlyWeatherClick(){
        Intent hourlyActivityIntent = new Intent(MainActivity.this, HourlyWeatherActivity.class);
        startActivity(hourlyActivityIntent);
    }

    @OnClick(R.id.minutelyWeatherTextView)
    public void minutelyWeatherClick(){
        Intent minutelyActivityIntent = new Intent(MainActivity.this, MinutelyWeatherActivity.class);
        startActivity(minutelyActivityIntent);
    }

    private CurrentWeather getCurrentWeatherFromJSON(String json)throws JSONException{

        JSONObject jsonObject = new JSONObject(json);
        JSONObject jsonWithCurrentWeather = jsonObject.getJSONObject(CURRENTLY);
        JSONObject jsonWithDailyWeather = jsonObject.getJSONObject(DAILY);

        JSONArray jsonWithDailyWeatherData = jsonWithDailyWeather.getJSONArray(DATA);

        JSONObject jsonWithTodayData = jsonWithDailyWeatherData.getJSONObject(0);

        String summary = jsonWithCurrentWeather.getString(SUMMARY);
        String icon = jsonWithCurrentWeather.getString(ICON);
        String temperature = Math.round(jsonWithCurrentWeather.getDouble(TEMPERATURE)) + "";
        String maxTemperature = Math.round(jsonWithTodayData.getDouble(TEMPERATURE_MAX)) + "";
        String minTemperature = Math.round(jsonWithTodayData.getDouble(TEMPERATURE_MIN)) + "";

        CurrentWeather currentWeather = new CurrentWeather(MainActivity.this);

        currentWeather.setDescription(summary);
        currentWeather.setIconImage(icon);
        currentWeather.setCurrentTemperature(temperature);
        currentWeather.setHighestTemperature(maxTemperature);
        currentWeather.setLowestTemperature(minTemperature);

        return currentWeather;

    }

    private ArrayList<Day> getDailyWeatherFromJson(String json)throws JSONException{

        DateFormat dateFormat = new SimpleDateFormat("EEEE");

        ArrayList<Day> days = new ArrayList<Day>();

        JSONObject jsonObject = new JSONObject(json);

        String timeZone = jsonObject.getString(TIMEZONE);
        dateFormat.setTimeZone(TimeZone.getTimeZone(timeZone));

        JSONObject jsonWithDailyWeather = jsonObject.getJSONObject(DAILY);

        JSONArray jsonWithDailyWeatherData = jsonWithDailyWeather.getJSONArray(DATA);

        for(int i = 0; i < jsonWithDailyWeatherData.length(); i++){ // Devuelve e numero de elementos

            Day day = new Day();

            JSONObject jsonWithDayData = jsonWithDailyWeatherData.getJSONObject(i);

            String rainProbability = "Rain probability: " + jsonWithDayData.getDouble(PRECIP_PROBABILITY)*100 + "%";
            String description = jsonWithDayData.getString(SUMMARY);
            String dayName = dateFormat.format(jsonWithDayData.getDouble(TIME)*1000); // Pasar de ms a s

            day.setDayName(dayName);
            day.setRainProbability(rainProbability);
            day.setWeatherDescription(description);

            days.add(day);

        }

        return days;

    }

    public ArrayList<Hour> getHourlyWeatherFromJson(String json) throws JSONException{

        DateFormat dateFormat = new SimpleDateFormat("HH:mm");

        ArrayList<Hour> hours = new ArrayList<Hour>();

        JSONObject jsonObject = new JSONObject(json);

        String timeZone = jsonObject.getString(TIMEZONE);
        dateFormat.setTimeZone(TimeZone.getTimeZone(timeZone));

        JSONObject jsonWithHourlyWeather = jsonObject.getJSONObject(HOURLY);

        JSONArray jsonWithHourlyWeatherData = jsonWithHourlyWeather.getJSONArray(DATA);

        for(int i = 0; i < jsonWithHourlyWeatherData.length(); i++){

            Hour hour = new Hour();

            JSONObject jsonWithHourData = jsonWithHourlyWeatherData.getJSONObject(i);

            String title = dateFormat.format(jsonWithHourData.getDouble(TIME)*1000);
            String description = jsonWithHourData.getString(SUMMARY);

            hour.setTitle(title);
            hour.setWeatherDescription(description);

            hours.add(hour);

        }

        return hours;

    }

    public ArrayList<Minute> getMinutelyWeatherFromJson(String json) throws JSONException{

        DateFormat dateFormat = new SimpleDateFormat("HH:mm");

        ArrayList<Minute> minutes = new ArrayList<Minute>();

        JSONObject jsonObject = new JSONObject(json);

        String timeZone = jsonObject.getString(TIMEZONE);
        dateFormat.setTimeZone(TimeZone.getTimeZone(timeZone));

        JSONObject jsonWithMinutelyWeather = jsonObject.getJSONObject(MINUTELY);

        JSONArray jsonWithMinutelyWeatherData = jsonWithMinutelyWeather.getJSONArray(DATA);

        for(int i = 0; i < jsonWithMinutelyWeatherData.length(); i++){

            Minute minute = new Minute();

            JSONObject jsonWithMinuteData = jsonWithMinutelyWeatherData.getJSONObject(i);

            String title = dateFormat.format(jsonWithMinuteData.getDouble(TIME)*1000);
            String precipProbability = jsonWithMinuteData.getDouble(PRECIP_PROBABILITY) + "";

            minute.setTitle(title);
            minute.setRainProbability(precipProbability);

            minutes.add(minute);

        }

        return minutes;

    }

}
