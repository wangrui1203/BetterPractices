package com.example.myview.nimblegums;

import android.content.Context;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import static com.ray.mysdk.nimblegums.NimBleGum.ACTIVITY_DEFAULT_DURATION;
import static com.ray.mysdk.nimblegums.NimBleGum.OPERATOR_DEFAULT_DURATION;

/**
 * @author ray
 * @date 2023/9/28 17:16
 */
public class NimBleGumApply implements Parcelable {
    private static long _ID = 1000;
    public long id;
    public int type = -1;
    public String applyType;
    public long operatorTimeout;
    public long activityTimeout;
    public Bundle extras = new Bundle();

    protected NimBleGumApply() {
    }

    protected NimBleGumApply(Parcel parcel) {
        readFromParcelImpl(parcel);
    }

    public static final Creator<NimBleGumApply> CREATOR = new Creator<NimBleGumApply>() {
        @Override
        public NimBleGumApply createFromParcel(Parcel in) {
            return new NimBleGumApply(in);
        }

        @Override
        public NimBleGumApply[] newArray(int size) {
            return new NimBleGumApply[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        try {
            // IMPORTANT: Add marshaling code in writeToParcelImpl as we
            // want to intercept all pending events written to the parcel.
            writeToParcelImpl(parcel, flags);
            // Must be written last!
        } finally {
        }
    }

    private void writeToParcelImpl(Parcel parcel, int flags) {
        parcel.writeLong(id);
        parcel.writeInt(type);
        parcel.writeLong(operatorTimeout);
        parcel.writeLong(activityTimeout);
        parcel.writeString(applyType);
        parcel.writeBundle(extras);
    }

    private void readFromParcelImpl(Parcel parcel) {
        id = parcel.readLong();
        type = parcel.readInt();
        operatorTimeout = parcel.readLong();
        activityTimeout = parcel.readLong();
        applyType = parcel.readString();
        extras = Bundle.setDefusable(parcel.readBundle(), true); // may be null
    }

    public static class Builder {

        private NimBleGumApply mNBG;
        private Bundle mUserExtras = new Bundle();

        public Builder(Context context) {
            this(context, null);
        }

        public Builder(Context context, NimBleGumApply toAdopt) {
            if (toAdopt == null) {
                mNBG = new NimBleGumApply();
                mNBG.id = context.getPackageName().hashCode() / 1000 + _ID++;
                mNBG.activityTimeout = ACTIVITY_DEFAULT_DURATION;
                mNBG.operatorTimeout = OPERATOR_DEFAULT_DURATION;
            } else {
                mNBG = toAdopt;
            }
        }

        public Builder setType(int type) {
            mNBG.type = type;
            return this;
        }

        public Builder setOperatorTimeout(long operatorTimeout) {
            mNBG.operatorTimeout = operatorTimeout;
            return this;
        }

        public Builder setActivityTimeout(long activityTimeout) {
            mNBG.activityTimeout = activityTimeout;
            return this;
        }

        public Builder setExtras(Bundle extras) {
            if (extras != null) {
                mUserExtras = extras;
            }
            return this;
        }

        private Bundle getAllExtras() {
            final Bundle saveExtras = (Bundle) mUserExtras.clone();
            saveExtras.putAll(mNBG.extras);
            return saveExtras;
        }

        public Builder setApplyType(String type) {
            mNBG.applyType = type;
            return this;
        }

        public NimBleGumApply build() {
            if (TextUtils.isEmpty(mNBG.applyType)) {
                throw new IllegalArgumentException(
                        "Not allowed to build without applyType ");
            }
            if (mNBG.type == -1) {
                throw new IllegalArgumentException(
                        "Not allowed to build without type ");
            }
            if (mUserExtras != null) {
                mNBG.extras = getAllExtras();
            }
            return mNBG;
        }

    }
}

