package mat0370.covid_stats.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "COVIDSTATS.db";
    public static final String COUNTRY_NAME = "country_name";

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, 1);
    }

    @Override
    public void onCreate(final SQLiteDatabase db) {
        db.execSQL("CREATE TABLE country" + "(id INTEGER PRIMARY KEY, country_name TEXT)");
    }

    @Override
    public void onUpgrade(final SQLiteDatabase db, final int oldVersion, final int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS country");
        onCreate(db);
    }

    public boolean upsertCountry(final String countryName) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("id", 1);
        contentValues.put("country_name", countryName);

        Cursor data = getData();
        data.moveToFirst();

        try {
            String string = data.getString(data.getColumnIndex(DBHelper.COUNTRY_NAME));
            updateItem(countryName);
        } catch (Exception e) {
            db.insert("country", null, contentValues);
        }

        return true;
    }

    public Cursor getData() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("select * from country where id=" + 1 + "", null);
    }

    public boolean updateItem(final String countryName) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("country_name", countryName);

        db.update("country", contentValues, "id=" + 1, null);

        return true;
    }
}
