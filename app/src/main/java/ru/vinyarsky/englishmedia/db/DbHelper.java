package ru.vinyarsky.englishmedia.db;

import android.content.ContentResolver;
import android.content.Context;
import android.content.res.XmlResourceParser;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.util.Log;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import ru.vinyarsky.englishmedia.R;

public class DbHelper extends SQLiteOpenHelper {

    private static int DB_VERSION = 1;

    private Context appContext;
    private ExecutorService executor;
    private SQLiteDatabase database;

    public DbHelper(Context appContext) {
        super(appContext, "data", null, DB_VERSION);
        this.appContext = appContext;
    }

    @Override
    public synchronized void close() {
        if (this.executor != null) {
            this.executor.shutdown();
            this.executor = null;
        }
        if (this.database != null) {
            this.database.close();
            this.database = null;
        }
        super.close();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        try {
            // Table creation
            Podcast.onCreate(db);

            // Populating database from xml/podcasts
            Podcast podcast = null;
            String lastTag = "";
            try (XmlResourceParser parser = appContext.getResources().getXml(R.xml.podcasts)) {
                int eventType = parser.getEventType();
                while (eventType != XmlPullParser.END_DOCUMENT) {
                    switch (eventType) {
                        case XmlPullParser.START_TAG:
                            lastTag = parser.getName();
                            if (lastTag.equals("podcast")) {
                                if (podcast != null)
                                    podcast.write(db);
                                podcast = new Podcast();
                                podcast.setSubscribed(false);
                            }
                            break;
                        case XmlPullParser.TEXT:
                            String value = parser.getText();
                            switch (lastTag) {
                                case "code":
                                    podcast.setCode(UUID.fromString(value));
                                    break;
                                case "level":
                                    podcast.setLevel(Podcast.PodcastLevel.valueOf(value));
                                    break;
                                case "title":
                                    podcast.setTitle(value);
                                    break;
                                case "description":
                                    podcast.setDescription(value);
                                    break;
                                case "rss_url":
                                    podcast.setRssUrl(value);
                                    break;
                                case "image_src":
                                    podcast.setImagePath(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + appContext.getPackageName() + "/raw/" + value);
                                    break;
                            }
                            break;
                    }
                    parser.next();
                    eventType = parser.getEventType();
                }
            }
            if (podcast != null)
                podcast.write(db);
        } catch(XmlPullParserException | IOException e) {
            Log.e("DbHelper", "onCreate", e);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // do nothing
    }

    ExecutorService getExecutor() {
        if (this.executor == null)
            this.executor = Executors.newCachedThreadPool();
        return executor;
    }

    SQLiteDatabase getDatabase() {
        if (this.database == null)
            this.database = getWritableDatabase();
        return this.database;
    }
}