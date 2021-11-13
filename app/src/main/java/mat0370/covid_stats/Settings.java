package mat0370.covid_stats;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.util.Preconditions;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.SearchView;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

import mat0370.covid_stats.api.Country;
import mat0370.covid_stats.api.DataFetcher;

public class Settings extends AppCompatActivity {

    private List<Country> countries;

    @SuppressLint("RestrictedApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        try {
            Objects.requireNonNull(this.getSupportActionBar()).hide();
        } catch (Exception e) {
            e.printStackTrace();
        }
        DataFetcher dataFetcher = new DataFetcher();
        try {
            countries = dataFetcher.fetchData().get();
        } catch (ExecutionException | InterruptedException e) {
            countries = null;
        }
        Preconditions.checkNotNull(countries);

        initRecyclerView();
    }

    private void initRecyclerView() {
        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        CountiresRecyclerViewAdapter countiresRecyclerViewAdapter = new CountiresRecyclerViewAdapter(this, countries);
        recyclerView.setAdapter(countiresRecyclerViewAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        SearchView searchView = findViewById(R.id.search);
        searchView.setImeOptions(EditorInfo.IME_ACTION_DONE);


        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(final String query) {
               return false;
            }

            @Override
            public boolean onQueryTextChange(final String newText) {
                countiresRecyclerViewAdapter.getCountryFilter().filter(newText);
                return false;
            }
        });
    }

    public void onBackBtnClicked(View view) {
        Intent mainActivity = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(mainActivity);
        finish();
    }
}