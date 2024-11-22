package com.example.watatap;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.os.Handler;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatToggleButton;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

public class LiveData extends AppCompatActivity {

    String http = Variables.DATABASE_URL_HTTP;
    String dbpath = Variables.DATABASE_URL_PATH;
    String postpath = Variables.VALVE_URL_PATH;
    String currentdata = Variables.CURRENT_DATA;
    String valves = Variables.VALVES;

    TextView txtv_phlevel, txtv_turbidity, txtv_waterconsumption, txtv_waterlevelthreshold, txtv_leakageamount, txtv_liters;
    AppCompatToggleButton tbtn_mainvalve, tbtn_overheadvalve;

    private Handler handler = new Handler();
    String get_url = http + "192.168.1.49" + dbpath + currentdata;
    String post_url = http + "192.168.1.49" + postpath + valves;
    private Runnable fetchDataRunnable = new Runnable() {
        @Override
        public void run() {
            fetchData(get_url); // Call the method to fetch data
            handler.postDelayed(this, 1000); // Schedule next fetch after 5 seconds
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_live_data);

        txtv_phlevel = findViewById(R.id.txt_value_phlevel);
        txtv_turbidity = findViewById(R.id.txt_value_turbidity);
        txtv_waterconsumption = findViewById(R.id.txt_value_waterconsumption);
        txtv_waterlevelthreshold = findViewById(R.id.txt_value_waterlevelthreshold);
        txtv_leakageamount = findViewById(R.id.txt_value_leakageamount);
        txtv_liters = findViewById(R.id.txt_value_liters);
        tbtn_mainvalve = findViewById(R.id.mainvalve_switch);
        tbtn_overheadvalve = findViewById(R.id.overheadvalve_switch);

        // Set listeners for toggle button state changes
        tbtn_mainvalve.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                Log.d("ToggleButton", "Main Valve is ON");
                Toast.makeText(LiveData.this, "Main Valve Button is set to: ON", Toast.LENGTH_SHORT).show();
            } else {
                Log.d("ToggleButton", "Main Valve is OFF");
                Toast.makeText(LiveData.this, "Main Valve Button is set to: OFF", Toast.LENGTH_SHORT).show();
            }
            sendValveStateChange(post_url);
        });

        tbtn_overheadvalve.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                Log.d("ToggleButton", "Overhead Valve is ON");
                Toast.makeText(LiveData.this, "Overhead Valve Button is set to: ON", Toast.LENGTH_SHORT).show();
            } else {
                Log.d("ToggleButton", "Overhead Valve is OFF");
                Toast.makeText(LiveData.this, "Overhead Valve Button is set to: OFF", Toast.LENGTH_SHORT).show();
            }
            sendValveStateChange(post_url);
        });

        handler.post(fetchDataRunnable);
    }

    private void fetchData(String url) {

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        if (response == null) {
                            Log.e("VolleyError", "Received empty response.");
                            return;
                        }

                        try {
                            String overheadvalve = response.getString("overheadvalve");
                            String mainvalve = response.getString("mainvalve");
                            String leakageamount = response.getString("leakageamount");
                            String phlevel = response.getString("phlevel");
                            String turbidity = response.getString("turbidity");
                            String waterconsumption = response.getString("waterconsumption");
                            String waterlevelthreshold = response.getString("waterlevelthreshold");
                            String liters = response.getString("liters");
                            String createddate = response.getString("createddate");

                            Log.d("VolleyResponse", "Overhead Valve: " + overheadvalve);
                            Log.d("VolleyResponse", "Main Valve: " + mainvalve);
                            Log.d("VolleyResponse", "Leakage Amount: " + leakageamount);
                            Log.d("VolleyResponse", "pH Level: " + phlevel);
                            Log.d("VolleyResponse", "Turbidity: " + turbidity);
                            Log.d("VolleyResponse", "Water Consumption: " + waterconsumption);
                            Log.d("VolleyResponse", "Water Level Threshold: " + waterlevelthreshold);
                            Log.d("VolleyResponse", "Liters: " + liters);
                            Log.d("VolleyResponse", "Created Date: " + createddate);

                            txtv_phlevel.setText(phlevel);
                            txtv_turbidity.setText(turbidity);
                            txtv_leakageamount.setText(leakageamount);
                            txtv_waterconsumption.setText(waterconsumption);
                            txtv_waterlevelthreshold.setText(waterlevelthreshold);
                            txtv_liters.setText(liters);

                            if (mainvalve.equals("On")) {
                                tbtn_mainvalve.setText(mainvalve);
                                tbtn_mainvalve.setChecked(true);
                            }
                            if (mainvalve.equals("Off")) {
                                tbtn_mainvalve.setText(mainvalve);
                                tbtn_mainvalve.setChecked(false);
                            }
                            if (overheadvalve.equals("On")) {
                                tbtn_overheadvalve.setText(overheadvalve);
                                tbtn_overheadvalve.setChecked(true);
                            }
                            if (overheadvalve.equals("Off")) {
                                tbtn_overheadvalve.setText(overheadvalve);
                                tbtn_overheadvalve.setChecked(false);
                            }

                        } catch (Exception e) {
                            Log.e("VolleyError", "Error parsing JSON: " + e.getMessage());
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(com.android.volley.VolleyError error) {
                        Log.e("VolleyError", "Error: " + error.getMessage());
                        if (error.networkResponse != null) {
                            String response = new String(error.networkResponse.data);
                            Log.e("VolleyError", "Response: " + response);
                        }
                    }
                });

        Volley.newRequestQueue(this).add(jsonObjectRequest);
    }

    // Method to send POST request when toggle state changes
    private void sendValveStateChange(String url) {
        // Prepare payload with current valve states
        JSONObject payload = new JSONObject();
        try {
            payload.put("mainvalve", tbtn_mainvalve.isChecked() ? "On" : "Off");
            payload.put("overheadvalve", tbtn_overheadvalve.isChecked() ? "On" : "Off");
        } catch (Exception e) {
            Log.e("JSONException", "Error creating JSON payload: " + e.getMessage());
            e.printStackTrace();
        }

        // Create a POST request
        JsonObjectRequest postRequest = new JsonObjectRequest(
                Request.Method.POST, url, payload,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("VolleyResponse", "Valve state update successful: " + response.toString());
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(com.android.volley.VolleyError error) {
                        Log.e("VolleyError", "Error: " + error.getMessage());
                        if (error.networkResponse != null) {
                            String response = new String(error.networkResponse.data);
                            Log.e("VolleyError", "Response: " + response);
                        }
                    }
                });

        // Add the request to the Volley queue
        Volley.newRequestQueue(this).add(postRequest);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(fetchDataRunnable);
    }
}
