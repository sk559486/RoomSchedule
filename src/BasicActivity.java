package umkc.edu.roomschedule;

import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.alamkanak.weekview.WeekViewEvent;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import umkc.edu.roomschedule.models.Entry;

/**
 * A basic example of how to use week view library.
 * Created by Raquib-ul-Alam Kanak on 1/3/2014.
 * Website: http://alamkanak.github.io
 */
public class BasicActivity extends BaseActivity {

    static String TAG = "BasicActivity";

    @Override
    public List<? extends WeekViewEvent> onMonthChange(int newYear, int newMonth) {
        List<Entry> entryList = AppSettings.getInstance().getEntries();

        List<WeekViewEvent> events = new ArrayList<WeekViewEvent>();
        for (int i = 0; i < entryList.size(); i++) {
            Entry e = entryList.get(i);
            Calendar startTime = new GregorianCalendar();
            startTime.setTime(e.startDate);

            Calendar endTime = new GregorianCalendar();
            endTime.setTime(e.endDate);

            WeekViewEvent event = new WeekViewEvent(i, e.title, startTime, endTime);
            event.setColor(getResources().getColor(R.color.event_color_02));
            events.add(event);
        }

        // Return only the events that matches newYear and newMonth.
        List<WeekViewEvent> matchedEvents = new ArrayList<WeekViewEvent>();
        for (WeekViewEvent event : events) {
            if (eventMatches(event, newYear, newMonth)) {
                matchedEvents.add(event);
            }
        }
        return matchedEvents;
    }

    /**
     * Checks if an event falls into a specific year and month.
     *
     * @param event The event to check for.
     * @param year  The year.
     * @param month The month.
     * @return True if the event matches the year and month.
     */
    private boolean eventMatches(WeekViewEvent event, int year, int month) {
        return (event.getStartTime().get(Calendar.YEAR) == year && event.getStartTime().get(Calendar.MONTH) == month - 1) || (event.getEndTime().get(Calendar.YEAR) == year && event.getEndTime().get(Calendar.MONTH) == month - 1);
    }

    @Override
    public void onEmptyViewClicked(Calendar time) {
        Log.d("Parent", getIntent().toString());
        Intent myIntent = getIntent();
        Toast.makeText(this, "Empty view pressed: " + getEventTitle(time), Toast.LENGTH_SHORT).show();
        Intent intent = new Intent();
        intent.putExtra("startDate", time.getTime().getTime());
        //
        intent.putExtra("endDate",time.getTime().getTime());
        setResult(RESULT_OK, intent);
        finish();
    }
}
