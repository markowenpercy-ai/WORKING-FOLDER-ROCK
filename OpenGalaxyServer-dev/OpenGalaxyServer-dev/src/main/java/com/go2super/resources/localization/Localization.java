package com.go2super.resources.localization;

import com.go2super.resources.json.LocalizationJson;
import com.google.gson.Gson;
import lombok.SneakyThrows;
import org.springframework.core.io.ClassPathResource;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;

public enum Localization {

    EN_US("en-us");

    private final LocalizationJson json;

    Localization(String fileName) {

        json = getJson(fileName, LocalizationJson.class);
    }

    public String get(String key) {

        return json.get(key);
    }

    public String getLocale() {

        return json.getLocale();
    }

    public LocalizationJson getJson() {

        return json;
    }

    @SneakyThrows
    private <T> T getJson(String jsonFile, Class<T> clazz) {

        return new Gson().fromJson(getReader(jsonFile), clazz);
    }

    @SneakyThrows
    private Reader getReader(String file) {

        return new InputStreamReader(getInputStream(file), StandardCharsets.UTF_8);
    }

    @SneakyThrows
    private InputStream getInputStream(String file) {

        return new ClassPathResource("data/lang/" + file + ".json").getInputStream();
    }

}
