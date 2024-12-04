package com.example.project1.data.pipeline.impl;

import com.example.project1.data.pipeline.Filter;
import com.example.project1.entity.ProfileEntity;
import com.example.project1.repository.ProfileRepository;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.List;

public class FilterCompanyData implements Filter<List<ProfileEntity>> {

    private final ProfileRepository profileRepository;

    public FilterCompanyData(ProfileRepository profileRepository) {
        this.profileRepository = profileRepository;
    }

    private static final String STOCK_MARKET_URL = "https://www.mse.mk/mk/stats/symbolhistory/kmb";

    @Override
    public List<ProfileEntity> execute(List<ProfileEntity> input) throws IOException {
        Document document = Jsoup.connect(STOCK_MARKET_URL).get();
        Element selectMenu = document.select("select#Code").first();

        if (selectMenu != null) {
            Elements options = selectMenu.select("option");
            for (Element option : options) {
                String code = option.attr("value");
                if (!code.isEmpty() && code.matches("^[a-zA-Z]+$")) {
                    if (profileRepository.findByCompanyCode(code).isEmpty()) {
                        profileRepository.save(new ProfileEntity(code));
                    }
                }
            }
        }

        return profileRepository.findAll();
    }
}