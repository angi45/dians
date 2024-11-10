package com.example.project1.data.pipeline.impl;

import com.example.project1.data.pipeline.Filter;
import com.example.project1.entity.Issuer;
import com.example.project1.repository.IssuerRepository;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.List;

public class FilterOne implements Filter<List<Issuer>> {

    private final IssuerRepository issuerRepository;

    public FilterOne(IssuerRepository issuerRepository) {
        this.issuerRepository = issuerRepository;
    }

    private static final String STOCK_MARKET_URL = "https://www.mse.mk/mk/stats/symbolhistory/kmb";

    @Override
    public List<Issuer> execute(List<Issuer> input) throws IOException {
        Document document = Jsoup.connect(STOCK_MARKET_URL).get();
        Element selectMenu = document.select("select#Code").first();

        if (selectMenu != null) {
            Elements options = selectMenu.select("option");
            for (Element option : options) {
                String code = option.attr("value");
                if (!code.isEmpty() && code.matches("^[a-zA-Z]+$")) {
                    if (issuerRepository.findByCompanyCode(code).isEmpty()) {
                        issuerRepository.save(new Issuer(code));
                    }
                }
            }
        }
        return issuerRepository.findAll();
    }
}
