package org.entresoft.ckannouncement;


import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.text.Layout;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
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
        inflater.inflate(R.menu.menu_ann_detail, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_openInBrowser) {
            if (annTitle == null) {
                Toast.makeText(getActivity(), R.string.plzWait, Toast.LENGTH_SHORT).show();
            } else {
                Uri uri = Uri.parse(openInBrowser);
                Intent callIntent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(callIntent);
                return true;
            }
        } else if (id == R.id.action_shareAnn) {
            String shareString = annTitle + '\n' + "http://twcl.ck.tp.edu.tw/announce/" + getArguments().getInt("id");
            if (annTitle == null) {
                Toast.makeText(getActivity(), R.string.plzWait, Toast.LENGTH_SHORT).show();
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
    TextView annDetailView;
    TextView annDetailTitleView;
    LinearLayout annDetailLoading;
    TextView annDetailLoadingText;
    TextView annDetailInfoView;
    LinearLayout fileLayout;
    Button retryButton;
    RequestQueue queue;
    static Integer id;
    String annTitle;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        queue = Volley.newRequestQueue(getActivity());
        annDetailView = (TextView) getActivity().findViewById(R.id.annDetailView);
        annDetailTitleView = (TextView) getActivity().findViewById(R.id.annDetailTitleView);
        annDetailLoading = (LinearLayout) getActivity().findViewById(R.id.annDetailLoading);
        annDetailLoadingText = (TextView) getActivity().findViewById(R.id.annDetailLoadingText);
        annDetailInfoView = (TextView) getActivity().findViewById(R.id.annDetailInfoView);
        fileLayout = (LinearLayout) getActivity().findViewById(R.id.annDetailFileLayout);
        retryButton = (Button) getActivity().findViewById(R.id.retryButton);
        retryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                annDetailLoadingText.setText(R.string.loading);
                retryButton.setVisibility(View.GONE);
                refreshAnn();
            }
        });
        Bundle arguments = getArguments();
        id = arguments.getInt("id");
        refreshAnn();
        Log.d(LOG_TAG, id.toString());
    }


    public void refreshAnn() {
        String url = "http://twcl.ck.tp.edu.tw/api/announce/" + id.toString();
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        if (getActivity() == null) return;
                        try {
                            JSONArray jArray = response.getJSONArray("atts");
                            if (jArray.length() != 0) getActivity().findViewById(R.id.fileTextView).setVisibility(View.VISIBLE);
                            for (int i = 0; i < jArray.length(); i++) {
                                final TextView textView = (TextView) View.inflate(getActivity(), R.layout.ann_attachment, null);
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
                                        DownloadManager downloadManager = (DownloadManager) getActivity().getSystemService(Context.DOWNLOAD_SERVICE);
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
                                        Toast.makeText(getActivity(), R.string.downloadStart, Toast.LENGTH_SHORT).show();
                                        long downloadId = downloadManager.enqueue(request);

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
