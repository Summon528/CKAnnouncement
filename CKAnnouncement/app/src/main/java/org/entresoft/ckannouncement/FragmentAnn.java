package org.entresoft.ckannouncement;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;


import com.afollestad.materialdialogs.MaterialDialog;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.melnykov.fab.FloatingActionButton;
import com.melnykov.fab.ScrollDirectionListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;

import in.srain.cube.views.ptr.PtrFrameLayout;
import in.srain.cube.views.ptr.PtrHandler;
import in.srain.cube.views.ptr.PtrDefaultHandler;
import in.srain.cube.views.ptr.header.MaterialHeader;


public class FragmentAnn extends Fragment {

    public class SearchInfo implements Parcelable {
        private Integer start, isSearchng;
        private String unit, group, time, search;

        public int describeContents() {
            return 0;
        }

        public void writeToParcel(Parcel out, int flags) {
            out.writeInt(start);
            out.writeString(unit);
            out.writeString(group);
            out.writeString(time);
            out.writeString(search);
            out.writeInt(isSearchng);
        }

        public final Parcelable.Creator<SearchInfo> CREATOR
                = new Parcelable.Creator<SearchInfo>() {
            public SearchInfo createFromParcel(Parcel in) {
                return new SearchInfo(in);
            }

            public SearchInfo[] newArray(int size) {
                return new SearchInfo[size];
            }
        };

        private SearchInfo(Parcel in) {
            start = in.readInt();
            unit = in.readString();
            group = in.readString();
            time = in.readString();
            search = in.readString();
            isSearchng = in.readInt();
        }

        public SearchInfo() {
            start = 0;
            search = "";
            time = "";
            unit = "";
            group = "";
            isSearchng = 0;
        }

        public Void reset() {
            start = 0;
            search = "";
            time = "";
            unit = "";
            group = "";
            return null;
        }

        public Void resetStart() {
            start = 0;
            return null;
        }

        public Void searchReset() {
            isSearchng = 0;
            return null;
        }

        public Void updateSearch(String search, String unit, String group, String time) {
            this.start = 0;
            this.time = time;
            this.unit = unit;
            this.search = search;
            this.group = group;
            isSearchng = 1;
            return null;
        }

        public boolean getIsSearching() {
            return isSearchng == 1;
        }

        public Void getMore() {
            start += 12;
            return null;
        }

        public String getInfo() {
            try {
                return "start=" + start.toString() + "&search=" + URLEncoder.encode(search, "UTF-8") + "&group=" + URLEncoder.encode(group, "UTF-8") +
                        "&author=" + URLEncoder.encode(unit, "UTF-8") + "&time=" + time.toString();
            } catch (Exception e) {
                return null;
            }
        }
    }

    public class Announcement implements Parcelable {

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

        private Announcement(Parcel in) {
            content = in.readString();
            unit = in.readString();
            date = in.readString();
            id = in.readInt();
        }

        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            out.writeString(content);
            out.writeString(unit);
            out.writeString(date);
            out.writeInt(id);
        }

        public final Parcelable.Creator<Announcement> CREATOR = new Parcelable.Creator<Announcement>() {
            public Announcement createFromParcel(Parcel in) {
                return new Announcement(in);
            }

            public Announcement[] newArray(int size) {
                return new Announcement[size];
            }
        };

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

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelableArrayList("annList", annList);
        outState.putParcelable("info", info);
        int index = mListView.getFirstVisiblePosition();
        View v = mListView.getChildAt(0);
        int top = (v == null) ? 0 : (v.getTop() - mListView.getPaddingTop());
        outState.putInt("index", index);
        outState.putInt("top", top);
        super.onSaveInstanceState(outState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        return initView(inflater, container);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_ann, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_openInBrowser) {
            Uri uri = Uri.parse("http://web.ck.tp.edu.tw/ann/");
            Intent callIntent = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(callIntent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    private View initView(LayoutInflater inflater, ViewGroup container) {
        View view = inflater.inflate(R.layout.fragment_ann, container, false);
        return view;
    }

    ArrayList<Announcement> annList = new ArrayList<Announcement>();
    AnnouncementAdapter announcementAdapter;
    ProgressDialog mDialog;
    SearchInfo info;
    ListView mListView;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mListView = (ListView) getActivity().findViewById(R.id.annListView);
        View v = getActivity().findViewById(R.id.loading_footer);
        View loadingFooter = getActivity().getLayoutInflater().inflate(R.layout.loading_footer, null);
        mListView.addFooterView(loadingFooter);
        announcementAdapter = new AnnouncementAdapter(getActivity(), R.layout.list_ann_item, annList);
        mListView.setAdapter(announcementAdapter);
        final FloatingActionButton fab = (FloatingActionButton) getActivity().findViewById(R.id.fab);
        info = new SearchInfo();
        mDialog = new ProgressDialog(getActivity());
        mDialog.setIndeterminateDrawable(getActivity().getResources().getDrawable(R.drawable.suika_loading));
        mDialog.setMessage("少女祈禱中...");
        ((ActionBarActivity) getActivity()).getSupportActionBar().setTitle(R.string.title_section2);
        final PtrFrameLayout mPtr = (PtrFrameLayout) getActivity().findViewById(R.id.mPtr);
        final MaterialHeader header = new MaterialHeader(getActivity());
        int[] colors = getResources().getIntArray(R.array.google_colors);
        header.setColorSchemeColors(colors);
        header.setLayoutParams(new PtrFrameLayout.LayoutParams(-1, -2));
        header.setPadding(0, dp2px(getActivity(), 15), 0, dp2px(getActivity(), 10));
        header.setPtrFrameLayout(mPtr);
        mPtr.setHeaderView(header);
        mPtr.addPtrUIHandler(header);


        if (savedInstanceState == null) {
            mDialog.show();
            refreshAnn(info, true);
        } else {
            annList = savedInstanceState.getParcelableArrayList("annList");
            announcementAdapter.clear();
            announcementAdapter.addAll(annList);
            announcementAdapter = new AnnouncementAdapter(getActivity(), R.layout.list_ann_item, annList);
            info = savedInstanceState.getParcelable("info");
            mListView.setAdapter(announcementAdapter);
        }

        mPtr.setPtrHandler(new PtrHandler() {
            @Override
            public void onRefreshBegin(PtrFrameLayout frame) {
                frame.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        info.resetStart();
                        refreshAnn(info, true);
                        mPtr.refreshComplete();
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
        });

        fab.attachToListView(mListView, new ScrollDirectionListener() {
            @Override
            public void onScrollDown() {
                fab.show();
            }

            @Override
            public void onScrollUp() {
                fab.hide();
            }
        }, new AbsListView.OnScrollListener() {
            boolean isLastRow = false;

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                if (isLastRow && scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE) {
                    info.getMore();
                    refreshAnn(info, false);
                    isLastRow = false;
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (firstVisibleItem + visibleItemCount == totalItemCount - 3 && totalItemCount > 0) {
                    isLastRow = true;
                }
            }
        });

        getActivity().findViewById(R.id.fab).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (info.getIsSearching()) {
                    info.reset();
                    info.searchReset();
                    mDialog.show();
                    refreshAnn(info, true);
                    fab.setColorNormalResId(R.color.colorPrimary);
                    fab.setColorPressedResId(R.color.colorPrimaryDark);
                    fab.setColorRippleResId(R.color.colorPrimaryDark);
                    fab.setImageResource(R.drawable.ic_action_search);
                    mListView.setSelectionAfterHeaderView();
                } else {
                    MaterialDialog.Builder md = new MaterialDialog.Builder(getActivity());
                    LayoutInflater factory = LayoutInflater.from(getActivity());
                    final View stdView = factory.inflate(R.layout.dialog_search, null);
                    final LinearLayout linearLayoutMine = (LinearLayout) stdView.findViewById(R.id.search_linearLayout);
                    final Spinner groupSpinner = (Spinner) linearLayoutMine.findViewById(R.id.group_spinner);
                    final Spinner unitSpinner = (Spinner) linearLayoutMine.findViewById(R.id.unit_spinner);
                    final Spinner timeSpinner = (Spinner) linearLayoutMine.findViewById(R.id.time_spinner);
                    ArrayAdapter<CharSequence> groupAdapter = ArrayAdapter.createFromResource(getActivity(),
                            R.array.group_array, android.R.layout.simple_spinner_item);
                    ArrayAdapter<CharSequence> unitAdapter = ArrayAdapter.createFromResource(getActivity(),
                            R.array.unit_array, android.R.layout.simple_spinner_item);
                    ArrayAdapter<CharSequence> timeAdapter = ArrayAdapter.createFromResource(getActivity(),
                            R.array.time_array, android.R.layout.simple_spinner_item);
                    groupAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    unitAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    timeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    groupSpinner.setAdapter(groupAdapter);
                    unitSpinner.setAdapter(unitAdapter);
                    timeSpinner.setAdapter(timeAdapter);
                    final TextView searchTextView = (TextView) linearLayoutMine.findViewById(R.id.search_text);
                    md.title(R.string.search)
                            .customView(linearLayoutMine, false)
                            .positiveText(android.R.string.yes)
                            .callback(new MaterialDialog.ButtonCallback() {
                                @Override
                                public void onPositive(MaterialDialog dialog) {
                                    String s = searchTextView.getText().toString();
                                    String time[] = {"", "1", "24", "168", "720", "8760"};
                                    String groupSpinnerText = "", unitSpinnerText = "", timeSpinnerText = "";
                                    if (groupSpinner.getSelectedItemPosition() != 0)
                                        groupSpinnerText = groupSpinner.getSelectedItem().toString();
                                    if (unitSpinner.getSelectedItemPosition() != 0)
                                        unitSpinnerText = unitSpinner.getSelectedItem().toString();
                                    timeSpinnerText = time[timeSpinner.getSelectedItemPosition()];
                                    if (!(s.isEmpty() && groupSpinnerText.isEmpty() && unitSpinnerText.isEmpty() && timeSpinnerText.isEmpty())) {
                                        info.updateSearch(s, unitSpinnerText, groupSpinnerText, timeSpinnerText);
                                        mDialog.show();
                                        refreshAnn(info, true);
                                        fab.setColorNormalResId(android.R.color.holo_red_light);
                                        fab.setColorPressedResId(android.R.color.holo_red_dark);
                                        fab.setColorRippleResId(android.R.color.holo_red_dark);
                                        fab.setImageResource(R.drawable.ic_clear);
                                        mListView.setSelectionAfterHeaderView();
                                    }
                                }
                            })
                            .build()
                            .show();
                }
            }
        });
    }


    public void refreshAnn(SearchInfo info, final boolean refresh) {
        if (annList.size() <=1 || info.start==0 || annList.get(annList.size() - 1).getId() != -1) {
            String uri = "http://twcl.ck.tp.edu.tw/api/announce?" + info.getInfo();
            Log.d("Refresh", uri);
            final RequestQueue queue = Volley.newRequestQueue(getActivity());
            final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(uri, null,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                JSONArray jArray = response.getJSONArray("anns");
                                if (refresh) annList.clear();
                                Log.d("Refresh", String.valueOf(jArray.length()));
                                for (int i = 0; i < jArray.length(); i++) {
                                    annList.add(new Announcement(jArray.getJSONObject(i).getString("title"),
                                            jArray.getJSONObject(i).getString("author_group_name").replaceAll("\\s+", ""),
                                            jArray.getJSONObject(i).getString("created").substring(5, 10),
                                            jArray.getJSONObject(i).getInt("id")));
                                }
                                if (jArray.length() < 12) {
                                    annList.add(new Announcement(getResources().getString(R.string.nothing), "", "", -1));
                                }
                                announcementAdapter.notifyDataSetChanged();
                                mDialog.dismiss();
                            } catch (JSONException e) {
                                mDialog.dismiss();
                                Toast.makeText(getActivity(), R.string.requestError, Toast.LENGTH_SHORT).show();
                            }
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    mDialog.dismiss();
                    Toast.makeText(getActivity(), R.string.requestError, Toast.LENGTH_SHORT).show();
                }
            });
            queue.add(jsonObjectRequest);
        }
        else mDialog.dismiss();
    }

    public static int dp2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }
}

