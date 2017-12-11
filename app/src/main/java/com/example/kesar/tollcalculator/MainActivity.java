package com.example.kesar.tollcalculator;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;

public class MainActivity extends Activity {

    //Declare a private requestQueue variable
    private RequestQueue requestQueue;
    private static MainActivity mInstance;

    private String JsonURL = "https://tce.cit.api.here.com/2/calculateroute.json?app_id=9wficJdCD3MPF2TcxnhM&app_code=JNEIrQ0_aoC0n2P1xbfQYg&driver_cost=15&vehicle_cost=0.7&currency=INR&tollVehicleType=3&trailerType=2&trailersCount=1&vehicleNumberAxles=3&trailerNumberAxles=2&emissionType=5&height=4.0m&trailerHeight=4.0m&vehicleWeight=12.0t&limitedWeight=40.0t&passengersCount=1&tiresCount=10&commercial=1&waypoint0=25.473034,81.878357&waypoint1=28.644800,77.216721&metricsystem=metric&maneuverattributes=none&routeattributes=gr&mode=fastest;truck&jsonattributes=41&combinechange=true&linkattributes=none,rt,fl&legattributes=none,li,sm&cost_optimize=1&detail=1";
    private String data = "";
    private TextView dataShow;
    private ProgressDialog mProgressDialog;

    private TextInputLayout mFrom;
    private TextInputLayout mTo;
    private Spinner mVehicleTypeSpinner;
    private Button mTollCostButton;

    private String [] vehicleTypeDropDown = {"Car/Jeep/Van", "LCV", "Bus/Truck", "Upto 3 Axle Vehicle", "4 to 6 Axle Vehicle", "HCM/EME"};

    private String vehicleType;

    int PLACE_AUTOCOMPLETE_REQUEST_CODE_FROM = 1;
    int PLACE_AUTOCOMPLETE_REQUEST_CODE_TO = 20;

    private String placeName;
    Double latitude, longitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Creates the volley request queue
        requestQueue = Volley.newRequestQueue(this);

        mProgressDialog = new ProgressDialog(this);

        mFrom = (TextInputLayout) findViewById(R.id.main_from);
        mTo = (TextInputLayout) findViewById(R.id.main_to);
        mVehicleTypeSpinner = (Spinner) findViewById(R.id.main_vehicle_type_spinner);
        mTollCostButton = (Button) findViewById(R.id.main_toll_cost_btn);
        dataShow = (TextView) findViewById(R.id.main_data_show);

        mVehicleTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

                vehicleType = (String) mVehicleTypeSpinner.getSelectedItem();

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

                Toast.makeText(MainActivity.this, "Please Select Vehicle Type", Toast.LENGTH_SHORT).show();

            }
        });

        ArrayAdapter<String> adapter_role = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, vehicleTypeDropDown);
        adapter_role.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mVehicleTypeSpinner.setAdapter(adapter_role);

        mFrom.getEditText().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Intent intent =
                            new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_OVERLAY)
                                    .build(MainActivity.this);
                    startActivityForResult(intent, PLACE_AUTOCOMPLETE_REQUEST_CODE_FROM);
                } catch (GooglePlayServicesRepairableException e) {

                } catch (GooglePlayServicesNotAvailableException e) {

                }
            }
        });
        mFrom.getEditText().setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus){
                    try {
                        Intent intent =
                                new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_OVERLAY)
                                        .build(MainActivity.this);
                        startActivityForResult(intent, PLACE_AUTOCOMPLETE_REQUEST_CODE_FROM);
                    } catch (GooglePlayServicesRepairableException e) {

                    } catch (GooglePlayServicesNotAvailableException e) {

                    }
                }
            }
        });

        mTo.getEditText().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Intent intent =
                            new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_OVERLAY)
                                    .build(MainActivity.this);
                    startActivityForResult(intent, PLACE_AUTOCOMPLETE_REQUEST_CODE_TO);
                } catch (GooglePlayServicesRepairableException e) {

                } catch (GooglePlayServicesNotAvailableException e) {

                }
            }
        });
        mTo.getEditText().setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus){
                    try {
                        Intent intent =
                                new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_OVERLAY)
                                        .build(MainActivity.this);
                        startActivityForResult(intent, PLACE_AUTOCOMPLETE_REQUEST_CODE_TO);
                    } catch (GooglePlayServicesRepairableException e) {

                    } catch (GooglePlayServicesNotAvailableException e) {

                    }
                }
            }
        });

        mTollCostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                mProgressDialog.setTitle("Please wait...");
                mProgressDialog.setMessage("It will take time according to your internet connection");
                mProgressDialog.setCanceledOnTouchOutside(false);
                mProgressDialog.show();

                String from = mFrom.getEditText().getText().toString();
                String to = mTo.getEditText().getText().toString();

                LatLng latLng_from, latLng_to;

                latLng_from = getLatLngFromAddress(from);
                latLng_to = getLatLngFromAddress(to);

                makeRequestToServer(latLng_from, latLng_to);

            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == PLACE_AUTOCOMPLETE_REQUEST_CODE_FROM){
            if(resultCode == RESULT_OK){
                Place place = PlaceAutocomplete.getPlace(MainActivity.this, data);
                placeName = place.getName()+"";
                LatLng latLng = place.getLatLng();
                latitude = latLng.latitude;
                longitude = latLng.longitude;
                mFrom.getEditText().setText(placeName);
            }
        }

        if (requestCode == PLACE_AUTOCOMPLETE_REQUEST_CODE_TO) {
            if (resultCode == RESULT_OK) {
                Place place = PlaceAutocomplete.getPlace(MainActivity.this, data);
                placeName = place.getName()+"";
                LatLng latLng = place.getLatLng();
                latitude = latLng.latitude;
                longitude = latLng.longitude;

                mTo.getEditText().setText(placeName);
            }
        }
    }

    private void makeRequestToServer(LatLng latLng_from, LatLng latLng_to) {

        LatLng latLngFrom = latLng_from;
        LatLng latLngTo = latLng_to;

        String JsonURL = "https://tce.cit.api.here.com/2/calculateroute.json?app_id=9wficJdCD3MPF2TcxnhM&app_code=JNEIrQ0_aoC0n2P1xbfQYg&driver_cost=15&vehicle_cost=0.7&currency=INR&tollVehicleType=3&trailerType=2&trailersCount=1&vehicleNumberAxles=3&trailerNumberAxles=2&emissionType=5&height=4.0m&trailerHeight=4.0m&vehicleWeight=12.0t&limitedWeight=40.0t&passengersCount=1&tiresCount=10&commercial=1"
                +"&waypoint0="+latLng_from.latitude+","+latLng_from.longitude+"&waypoint1="+latLng_to.latitude+","+latLng_to.longitude+"&metricsystem=metric&maneuverattributes=none&routeattributes=gr&mode=fastest;truck&jsonattributes=41&combinechange=true&linkattributes=none,rt,fl&legattributes=none,li,sm&cost_optimize=1&detail=1";

        // Creating the JsonObjectRequest class called obreq, passing required parameters:
        //GET is used to fetch data from the server, JsonURL is the URL to be fetched from.
        JsonObjectRequest obreq = new JsonObjectRequest(Request.Method.GET, JsonURL, null,
                // The third parameter Listener overrides the method onResponse() and passes
                //JSONObject as a parameter
                new Response.Listener<JSONObject>() {

                    // Takes the response from the JSON request
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONObject obj = response.getJSONObject("costs");
                            // Retrieves the string labeled "colorName" and "description" from
                            //the response JSON Object
                            //and converts them into javascript objects
                            String totalCost = obj.getString("totalCost");
                            String currency = obj.getString("currency");
                            String driverCost = obj.getJSONObject("details").getString("driverCost");
                            String vehicleCost = obj.getJSONObject("details").getString("vehicleCost");
                            String tollCost = obj.getJSONObject("details").getString("tollCost");

                            // Adds strings from object to the "data" string
                            data += "Total Cost: " + totalCost +
                                    "\nCurrency : " + currency +
                                    "\n Driver Cost : " + driverCost +
                                    "\n Vehicle Cost : " + vehicleCost +
                                    "\n Toll Cost : " + tollCost;

                            // Adds the data string to the TextView "results"
                            dataShow.setText(data);
                            mProgressDialog.dismiss();
                        }
                        // Try and catch are included to handle any errors due to JSON
                        catch (JSONException e) {
                            // If an error occurs, this prints the error to the log
                            e.printStackTrace();
                        }
                    }
                },
                // The final parameter overrides the method onErrorResponse() and passes VolleyError
                //as a parameter
                new Response.ErrorListener() {
                    @Override
                    // Handles errors that occur due to Volley
                    public void onErrorResponse(VolleyError error) {
                        Log.e("Volley", String.valueOf(error));
                        mProgressDialog.dismiss();
                        Toast.makeText(MainActivity.this, "Error while calculating toll cost....Please Try Again", Toast.LENGTH_LONG).show();
                    }
                }
        );

        obreq.setRetryPolicy(new DefaultRetryPolicy(500000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        // Adds the JSON object request "obreq" to the request queue
        requestQueue.add(obreq);

    }

    public LatLng getLatLngFromAddress(String strAddress) {

        Geocoder geocoder = new Geocoder(this);
        List<Address> address;
        LatLng latLng = null;

        try {

            address = geocoder.getFromLocationName(strAddress, 5);

            if (address == null) {
                return null;
            }

            Address location = address.get(0);
            latLng = new LatLng(location.getLatitude(), location.getLongitude());

        } catch (IOException e) {
            e.printStackTrace();
        }

        return latLng;
    }
}
