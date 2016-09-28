package umkc.edu.roomschedule;

import java.util.ArrayList;
import java.util.List;

import umkc.edu.roomschedule.models.Entry;

public class AppSettings {
    public static final String[] tabs = {"Event Info", "Reserve"};
    private static AppSettings instance = null;

    private List<Entry> mEntries;

    protected AppSettings() {
        mEntries = new ArrayList<>();
    }

    public static AppSettings getInstance() {
        if (instance == null) {
            instance = new AppSettings();
        }

        return instance;
    }

    public void setEntries(List<Entry> entries) {
        this.mEntries = entries;
    }

    public List<Entry> getEntries() {
        return this.mEntries;
    }
}
