package org.entresoft.ckannouncement;


import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;

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
import java.util.regex.Pattern;


/**
 * A simple {@link Fragment} subclass.
 */
public class FragmentAnnDetail extends Fragment {
    private static final String LOG_TAG = FragmentAnnDetail.class.getSimpleName();


    public FragmentAnnDetail() {
        // Required empty public constructor
    }

    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_ann, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_openInBrowser) {
            Uri uri = Uri.parse(openInBrowser);
            Intent callIntent = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(callIntent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        setHasOptionsMenu(true);
        View rootView = inflater.inflate(R.layout.fragment_ann_detail, container, false);

        ScrollView sv = (ScrollView) rootView.findViewById(R.id.annDetailScrollView);
        sv.smoothScrollTo(0, 0);
        return rootView;
    }
    String openInBrowser;
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        RequestQueue queue = Volley.newRequestQueue(getActivity());
        final TextView annDetailView = (TextView) getActivity().findViewById(R.id.annDetailView);
        final TextView annDetailTitleView = (TextView) getActivity().findViewById(R.id.annDetailTitleView);
        final TextView annDetailLoading = (TextView) getActivity().findViewById(R.id.annDetailLoading);
        final TextView annDetailInfoView = (TextView) getActivity().findViewById(R.id.annDetailInfoView);
        final LinearLayout fileLayout = (LinearLayout) getActivity().findViewById(R.id.annDetailFileLayout);
        Bundle arguments = getArguments();
        Integer id = arguments.getInt("id");
        Log.d(LOG_TAG, id.toString());
        String url = "http://twcl.ck.tp.edu.tw/api/announce/" + id.toString();
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            annDetailLoading.setVisibility(View.GONE);
                            JSONArray jArray = response.getJSONArray("atts");
                            if (jArray.length() != 0) fileLayout.setVisibility(View.VISIBLE);
                            for (int i = 0; i < jArray.length(); i++) {
                                final TextView textView = new TextView(getActivity());
                                final String path = jArray.getJSONObject(i).getString("path");
                                textView.setText(jArray.getJSONObject(i).getString("filename"));
                                textView.setMinHeight(150);
                                textView.setGravity(Gravity.CENTER);
                                fileLayout.addView(textView);
                                textView.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        Uri uri = Uri.parse("http://twcl.ck.tp.edu.tw/file/" + path + "?download=1");
                                        Intent callIntent = new Intent(Intent.ACTION_VIEW, uri);
                                        startActivity(callIntent);
                                    }
                                });
                            }

                            String annContent = response.getString("content");
                            openInBrowser = annContent.substring(annContent.lastIndexOf("(")+1, annContent.lastIndexOf(")"));
                            annContent = annContent.substring(0, annContent.lastIndexOf("***"));
                            annContent = Pattern.compile(" *\r\n *([^ 123456789一二三四五六七八九十(１２３４５６７８９（])", Pattern.DOTALL).matcher(annContent).replaceAll("$1");
                            annContent = Pattern.compile("\\[★相關網址([123456789]+)：[^]]+[^(]+[(]([^)]+)[)]", Pattern.DOTALL).matcher(annContent).replaceAll("相關網址$1：$2");
                            String annTitle = response.getString("title");
                            String info = response.getString("author_group_name").replaceAll("\\s+", "") + "  " + response.getString("author_name").replaceAll("\\s+", "") +
                                    "\n發表：" + response.getString("created") + "\n更新：" + response.getString("updated");
                            annDetailInfoView.setText(info);
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

        /** listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
        @Override public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Uri number = Uri.parse("http://twcl.ck.tp.edu.tw/file/" + filePathList.get(position) + "?download=1");
        Intent callIntent = new Intent(Intent.ACTION_VIEW, number);
        startActivity(callIntent);
        }

        });
         **/
    }
}
