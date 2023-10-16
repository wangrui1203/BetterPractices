package com.example.myview.nimblegums;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * @author ray
 * @date 2023/9/28 16:40
 */
public class NimbleGumOperateEvent implements Parcelable {

    public static final int DISMISS_OUT = 0;
    private int type;
    private String dismissReason;
    private String data;


    protected NimbleGumOperateEvent(Parcel in) {
        type = in.readInt();
        dismissReason = in.readString();
        data = in.readString();
    }

    public NimbleGumOperateEvent() {
    }

    public NimbleGumOperateEvent(int type) {
        this.type = type;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getDismissReason() {
        return dismissReason;
    }

    public void setDismissReason(String dismissReason) {
        this.dismissReason = dismissReason;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(type);
        parcel.writeString(dismissReason);
        parcel.writeString(data);
    }


    public static final Creator<NimbleGumOperateEvent> CREATOR = new Creator<NimbleGumOperateEvent>() {
        @Override
        public NimbleGumOperateEvent createFromParcel(Parcel in) {
            return new NimbleGumOperateEvent(in);
        }

        @Override
        public NimbleGumOperateEvent[] newArray(int size) {
            return new NimbleGumOperateEvent[size];
        }
    };

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("SC.SmartComponentOperateEvent{");
        sb.append(type);
        sb.append(',' + dismissReason);
        sb.append(',' + data);
        sb.append('}');
        return sb.toString();
    }

}

