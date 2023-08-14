package com.ray.mysdk.bean;

import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.NonNull;

/**
 * @author ray
 * @date 2023/8/14 10:13
 */
public class Sample implements Parcelable {
    private int mNum;
    private String mStr;

    public int getNum() {
        return mNum;
    }

    public void setNum(final int num) {
        mNum = num;
    }

    public String getStr() {
        return mStr;
    }

    public void setStr(final String str) {
        mStr = str;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel parcel, int i) {
        parcel.writeInt(this.mNum);
        parcel.writeString(this.mStr);
    }

    public void readFromParcel(Parcel source) {
        this.mNum = source.readInt();
        this.mStr = source.readString();
    }

    public Sample() {
    }

    protected Sample(Parcel in) {
        this.mNum = in.readInt();
        this.mStr = in.readString();
    }

    public static final Creator<Sample> CREATOR = new Creator<Sample>() {
        @Override
        public Sample createFromParcel(Parcel source) {
            return new Sample(source);
        }

        @Override
        public Sample[] newArray(int size) {
            return new Sample[size];
        }
    };
}
