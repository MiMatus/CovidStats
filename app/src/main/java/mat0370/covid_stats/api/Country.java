package mat0370.covid_stats.api;

import java.util.Date;

public class Country {
    private final String name;
    private final int casesNew;
    private final int totalCases;
    private final int deathsNew;
    private final int totalDeaths;
    private final int tests;
    private final int active;
    private final int recovered;
    private final int critical;
    private Date  day;


    public Country(final String name, final int casesNew, final int totalCases, final int deathsNew, final int totalDeaths, final int tests, final int active, final int recovered, final int critical, final Date day) {
        this.name = name;
        this.casesNew = casesNew;
        this.totalCases = totalCases;
        this.deathsNew = deathsNew;
        this.totalDeaths = totalDeaths;
        this.tests = tests;
        this.active = active;
        this.recovered = recovered;
        this.critical = critical;
        this.day = day;
    }

    public String getName() {
        return name;
    }

    public int getCasesNew() {
        return casesNew;
    }

    public int getTotalCases() {
        return totalCases;
    }

    public int getDeathsNew() {
        return deathsNew;
    }

    public int getTotalDeaths() {
        return totalDeaths;
    }

    public int getTests() {
        return tests;
    }

    public int getActive() {
        return active;
    }

    public int getRecovered() {
        return recovered;
    }

    public int getCritical() {
        return critical;
    }

    public Date getDay() {
        return day;
    }
}
