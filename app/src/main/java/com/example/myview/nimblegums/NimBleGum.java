package com.example.myview.nimblegums;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.os.*;
import android.util.Log;
import com.example.myview.view.NRemoteViews;

import java.util.ArrayList;
import java.util.List;

/**
 * @author ray
 * @date 2023/9/28 16:38
 */
public class NimBleGum  implements Parcelable, NimBleGumInterface {
    private static final String TAG = NimBleGumDebugConfig.TAG_NBG;
    private static long _ID = 1000;
    /**
     * @hide
     */
    public static final String EXTRA_BUILDER_APPLICATION_INFO = "android.appInfo";

    public NRemoteViews heavyContent;

    public NRemoteViews lightContent;

    public int type;

    public int priority;

    public int showState;

    public long operatorTimeout;

    public static final long OPERATOR_DEFAULT_DURATION = 2000;

    public long activityTimeout;

    public static final long ACTIVITY_DEFAULT_DURATION = 4000;

    public Bundle extras = new Bundle();

    public long id;

    public boolean canSlideUp;

    public boolean lockedTime;

    private OnShowListener mOnShowListener;

    private OnDismissListener mOnDismissListener;

    private OnConvertListener mOnConvertListener;

    private OnSlideDownListener mOnSlideDownListener;

    private OnUserLeaveListener mOnUserLeaveListener;

    private OnUserOperateListener mOnUserOperateListener;

    private OnOperateEventListener mOnOperateEventListener;

    private OnUiModeChangerListener mOnUiModeChangerListener;

    private List<OnChangerListener> mOnChangerListener;

    /**
     * The creation time of the notification
     */
    private long creationTime;

    public static final int TYPE_CALL = 0;

    public static final int TYPE_NOMI_ASR = 1;

    public static final int TYPE_NOMI_TRIGGER = 2;

    public static final int TYPE_TRIGGER = 3;

    public static final int TYPE_ELSE = 4;

    public static final int STATE_SHOW_LIGHT = 0;

    public static final int STATE_SHOW_HEAVY = 1;

    public static final int STATE_DISMISS_SLIDE_UP = 2;

    public static final int STATE_DISMISS_OUTSIDE = 3;

    public static final int STATE_DISMISS_REPLACE = 4;

    public static final int STATE_DISMISS_CANCEL = 5;

    public static final int STATE_DISMISS_FUSION_EXPAND = 6;

    public static final int STATE_DISMISS_SHOW_NULL = 7;

    public static final int STATE_DISMISS_UI_MODE = 8;

    public static final int STATE_DISMISS_PROVIDER_DIE = 9;

    public static final int STATE_DISMISS_LAYOUT_REQUEST = 10;

    public static final int STATE_DISMISS_BIND_DIE = 11;

    public static final int STATE_CONVERT_LIGHT = 12;

    public static final int STATE_CONVERT_HEAVY = 13;

    public static final int PRIORITY_DEFAULT = 0;

    public static final int PRIORITY_LOW = -1;

    public static final int PRIORITY_MIN = -2;

    public static final int PRIORITY_HIGH = 1;

    public static final int PRIORITY_MAX = 2;

    public NimBleGum() {
        this.creationTime = System.currentTimeMillis();
        this.priority = PRIORITY_DEFAULT;
        this.showState = STATE_SHOW_LIGHT;
        this.canSlideUp = true;
        this.lockedTime = false;
    }

    public NimBleGum(Parcel parcel) {
        // IMPORTANT: Add unmarshaling code in readFromParcel as the pending
        // intents in extras are always written as the last entry.
        readFromParcelImpl(parcel);
    }

    /**
     * Parcelable.Creator that instantiates Notification objects
     */
    public static final Creator<NimBleGum> CREATOR
            = new Creator<NimBleGum>() {
        public NimBleGum createFromParcel(Parcel parcel) {
            return new NimBleGum(parcel);
        }

        public NimBleGum[] newArray(int size) {
            return new NimBleGum[size];
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
        parcel.writeInt(1);
        parcel.writeLong(id);
        parcel.writeLong(creationTime);
        parcel.writeInt(type);
        parcel.writeLong(operatorTimeout);
        parcel.writeLong(activityTimeout);
        parcel.writeInt(priority);
        parcel.writeInt(showState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            parcel.writeBoolean(canSlideUp);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            parcel.writeBoolean(lockedTime);
        }
        if (heavyContent != null) {
            parcel.writeInt(1);
            heavyContent.writeToParcel(parcel, 0);
        } else {
            parcel.writeInt(0);
        }

        if (lightContent != null) {
            parcel.writeInt(1);
            lightContent.writeToParcel(parcel, 0);
        } else {
            parcel.writeInt(0);
        }
        parcel.writeBundle(extras); // null ok
    }

    private void readFromParcelImpl(Parcel parcel) {
        int version = parcel.readInt();
        id = parcel.readLong();
        creationTime = parcel.readLong();
        type = parcel.readInt();
        operatorTimeout = parcel.readLong();
        activityTimeout = parcel.readLong();
        priority = parcel.readInt();
        showState = parcel.readInt();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            canSlideUp = parcel.readBoolean();
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            lockedTime = parcel.readBoolean();
        }

        if (parcel.readInt() != 0) {
            heavyContent = NRemoteViews.CREATOR.createFromParcel(parcel);
        }

        if (parcel.readInt() != 0) {
            lightContent = NRemoteViews.CREATOR.createFromParcel(parcel);
        }
        extras = Bundle.setDefusable(parcel.readBundle(), true); // may be null

    }


    protected Builder mBuilder;

    public void setBuilder(Builder builder) {
        if (mBuilder != builder) {
            mBuilder = builder;
        }
    }

    public void setShowState(int state) {
        synchronized (this) {
            this.showState = state;
        }
    }

    public int getShowState() {
        synchronized (this) {
            return showState;
        }
    }

    public NimBleGum build() {
        checkBuilder();
        return mBuilder.build();
    }

    protected void checkBuilder() {
        if (mBuilder == null) {
            throw new IllegalArgumentException("Style requires a valid Builder object");
        }
    }

    @Override
    public void addOnShowListener(OnShowListener listener) {
        this.mOnShowListener = listener;
    }

    @Override
    public void removeOnShowListener() {
        this.mOnShowListener = null;
    }

    public OnShowListener getOnShowListener() {
        return mOnShowListener;
    }

    @Override
    public void addOnDismissListener(OnDismissListener listener) {
        this.mOnDismissListener = listener;
    }

    @Override
    public void removeOnDismissListener() {
        this.mOnDismissListener = null;
    }

    public OnDismissListener getOnDismissListener() {
        return mOnDismissListener;
    }

    @Override
    public void addOnConvertListener(OnConvertListener listener) {
        this.mOnConvertListener = listener;
    }

    @Override
    public void removeOnConvertListener() {
        this.mOnConvertListener = null;
    }

    public OnConvertListener getOnConvertListener() {
        return mOnConvertListener;
    }

    @Override
    public void addOnSlideDownListener(OnSlideDownListener listener) {
        this.mOnSlideDownListener = listener;
    }

    @Override
    public void removeOnSlideDownListener() {
        this.mOnSlideDownListener = null;
    }

    public OnSlideDownListener getOnSlideDownListener() {
        return mOnSlideDownListener;
    }

    @Override
    public void addOnUserLeaveListener(OnUserLeaveListener listener) {
        this.mOnUserLeaveListener = listener;
    }

    @Override
    public void removeOnUserLeaveListener() {
        this.mOnUserLeaveListener = null;
    }

    public OnUserLeaveListener getOnUserLeaveListener() {
        return mOnUserLeaveListener;
    }

    @Override
    public void addOnUserOperateListener(OnUserOperateListener listener) {
        this.mOnUserOperateListener = listener;
    }

    @Override
    public void removeOnUserOperateListener() {
        this.mOnUserOperateListener = null;
    }

    public OnUserOperateListener getOnUserOperateListener() {
        return mOnUserOperateListener;
    }

    @Override
    public void addOnOperateEventListener(OnOperateEventListener listener) {
        this.mOnOperateEventListener = listener;
    }

    @Override
    public void removeOnOperateEventListener() {
        this.mOnOperateEventListener = null;
    }

    public OnOperateEventListener getOnOperateEventListener() {
        return mOnOperateEventListener;
    }

    @Override
    public void addOnUiModeChangerListener(OnUiModeChangerListener listener) {
        this.mOnUiModeChangerListener = listener;
    }

    @Override
    public void removeOnUiModeChangerListener() {
        this.mOnUiModeChangerListener = null;
    }

    public OnUiModeChangerListener getOnUiModeChangerListener() {
        return mOnUiModeChangerListener;
    }

    @Override
    public void addOnChangerListener(OnChangerListener listener) {
        this.mOnChangerListener.add(listener);
    }

    @Override
    public void removeOnChangerListener() {
        this.mOnChangerListener.clear();
    }

    @Override
    public void removeOnChangerListeners(OnChangerListener listener) {
        this.mOnChangerListener.remove(listener);
    }

    public List<OnChangerListener> getOnChangerListener() {
        return mOnChangerListener;
    }

    @Override
    public void cancel() {
        NimbleGumManager.getInstance().cancel(this);
    }

    @Override
    public boolean isShowing() {
        return NimbleGumManager.getInstance().isShowing(this);
    }

    public static class Builder {
        /**
         * @hide
         */
        public static final String EXTRA_REBUILD_CONTENT_VIEW_ACTION_COUNT =
                "android.rebuild.heavyContentActionCount";
        /**
         * @hide
         */
        public static final String EXTRA_REBUILD_CALLER_VIEW_ACTION_COUNT =
                "android.rebuild.lightContentActionCount";
        private Context mContext;

        private NimBleGum mNBG;
        private Bundle mUserExtras = new Bundle();

        public Builder(Context context) {
            this(context, null);
        }

        public Builder(Context context, NimBleGum toAdopt) {
            mContext = context;
            if (toAdopt == null) {
                mNBG = new NimBleGum();
                mNBG.priority = PRIORITY_DEFAULT;
                mNBG.showState = STATE_SHOW_LIGHT;
                mNBG.activityTimeout = ACTIVITY_DEFAULT_DURATION;
                mNBG.operatorTimeout = OPERATOR_DEFAULT_DURATION;
                mNBG.type = TYPE_ELSE;
                mNBG.id = mContext.getPackageName().hashCode() / 1000 + _ID++;
                mNBG.canSlideUp = true;
                mNBG.lockedTime = false;
                mNBG.mOnChangerListener = new ArrayList<>();
            } else {
                mNBG = toAdopt;
            }
        }

        public Builder setHeavyContent(NRemoteViews heavyContent) {
            mNBG.heavyContent = heavyContent;
            return this;
        }

        public Builder setLightContent(NRemoteViews lightContent) {
            mNBG.lightContent = lightContent;
            return this;
        }

        public Builder setType(int type) {
            mNBG.type = type;
            return this;
        }

        public Builder setPriority(int priority) {
            mNBG.priority = priority;
            return this;
        }

        public Builder setShowState(int showState) {
            mNBG.showState = showState;
            return this;
        }

        public Builder setCanSlideUp(boolean canSlideUp){
            mNBG.canSlideUp = canSlideUp;
            return this;
        }

        public Builder setLockTime(boolean lockedTime){
            mNBG.lockedTime = lockedTime;
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

        public Builder addExtras(Bundle extras) {
            if (extras != null) {
                mUserExtras.putAll(extras);
            }
            return this;
        }

        public Builder addOnShowListener(OnShowListener listener) {
            mNBG.mOnShowListener = listener;
            return this;
        }

        public Builder addOnDismissListener(OnDismissListener listener) {
            mNBG.mOnDismissListener = listener;
            return this;
        }

        public Builder addOnConvertListener(OnConvertListener listener){
            mNBG.mOnConvertListener = listener;
            return this;
        }

        public Builder addOnSlideDownListener(OnSlideDownListener listener){
            mNBG.mOnSlideDownListener = listener;
            return this;
        }

        public Builder addOnUserLeaveListener(OnUserLeaveListener listener) {
            mNBG.mOnUserLeaveListener = listener;
            return this;
        }

        public Builder addOnUserOperateListener(OnUserOperateListener listener) {
            mNBG.mOnUserOperateListener = listener;
            return this;
        }

        public Builder addOnOperateEventListener(OnOperateEventListener listener) {
            mNBG.mOnOperateEventListener = listener;
            return this;
        }

        public Builder addOnUiModeChangerListener(OnUiModeChangerListener listener) {
            mNBG.mOnUiModeChangerListener = listener;
            return this;
        }

        public Builder addOnChangerListener(OnChangerListener listener) {
            mNBG.mOnChangerListener.add(listener);
            return this;
        }

        public Bundle getExtras() {
            return mUserExtras;
        }

        private Bundle getAllExtras() {
            final Bundle saveExtras = (Bundle) mUserExtras.clone();
            saveExtras.putAll(mNBG.extras);
            return saveExtras;
        }

        public NimBleGum getNimBleGum() {
            return build();
        }

        public NimBleGum build() {
            if (mUserExtras != null) {
                mNBG.extras = getAllExtras();
            }
            mNBG.creationTime = System.currentTimeMillis();
            NimBleGum.addFieldsFromContext(mContext, mNBG);
            return mNBG;
        }

        public NimBleGum buildInto(NimBleGum n, NRemoteViews light, NRemoteViews heavy) {
            build().cloneInto(n, light, heavy);
            return n;
        }

    }

    public void cloneInto(NimBleGum that, NRemoteViews light, NRemoteViews heavy) {
        that.operatorTimeout = this.operatorTimeout;
        that.activityTimeout = this.activityTimeout;
        that.creationTime = this.creationTime;
        that.type = this.type;
        that.id = this.id;
        that.priority = this.priority;
        that.showState = this.showState;
        that.canSlideUp = this.canSlideUp;
        that.lockedTime = this.lockedTime;
        that.heavyContent = heavy;
        that.lightContent = light;

        if (this.extras != null) {
            try {
                that.extras = new Bundle(this.extras);
                // will unparcel
                that.extras.size();
            } catch (BadParcelableException e) {
                Log.e(TAG, "could not unparcel extras from notification: " + this, e);
                that.extras = null;
            }
        }
    }


    /**
     * @hide
     */
    public static void addFieldsFromContext(Context context, NimBleGum nimBleGum) {
        addFieldsFromContext(context.getApplicationInfo(), nimBleGum);
    }

    /**
     * @hide
     */
    public static void addFieldsFromContext(ApplicationInfo ai, NimBleGum nimBleGum) {
        nimBleGum.extras.putParcelable(EXTRA_BUILDER_APPLICATION_INFO, ai);
    }

    @Override
    public String toString() {
        return "NimBleGum{" +
                "type=" + type +
                ", priority=" + priority +
                ", showState=" + showState +
                ", operatorTimeout=" + operatorTimeout +
                ", activityTimeout=" + activityTimeout +
                ", canSlideUp=" + canSlideUp +
                ", lockedTime=" + lockedTime +
                '}';
    }
}
