package com.go2super.resources.serialization;

import com.go2super.resources.json.BuildingsJson;
import com.google.gson.*;

import java.lang.reflect.Type;

public class BuildingsJsonDeserializer implements JsonDeserializer<BuildingsJson> {

    @Override
    public BuildingsJson deserialize(JsonElement jsonElement, Type clazzType, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {

        JsonObject result = jsonElement.getAsJsonObject();
        return new Gson().fromJson(result, BuildingsJson.class);

    }

}
