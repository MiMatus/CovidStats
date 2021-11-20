package mat0370.covid_stats;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.util.Preconditions;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

import mat0370.covid_stats.api.Country;
import mat0370.covid_stats.api.DataFetcher;
import mat0370.covid_stats.api.Article;
import mat0370.covid_stats.db.DBHelper;

public class MainActivity extends AppCompatActivity implements ActivityCompat.OnRequestPermissionsResultCallback {

    List<Country> history = new ArrayList<>();
    Country country;
    String globalName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        try {
            Objects.requireNonNull(this.getSupportActionBar()).hide();
        } catch (Exception e) {
            e.printStackTrace();
        }

        DBHelper dbHelper = new DBHelper(this);
        Cursor data = dbHelper.getData();
        data.moveToFirst();
        try {
            String coutnry = data.getString(data.getColumnIndex(DBHelper.COUNTRY_NAME));
            loadData(coutnry);
        } catch (Exception e) {
            Intent mainActivity = getIntent();
            String country = mainActivity.getStringExtra("country");
            if (country != null) {
                System.out.println(country);
                try {
                    loadData(country);
                } catch (ExecutionException | InterruptedException ef) {
                    ef.printStackTrace();
                }
            } else {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 44);
                } else {
                    locationRequest();
                }            
            }
        }
    }

    @SuppressLint("RestrictedApi")
    private void loadData(final String countryName) throws ExecutionException, InterruptedException {
        DataFetcher dataFetcher = new DataFetcher();
        try {
            country = dataFetcher.fetchCountry(countryName).get().get(0);
        } catch (ExecutionException | InterruptedException e) {
            country = null;
        }

        history = dataFetcher.fetchCountryHistory(countryName).get();
        globalName = countryName;
        render();
    }

    @SuppressLint("RestrictedApi")
    private void render() {
        Preconditions.checkNotNull(country);

        TextView newCases = findViewById(R.id.new_cases);
        TextView totalCases = findViewById(R.id.total_cases);
        TextView newDeaths = findViewById(R.id.new_deaths);
        TextView totalDeaths = findViewById(R.id.deaths);
        TextView tests = findViewById(R.id.tests);
        TextView active = findViewById(R.id.active);
        TextView recovered = findViewById(R.id.recovered);
        TextView critical = findViewById(R.id.critical);
        TextView countryName = findViewById(R.id.country_name);

        newCases.setText(String.valueOf(country.getCasesNew()));
        totalCases.setText(String.valueOf(country.getTotalCases()));
        newDeaths.setText(String.valueOf(country.getDeathsNew()));
        totalDeaths.setText(String.valueOf(country.getTotalDeaths()));
        tests.setText(String.valueOf(country.getTests()));
        active.setText(String.valueOf(country.getActive()));
        recovered.setText(String.valueOf(country.getRecovered()));
        critical.setText(String.valueOf(country.getCritical()));
        countryName.setText(globalName);

        recoveredDeathsData(country.getRecovered(), country.getTotalDeaths());
        activeCriticalData(country.getActive(), country.getCritical());
        generateGraph(30);
        generateGraph2(30);
    }

    private void activeCriticalData(final int active, final int critical) {
        PieChart pieChart = findViewById(R.id.piechart_2);

        List<PieEntry> entries = new ArrayList<>();
        entries.add(new PieEntry(active, "Active"));
        entries.add(new PieEntry(critical, "Critical"));

        PieDataSet pieDataSet = new PieDataSet(entries, "sup");
        pieDataSet.setValueTextSize(2f);
        pieDataSet.setValueTextColor(Color.WHITE);
        pieDataSet.setColors(new int[]{R.color.active, R.color.critical}, this);

        PieData pieData = new PieData(pieDataSet);
        pieChart.setData(pieData);
        pieData.setValueTextSize(18f);
        pieChart.animate();
        pieChart.getLegend().setEnabled(false);
        Description description = new Description();
        description.setText("");
        pieChart.setDescription(description);
        pieChart.setEntryLabelTextSize(12f);
        pieChart.setDrawHoleEnabled(true);
        pieChart.setHoleColor(getResources().getColor(R.color.bg));
        pieChart.setNoDataText("Generating graph...");
        pieChart.invalidate();
    }

    private void recoveredDeathsData(final int recovered, final int deaths) {
        PieChart pieChart = findViewById(R.id.piechart);

        List<PieEntry> entries = new ArrayList<>();
        entries.add(new PieEntry(recovered, "Recovered"));
        entries.add(new PieEntry(deaths, "Deaths"));

        PieDataSet pieDataSet = new PieDataSet(entries, "sup");
        pieDataSet.setValueTextSize(2f);
        pieDataSet.setValueTextColor(Color.WHITE);
        pieDataSet.setColors(new int[]{R.color.recovered, R.color.deaths}, this);

        PieData pieData = new PieData(pieDataSet);
        pieData.setValueTextSize(18f);
        pieChart.setData(pieData);
        pieChart.animate();
        pieChart.getLegend().setEnabled(false);
        Description description = new Description();
        description.setText("");
        pieChart.setDescription(description);
        pieChart.setEntryLabelTextSize(12f);
        pieChart.setDrawHoleEnabled(true);
        pieChart.setHoleColor(getResources().getColor(R.color.bg));
        pieChart.setNoDataText("Generating graph...");
        pieChart.invalidate();
    }

    private void generateGraph(final int days) {
        LineChart lineChart = findViewById(R.id.linechart_1);
        List<Entry> cases = new ArrayList<>();
        List<Entry> deaths = new ArrayList<>();
        List<Entry> recovered = new ArrayList<>();
        List<String> labels = new ArrayList<>();

        for (int i = 0; i < days; i++) {
            cases.add(new Entry(i, history.get(days - i).getTotalCases()));
            recovered.add(new Entry(i, history.get(days - i).getRecovered()));
            deaths.add(new Entry(i, history.get(days - i).getTotalDeaths()));

            LocalDateTime localDateTime = LocalDateTime.ofInstant(history.get(days - i).getDay().toInstant(), ZoneOffset.UTC);
            labels.add(localDateTime.getDayOfMonth() + "." + localDateTime.getMonth().getValue());
        }
        LineDataSet lineDataSet = new LineDataSet(cases, "Cases");
        lineDataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        lineDataSet.setDrawFilled(true);
        lineDataSet.setFillColor(ContextCompat.getColor(this, R.color.totalCases));
        lineDataSet.setColors(new int[]{R.color.totalCases}, this);
        lineDataSet.setCircleColors(new int[]{R.color.totalCases}, this);
        lineDataSet.setCircleHoleColor(R.color.totalCases);
        lineDataSet.setValueTextSize(0);

        LineDataSet lineDataSet2 = new LineDataSet(recovered, "Recovered");
        lineDataSet2.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        lineDataSet2.setDrawFilled(true);
        lineDataSet2.setFillColor(ContextCompat.getColor(this, R.color.recovered));
        lineDataSet2.setColors(new int[]{R.color.recovered}, this);
        lineDataSet2.setCircleColors(new int[]{R.color.recovered}, this);
        lineDataSet2.setCircleHoleColor(R.color.recovered);
        lineDataSet2.setValueTextSize(0);


        LineDataSet lineDataSet3 = new LineDataSet(deaths, "Deaths");
        lineDataSet3.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        lineDataSet3.setDrawFilled(true);
        lineDataSet3.setFillColor(ContextCompat.getColor(this, R.color.deaths));
        lineDataSet3.setColors(new int[]{R.color.deaths}, this);
        lineDataSet3.setCircleColors(new int[]{R.color.deaths}, this);
        lineDataSet3.setCircleHoleColor(R.color.deaths);
        lineDataSet3.setValueTextSize(0);


        LineData lineData = new LineData();
        lineData.addDataSet(lineDataSet);
        lineData.addDataSet(lineDataSet2);
        lineData.addDataSet(lineDataSet3);
        Description description = new Description();
        description.setText("");
        lineChart.setDescription(description);
        lineChart.getAxisLeft().setTextColor(Color.WHITE);
        lineChart.getAxisRight().setTextColor(Color.WHITE);
        lineChart.getXAxis().setTextColor(Color.WHITE);
        lineChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));
        lineChart.getLegend().setEnabled(true);
        lineChart.getLegend().setTextColor(Color.WHITE);
        lineChart.getLegend().setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        lineChart.setData(lineData);
        lineChart.setNoDataText("Generating graph...")
        lineChart.invalidate();
    }

    private void generateGraph2(final int days) {
        LineChart lineChart = findViewById(R.id.linechart_2);
        List<Entry> cases = new ArrayList<>();
        List<Entry> deaths = new ArrayList<>();
        List<String> labels = new ArrayList<>();

        for (int i = 0; i < days; i++) {
            cases.add(new Entry(i, history.get(days - i).getCasesNew()));
            deaths.add(new Entry(i, history.get(days - i).getDeathsNew()));

            LocalDateTime localDateTime = LocalDateTime.ofInstant(history.get(days - i).getDay().toInstant(), ZoneOffset.UTC);
            labels.add(localDateTime.getDayOfMonth() + "." + localDateTime.getMonth().getValue());
        }
        LineDataSet lineDataSet = new LineDataSet(cases, "New Cases");
        lineDataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        lineDataSet.setDrawFilled(true);
        lineDataSet.setFillColor(ContextCompat.getColor(this, R.color.totalCases));
        lineDataSet.setColors(new int[]{R.color.totalCases}, this);
        lineDataSet.setCircleColors(new int[]{R.color.totalCases}, this);
        lineDataSet.setCircleHoleColor(R.color.totalCases);
        lineDataSet.setValueTextSize(0);

        LineDataSet lineDataSet3 = new LineDataSet(deaths, "New Deaths");
        lineDataSet3.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        lineDataSet3.setDrawFilled(true);
        lineDataSet3.setFillColor(ContextCompat.getColor(this, R.color.deaths));
        lineDataSet3.setColors(new int[]{R.color.deaths}, this);
        lineDataSet3.setCircleColors(new int[]{R.color.deaths}, this);
        lineDataSet3.setCircleHoleColor(R.color.deaths);
        lineDataSet3.setValueTextSize(0);

        LineData lineData = new LineData();
        lineData.addDataSet(lineDataSet);
        lineData.addDataSet(lineDataSet3);
        Description description = new Description();
        description.setText("");
        lineChart.setDescription(description);
        lineChart.getAxisLeft().setTextColor(Color.WHITE);
        lineChart.getAxisRight().setTextColor(Color.WHITE);
        lineChart.getXAxis().setTextColor(Color.WHITE);
        lineChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));
        lineChart.getLegend().setEnabled(true);
        lineChart.getLegend().setTextColor(Color.WHITE);
        lineChart.getLegend().setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        lineChart.setData(lineData);
        lineChart.setNoDataText("Generating graph...");
        lineChart.invalidate();
    }

    public void weekBtnOnClick(View view) {
        generateGraph(7);
        generateGraph2(7);
    }

    public void monthBtnOnClick(View view) {
        generateGraph(30);
        generateGraph2(30);
    }

    public void threeMonthsBtnOnClick(View view) {
        generateGraph(60);
        generateGraph2(60);
    }

    public void settingsBtnOnClick(View view) {
        Intent settings = new Intent(getApplicationContext(), Settings.class);
        startActivity(settings);
        finish();
    }

    public void newsBtnOnClick(View view) {
        Intent news = new Intent(getApplicationContext(), News.class);
        startActivity(news);
        finish();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults){
        locationRequest();
    }

    public void locationRequest() {
        LocationRequest mLocationRequest = LocationRequest.create();
        mLocationRequest.setInterval(999999999);
        mLocationRequest.setFastestInterval(999999999);        
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        LocationCallback mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    Geocoder geocoder = new Geocoder(MainActivity.this, Locale.getDefault());
                    try {
                        List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                        loadData(addresses.get(0).getCountryName());
                    } catch (IOException | InterruptedException | ExecutionException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        LocationServices.getFusedLocationProviderClient(this).requestLocationUpdates(mLocationRequest, mLocationCallback, null);
    }

}