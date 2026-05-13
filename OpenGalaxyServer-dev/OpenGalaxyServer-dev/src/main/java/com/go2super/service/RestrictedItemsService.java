package com.go2super.service;

import com.go2super.resources.ResourceManager;
import com.go2super.resources.data.PropData;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@Service
@Getter
public class RestrictedItemsService {

    private static RestrictedItemsService instance;

    private final Set<Integer> restrictedIds = new HashSet<>();

    @Value("${game.restricted.items:classpath:data/restricted_items.json}")
    private String restrictedItemsPath;

    @Value("${game.restricted.items.external:#{null}}")
    private String externalItemsPath;

    private final ResourceLoader resourceLoader;

    public RestrictedItemsService(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    @PostConstruct
    public void init() {
        instance = this;
        loadRestrictedItems();
    }

    public static RestrictedItemsService getInstance() {
        return instance;
    }

    private void loadRestrictedItems() {
        // Load from classpath first
        loadFromClasspath();

        // Check for external file override (writable at runtime)
        if (externalItemsPath != null && !externalItemsPath.isEmpty()) {
            loadFromExternal();
        }
    }

    private void loadFromClasspath() {
        try {
            Resource resource = resourceLoader.getResource(restrictedItemsPath);
            if (!resource.exists()) {
                return;
            }
            try (InputStream is = resource.getInputStream()) {
                String json = new String(is.readAllBytes(), StandardCharsets.UTF_8);
                parseJson(json);
            }
        } catch (Exception e) {
            // If no restricted_items.json exists, silently skip
        }
    }

    private void loadFromExternal() {
        try {
            Path path = Paths.get(externalItemsPath);
            if (Files.exists(path)) {
                String json = new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
                parseJson(json);
            }
        } catch (Exception e) {
        }
    }

    private void parseJson(String json) {
        int restrictedStart = json.indexOf("\"restricted\"");
        if (restrictedStart == -1) return;

        int arrayStart = json.indexOf("[", restrictedStart);
        int arrayEnd = json.lastIndexOf("]");
        if (arrayStart == -1 || arrayEnd == -1) return;

        String arrayContent = json.substring(arrayStart + 1, arrayEnd);
        String[] parts = arrayContent.split(",");
        for (String part : parts) {
            String trimmed = part.trim();
            if (trimmed.matches("\\d+")) {
                restrictedIds.add(Integer.parseInt(trimmed));
            }
        }
    }

    public boolean isRestricted(int propId) {
        return restrictedIds.contains(propId);
    }

    public boolean addRestrictedId(int propId) {
        if (restrictedIds.contains(propId)) {
            return false;
        }
        restrictedIds.add(propId);
        persistToFile();
        return true;
    }

    public boolean removeRestrictedId(int propId) {
        if (!restrictedIds.contains(propId)) {
            return false;
        }
        restrictedIds.remove(propId);
        persistToFile();
        return true;
    }

    public Set<Integer> getRestrictedIds() {
        return Collections.unmodifiableSet(restrictedIds);
    }

    private void persistToFile() {
        // pretty sure this shit dont work
        if (externalItemsPath != null && !externalItemsPath.isEmpty()) {
            writeToExternal();
        }
    }

    private void writeToExternal() {
        try {
            Path path = Paths.get(externalItemsPath);
            Files.createDirectories(path.getParent());

            StringBuilder json = new StringBuilder();
            json.append("{\n  \"restricted\": [\n    ");
            List<Integer> sorted = new ArrayList<>(restrictedIds);
            Collections.sort(sorted);
            for (int i = 0; i < sorted.size(); i++) {
                json.append(sorted.get(i));
                if (i < sorted.size() - 1) {
                    json.append(", ");
                }
                if ((i + 1) % 9 == 0 && i < sorted.size() - 1) {
                    json.append("\n    ");
                }
            }
            json.append("\n  ]\n}");

            Files.writeString(path, json.toString(), StandardCharsets.UTF_8);
        } catch (Exception e) {e
        }
    }

    public String getPrettyName(int propId) {
        try {
            PropData prop = ResourceManager.getProps().getData(propId);
            if (prop != null) {
                String name = prop.getName();
                if (name != null) {
                    return name.replace("prop:", "");
                }
            }
        } catch (Exception e) {
        }
        return "Unknown";
    }
}