package com.sirtts.hcp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * A placeholder fragment containing a simple view.
 */
public class Listspo2VitalSignsActivityFragment extends Fragment implements View.OnClickListener{

    private RequestQueue mQueue;
    ListView listview;
    VitalListAdapter adp;
    ArrayList<String> date_ArrayList = new ArrayList<String>();
    ArrayList<String> time_ArrayList = new ArrayList<String>();
    ArrayList<String> val1_ArrayList = new ArrayList<String>();
    ProgressBar mProgressbar;
    Button graph;
    public static final String REQUEST_TAG = "ListSpo2VitalVolley";


    public Listspo2VitalSignsActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_listspo2_vital_signs, container, false);

        listview = (ListView) rootView.findViewById(R.id.ListSpo2VitalSigns_listView);
        mProgressbar = (ProgressBar) rootView.findViewById(R.id.ListSpo2VitalSigns_progressBar);

        graph = (Button) rootView.findViewById(R.id.spo2vital_graphbtn);
        graph.setOnClickListener(this);
        graph.setVisibility(View.INVISIBLE);

        if (isNetworkAvailable(getContext())) {
            SharedPreferences sharedPre = getActivity().getSharedPreferences(getString(R.string.shared_isUserLoged), Context.MODE_PRIVATE);
            mQueue = VolleyRequestQueue.getInstance(getContext().getApplicationContext())
                    .getRequestQueue();

            JsonArrayRequest jsonRequest = new JsonArrayRequest(Request.Method.POST, getString(R.string.api_url_spo2Vital_list),
                    sendData(sharedPre.getInt(getString(R.string.shared_userId),0)),
                    new Response.Listener<JSONArray>() {
                        @Override
                        public void onResponse(JSONArray spo2onse) {
                            try {
                                mProgressbar.setVisibility(View.INVISIBLE);

                                for(int i=0;i<spo2onse.length();i++){
                                    date_ArrayList.add(String.valueOf(spo2onse.optJSONObject(i).optString(getString(R.string.api_receive_json_vital_list_arr_date))));
                                    time_ArrayList.add(String.valueOf(spo2onse.optJSONObject(i).optString(getString(R.string.api_receive_json_vital_list_arr_time))));
                                    val1_ArrayList.add(String.valueOf(spo2onse.optJSONObject(i).optInt(getString(R.string.api_receive_json_vital_Spo2Rate_list_arr_percent)))
                                    );
                                }

                                adp = new VitalListAdapter(getContext(),date_ArrayList,time_ArrayList,val1_ArrayList,new ArrayList<Integer>());
                                listview.setAdapter(adp);

                                graph.setVisibility(View.VISIBLE);

                            }
                            catch(Exception e){
                                mProgressbar.setVisibility(View.INVISIBLE);
                                Toast.makeText(getActivity(), "Unexpected Error happened!", Toast.LENGTH_SHORT).show();
                            }

                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError errork) {
                            mProgressbar.setVisibility(View.INVISIBLE);
                            Toast.makeText(getActivity(), "Unexpected Error happened!", Toast.LENGTH_SHORT).show();
                        }
                    });

            jsonRequest.setTag(REQUEST_TAG);
            jsonRequest.setRetryPolicy(new DefaultRetryPolicy(
                    0,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            mQueue.add(jsonRequest);
        }
        else Toast.makeText(getActivity(), "Failed to Connect! Check your Connection", Toast.LENGTH_SHORT).show();


        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    public  void onResume(){
        super.onResume();
    }

    @Override
    public void onStop() {
        super.onStop();
        mProgressbar.setVisibility(View.INVISIBLE);
        if (mQueue != null) {
            mQueue.cancelAll(REQUEST_TAG);
        }
    }

    public JSONArray sendData(int userid){
        HashMap m = new HashMap();
        m.put(getString(R.string.api_send_json_vital_list_arr_userid),userid);
        Log.e(REQUEST_TAG, "sendData: "+(new JSONObject(m)).toString());
        JSONArray x = new JSONArray();
        x.put(new JSONObject(m));
        return x;
    }

    public boolean isNetworkAvailable(final Context context) {
        final ConnectivityManager connectivityManager = ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE));
        return connectivityManager.getActiveNetworkInfo() != null && connectivityManager.getActiveNetworkInfo().isConnected();
    }

    @Override
    public void onClick(View v) {
        if(v == graph){
            Intent intent = new Intent(getContext(),blood_vital_graphActivity.class);
            intent.putStringArrayListExtra("graphDate", date_ArrayList);
            intent.putStringArrayListExtra("graphVal1", val1_ArrayList);
            startActivity(intent);
        }
    }
}
