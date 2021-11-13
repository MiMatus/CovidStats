package mat0370.covid_stats;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import mat0370.covid_stats.api.Article;

public class NewsRecyclerViewAdapter extends RecyclerView.Adapter<NewsRecyclerViewAdapter.ViewHolder> {
    private Context context;
    private List<Article> articleList;

    public NewsRecyclerViewAdapter(final Context context, final List<Article> articleList) {
        this.context = context;
        this.articleList = articleList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull final ViewGroup parent, final int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.news_listitem, parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        holder.title.setText(articleList.get(position).getTitle());

        holder.constraintLayout.setOnClickListener(v -> {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(articleList.get(position).getUrl()));
            context.startActivity(browserIntent);
        });
    }

    @Override
    public int getItemCount() {
        return articleList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        TextView title;
        ConstraintLayout constraintLayout;

        public ViewHolder(@NonNull final View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.article_text);
            constraintLayout = itemView.findViewById(R.id.news_parent);
        }
    }
}
