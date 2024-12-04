package com.example.project1.data.pipeline.impl;

import com.example.project1.data.TransformData;
import com.example.project1.data.pipeline.Filter;
import com.example.project1.entity.ProfileEntity;
import com.example.project1.entity.TradeHistoryEntity;
import com.example.project1.repository.ProfileRepository;
import com.example.project1.repository.TradeHistoryRepository;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class FilterCompanyDetails implements Filter<List<ProfileEntity>> {

    private final ProfileRepository profileRepository;
    private final TradeHistoryRepository tradeHistoryRepository;

    private static final String HISTORICAL_DATA_URL = "https://www.mse.mk/mk/stats/symbolhistory/";

    public FilterCompanyDetails(ProfileRepository profileRepository, TradeHistoryRepository tradeHistoryRepository) {
        this.profileRepository = profileRepository;
        this.tradeHistoryRepository = tradeHistoryRepository;
    }

    public List<ProfileEntity> execute(List<ProfileEntity> input) throws IOException {
        List<ProfileEntity> companies = new ArrayList<>();

        for (ProfileEntity company : input) {
            if (company.getLastUpdated() == null) {
                for (int i = 1; i <= 10; i++) {
                    int temp = i - 1;
                    LocalDate fromDate = LocalDate.now().minusYears(i);
                    LocalDate toDate = LocalDate.now().minusYears(temp);
                    addHistoricalData(company, fromDate, toDate);
                }
            } else {
                companies.add(company);
            }
        }

        return companies;
    }

    private void addHistoricalData(ProfileEntity company, LocalDate fromDate, LocalDate toDate) throws IOException {
        Connection.Response response = Jsoup.connect(HISTORICAL_DATA_URL + company.getCompanyCode())
                .data("FromDate", fromDate.toString())
                .data("ToDate", toDate.toString())
                .method(Connection.Method.POST)
                .execute();

        Document document = response.parse();

        Element table = document.select("table#resultsTable").first();

        if (table != null) {
            Elements rows = table.select("tbody tr");

            for (Element row : rows) {
                Elements columns = row.select("td");

                if (columns.size() > 0) {
                    LocalDate date = TransformData.parseDate(columns.get(0).text(), "d.M.yyyy");

                    if (tradeHistoryRepository.findByDateAndCompany(date, company).isEmpty()) {


                        NumberFormat format = NumberFormat.getInstance(Locale.GERMANY);

                        Double lastTransactionPrice = TransformData.parseDouble(columns.get(1).text(), format);
                        Double maxPrice = TransformData.parseDouble(columns.get(2).text(), format);
                        Double minPrice = TransformData.parseDouble(columns.get(3).text(), format);
                        Double averagePrice = TransformData.parseDouble(columns.get(4).text(), format);
                        Double percentageChange = TransformData.parseDouble(columns.get(5).text(), format);
                        Integer quantity = TransformData.parseInteger(columns.get(6).text(), format);
                        Integer turnoverBest = TransformData.parseInteger(columns.get(7).text(), format);
                        Integer totalTurnover = TransformData.parseInteger(columns.get(8).text(), format);

                        if (maxPrice != null) {

                            if (company.getLastUpdated() == null || company.getLastUpdated().isBefore(date)) {
                                company.setLastUpdated(date);
                            }

                            TradeHistoryEntity tradeHistoryEntity = new TradeHistoryEntity(
                                    date, lastTransactionPrice, maxPrice, minPrice, averagePrice, percentageChange,
                                    quantity, turnoverBest, totalTurnover);
                            tradeHistoryEntity.setCompany(company);
                            tradeHistoryRepository.save(tradeHistoryEntity);
                            company.getHistoricalData().add(tradeHistoryEntity);
                        }
                    }
                }
            }
        }

        profileRepository.save(company);
    }

}
