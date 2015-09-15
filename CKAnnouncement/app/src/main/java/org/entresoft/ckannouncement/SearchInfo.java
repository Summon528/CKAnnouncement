package org.entresoft.ckannouncement;

import android.os.Parcel;
import android.os.Parcelable;

import java.net.URLEncoder;

/**
 * Created by CodyTseng on 2015/7/20.
 */
public class SearchInfo implements Parcelable {
    private Integer start, isSearchng, isRequesting;
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
        out.writeInt(isRequesting);
    }

    public static final Parcelable.Creator<SearchInfo> CREATOR
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
        isRequesting=in.readInt();
    }

    public SearchInfo() {
        start = 0;
        search = "";
        time = "";
        unit = "";
        group = "";
        isSearchng = 0;
        isRequesting = 0;
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

    public boolean getIsRequesting (){
        return isRequesting==1;
    }

    public void setIsRequesting (int n) {
        isRequesting = n;
    }
    public Integer getStart(){
        return start;
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
                    "&author=" + URLEncoder.encode(unit, "UTF-8") + "&hours=" + time.toString();
        } catch (Exception e) {
            return null;
        }
    }
}

