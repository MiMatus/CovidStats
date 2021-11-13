package mat0370.covid_stats.api;

public class Article {
    private final String title;
    private final String url;

    public Article(final String title, final String url) {
        this.title = title;
        this.url = url;
    }

    public String getTitle() {
        return title;
    }

    public String getUrl() {
        return url;
    }
}
