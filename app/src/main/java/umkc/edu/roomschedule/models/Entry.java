package umkc.edu.roomschedule.models;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by pradyumnad on 2/7/16.
 */
public class Entry {

    static String TAG = Entry.TAG;

    public String title;
    String link;
    String content;
    String location;
    public Date startDate;
    public Date endDate;
    String cost;
    String contact;
    String roomNumber;
    String description;

    /**
     *
     * fields: "ActivityName,ParentActivityName,Description,StartDate,EndDate,StartMinute,EndMinute,ActivityTypeCode,CampusName,BuildingCode,RoomNumber,RoomName",
     * @param object
     */
    public Entry(JSONArray object) throws JSONException, ParseException {
        Log.i(TAG, object.toString());
        this.title = object.getString(0);
        this.description = object.getString(2);
        SimpleDateFormat parserSDF = new SimpleDateFormat("yyyy-MM-dd", Locale.US);

        String startDate = object.getString(3).split("T")[0];
        this.startDate = parserSDF.parse(startDate);
        String endDate = object.getString(4).split("T")[0];
        this.endDate = parserSDF.parse(endDate);
        int startMinutes = object.getInt(5);
        int endMinutes = object.getInt(6);

        this.roomNumber = object.getString(11);

        Calendar cal = Calendar.getInstance();
        cal.setTime(this.startDate);
        cal.add(Calendar.MINUTE, startMinutes);
        this.startDate = cal.getTime();

        cal.setTime(this.endDate);
        cal.add(Calendar.MINUTE, endMinutes);
        this.endDate = cal.getTime();

//        Log.d("SD", this.startDate.toString());
//        Log.d("ED", this.endDate.toString());
    }

    @Override
    public String toString() {
        return this.title + "\n" + this.startDate + " to " + this.endDate + "\n" + this.link +"\n"+ this.roomNumber;
    }
}
