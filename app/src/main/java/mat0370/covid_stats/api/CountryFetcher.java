package mat0370.covid_stats.api;

import com.google.common.collect.Lists;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import io.reactivex.rxjava3.core.Observable;
import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class CountryFetcher {

    private static final String API_STATS_ENDPOINT = "https://covid-193.p.rapidapi.com/statistics";
    private static final String API_STATS_COUNTRY_ENDPOINT = "https://covid-193.p.rapidapi.com/statistics?country=";
    private static final String API_HOST = "covid-193.p.rapidapi.com";
    private static final String API_KEY = "195a93e6eemsha12f3dd4a3db7fdp1411d9jsn324ff2434bd0";
    private static final String API_HISTORY = "https://covid-193.p.rapidapi.com/history?country=";

    private ExecutorService executorService = Executors.newSingleThreadExecutor();

    public Future<List<Country>> fetchCountries() {
        return executorService.submit(() -> {
            final OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url(API_STATS_ENDPOINT)
                    .addHeader("x-rapidapi-host", API_HOST)
                    .addHeader("x-rapidapi-key", API_KEY)
                    .build();
            return parseData(client, request);
        });
    }

    public Future<List<Country>> fetchCountry(final String countryToFetch) {
        return executorService.submit(() -> {
            final OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url(API_STATS_COUNTRY_ENDPOINT.concat(countryToFetch))
                    .addHeader("x-rapidapi-host", API_HOST)
                    .addHeader("x-rapidapi-key", API_KEY)
                    .build();

            return parseData(client, request);
        });
    }

    public Future<List<Country>> fetchCountryHistory(final String countryToFetch) {
        return executorService.submit(() -> {
            final OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url(API_HISTORY.concat(countryToFetch))
                    .addHeader("x-rapidapi-host", API_HOST)
                    .addHeader("x-rapidapi-key", API_KEY)
                    .build();

            return Lists.newArrayList(Observable.fromIterable(parseData(client, request)).distinct(Country::getDay).blockingIterable());
        });
    }

    private List<Country> parseData(final OkHttpClient client, final Request request) {
        final List<Country> data = new ArrayList<>();
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

            Headers responseHeaders = response.headers();
            for (int i = 0; i < responseHeaders.size(); i++) {
                System.out.println(responseHeaders.name(i) + ": " + responseHeaders.value(i));
            }

            String string = response.body().string();
            JSONObject jsonObject = new JSONObject(string);

            JSONArray jsonArray = jsonObject.getJSONArray("response");

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject country = jsonArray.getJSONObject(i);
                String name = country.getString("country");
                String day = country.getString("day");

                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);

                Date date;
                try {
                    date = formatter.parse(day);
                } catch (ParseException e) {
                    date = new Date();
                }

                JSONObject cases = country.getJSONObject("cases");

                String newCases;
                try {
                    newCases = cases.getString("new");
                    if (newCases.equals("null")) {
                        newCases = "0";
                    }
                } catch (Exception e) {
                    newCases = "0";
                }
                int totalCases;
                try {
                    totalCases = cases.getInt("total");
                } catch (Exception e) {
                    totalCases = 0;
                }
                int active;
                try {
                    active = cases.getInt("active");
                } catch (Exception e) {
                    active = 0;
                }
                int recovered;
                try {
                    recovered = cases.getInt("recovered");
                } catch (Exception e) {
                    recovered = 0;
                }
                int critical;
                try {
                    critical = cases.getInt("critical");
                } catch (Exception e) {
                    critical = 0;
                }

                JSONObject deaths = country.getJSONObject("deaths");
                String newDeaths;
                try {
                    newDeaths = deaths.getString("new");
                    if (newDeaths.equals("null")) {
                        newDeaths = "0";
                    }
                } catch (Exception e) {
                    newDeaths = "0";
                }
                int totalDeaths;
                try {
                    totalDeaths = deaths.getInt("total");
                } catch (Exception e) {
                    totalDeaths = 0;
                }
                JSONObject tests = country.getJSONObject("tests");
                int totalTests;
                try {
                    totalTests = tests.getInt("total");
                } catch (Exception e) {
                    totalTests = 0;
                }
                data.add(new Country(name, Integer.parseInt(newCases), totalCases, Integer.parseInt(newDeaths), totalDeaths, totalTests, active, recovered, critical, date));

            }

        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }

        return data.stream().sorted(Comparator.comparing(Country::getName)).collect(Collectors.toList());
    }

}
