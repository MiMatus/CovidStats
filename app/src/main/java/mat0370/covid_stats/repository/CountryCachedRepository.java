package mat0370.covid_stats.repository;

import java.util.List;

import mat0370.covid_stats.api.Country;
import mat0370.covid_stats.api.CountryFetcher;
import mat0370.covid_stats.db.DBHelper;

public class CountryCachedRepository {

    private DBHelper dbHelper;

    private CountryFetcher countryFetcher;

    public CountryCachedRepository(DBHelper dbHelper, CountryFetcher countryFetcher){
        this.dbHelper = dbHelper;
        this.countryFetcher = countryFetcher;
    }

    public Country findCountry(String countryName) {
        //TODO
    }

    public List<Country> findCountryHistory(String countryName){
        //TODO
    }

    public boolean saveCountry()
    {
        //TODO
    }

}
