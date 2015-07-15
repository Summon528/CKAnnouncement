package org.entresoft.ckannouncement;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;


import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import in.srain.cube.views.ptr.PtrClassicFrameLayout;
import in.srain.cube.views.ptr.PtrFrameLayout;
import in.srain.cube.views.ptr.PtrHandler;
import in.srain.cube.views.ptr.PtrDefaultHandler;
import in.srain.cube.views.ptr.header.MaterialHeader;
import in.srain.cube.views.ptr.header.MaterialProgressDrawable;
import in.srain.cube.views.ptr.header.StoreHouseHeader;

public class FragmentAnn extends Fragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        return initView(inflater, container);
    }

    private View initView(LayoutInflater inflater, ViewGroup container) {
        View view = inflater.inflate(R.layout.fragment_ann, container, false);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        /**
         * Making Request.
         */
        final RequestQueue queue = Volley.newRequestQueue(getActivity());
        final ListView mListView = (ListView) getActivity().findViewById(R.id.annListView);
        final ArrayList<String> annList = new ArrayList<String>();
        final ArrayList<Integer> annIdList = new ArrayList<Integer>();
        final ArrayAdapter<String> listAdapter = new ArrayAdapter(getActivity(), android.R.layout.simple_list_item_1, annList);
        // Refresh
        mListView.setAdapter(listAdapter);

        final ProgressDialog mDialog = new ProgressDialog(getActivity());
        mDialog.setIndeterminateDrawable(getActivity().getResources().getDrawable(R.drawable.suika_loading));
        mDialog.setMessage("少女祈禱中...");
        mDialog.show();

        /**
         * Ptr Settings
         */
        final PtrFrameLayout mPtr = (PtrFrameLayout) getActivity().findViewById(R.id.mPtr);
        final MaterialHeader header = new MaterialHeader(getActivity());
        int[] colors = getResources().getIntArray(R.array.google_colors);
        header.setColorSchemeColors(colors);
        header.setLayoutParams(new PtrFrameLayout.LayoutParams(-1, -2));
        header.setPadding(0, dp2px(getActivity(), 15), 0, dp2px(getActivity(), 10));
        header.setPtrFrameLayout(mPtr);
        mPtr.setHeaderView(header);
        mPtr.addPtrUIHandler(header);


        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest("http://twcl.ck.tp.edu.tw/api/announce", null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        mDialog.dismiss();
                        try {
                            JSONArray jArray = response.getJSONArray("anns");
                            for (int i = 0; i < jArray.length(); i++) {
                                annList.add(jArray.getJSONObject(i).getString("title"));
                                annIdList.add(jArray.getJSONObject(i).getInt("id"));
                                Log.d("TAG", "jizzzzzzzzzz" + i);
                                mListView.setAdapter(listAdapter);
                                // Pulling items from the array
                            }

                        } catch (JSONException e) {
                            mDialog.dismiss();
                            annList.add("JSON ERROR");
                            mListView.setAdapter(listAdapter);
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                mDialog.dismiss();
                annList.add("Request ERROR");
                mListView.setAdapter(listAdapter);
            }
        });
        queue.add(jsonObjectRequest);

        mPtr.setPtrHandler(new PtrHandler() {
            @Override
            public void onRefreshBegin(PtrFrameLayout frame) {
                frame.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest("http://twcl.ck.tp.edu.tw/api/announce", null,
                                new Response.Listener<JSONObject>() {
                                    @Override
                                    public void onResponse(JSONObject response) {
                                        try {
                                            JSONArray jArray = response.getJSONArray("anns");
                                            annList.clear();
                                            annIdList.clear();
                                            for (int i = 0; i < jArray.length(); i++) {
                                                annList.add(jArray.getJSONObject(i).getString("title"));
                                                annIdList.add(jArray.getJSONObject(i).getInt("id"));
                                            }
                                            listAdapter.notifyDataSetChanged();
                                        } catch (JSONException e) {
                                            Toast.makeText(getActivity(), "Request ERROR", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Toast.makeText(getActivity(), "Request ERROR", Toast.LENGTH_SHORT).show();
                            }
                        });
                        mPtr.refreshComplete();
                        queue.add(jsonObjectRequest);
                    }
                }, 1800);
            }

            @Override
            public boolean checkCanDoRefresh(PtrFrameLayout frame, View content, View header) {
                return PtrDefaultHandler.checkContentCanBePulledDown(frame, content, header);
            }
        });
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener()

                                         {
                                             @Override
                                             public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                                 Intent intent = new Intent(getActivity(), AnnDetailActivity.class)
                                                         .putExtra(Intent.EXTRA_TEXT, annIdList.get(position).toString());
                                                 startActivity(intent);
                                             }
                                         }

        );
    }

    public static int dp2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }
}
