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

public class NewsFetcher {

    private static final String NEWS = "https://newsapi.org/v2/everything?q=covid&apiKey=840e23f8dd984fb98ad10b01f67146e7";

    private ExecutorService executorService = Executors.newSingleThreadExecutor();


    public NewsFetcher() {
    }

    public Future<List<Article>> fetchNews() {
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
