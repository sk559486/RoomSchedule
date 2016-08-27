package umkc.edu.roomschedule;

import android.content.Context;
import android.os.CountDownTimer;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;


interface CountDownTimerListener {
    public void onFinish();
}

public class ReserveHeaderView extends RelativeLayout {

    Context mContext;
    TextView mTimerText;
    TextView mInfoText;
    CountDownTimer mCountDownTimer;
    CountDownTimerListener mCountDownTimerListener;

    public ReserveHeaderView(final Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        View inflate = inflate(context, R.layout.header_reserve, null);
        addView(inflate);
        mTimerText = (TextView) this.findViewById(R.id.text_timer);
        mInfoText = (TextView) this.findViewById(R.id.textview_info);
        mTimerText.setVisibility(View.INVISIBLE);
        mInfoText.setVisibility(View.INVISIBLE);

        mCountDownTimer = new CountDownTimer(30000, 1000) {

            public void onTick(long millisUntilFinished) {
                mTimerText.setText("" + millisUntilFinished / 1000);
            }

            public void onFinish() {
                mTimerText.setText("Done!");
                mInfoText.setVisibility(View.INVISIBLE);
                if (mCountDownTimerListener != null) {
                    mCountDownTimerListener.onFinish();
                }
                Log.d("HeaderView", "Done with Timer");
            }
        };
    }

    public void startTimer(CountDownTimerListener listener) {
        mCountDownTimerListener = listener;
        mTimerText.setVisibility(View.VISIBLE);
        mInfoText.setVisibility(View.VISIBLE);
        mCountDownTimer.cancel();
        mCountDownTimer.start();
    }

    public void stopTimer() {
        mTimerText.setVisibility(View.INVISIBLE);
        mInfoText.setVisibility(View.INVISIBLE);
        mCountDownTimer.cancel();
    }
}
