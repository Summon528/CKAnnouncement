package org.entresoft.ckannouncement;

import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.LinearLayout;
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

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.regex.Pattern;

public class ShowFromWeb extends ActionBarActivity {

    String openInBrowser;
    TextView annDetailView;
    TextView annDetailTitleView;
    LinearLayout annDetailLoading;
    TextView annDetailLoadingText;
    TextView annDetailInfoView;
    LinearLayout fileLayout;
    Button retryButton;
    RequestQueue queue;
    Integer id;
    String annTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_ann_detail);
        Intent mIntent = this.getIntent();
        Uri URI = mIntent.getData();
        id = Integer.valueOf(Pattern.compile("http://twcl.ck.tp.edu.tw/announce/([^?]*).*").matcher(URI.toString()).replaceFirst("$1"));
        queue = Volley.newRequestQueue(this);
        annDetailView = (TextView) findViewById(R.id.annDetailView);
        annDetailTitleView = (TextView) findViewById(R.id.annDetailTitleView);
        annDetailLoading = (LinearLayout) findViewById(R.id.annDetailLoading);
        annDetailLoadingText = (TextView) findViewById(R.id.annDetailLoadingText);
        annDetailInfoView = (TextView) findViewById(R.id.annDetailInfoView);
        fileLayout = (LinearLayout) findViewById(R.id.annDetailFileLayout);
        retryButton = (Button) findViewById(R.id.retryButton);
        retryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                annDetailLoadingText.setText(R.string.loading);
                retryButton.setVisibility(View.GONE);
                refreshAnn();
            }
        });
        refreshAnn();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_ann_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_openInBrowser) {
            Uri uri = Uri.parse(openInBrowser);
            Intent callIntent = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(callIntent);
            return true;
        } else if (id == R.id.action_shareAnn) {
            String shareString = annTitle + '\n' + "http://twcl.ck.tp.edu.tw/announce/" + id;
            if (annTitle == null) {
                Toast.makeText(this, R.string.plzWait, Toast.LENGTH_SHORT).show();
            } else {
                Intent sendIntent = new Intent(Intent.ACTION_SEND);
                sendIntent.setType("text/plain");
                sendIntent.putExtra(Intent.EXTRA_TEXT, shareString);
                startActivity(sendIntent);
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    public void refreshAnn() {
        String url = "http://twcl.ck.tp.edu.tw/api/announce/" + id.toString();
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONArray jArray = response.getJSONArray("atts");
                            if (jArray.length() != 0)
                                findViewById(R.id.fileTextView).setVisibility(View.VISIBLE);
                            for (int i = 0; i < jArray.length(); i++) {
                                final TextView textView = (TextView) View.inflate(getBaseContext(), R.layout.ann_attachment, null);
                                final String path = jArray.getJSONObject(i).getString("path");
                                 if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
                                    textView.setBackground(getResources().getDrawable(R.drawable.attachment_ripple));
                                }else{
                                textView.setBackground(getResources().getDrawable(R.drawable.attachment));
                                }
                                textView.setText(jArray.getJSONObject(i).getString("filename"));
                                fileLayout.addView(textView);
                                textView.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        DownloadManager downloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
                                        Uri uri = null;
                                        try {
                                            String encodedPath = URLEncoder.encode(path, "UTF-8");
                                            uri = Uri.parse("http://twcl.ck.tp.edu.tw/file/" + encodedPath + "?download=1");
                                        } catch (UnsupportedEncodingException e) {
                                            e.printStackTrace();
                                        }
                                        Log.d("Uri", uri.toString());
                                        String extension = MimeTypeMap.getFileExtensionFromUrl(uri.toString());
                                        Log.d("Uri", extension);
                                        String mimeType = "application/octet-stream";
                                        if (extension != null)
                                            mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
                                        DownloadManager.Request request = new DownloadManager.Request(uri);
                                        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                                                .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS + "/" + getString(R.string.app_name), path.substring(path.indexOf("/")))
                                                .setMimeType(mimeType);
                                        Toast.makeText(getApplicationContext(), R.string.downloadStart, Toast.LENGTH_SHORT).show();
                                        downloadManager.enqueue(request);
                                    }
                                });
                            }

                            String annContent = response.getString("content");
                            openInBrowser = annContent.substring(annContent.lastIndexOf("(") + 1, annContent.lastIndexOf(")"));
                            annContent = annContent.substring(0, annContent.lastIndexOf("***"));
                            // annContent = Pattern.compile("[\uF06C  　]*\r\n[\uF06C  　]*([^\uF06C  　123456789abcdefg一二三四五六七八九十(１２３４５６７８９（★])", Pattern.DOTALL).matcher(annContent).replaceAll("$1");
                            annContent = Pattern.compile("\r\n[\uF06C  　]*[123456789abcdefg][.]|^1[.]", Pattern.DOTALL).matcher(annContent).replaceAll("$0 ");
                            annContent = Pattern.compile("(\\(|（)(http|www)", Pattern.DOTALL).matcher(annContent).replaceAll("$1 $2");
                            annContent = Pattern.compile("(.tw|.php|.aspx|.com|.tw//?|.aspx//?|.php//?|.com//?)(\\)|）)", Pattern.DOTALL).matcher(annContent).replaceAll("$1 $2");
                            annContent = Pattern.compile("\\[★相關網址([123456789]+)：[^]]+[^(]+[(]([^)]+)[)]", Pattern.DOTALL).matcher(annContent).replaceAll("相關網址$1：$2");
                            annTitle = response.getString("title");
                            String info = response.getString("author_group_name").replaceAll("\\s+", "") + "  " + response.getString("author_name").replaceAll("\\s+", "") +
                                    "\n發表：" + response.getString("created") + "\n更新：" + response.getString("updated");
                            annDetailLoading.setVisibility(View.GONE);
                            annDetailInfoView.setText(info);
                            annDetailView.setText(annContent);
                            annDetailTitleView.setText(annTitle);
                        } catch (JSONException e) {
                            annDetailLoadingText.setText(R.string.loadfail);
                            retryButton.setVisibility(View.VISIBLE);
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                annDetailLoadingText.setText(R.string.loadfail);
                retryButton.setVisibility(View.VISIBLE);
            }
        });
        queue.add(jsonObjectRequest);
    }
}
