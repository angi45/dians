package com.example.project1.data.pipeline.impl;

import com.example.project1.data.Parser;
import com.example.project1.data.pipeline.Filter;
import com.example.project1.entity.Issuer;
import com.example.project1.entity.IssuerData;
import com.example.project1.repository.IssuerRepository;
import com.example.project1.repository.IssuerDataRepository;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.text.NumberFormat;
import java.text.ParseException;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

public class FilterThree implements Filter<List<Issuer>> {

    private final IssuerRepository issuerRepository;
    private final IssuerDataRepository issuerDataRepository;

    private static final String HISTORICAL_DATA_URL = "https://www.mse.mk/mk/stats/symbolhistory/";

    public FilterThree(IssuerRepository issuerRepository, IssuerDataRepository issuerDataRepository) {
        this.issuerRepository = issuerRepository;
        this.issuerDataRepository = issuerDataRepository;
    }

    public List<Issuer> execute(List<Issuer> input) throws IOException, ParseException {

        for (Issuer company : input) {
            LocalDate fromDate = LocalDate.now();
            LocalDate toDate = LocalDate.now().plusYears(1);
            addData(company, fromDate, toDate);
        }

        return null;
    }

    private void addData(Issuer company, LocalDate fromDate, LocalDate toDate) throws IOException, ParseException {
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
                    LocalDate date = Parser.parseDate(columns.get(0).text(), "d.M.yyyy");

                    if (date != null && issuerDataRepository.findByDateAndCompany(date, company).isEmpty()) {
                        if (company.getLastUpdated() == null || company.getLastUpdated().isBefore(date)) {
                            company.setLastUpdated(date);
                            issuerRepository.save(company);
                        }

                        NumberFormat format = NumberFormat.getInstance(Locale.GERMANY);

                        Double lastTransactionPrice = Parser.parseDouble(columns.get(1).text(), format);
                        Double maxPrice = Parser.parseDouble(columns.get(2).text(), format);
                        Double minPrice = Parser.parseDouble(columns.get(3).text(), format);
                        Double averagePrice = Parser.parseDouble(columns.get(4).text(), format);
                        Double percentageChange = Parser.parseDouble(columns.get(5).text(), format);
                        Integer quantity = Parser.parseInteger(columns.get(6).text(), format);
                        Integer turnoverBest = Parser.parseInteger(columns.get(7).text(), format);
                        Integer totalTurnover = Parser.parseInteger(columns.get(8).text(), format);

                        IssuerData issuerData = new IssuerData(
                                date, lastTransactionPrice, maxPrice, minPrice, averagePrice, percentageChange,
                                quantity, turnoverBest, totalTurnover);
                        issuerData.setCompany(company);

                        issuerDataRepository.save(issuerData);
                    }
                }
            }
        }
    }


}
