package umkc.edu.roomschedule;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import javax.net.ssl.SSLSocketFactory;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import umkc.edu.roomschedule.models.Entry;

public class MainActivity extends AppCompatActivity implements HeaderViewListener {

    private TextView statusTextView;
    private TextView timeTextView;
    private HeaderView mHeaderView;
    private Button mReserveButton;
    List<Entry> mEntries;

    static String TAG = "MainActivity";
    static int SCHEDULE = 111;

    private final OkHttpClient client = new OkHttpClient();

    public void fetchPasswords() throws Exception {
        Request request = new Request.Builder()
                .url("passwords url to be given")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

                try {
                    SharedPreferences prefs =
                            PreferenceManager.getDefaultSharedPreferences(MainActivity.this);

                    JSONObject data = new JSONObject(response.body().string());
                    JSONObject passwords = data.getJSONObject("data");
                    Log.d("JSON", passwords.toString());
                    SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
                    SharedPreferences.Editor editor = settings.edit();
                    editor.putString("passwords", passwords.toString());
                    editor.apply();

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        assert fab != null;
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
                Intent intent = new Intent(MainActivity.this, HelpActivity.class);
                startActivity(intent);
            }
        });

        statusTextView = (TextView) findViewById(R.id.status_textView);
        timeTextView = (TextView) findViewById(R.id.time_textView);
        mHeaderView = (HeaderView) findViewById(R.id.header_view_main);
        assert mHeaderView != null;
        mHeaderView.setHeaderViewListener(this);

        mReserveButton = (Button) findViewById(R.id.reserve_button);
        assert mReserveButton != null;
        mReserveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                reserveNow(v, 0);
                showSchedule(v);
            }
        });

        mEntries = new ArrayList<Entry>();
        new AdAstraTask().execute("");
        try {
            fetchPasswords();
        } catch (Exception e) {
            e.printStackTrace();
        }
        startLockTask();

        KeyguardManager keyguardManager = (KeyguardManager) getSystemService(Activity.KEYGUARD_SERVICE);
        KeyguardManager.KeyguardLock lock = keyguardManager.newKeyguardLock(KEYGUARD_SERVICE);
        lock.disableKeyguard();
        hideStatusBar(null);
    }

    @Override
    public void onBackPressed() {
//        super.onBackPressed();
//        moveTaskToBack(false);
        Log.d("Log", "onBackPressed");
    }

    @Override
    public void startLockTask() {
        super.startLockTask();
    }

    @Override
    protected void onStart() {
        super.onStart();

        SharedPreferences prefs =
                PreferenceManager.getDefaultSharedPreferences(MainActivity.this);

        int MINUTES = Integer.parseInt(prefs.getString("sync_frequency", "15")); // The delay in minutes
        Log.d(TAG, "onStart: Syncing for every " + MINUTES + " minutes");

        reScheduleTimer(MINUTES);
    }

    public void hideStatusBar(View view) {
        // Hide status bar
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

//    To disable the active apps button
    @Override
    protected void onPause() {
        super.onPause();

        ActivityManager activityManager = (ActivityManager) getApplicationContext()
                .getSystemService(Context.ACTIVITY_SERVICE);

        activityManager.moveTaskToFront(getTaskId(), 0);
    }

    private Timer timer = new Timer("alertTimer", true);

    public void reScheduleTimer(int duration) {
        timer = new Timer("alertTimer", true);
        MyTimerTask timerTask = new MyTimerTask();
        timer.schedule(timerTask, 1000L, 1000 * 60 * duration);
    }

    private class MyTimerTask extends TimerTask {
        @Override
        public void run() {
            // Do stuff
            new AdAstraTask().execute("");
        }
    }

    @Override
    public void touchedOnRoomName() {
        final Dialog myDialog = new Dialog(this);
        myDialog.setTitle("Admin Login");
        myDialog.setContentView(R.layout.dialog_login);
        Button loginButton = (Button) myDialog.findViewById(R.id.dialog_button_login);
        final EditText name = (EditText) myDialog.findViewById(R.id.dialog_edit_name);
        final EditText password = (EditText) myDialog.findViewById(R.id.dialog_edit_password);

        SharedPreferences prefs =
                PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
        String passwords = prefs.getString("passwords", "{}");
        String pass = "";
        try {
            JSONObject data = new JSONObject(passwords);
            String roomName = prefs.getString("room_name", "");
            pass = data.getString(roomName);
        } catch (JSONException e) {
            e.printStackTrace();
            pass = "";
        }

        final String finalPass = pass;
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (password.getText().toString().equals(finalPass)) {
                    Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                    startActivity(intent);
                }
                myDialog.dismiss();
            }
        });
        myDialog.show();
    }

    public void update(Entry entry) {
        if (isReserveAvailable()) {
            if (entry == null) {
                mHeaderView.setStatus(Status.AVAILABLE);
                statusTextView.setText(R.string.available_text);
                timeTextView.setText(getString(R.string.event_time_format1));
                return;
            }
            Date now = new Date();
            Calendar calendar = Calendar.getInstance(Locale.US);
            calendar.setTime(now);
            int nowDate = calendar.get(Calendar.DATE);
            calendar.setTime(entry.startDate);
            int entryDate = calendar.get(Calendar.DATE);

            DateFormat dateFormat1 = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
            if (nowDate == entryDate) {
                dateFormat1 = new SimpleDateFormat("hh:mm a", Locale.US);
            }

            // Up coming Event - Available
            if (now.before(entry.startDate)) {
                mHeaderView.setStatus(Status.AVAILABLE);
                statusTextView.setText(R.string.available_text);
//            Log.w("EventInfo", R.string.available_text + " - " + entry);
                timeTextView.setText(getString(R.string.event_time_format, dateFormat1.format(entry.startDate)));
            } else {
                mHeaderView.setStatus(Status.NOT_AVAILABLE);
                statusTextView.setText(R.string.not_available_text);
//            Log.w("EventInfo", R.string.not_available_text + " - " + entry);
                timeTextView.setText(entry.title + " " + getString(R.string.event_time_format, dateFormat1.format(entry.endDate)));
            }
        } else {
            updateUnAvailability();
        }
    }

    boolean isReserveAvailable() {
        SharedPreferences prefs =
                PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
        return prefs.getBoolean("reserve_switch", true);
    }

    void updateUnAvailability() {
        SharedPreferences prefs =
                PreferenceManager.getDefaultSharedPreferences(MainActivity.this);

        String unavailableMessage = prefs.getString("room_info_message", getString(R.string.pref_description_reserve_message));
        mReserveButton.setVisibility(View.INVISIBLE);
        mHeaderView.setStatus(Status.NOT_AVAILABLE);
        statusTextView.setText(R.string.not_available_text);
        timeTextView.setText(unavailableMessage);
    }
    //checking network connection
    public boolean isConnected() {
        ConnectivityManager manager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo info = manager.getActiveNetworkInfo();
        if (info != null && info.isConnected()) {
            return true;
        } else {
            return false;

        }
    }

    @Override
    protected void onResume() {
        super.onResume();
//        Log.d("Main", "onResume");
        if (!isReserveAvailable()) {
            updateUnAvailability();
        } else {
            mReserveButton.setVisibility(View.VISIBLE);
            Entry entry = getCurrentEntry();
            update(entry);
        }

        mHeaderView.updateNetworkUnavailability(isConnected());
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == SCHEDULE) {
            if (resultCode == RESULT_OK) {
                long startDate = data.getLongExtra("startDate", new Date().getTime());
                Log.d(TAG, "onActivityResult: " + startDate);
                reserveNow(null, startDate);
            }
        }
    }


    public void reserveNow(View view, long startDate) {
        SharedPreferences prefs =
                PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
        boolean enabled = prefs.getBoolean("reserve_switch", true);
        if (enabled) {
            Intent intent = new Intent(this, ReserveActivity.class);
            intent.putExtra("startDate", startDate);
            startActivity(intent);
        } else {
            String roomInfoMessage = prefs.getString("room_info_message", "UNAVAILABLE");
            AlertDialog dialog = new AlertDialog.Builder(MainActivity.this)
                    .setTitle("Reserve")
                    .setMessage(roomInfoMessage)
                    .setCancelable(false)
                    .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    }).create();
            dialog.show();
        }
    }

    public void showSchedule(View view) {
        SharedPreferences prefs =
                PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
        boolean enabled = prefs.getBoolean("reserve_switch", true);
        if (enabled) {
            Intent intent = new Intent(this, BasicActivity.class);
            startActivityForResult(intent, SCHEDULE);
        } else {
            String roomInfoMessage = prefs.getString("room_info_message", "UNAVAILABLE");
            AlertDialog dialog = new AlertDialog.Builder(MainActivity.this)
                    .setTitle("Reserve")
                    .setMessage(roomInfoMessage)
                    .setCancelable(false)
                    .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    }).create();
            dialog.show();
        }
    }

    /**
     * AdAstra API caller
     * Asynchronous Task
     */
    public class AdAstraTask extends AsyncTask<String, Long, String> {
        @Override
        protected String doInBackground(String... params) {
            AdAstraApi api = null;
            String jsonString = "";
            try {
                SSLSocketFactory sslSocketFactory = AdAstraApi.trustIFNetServer(MainActivity.this);
                api = new AdAstraApi(sslSocketFactory);

//            System.out.println(api.login());
                jsonString = api.ApiResponder(api.login());
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return jsonString;
        }

        @Override
        protected void onPostExecute(String s) {
            try {
                JSONObject object = new JSONObject(s);
                Log.d("JSON", object.toString());
                Log.i("JSON", object.get("fields").toString());
                JSONArray data = object.getJSONArray("data");
                //
                Log.i("JSON", "No of records " + data.length());
                if (mEntries.size() > 0) {
                    mEntries.clear();
                }

                SharedPreferences prefs =
                        PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
                String roomName = prefs.getString("room_name", "");

                for (int i = 0; i < data.length(); i++) {
                    JSONArray item = data.getJSONArray(i);
                    String room = item.getString(item.length() - 3);
                    Log.i(TAG, "onPostExecute: " + room);
                    if (room.equals(roomName)) {
                        mEntries.add(new Entry(item));
                    }
                }

//                Sort the collection
                Collections.sort(mEntries, new Comparator<Entry>() {
                    public int compare(Entry m1, Entry m2) {
                        return m1.startDate.compareTo(m2.startDate);
                    }
                });

                Log.i(TAG, "onPostExecute: No of events in this room : " + mEntries.size());

                //undo if not worked
                if(mEntries.size()==0){
                    System.out.println("AVAILABLE ALL DAY");
                }

                Entry entry = getCurrentEntry();
                AppSettings.getInstance().setEntries(mEntries);


                //
                if (entry != null) {
                    Log.d("E", entry.toString());
                }
                update(entry);
            } catch (JSONException | ParseException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * @return Entry suitable to show on Main View
     */
    private Entry getCurrentEntry() {
        Log.i("MainActivity", Arrays.toString(mEntries.toArray()));
        Date now = new Date();

        int n = mEntries.size();
        if (n == 0) {
            return null;
        }
        int i = 0;
        Entry e = mEntries.get(0);

        if (now.before(e.startDate)) {
            return e;
        }

        while (i < n) {
            e = mEntries.get(i);
            if (now.after(e.startDate) && now.before(e.endDate)) {
                break;
            } else if (i < n - 1 && now.after(e.endDate) && now.before(mEntries.get(i + 1).startDate)) {
                e = mEntries.get(i + 1);
                break;
            }
            i++;
        }
        return e;
    }
}
