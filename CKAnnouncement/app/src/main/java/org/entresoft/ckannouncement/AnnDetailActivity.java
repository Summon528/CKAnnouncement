package org.entresoft.ckannouncement;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;


public class AnnDetailActivity extends ActionBarActivity {
    private static final String LOG_TAG = AnnDetailActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ann_detail);
        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        final TextView annDetailView = (TextView) findViewById(R.id.annDetailView);
        final TextView annDetailTitleView = (TextView) findViewById(R.id.annDetailTitleView);
        final TextView annDetailLoading = (TextView) findViewById(R.id.annDetailLoading);
        Intent intent = getIntent();
        String url = "http://twcl.ck.tp.edu.tw/api/announce/" + intent.getStringExtra(Intent.EXTRA_TEXT);
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

    @Override
        public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_ann_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
