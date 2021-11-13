package mat0370.covid_stats;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.google.common.collect.Lists;

import java.util.List;
import java.util.stream.Collectors;

import mat0370.covid_stats.api.Country;
import mat0370.covid_stats.db.DBHelper;

public class CountiresRecyclerViewAdapter extends RecyclerView.Adapter<CountiresRecyclerViewAdapter.ViewHolder> {

    private Context mContext;
    private List<Country> countries;
    private List<Country> countiresFull;


    public CountiresRecyclerViewAdapter(final Context mContext, final List<Country> countries) {
        this.mContext = mContext;
        this.countries = countries;
        countiresFull = Lists.newArrayList(countries);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull final ViewGroup parent, final int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_listitem, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        holder.countryName.setText(countries.get(position).getName());
        holder.totalCases.setText(String.valueOf(countries.get(position).getTotalCases()));
        holder.recovered.setText(String.valueOf(countries.get(position).getRecovered()));
        holder.deaths.setText(String.valueOf(countries.get(position).getTotalDeaths()));

        holder.constraintLayout.setOnClickListener(v -> {
            DBHelper dbHelper = new DBHelper(mContext);
            dbHelper.upsertCountry(countries.get(position).getName());
            Intent mainActivity = new Intent(mContext, MainActivity.class);
            mainActivity.putExtra("country", countries.get(position).getName());
            mContext.startActivity(mainActivity);
        });
    }

    private Filter countryFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(final CharSequence constraint) {
            List<Country> filteredList = Lists.newArrayList();

            if(constraint == null || constraint.length() ==0){
                filteredList.addAll(countiresFull);
            } else {
                String filterPattern = constraint.toString().toLowerCase().trim();
                List<Country> filtered = countiresFull.stream().filter(country -> country.getName().toLowerCase().trim().contains(filterPattern)).collect(Collectors.toList());
                filteredList.addAll(filtered);
            }
            FilterResults results = new FilterResults();
            results.values = filteredList;
            return results;
        }

        @Override
        protected void publishResults(final CharSequence constraint, final FilterResults results) {
            countries.clear();
            countries.addAll((List<Country>) results.values);
            notifyDataSetChanged();
        }
    };

    public Context getmContext() {
        return mContext;
    }

    public List<Country> getCountries() {
        return countries;
    }

    public List<Country> getCountiresFull() {
        return countiresFull;
    }

    public Filter getCountryFilter() {
        return countryFilter;
    }

    @Override
    public int getItemCount() {
        return countries.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        TextView countryName;
        TextView totalCases;
        TextView recovered;
        TextView deaths;
        ConstraintLayout constraintLayout;

        public ViewHolder(@NonNull final View itemView) {
            super(itemView);
            countryName = itemView.findViewById(R.id.list_country_name);
            totalCases = itemView.findViewById(R.id.list_total_cases);
            recovered = itemView.findViewById(R.id.list_recoverd);
            deaths = itemView.findViewById(R.id.list_deaths);
            constraintLayout = itemView.findViewById(R.id.list_parent);
        }
    }
}
