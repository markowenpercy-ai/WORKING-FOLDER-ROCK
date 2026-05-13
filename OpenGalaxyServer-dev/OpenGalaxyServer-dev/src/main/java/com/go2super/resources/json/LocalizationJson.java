package com.go2super.resources.json;

import com.go2super.logger.BotLogger;
import lombok.Data;
import lombok.ToString;

import java.util.*;

@Data
@ToString
public class LocalizationJson {

    private String locale;
    private Map<String, Map<String, String>> strings;

    public String getLocale() {

        return locale;
    }

    public boolean has(String group, String key) {

        return strings.containsKey(group) && strings.get(group).containsKey(key);
    }

    public String get(String group, String key) {

        try {
            return strings.get(group).get(key);
        } catch (Exception e) {
            return "Undefined: " + key;
        }
    }

    public String get(String fullKey) {

        String[] composition = fullKey.split(":");
        if (composition.length == 1) {
            BotLogger.error("Localization Failed for: " + fullKey);
            return fullKey;
        }
        return get(composition[0], composition[1]);
    }

}
