package com.go2super.resources.serialization;

import com.go2super.resources.json.LevelsJson;
import com.google.gson.*;

import java.lang.reflect.Type;

public class LevelsJsonDeserializer implements JsonDeserializer<LevelsJson> {

    @Override
    public LevelsJson deserialize(JsonElement jsonElement, Type clazzType, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {

        LevelsJson levelsJson = new LevelsJson();
        JsonObject result = jsonElement.getAsJsonObject();
        levelsJson = new Gson().fromJson(result, LevelsJson.class);
        return levelsJson;

    }

}
