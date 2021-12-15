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

public class DataFetcher {
    private static final String STATISTICS = "https://covid-193.p.rapidapi.com/statistics";
    private static final String STATISTICS_COUNTRY = "https://covid-193.p.rapidapi.com/statistics?country=";
    private static final String HISTORY = "https://covid-193.p.rapidapi.com/history?country=";
    private static final String NEWS = "https://newsapi.org/v2/everything?q=covid&apiKey=840e23f8dd984fb98ad10b01f67146e7";
    private static final String HOST = "covid-193.p.rapidapi.com";
    private static final String KEY = "195a93e6eemsha12f3dd4a3db7fdp1411d9jsn324ff2434bd0";

    private ExecutorService executorService = Executors.newSingleThreadExecutor();


    public DataFetcher() {
    }


    public Future<List<Article>> getNews() {
        return executorService.submit(() -> {
            final OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url(NEWS)
                    .build();

            return parseNews(client, request);
        });
    }

    private List<Article> parseNews(final OkHttpClient client, final Request request) {
        final List<Article> articles = Lists.newArrayList();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

            Headers responseHeaders = response.headers();
            for (int i = 0; i < responseHeaders.size(); i++) {
                System.out.println(responseHeaders.name(i) + ": " + responseHeaders.value(i));
            }

            String string = response.body().string();
            JSONObject jsonObject = new JSONObject(string);

            JSONArray jsonArray = jsonObject.getJSONArray("articles");

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject article = jsonArray.getJSONObject(i);
                final String title = article.getString("title");
                final String url = article.getString("url");

                articles.add(new Article(title, url));
            }


        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }

        return articles;
    }

}
