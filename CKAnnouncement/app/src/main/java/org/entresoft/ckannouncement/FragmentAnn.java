package org.entresoft.ckannouncement;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;


import com.afollestad.materialdialogs.MaterialDialog;
import com.android.volley.Request;
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import in.srain.cube.views.ptr.PtrFrameLayout;
import in.srain.cube.views.ptr.PtrHandler;
import in.srain.cube.views.ptr.PtrDefaultHandler;
import in.srain.cube.views.ptr.header.MaterialHeader;


public class FragmentAnn extends Fragment {

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
                holder.title = (TextView) row.findViewById(R.id.list_ann_item_textview);
                holder.date = (TextView) row.findViewById(R.id.list_ann_item_date_textview);
                holder.unit = (TextView) row.findViewById(R.id.list_ann_item_unit_textview);
                holder.isNew = (TextView) row.findViewById(R.id.list_ann_item_isnew_textview);

                row.setTag(holder);
            } else {
                holder = (ViewHolder) row.getTag();
            }

            Announcement announcement = data.get(position);


            holder.title.setText(announcement.getContent());
            holder.unit.setText(announcement.getUnit());
            if (announcement.getDate().length() != 0) {
                holder.date.setText(announcement.getDate().substring(5, 10));
                Calendar cal = Calendar.getInstance();
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
                String formattedCal = format.format(cal.getTime());
                if (formattedCal.equals(announcement.getDate().substring(0, 10)))
                    holder.isNew.setVisibility(View.VISIBLE);
                else holder.isNew.setVisibility(View.INVISIBLE);
            } else {
                holder.isNew.setVisibility(View.INVISIBLE);
                holder.date.setText("");
            }
            return row;
        }

        class ViewHolder {
            TextView title;
            TextView date;
            TextView unit;
            TextView isNew;
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (mListView.getLastVisiblePosition() == annList.size() && (annList.size() == 0 || annList.get(annList.size() - 1).getId() != -1))
            outState.putBoolean("refresh", true);
        else outState.putBoolean("refresh", false);
        outState.putParcelableArrayList("annList", annList);
        outState.putParcelable("info", info);
        outState.putInt("lastId", lastId);
        outState.putInt("FVC", mListView.getFooterViewsCount());
        super.onSaveInstanceState(outState);
    }


    @Override
    public void onDestroy() {
        if (queue != null) {
            queue.cancelAll(new RequestQueue.RequestFilter() {
                @Override
                public boolean apply(Request<?> request) {
                    return true;
                }
            });
        }
        mDialog.dismiss();
        super.onDestroy();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d("Hello", "createview");
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
    View loadingFooter;
    Integer lastId = -1;
    RequestQueue queue;
    FloatingActionButton fab, fab2;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        Log.d("Hello", "activity");
        queue = Volley.newRequestQueue(getActivity());
        super.onActivityCreated(savedInstanceState);
        mListView = (ListView) getActivity().findViewById(R.id.annListView);
        loadingFooter = getActivity().getLayoutInflater().inflate(R.layout.loading_footer, null);
        mListView.addFooterView(loadingFooter, null, false);
        announcementAdapter = new AnnouncementAdapter(getActivity(), R.layout.list_ann_item, annList);
        mListView.setAdapter(announcementAdapter);
        fab = (FloatingActionButton) getActivity().findViewById(R.id.fab);
        fab2 = (FloatingActionButton) getActivity().findViewById(R.id.fab2);
        info = new SearchInfo();
        mDialog = new ProgressDialog(getActivity());
        mDialog.setIndeterminateDrawable(getActivity().getResources().getDrawable(R.drawable.suika_loading));
        mDialog.setMessage("少女祈禱中...");
        mDialog.setCanceledOnTouchOutside(false);
        mDialog.setCancelable(false);
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

        final ViewPager mViewPager = (ViewPager) getActivity().findViewById(R.id.vpPager);
        if (savedInstanceState == null) {
            mDialog.show();
            refreshAnn(true);
        } else {

            annList = savedInstanceState.getParcelableArrayList("annList");
            lastId = savedInstanceState.getInt("lastId");
            announcementAdapter.clear();
            announcementAdapter.addAll(annList);
            announcementAdapter = new AnnouncementAdapter(getActivity(), R.layout.list_ann_item, annList);
            info = savedInstanceState.getParcelable("info");
            mListView.setAdapter(announcementAdapter);

            if (info.getIsSearching()) {
                fab2.setVisibility(View.VISIBLE);
            }

            if (lastId != -1 && ((FragmentAnnViewpager.ViewpagerAdapter) mViewPager.getAdapter()).getCount() == 1) {
                Log.d("LastId", lastId.toString());
                ((FragmentAnnViewpager.ViewpagerAdapter) mViewPager.getAdapter()).setValue(2, lastId);
                (mViewPager.getAdapter()).notifyDataSetChanged();
                mViewPager.setCurrentItem(1);
            }
            if (info.getIsRequesting()) {
                if (savedInstanceState.getBoolean("refresh"))
                    refreshAnn(false);
                else {
                    mDialog.show();
                    refreshAnn(true);
                }
            }
            if (savedInstanceState.getInt("FVC") == 0) mListView.removeFooterView(loadingFooter);
        }


        mPtr.setPtrHandler(new PtrHandler() {
            @Override
            public void onRefreshBegin(PtrFrameLayout frame) {
                mDialog.show();
                frame.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        info.resetStart();
                        if (mListView.getFooterViewsCount() == 0)
                            mListView.addFooterView(loadingFooter);
                        refreshAnn(true);
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
                Log.d("Hello", "CLICK");
                if (annList.get(position).getId() != -1) {
                    if (mViewPager.getChildAt(1) != null)
                        mViewPager.removeViewAt(1);
                    lastId = annList.get(position).getId();
                    ((FragmentAnnViewpager.ViewpagerAdapter) mViewPager.getAdapter()).setValue(2, lastId);
                    (mViewPager.getAdapter()).notifyDataSetChanged();
                    mViewPager.setCurrentItem(1);
                }
            }
        });


        fab.attachToListView(mListView, new ScrollDirectionListener() {
            @Override
            public void onScrollDown() {
                fab2.show();
            }

            @Override
            public void onScrollUp() {
                fab2.hide();
            }
        }, new AbsListView.OnScrollListener() {
            boolean isLastRow = false;

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                if (isLastRow && scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE) {
                    info.getMore();
                    refreshAnn(false);
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

        fab2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                info.reset();
                info.searchReset();
                mListView.addFooterView(loadingFooter, null, false);
                mDialog.show();
                refreshAnn(true);
                fab2.setVisibility(View.GONE);
                mListView.setSelectionAfterHeaderView();
            }
        });

        fab.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final String time[] = {"0", "1", "24", "168", "720", "8760"};
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
                        final EditText searchText = (EditText) linearLayoutMine.findViewById(R.id.search_text);
                        searchText.setText(info.getSearch());
                        searchText.setSelection(info.getSearch().length());
                        String temp = info.getUint();
                        int spinnerPosition = unitAdapter.getPosition(temp);
                        unitSpinner.setSelection(spinnerPosition);
                        temp = info.getGroup();
                        spinnerPosition = groupAdapter.getPosition(temp);
                        groupSpinner.setSelection(spinnerPosition);
                        temp = info.getTime();
                        for (int i = 0; i < 6; i++) {
                            if (temp.length() == 0) {
                                timeSpinner.setSelection(4);
                                break;
                            }
                            if (temp.equals(time[i])) {
                                timeSpinner.setSelection(i);
                                break;
                            }
                        }
                        MaterialDialog searchDialog = md.title(R.string.search)
                                .customView(linearLayoutMine, false)
                                .positiveText(android.R.string.yes)
                                .negativeText(android.R.string.no)
                                .callback(new MaterialDialog.ButtonCallback() {
                                    @Override
                                    public void onPositive(MaterialDialog dialog) {
                                        String s = searchText.getText().toString();
                                        String groupSpinnerText = "", unitSpinnerText = "", timeSpinnerText = "";
                                        if (groupSpinner.getSelectedItemPosition() != 0)
                                            groupSpinnerText = groupSpinner.getSelectedItem().toString();
                                        if (unitSpinner.getSelectedItemPosition() != 0)
                                            unitSpinnerText = unitSpinner.getSelectedItem().toString();
                                        timeSpinnerText = time[timeSpinner.getSelectedItemPosition()];
                                        if (!(s.isEmpty() && groupSpinnerText.isEmpty() && unitSpinnerText.isEmpty() && timeSpinnerText.isEmpty())) {
                                            info.updateSearch(s, unitSpinnerText, groupSpinnerText, timeSpinnerText);
                                            mDialog.show();
                                            refreshAnn(true);
                                            fab2.setVisibility(View.VISIBLE);
                                            mListView.setSelectionAfterHeaderView();
                                        }
                                    }
                                })
                                .build();
                        if (!info.getIsSearching())
                            searchDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
                        searchDialog.show();
                    }
                }

        );
    }


    public void refreshAnn(final boolean refresh) {
        if (getActivity() == null) return;
        if (annList.size() <= 1 || info.getStart() == 0 || annList.get(annList.size() - 1).getId() != -1) {
            String uri = "http://twcl.ck.tp.edu.tw/api/announce?" + info.getInfo();
            Log.d("Refresh", uri);
            info.setIsRequesting(1);
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
                                            jArray.getJSONObject(i).getString("created"),
                                            jArray.getJSONObject(i).getInt("id")));
                                }
                                announcementAdapter.notifyDataSetChanged();
                                mDialog.dismiss();
                                if (jArray.length() < 12) {
                                    if (!isAdded()) return;
                                    annList.add(new Announcement(getResources().getString(R.string.nothing), "", "", -1));
                                    mListView.removeFooterView(loadingFooter);
                                }
                                info.setIsRequesting(0);
                            } catch (JSONException e) {
                                mDialog.dismiss();
                                Toast.makeText(getActivity(), R.string.requestError, Toast.LENGTH_SHORT).show();
                                info.setIsRequesting(0);
                            }
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    mDialog.dismiss();
                    if (annList.size() == 0 || refresh) {
                        if (refresh) annList.clear();
                        annList.add(new Announcement(getResources().getString(R.string.loadfail_swipe), "", "", -1));
                        announcementAdapter.notifyDataSetChanged();
                        mListView.removeFooterView(loadingFooter);
                    }
                    if (annList.get(annList.size() - 1).getId() == -1)
                        mListView.removeFooterView(loadingFooter);
                    info.setIsRequesting(0);
                    Toast.makeText(getActivity(), R.string.requestError, Toast.LENGTH_SHORT).show();
                }
            });
            queue.add(jsonObjectRequest);
        } else mDialog.dismiss();
    }

    public static int dp2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

}

