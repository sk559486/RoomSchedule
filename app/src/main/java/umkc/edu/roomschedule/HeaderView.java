package umkc.edu.roomschedule;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.CountDownTimer;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.DigitalClock;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextClock;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;


enum Status {
    AVAILABLE, NOT_AVAILABLE
}

interface HeaderViewListener {
    void touchedOnRoomName();
}

public class HeaderView extends RelativeLayout {

    Context mContext;
    TextView mRoomName;
    TextClock mDateText;
    TextClock mTimeText;
    HeaderViewListener mListener;
    private ImageView mNetworkUnAvailImageView;

    public HeaderView(final Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        View inflate = inflate(context, R.layout.header, null);
        addView(inflate);

        SharedPreferences prefs =
                PreferenceManager.getDefaultSharedPreferences(context);
        String roomName = prefs.getString("room_name", "");
        Log.w("Pref", roomName);

        mRoomName = (TextView) this.findViewById(R.id.text_room_name);
        mDateText = (TextClock) this.findViewById(R.id.text_header_date);
        mTimeText = (TextClock) this.findViewById(R.id.text_header_time);
        mNetworkUnAvailImageView = (ImageView) this.findViewById(R.id.network_unavail_image);
        mNetworkUnAvailImageView.setVisibility(View.INVISIBLE);

        mRoomName.setText(String.format(roomName));
        mRoomName.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.touchedOnRoomName();
            }
        });
    }

    public void updateNetworkUnavailability(boolean available) {
        if (available) {
            mNetworkUnAvailImageView.setVisibility(View.INVISIBLE);
        } else {
            mNetworkUnAvailImageView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onWindowVisibilityChanged(int visibility) {
        super.onWindowVisibilityChanged(visibility);
        if (visibility == View.VISIBLE) {
            SharedPreferences prefs =
                    PreferenceManager.getDefaultSharedPreferences(mContext);
            String roomName = prefs.getString("room_name", "Room #");
            mRoomName.setText(roomName);
        }
    }

    public void setHeaderViewListener(HeaderViewListener listener) {
        this.mListener = listener;
    }

    public void setStatus(Status status) {
        if (status == Status.AVAILABLE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                this.setBackground(new ColorDrawable(mContext.getResources().getColor(R.color.colorAvailable, mContext.getTheme())));
                mRoomName.setTextColor(mContext.getResources().getColor(R.color.colorAvailableText, mContext.getTheme()));
                mDateText.setTextColor(mContext.getResources().getColor(R.color.colorAvailableText, mContext.getTheme()));
                mTimeText.setTextColor(mContext.getResources().getColor(R.color.colorAvailableText, mContext.getTheme()));
            } else {
                this.setBackground(new ColorDrawable(mContext.getResources().getColor(R.color.colorAvailable)));
                mRoomName.setTextColor(mContext.getResources().getColor(R.color.colorAvailableText));
                mDateText.setTextColor(mContext.getResources().getColor(R.color.colorAvailableText));
                mTimeText.setTextColor(mContext.getResources().getColor(R.color.colorAvailableText));
            }
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                this.setBackground(new ColorDrawable(mContext.getResources().getColor(R.color.colorNotAvailable, mContext.getTheme())));
                mRoomName.setTextColor(mContext.getResources().getColor(R.color.colorNotAvailableText, mContext.getTheme()));
                mDateText.setTextColor(mContext.getResources().getColor(R.color.colorNotAvailableText, mContext.getTheme()));
                mTimeText.setTextColor(mContext.getResources().getColor(R.color.colorNotAvailableText, mContext.getTheme()));
            } else {
                this.setBackground(new ColorDrawable(mContext.getResources().getColor(R.color.colorNotAvailable)));
                mRoomName.setTextColor(mContext.getResources().getColor(R.color.colorNotAvailableText));
                mDateText.setTextColor(mContext.getResources().getColor(R.color.colorNotAvailableText));
                mTimeText.setTextColor(mContext.getResources().getColor(R.color.colorNotAvailableText));
            }
        }
    }
}
