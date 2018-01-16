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
import android.widget.AbsListView;
import android.widget.AdapterView;
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
public class ListDentistVisitsActivityFragment extends Fragment {

    private RequestQueue mQueue;
    ListView listview;
    VitalListAdapter adp;
    ArrayList<String> date_ArrayList;
    ArrayList<String> time_ArrayList;
    ArrayList<String> val1_ArrayList;
    ArrayList<Integer> id_ArrayList;
    ProgressBar mProgressbar;
    int offset;
    boolean flag_loading;
    public static final String REQUEST_TAG = "ListDentistVolley";


    public ListDentistVisitsActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_list_dentist_visits, container, false);

        listview = (ListView) rootView.findViewById(R.id.ListDentist_listView);
        mProgressbar = (ProgressBar) rootView.findViewById(R.id.ListDentist_progressBar);

        date_ArrayList = new ArrayList<String>();
        time_ArrayList = new ArrayList<String>();
        val1_ArrayList = new ArrayList<String>();
        id_ArrayList = new ArrayList<Integer>();
        offset = 0;
        flag_loading = false;
        sendVolley(true);
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {

                Integer x = (Integer) parent.getItemAtPosition(position);
                Intent intent = new Intent(getContext(), DentistDetailsActivity.class);
                intent.putExtra("dentistID", x.intValue());
                startActivity(intent);
            }
        });
        listview.setOnScrollListener(new AbsListView.OnScrollListener() {

            public void onScrollStateChanged(AbsListView view, int scrollState) {


            }

            public void onScroll(AbsListView view, int firstVisibleItem,
                                 int visibleItemCount, int totalItemCount) {

                if(firstVisibleItem+visibleItemCount == totalItemCount && totalItemCount!=0)
                {
                    if(flag_loading == false)
                    {
                        flag_loading = true;
                        offset +=10;
                        sendVolley(false);
                    }
                }
            }
        });


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

    public JSONArray sendData(int userid, int limit, int offset){
        HashMap m = new HashMap();
        m.put(getString(R.string.api_send_json_dentist_userId),userid);
        m.put(getString(R.string.api_send_json_limit),limit);
        m.put(getString(R.string.api_send_json_offset),offset);
        Log.e(REQUEST_TAG, "sendData: "+(new JSONObject(m)).toString());
        JSONArray x = new JSONArray();
        x.put(new JSONObject(m));
        return x;
    }

    public boolean isNetworkAvailable(final Context context) {
        final ConnectivityManager connectivityManager = ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE));
        return connectivityManager.getActiveNetworkInfo() != null && connectivityManager.getActiveNetworkInfo().isConnected();
    }
    public void sendVolley(final boolean first){
        flag_loading = false;
        if (isNetworkAvailable(getContext())) {
            SharedPreferences sharedPre = getActivity().getSharedPreferences(getString(R.string.shared_isUserLoged), Context.MODE_PRIVATE);

            mQueue = VolleyRequestQueue.getInstance(getContext().getApplicationContext())
                    .getRequestQueue();

            JsonArrayRequest jsonRequest = new JsonArrayRequest(Request.Method.POST, getString(R.string.api_url_dentist_list),
                    sendData(sharedPre.getInt(getString(R.string.shared_userId),0),10, offset),
                    new Response.Listener<JSONArray>() {
                        @Override
                        public void onResponse(JSONArray response) {
                            try {
                                mProgressbar.setVisibility(View.INVISIBLE);

                                for(int i=0;i<response.length();i++){
                                    date_ArrayList.add(String.valueOf(response.optJSONObject(i).optString(getString(R.string.api_receive_json_dentist_date))));
                                    time_ArrayList.add(String.valueOf(response.optJSONObject(i).optString(getString(R.string.api_receive_json_dentist_time))));
                                    val1_ArrayList.add("Click to View");
                                    id_ArrayList.add(response.optJSONObject(i).optInt(getString(R.string.api_send_json_dentist_Id)));
                                }
                                if(response.length() == 0) flag_loading = true;
                                if(first) {adp = new VitalListAdapter(getContext(),date_ArrayList,time_ArrayList,val1_ArrayList,id_ArrayList);
                                    listview.setAdapter(adp);
                                }
                                else {
                                    adp.notifyDataSetChanged();
                                }
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
    }

}
