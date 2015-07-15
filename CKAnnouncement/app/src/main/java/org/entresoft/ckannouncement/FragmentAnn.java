package org.entresoft.ckannouncement;

import android.app.Activity;
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
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
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

    public class Announcement {

        private String content;
        private String unit, date;
        private Integer id;

        public Announcement(String content, String unit, String date, Integer id) {
            this.content = content;
            this.unit = unit;
            this.date = date;
            this.id = id;
        }

        public String getContent() {
            return content;
        }

        public String getUnit() {
            return unit;
        }

        public String getDate() {
            return date;
        }

        public Integer getId() {
            return id;
        }
    }

    public class AnnouncementAdapter extends ArrayAdapter<Announcement> {
        private final Context context;
        private final ArrayList<Announcement> data;
        private final int layoutResourceId;

        public AnnouncementAdapter(Context context, int layoutResourceId, ArrayList<Announcement> data) {
            super(context, layoutResourceId, data);
            this.context = context;
            this.data = data;
            this.layoutResourceId = layoutResourceId;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View row = convertView;
            ViewHolder holder = null;

            if (row == null) {
                LayoutInflater inflater = ((Activity) context).getLayoutInflater();
                row = inflater.inflate(layoutResourceId, parent, false);

                holder = new ViewHolder();
                holder.textView1 = (TextView) row.findViewById(R.id.list_ann_item_textview);
                holder.textView2 = (TextView) row.findViewById(R.id.list_ann_item_date_textview);
                holder.textView3 = (TextView) row.findViewById(R.id.list_ann_item_unit_textview);

                row.setTag(holder);
            } else {
                holder = (ViewHolder) row.getTag();
            }

            Announcement announcement = data.get(position);

            holder.textView1.setText(announcement.getContent());
            holder.textView2.setText(announcement.getDate());
            holder.textView3.setText(announcement.getUnit());

            return row;
        }

        class ViewHolder {
            TextView textView1;
            TextView textView2;
            TextView textView3;
        }
    }

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
        final ArrayList<Announcement> annList = new ArrayList<Announcement>();
        final AnnouncementAdapter announcementAdapter = new AnnouncementAdapter(getActivity(), R.layout.list_ann_item, annList);
        mListView.setAdapter(announcementAdapter);

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
                                annList.add(new Announcement(jArray.getJSONObject(i).getString("title"),
                                        jArray.getJSONObject(i).getString("author_group_name"),
                                        jArray.getJSONObject(i).getString("created").substring(5, 10),
                                        jArray.getJSONObject(i).getInt("id")));
                                Log.d("TAG", "jizzzzzzzzzz" + i);
                                mListView.setAdapter(announcementAdapter);
                            }

                        } catch (JSONException e) {
                            mDialog.dismiss();
                            annList.add(new Announcement("Request ERROR", "", "", -1));
                            mListView.setAdapter(announcementAdapter);
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                mDialog.dismiss();
                annList.add(new Announcement("Request ERROR", "", "", -1));
                mListView.setAdapter(announcementAdapter);
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
                                            for (int i = 0; i < jArray.length(); i++) {
                                                annList.add(new Announcement(jArray.getJSONObject(i).getString("title"),
                                                        jArray.getJSONObject(i).getString("author_group_name"),
                                                        jArray.getJSONObject(i).getString("created").substring(5, 10),
                                                        jArray.getJSONObject(i).getInt("id")));
                                            }
                                            announcementAdapter.notifyDataSetChanged();
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
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                             @Override
                                             public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                                 if (annList.get(position).getId() != -1) {
                                                     Intent intent = new Intent(getActivity(), AnnDetailActivity.class)
                                                             .putExtra(Intent.EXTRA_TEXT, annList.get(position).getId().toString());
                                                     startActivity(intent);
                                                 }
                                             }
                                         }

        );

        mListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            boolean isLastRow = false;
            Integer start = 12;
            @Override
            public void onScrollStateChanged(AbsListView absListView, int i) {
                if (isLastRow && i == AbsListView.OnScrollListener.SCROLL_STATE_IDLE) {
                    Log.d("Hello", start.toString());
                    final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest("http://twcl.ck.tp.edu.tw/api/announce?start=" + start.toString(), null,
                            new Response.Listener<JSONObject>() {
                                @Override
                                public void onResponse(JSONObject response) {
                                    try {
                                        JSONArray jArray = response.getJSONArray("anns");
                                        for (int i = 0; i < jArray.length(); i++) {
                                            annList.add(new Announcement(jArray.getJSONObject(i).getString("title"),
                                                    jArray.getJSONObject(i).getString("author_group_name"),
                                                    jArray.getJSONObject(i).getString("created").substring(5, 10),
                                                    jArray.getJSONObject(i).getInt("id")));
                                        }
                                        announcementAdapter.notifyDataSetChanged();
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
                    start += 12;
                }
            }


            @Override
            public void onScroll(AbsListView absListView, int i, int i1, int i2) {
                if (i + i1 == i2 && i2 > 0) {
                    isLastRow = true;
                }
            }
        });
    }

    public static int dp2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }
}
