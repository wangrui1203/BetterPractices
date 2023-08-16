package com.ray.mysdk.bean;

import android.os.Parcel;
import android.os.Parcelable;
import android.renderscript.Sampler;
import androidx.annotation.NonNull;

/**
 * @author ray
 * @date 2023/8/14 10:13
 */
public class Sample implements Parcelable {
    private int mNum;
    private String mStr;

    private Sample2 sample2;

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

    public Sample2 getSample2() {
        return sample2;
    }

    public void setSample2(Sample2 sample2) {
        this.sample2 = sample2;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel parcel, int i) {
        parcel.writeInt(mNum);
        parcel.writeString(mStr);
        parcel.writeSerializable(sample2);
    }
    public Sample(){}

    protected Sample(Parcel in) {
        mNum = in.readInt();
        mStr = in.readString();
        sample2 = (Sample2) in.readSerializable();
    }

    public static final Creator<Sample> CREATOR = new Creator<Sample>() {
        @Override
        public Sample createFromParcel(Parcel in) {
            return new Sample(in);
        }

        @Override
        public Sample[] newArray(int size) {
            return new Sample[size];
        }
    };

    @Override
    public String toString() {
        return "Sample{" +
                "mNum=" + mNum +
                ", mStr='" + mStr + '\'' +
                ", sample2=" + sample2 +
                '}';
    }
}
