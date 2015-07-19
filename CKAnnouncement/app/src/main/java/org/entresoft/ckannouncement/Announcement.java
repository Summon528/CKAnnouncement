package org.entresoft.ckannouncement;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by CodyTseng on 2015/7/20.
 */
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

    public static final Parcelable.Creator<Announcement> CREATOR = new Parcelable.Creator<Announcement>() {
        public Announcement createFromParcel(Parcel in) {
            return new Announcement(in);
        }

        public Announcement[] newArray(int size) {
            return new Announcement[size];
        }
    };

}
