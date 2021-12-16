package mat0370.covid_stats.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Pair;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import mat0370.covid_stats.api.Country;

public class DBHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "COVIDSTATS.db";
    public static final String COUNTRY_NAME = "country_name";
    public static final String COUNTRY_TABLE_NAME = "country";
    public static final String COUNTRY_DATA_TABLE_NAME = "country_data";

    private static final String COUNTRY_DATA_QUERY = "" +
            "SELECT * FROM "+COUNTRY_DATA_TABLE_NAME +
            "WHERE country_name= ?"+
            "";

    private static final String COUNTRY_SELECTED_DATA_QUERY = "" +
            "SELECT * FROM "+COUNTRY_DATA_TABLE_NAME +
            "WHERE country_name = ? AND " +
            "country_name = (SELECT country_name FROM "+COUNTRY_TABLE_NAME+" WHERE country_name = ?)"+
            "";

    private static final int DATABASE_VERSION = 1;

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(final SQLiteDatabase db) {
        db.execSQL("CREATE TABLE "+COUNTRY_TABLE_NAME+" (country_name TEXT PRIMARY KEY, is_selected INTEGER)");
        db.execSQL("CREATE TABLE " + COUNTRY_DATA_TABLE_NAME + " "+
                "(" +
                "country_name TEXT, " +
                "cases_new INTEGER, " +
                "total_cases INTEGER, " +
                "deaths_new INTEGER, " +
                "total_deaths INTEGER, " +
                "tests INTEGER, " +
                "active INTEGER, " +
                "recovered INTEGER, " +
                "critical INTEGER, " +
                "day TEXT," +
                "PRIMARY KEY(country_name, day)," +
                "FOREIGN KEY(country_name) REFERENCES "+COUNTRY_TABLE_NAME+"(country_name)" +
                ")"
        );


    }

    public boolean saveCountry(Country country, boolean isSelected) {
        SQLiteDatabase db = this.getWritableDatabase();
        Pair<ContentValues, ContentValues> values = mapDataToColumns(country, isSelected);
        ContentValues countryValues = values.first;
        ContentValues countryDataValues = values.second;

        long result = db.replace(COUNTRY_TABLE_NAME, null, countryValues);
        if(result == -1){
            return false;
        }

        long dataResult = db.replace(COUNTRY_DATA_TABLE_NAME, null, countryDataValues)
        if(dataResult == -1){
            return false;
        }
        return true;
    }

    public Country findCountry(String countryName, boolean isSelected) {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor dbCursor;

        if (isSelected) {
            dbCursor = db.rawQuery(COUNTRY_SELECTED_DATA_QUERY, new String[]{countryName, countryName});
        } else {
            dbCursor = db.rawQuery(COUNTRY_DATA_QUERY, new String[]{countryName});
        }
    }

    public List<Country> findCountryHistory(String countryName){

    }

    private Country mapColumnsToData(){}

    private Pair<ContentValues, ContentValues> mapDataToColumns(Country country, boolean isSelected) {
        ContentValues countryValues = new ContentValues();
        countryValues.put("country_name", country.getName());
        countryValues.put("is_selected", isSelected);

        ContentValues countryDataValues = new ContentValues();
        countryDataValues.put("country_name", country.getName());
        countryDataValues.put("cases_new", country.getCasesNew());
        countryDataValues.put("total_cases", country.getTotalCases());
        countryDataValues.put("deaths_new", country.getDeathsNew());
        countryDataValues.put("active", country.getActive());
        countryDataValues.put("recovered", country.getRecovered());

        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
        countryDataValues.put("day", formatter.format(country.getDay()));

        return new Pair(countryValues, countryDataValues);
    }


    @Override
    public void onUpgrade(final SQLiteDatabase db, final int oldVersion, final int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS country");
        db.execSQL("DROP TABLE IF EXISTS country_data");
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
