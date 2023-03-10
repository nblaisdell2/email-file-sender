package com.example.sendfiles;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private static final String AWS_SEND_EMAIL_BASE_URL = "https://p38psfd4f2.execute-api.us-east-1.amazonaws.com/dev";
    private static final String AWS_GET_FILES_BASE_URL = "https://b20dlo3hzh.execute-api.us-east-1.amazonaws.com/dev";
    private final String DEFAULT_SPINNER_VALUE = "Please select a file to send...";

    Spinner spnFiles;
    EditText etEmailRecipient;
    EditText etEmailSubject;
    Button btnSendEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        spnFiles = findViewById(R.id.spnFiles);
        etEmailRecipient = findViewById(R.id.etEmailRecipient);
        etEmailSubject = findViewById(R.id.etSubject);

        // getting a new volley request queue for making new requests
        RequestQueue volleyQueue = Volley.newRequestQueue(MainActivity.this);

        // since the response we get from the api is in JSON, we
        // need to use `JsonObjectRequest` for parsing the
        // request response
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                // we are using GET HTTP request method
                Request.Method.GET,

                // url we want to send the HTTP request to
                AWS_GET_FILES_BASE_URL,

                // this parameter is used to send a JSON object to the
                // server, since this is not required in our case,
                // we are keeping it `null`
                null,

                // lambda function for handling the case
                // when the HTTP request succeeds
                (Response.Listener<JSONObject>) response -> {
                    // get the image url from the JSON object
                    String msg = "";
                    String input = "";
                    List<String> items = new ArrayList<>();
                    try {
                        msg = response.getString("msg");
                        input = response.getString("data");
                        String[] tokens = input.substring(1, input.length()-2).split(",");
                        items = Arrays.asList(tokens).stream().map(s -> s.replace("'", "").replace(" ", "")).collect(Collectors.toList());
                    } catch (JSONException e) {
                        Log.e(TAG, e.toString());
                    }

                    ArrayList<String> fileList = new ArrayList<>();
                    fileList.add(DEFAULT_SPINNER_VALUE);
                    fileList.addAll(items);

                    ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, fileList);
                    adapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
                    spnFiles.setAdapter(adapter);

                    Log.d(TAG, msg);
                },

                // lambda function for handling the case
                // when the HTTP request fails
                (Response.ErrorListener) error -> {
                    // make a Toast telling the user
                    // that something went wrong
                    // As of f605da3 the following should work
                    NetworkResponse response = error.networkResponse;
                    if (response.statusCode == 200) {
                        Toast.makeText(MainActivity.this, "Data Retrieved Successfully!", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(MainActivity.this, "Error Occurred!", Toast.LENGTH_LONG).show();
                    }

                    // log the error message in the error stream
                    Log.e(TAG, error.toString());
                }
        );
        jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(0, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        // add the json request object created above
        // to the Volley request queue
        volleyQueue.add(jsonObjectRequest);

        btnSendEmail = findViewById(R.id.btnSendEmail);
        btnSendEmail.setOnClickListener(view -> sendEmail());
    }

    // function for making a HTTP request using Volley
    private void sendEmail() {
        String send_to = etEmailRecipient.getText().toString();
        String email_subject = etEmailSubject.getText().toString().replace(" ", "%20");
        String folder_name = spnFiles.getSelectedItem().toString();

        if (send_to.equals("") || email_subject.equals("") || folder_name.equals(DEFAULT_SPINNER_VALUE)) {
            Toast.makeText(MainActivity.this, "Must enter all information!", Toast.LENGTH_LONG).show();
            return;
        }

        // url of the api through which we get random dog images
        StringBuilder sb = new StringBuilder();
        sb.append("?send_to=");
        sb.append(send_to);
        sb.append("&email_subject=");
        sb.append(email_subject);
        sb.append("&folder_name=");
        sb.append(folder_name);
        String url = AWS_SEND_EMAIL_BASE_URL + sb.toString();

        // getting a new volley request queue for making new requests
        RequestQueue volleyQueue = Volley.newRequestQueue(MainActivity.this);

        // since the response we get from the api is in JSON, we
        // need to use `JsonObjectRequest` for parsing the
        // request response
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
            // we are using GET HTTP request method
            Request.Method.GET,

            // url we want to send the HTTP request to
            url,

            // this parameter is used to send a JSON object to the
            // server, since this is not required in our case,
            // we are keeping it `null`
            null,

            // lambda function for handling the case
            // when the HTTP request succeeds
            (Response.Listener<JSONObject>) response -> {
                // get the image url from the JSON object
                String msg = "";
                try {
                    msg = response.getString("msg");
                } catch (JSONException e) {

                }

                Log.d(TAG, msg);
                Toast.makeText(MainActivity.this, msg, Toast.LENGTH_LONG).show();

                spnFiles.setSelection(0);
                etEmailSubject.setText("");
                etEmailRecipient.setText("");
            },

            // lambda function for handling the case
            // when the HTTP request fails
            (Response.ErrorListener) error -> {
                // make a Toast telling the user
                // that something went wrong
                // As of f605da3 the following should work
                NetworkResponse response = error.networkResponse;
                if (response.statusCode == 200) {
                    Toast.makeText(MainActivity.this, "Email Sent Successfully!", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(MainActivity.this, "Error Occurred!", Toast.LENGTH_LONG).show();
                }

                // log the error message in the error stream
                Log.e(TAG, error.toString());
            }
        );
        jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(0, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        // add the json request object created above
        // to the Volley request queue
        volleyQueue.add(jsonObjectRequest);
    }
}