package org.entresoft.ckannouncement;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;


/**
 * A simple {@link Fragment} subclass.
 */
public class FragmentAnnDetail extends Fragment {
    private static final String LOG_TAG = FragmentAnnDetail.class.getSimpleName();


    public FragmentAnnDetail() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_ann_detail, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        RequestQueue queue = Volley.newRequestQueue(getActivity());
        final TextView annDetailView = (TextView) getActivity().findViewById(R.id.annDetailView);
        final TextView annDetailTitleView = (TextView) getActivity().findViewById(R.id.annDetailTitleView);
        final TextView annDetailLoading = (TextView) getActivity().findViewById(R.id.annDetailLoading);
        Bundle arguments = getArguments();
        Integer id = arguments.getInt("id");
        Log.d(LOG_TAG,id.toString());
        String url = "http://twcl.ck.tp.edu.tw/api/announce/" + id.toString();
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            annDetailLoading.setVisibility(View.GONE);
                            String annContent = response.getString("content");
                            annContent = annContent.substring(0,annContent.lastIndexOf("***"));
                            String annTitle = response.getString("title");
                            annDetailView.setText(annContent);
                            annDetailTitleView.setText(annTitle);
                        } catch (JSONException e) {
                            Log.e(LOG_TAG, e.toString());
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                annDetailView.setText("Request ERROR");
            }
        });
        queue.add(jsonObjectRequest);
    }
}
