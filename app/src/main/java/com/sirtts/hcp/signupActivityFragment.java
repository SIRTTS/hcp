package com.sirtts.hcp;

import android.app.DatePickerDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONObject;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

/**
 * A placeholder fragment containing a simple view.
 */
public class signupActivityFragment extends Fragment implements View.OnClickListener{

    EditText email,password,repassword ,marital,username;
    Button signup;
    TextView error,date;
    RadioGroup genderGroup;
    RadioButton gender;
    ProgressBar mProgressbar;
    DatePickerDialog datePickerDialog;
    CheckBox isDoctor;
    Spinner ethn;
    private RequestQueue mQueue;
    public static final String REQUEST_TAG = "signupVolleyActivity";

    public signupActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_signup, container, false);

        email = (EditText) rootView.findViewById(R.id.signup_emailtxtid);
        password = (EditText) rootView.findViewById(R.id.signup_passwordtxtid);
        repassword = (EditText) rootView.findViewById(R.id.signup_repasswordtxtid);
        date = (TextView) rootView.findViewById(R.id.signup_datebirthtxtid);
        marital = (EditText) rootView.findViewById(R.id.signup_maritaltxtid);
        username = (EditText) rootView.findViewById(R.id.signup_usernametxtid);

        isDoctor = (CheckBox) rootView.findViewById(R.id.signup_doctorCheckrid);

        ethn = (Spinner) rootView.findViewById(R.id.signup_ethnSpinnerid);

        signup = (Button) rootView.findViewById(R.id.signup_signupbtnid);

        error = (TextView) rootView.findViewById(R.id.signup_errortvid);

        genderGroup = (RadioGroup) rootView.findViewById(R.id.signup_gendergroup);

        signup.setOnClickListener(this);

        date.setOnClickListener(this);

        mProgressbar = (ProgressBar) rootView.findViewById(R.id.signup_progressBar);

        String[] items = new String[]{"EUROPEAN", "MAORI", "PACIFIC_PEOPLES", "ASIAN",
                "MIDDLE_EASTERN", "LATIN_AMERICAN", "AFRICAN", "OTHER_ETHNICITY",
                "RESIDUAL_CATEGORIES"};
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(),
                android.R.layout.simple_dropdown_item_1line, items);
        ethn.setAdapter(adapter);


        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        // Instantiate the RequestQueue.
        mQueue = VolleyRequestQueue.getInstance(getContext().getApplicationContext())
                .getRequestQueue();
    }

    @Override
    public void onStop() {
        super.onStop();
        mProgressbar.setVisibility(View.INVISIBLE);
        if (mQueue != null) {
            mQueue.cancelAll(REQUEST_TAG);
        }
    }

    @Override
    public void onClick(View v) {

        if(v == date){
            final Calendar c = Calendar.getInstance();
            int mYear = c.get(Calendar.YEAR);
            int mMonth = c.get(Calendar.MONTH);
            int mDay = c.get(Calendar.DAY_OF_MONTH);

            datePickerDialog = new DatePickerDialog(getContext(),
                    new DatePickerDialog.OnDateSetListener() {
                        @Override
                        public void onDateSet(DatePicker view, int year,
                                              int monthOfYear, int dayOfMonth) {
                            String month = "", day = "";
                            if(monthOfYear + 1 < 10) month = "0";
                            if(dayOfMonth < 10) day = "0";
                            date.setText(year +"-"+ month + (monthOfYear + 1) + "-"+day +dayOfMonth);
                        }
                    }, mYear, mMonth, mDay);
            datePickerDialog.getDatePicker().setMaxDate(new Date().getTime());
            datePickerDialog.show();
        }
        else if(v == signup){
            if(username.getText().toString().equals("") ){
                username.requestFocus();
                username.setError("Enter a Valid Username");
            }
            if(email.getText().toString().equals("") || !android.util.Patterns.EMAIL_ADDRESS.matcher(email.getText().toString()).matches()){
                email.requestFocus();
                email.setError("Enter a Valid Email");
            }
            else if(password.getText().toString().equals("") || password.getText().toString().length() < 4){
                password.requestFocus();
                password.setError("Enter a Valid password");
            }
            else if(!repassword.getText().toString().equals(password.getText().toString())){
                repassword.requestFocus();
                repassword.setError("Passwords aren't equal");
            }
            else if(date.getText().toString().equals("")){
                date.requestFocus();
                date.setError("Enter a Valid Date");
            }
            else if(marital.getText().toString().equals("")){
                marital.requestFocus();
                marital.setError("Enter a Valid Marital");
            }
            else{
                gender = (RadioButton) getActivity().findViewById(genderGroup.getCheckedRadioButtonId());
                error.setText("");
                if (!isNetworkAvailable(getContext()))
                    Toast.makeText(getActivity(), "Failed to Connect! Check your Connection", Toast.LENGTH_SHORT).show();
                else {
                    mProgressbar.setVisibility(View.VISIBLE);

                    JsonObjectRequest jsonRequest = new JsonObjectRequest(Request.Method.POST, getString(R.string.api_url_signup),
                            sendData(email.getText().toString(), password.getText().toString(),
                                    date.getText().toString(),gender.getText().toString(),
                                    username.getText().toString(),marital.getText().toString(),
                                    ethn.getSelectedItem().toString(),isDoctor.isChecked()),
                            new Response.Listener<JSONObject>() {
                                @Override
                                public void onResponse(JSONObject response) {

                                }
                            },
                            new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError errork) {
                                    mProgressbar.setVisibility(View.INVISIBLE);
                                    NetworkResponse networkResponse = errork.networkResponse;
                                    if(networkResponse != null)
                                        error.setText(networkResponse.headers.get(getString(R.string.api_header_error_key)));
                                }
                            }) {

                        @Override
                        protected Response<JSONObject> parseNetworkResponse(NetworkResponse response) {
                            updateUi(response.statusCode);
                            return super.parseNetworkResponse(response);
                        }
                    };
                    jsonRequest.setTag(REQUEST_TAG);
                    jsonRequest.setRetryPolicy(new DefaultRetryPolicy(
                            0,
                            DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
                    mQueue.add(jsonRequest);
                }
            }
        }
    }
    private void updateUi(final int statusCode){
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(statusCode == 201) {
                    mProgressbar.setVisibility(View.INVISIBLE);
                    Toast.makeText(getActivity(), "Congratulations! Check your email!", Toast.LENGTH_LONG).show();
                    getActivity().onBackPressed();
                }
                else{
                    mProgressbar.setVisibility(View.INVISIBLE);
                    error.setText("Unexpected Error happened!qqq");
                }
            }
        });
    }


    public JSONObject sendData(String email,String password,String date,String gender,String username,
                               String marital, String ethn, boolean isdoctor){
        HashMap m = new HashMap<>();
        m.put(getString(R.string.api_send_json_signup_email),email);
        m.put(getString(R.string.api_send_json_signup_password),password);
        m.put(getString(R.string.api_send_json_signup_dateOfBirth),date);
        m.put(getString(R.string.api_send_json_signup_gender),gender);
        m.put(getString(R.string.api_send_json_signup_ethnicity),ethn);
        m.put(getString(R.string.api_send_json_signup_doctor),isdoctor);
        m.put(getString(R.string.api_send_json_signup_marital),marital);
        m.put(getString(R.string.api_send_json_signup_login),username);
        m.put(getString(R.string.api_send_json_signup_langKey),"en");

        Log.e(REQUEST_TAG, "sendData: "+(new JSONObject(m)).toString());
        return new JSONObject(m);
    }

    public boolean isNetworkAvailable(final Context context) {
        final ConnectivityManager connectivityManager = ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE));
        return connectivityManager.getActiveNetworkInfo() != null && connectivityManager.getActiveNetworkInfo().isConnected();
    }

}

