package umkc.edu.roomschedule;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * A placeholder fragment containing a simple view.
 */
public class ReserveActivity extends Activity {
    /**
     * The fragment argument representing the section number for this
     * fragment.
     */
    private static final String ARG_SECTION_NUMBER = "section_number";
    private ProgressDialog mProgressDialog;

    private AutoCompleteTextView mTitleText;
    private EditText mEmailEditText;
    private EditText mNameEditText;
    private Spinner mDurationSpinner;
    private Spinner mDomainsSpinner;
    private ReserveHeaderView mHeaderView;
    private Date mStartDate;
    TextView startDateTextView;

    static String TAG = "ReserveActivity";

    static int SCHEDULE = 112;

    private static final String[] TITLES = new String[]{
            "Class",
            "Exam",
            "Faculty+",
            "Interview",
            "Meeting",
            "Special Event",
            "Student+",
            "Study Group",
            "Testing"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_reserve);

        mNameEditText = (EditText) findViewById(R.id.edit_name);

        mTitleText = (AutoCompleteTextView) findViewById(R.id.edit_title);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_dropdown_item_1line, TITLES);
        mTitleText.setAdapter(adapter);

        mEmailEditText = (EditText) findViewById(R.id.edit_email);
        mDurationSpinner = (Spinner) findViewById(R.id.spinner_duration);
        mDomainsSpinner = (Spinner) findViewById(R.id.spinner_domains);
        mHeaderView = (ReserveHeaderView) findViewById(R.id.headerview);

        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setTitle("Sending Email..");

        TableLayout tableLayout = (TableLayout) findViewById(R.id.tableLayout);
        tableLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideKeyboard(mEmailEditText);
            }
        });

        // Got start date from the Schedule activity
        Intent intent = getIntent();
        long startDate = intent.getLongExtra("startDate", 0);
        startDateTextView = (TextView) findViewById(R.id.text_start_date);

        startDateTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ReserveActivity.this, BasicActivity.class);
                intent.putExtra("caller", "Reserve");
                startActivityForResult(intent, SCHEDULE);
            }
        });

        setaDate(startDate);
    }

    void setaDate(long startDate) {
        TableRow tablerow = (TableRow) findViewById(R.id.table_row_start_date);
        if (startDate != 0) {
            Date date = new Date(startDate);

            // Rounding off the selected time to 30 mins slot.
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            int unroundedMinutes = calendar.get(Calendar.MINUTE);
            if (unroundedMinutes > 30) {
                calendar.add(Calendar.MINUTE, -(unroundedMinutes - 30));
            } else if (unroundedMinutes > 0) {
                calendar.add(Calendar.MINUTE, -unroundedMinutes);
            }
//            int mod = unroundedMinutes % 30;
//            calendar.add(Calendar.MINUTE, mod < 8 ? -mod : 30 - mod);

            Log.d(TAG, "onCreate: " + calendar.getTime().toString());
            Log.d(TAG, "onCreate: " + date.toString());
            tablerow.setVisibility(View.VISIBLE);
            SimpleDateFormat dateFormat1 = new SimpleDateFormat("EEE, MMM d, h:mm a", Locale.US);
            mStartDate = calendar.getTime();
            startDateTextView.setText(dateFormat1.format(calendar.getTime()));
        } else {
            mStartDate = null;
            tablerow.setVisibility(View.GONE);
        }
    }

    public void hideKeyboard(View editableView) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(editableView.getWindowToken(), 0);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d("MyApp", "OnStart");

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                hideKeyboard(mTitleText);
            }
        });

        delayedIdle();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == SCHEDULE) {
            if (resultCode == RESULT_OK) {
                long startDate = data.getLongExtra("startDate", new Date().getTime());
                Log.d(TAG, "onActivityResult: " + startDate);
                setaDate(startDate);
            }
        }
    }

    Handler _idleHandler = new Handler();
    Runnable _idleRunnable = new Runnable() {
        @Override
        public void run() {
            //handle your IDLE state
            mHeaderView.startTimer(new CountDownTimerListener() {
                @Override
                public void onFinish() {
                    finish();
                }
            });
        }
    };

    @Override
    public void onUserInteraction() {
        super.onUserInteraction();
        Log.d("MyApp", "UserInteraction");
        delayedIdle();
    }

    private void delayedIdle() {
        mHeaderView.stopTimer();
        _idleHandler.removeCallbacks(_idleRunnable);
        _idleHandler.postDelayed(_idleRunnable, (1000 * 60));
    }

    public void cancel(View view) {
        finish();
    }

    public void reserve(View view) {
        boolean isValid = true;
        if( mNameEditText.getText().toString().length() == 0) {
            mNameEditText.setError( "Name is required!" );
            isValid = false;
        }
        if( mEmailEditText.getText().toString().length() == 0 ) {
            mEmailEditText.setError( "Email is required!" );
            isValid = false;
        }
        if( mTitleText.getText().toString().length() == 0 ) {
            mTitleText.setError( "Meeting Title is required!" );
            isValid = false;
        }

        if (!isValid) {
            return;
        }
        String body = getEmailBody();
        Log.d("Email", body);
        new EmailTask().execute(
                mEmailEditText.getText().toString() + "" +
                        mDomainsSpinner.getSelectedItem().toString(), body);
        mProgressDialog.show();
    }


    public String getEmailBody() {

        String duration = mDurationSpinner.getSelectedItem().toString();

        String unit = duration.split(" ")[1];
        int val = Integer.parseInt(duration.split(" ")[0]);


        Date now = new Date();
        if (mStartDate != null) {
            now = mStartDate;
        }

        String from = now.toString();
        String to = "";
        Calendar cal = Calendar.getInstance();
        cal.setTime(now);

        if (unit.equalsIgnoreCase("hr")) {
            cal.add(Calendar.HOUR, val);
            Date end = cal.getTime();
            to = end.toString();
        } else {
            cal.add(Calendar.MINUTE, val);
            Date end = cal.getTime();
            to = end.toString();
        }

        StringBuilder body = new StringBuilder();
        body.append("Title : ").append(mTitleText.getText().toString()).append("\n");
        body.append("From : ").append(mNameEditText.getText().toString()).append("\n");
        body.append("Email : ").append(mEmailEditText.getText().toString())
                .append(mDomainsSpinner.getSelectedItem().toString()).append("\n");
        body.append("Time : ")
                .append(from).append(" -- ").append(to).append("\n");

        return body.toString();
    }

    class EmailTask extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... params) {
            OkHttpClient client = new OkHttpClient();

            SharedPreferences prefs =
                    PreferenceManager.getDefaultSharedPreferences(ReserveActivity.this);
            String roomName = prefs.getString("room_name", "Room #");
            String adminName = prefs.getString("admin_name", getString(R.string.pref_default_admin_name));
            String adminEmail = prefs.getString("admin_email", getString(R.string.pref_default_admin_email));
            String adminPhone = prefs.getString("admin_phone", getString(R.string.pref_default_admin_phone));

            RequestBody formBody = new FormBody.Builder()
                    .add("room_name", roomName)
                    .add("email", params[0])
                    .add("message", params[1])
                    .add("admin_name", adminName)
                    .add("admin_email", adminEmail)
                    .add("admin_phone", adminPhone)
                    .build();
            Request request = new Request.Builder()
                    .url("http://umkclaw.link/doNOTdelete/contact.php")
                    .post(formBody)
                    .build();

            try {
                Response response = client.newCall(request).execute();
                Log.d("EmailRequest", response.body().string());
                // Do something with the response.
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            mProgressDialog.dismiss();
            Toast.makeText(getApplicationContext(), "Your Reservation is Successful!", Toast.LENGTH_LONG).show();
            finish();
        }
    }
}