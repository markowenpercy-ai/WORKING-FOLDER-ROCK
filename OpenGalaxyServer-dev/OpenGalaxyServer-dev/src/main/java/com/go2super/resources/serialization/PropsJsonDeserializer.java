package com.go2super.resources.serialization;

import com.go2super.resources.data.PropData;
import com.go2super.resources.data.props.*;
import com.go2super.resources.json.PropsJson;
import com.google.gson.*;

import java.lang.reflect.Type;
import java.util.*;

public class PropsJsonDeserializer implements JsonDeserializer<PropsJson> {

    @Override
    public PropsJson deserialize(JsonElement jsonElement, Type clazzType, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {

        PropsJson result = new PropsJson();
        JsonArray props = jsonElement.getAsJsonObject().get("props").getAsJsonArray();

        result.setProps(new ArrayList<>());

        for (JsonElement element : props) {

            PropData obj = new PropData();
            JsonObject prop = element.getAsJsonObject();

            int id = prop.get("id").getAsInt();
            String name = prop.get("name").getAsString();
            String type = prop.get("type").getAsString();
            int salvage = prop.get("salvage").getAsInt();

            PropMetaData data = null;
            PropMallData[] mall = new PropMallData[0];

            switch (type) {

                case "basic":
                    break;

                case "commander":
                    data = new Gson().fromJson(prop.get("data"), PropCommanderData.class);
                    break;

                case "scroll":
                    data = new Gson().fromJson(prop.get("data"), PropScrollData.class);
                    break;

                case "buff":
                    data = new Gson().fromJson(prop.get("data"), PropBuffData.class);
                    break;

                case "gem":
                    data = new Gson().fromJson(prop.get("data"), PropGemData.class);
                    break;

                case "chip":
                    data = new Gson().fromJson(prop.get("data"), PropChipData.class);
                    break;

                case "pack":
                    data = new Gson().fromJson(prop.get("data"), PropContainerData.class);
                    break;

                case "chest":
                    data = new Gson().fromJson(prop.get("data"), PropChestData.class);
                    break;

                case "blueprintBody":
                    data = new Gson().fromJson(prop.get("data"), PropBodyData.class);
                    break;

                case "blueprintPart":
                    data = new Gson().fromJson(prop.get("data"), PropPartData.class);
                    break;

                case "Instance Drop":
                    data = new Gson().fromJson(prop.get("data"), DropData.class);
                    break;

            }

            if (prop.has("mall")) {
                mall = new Gson().fromJson(prop.get("mall"), PropMallData[].class);
            }

            obj.setId(id);
            obj.setName(name);
            obj.setType(type);
            obj.setData(data);
            obj.setMall(mall);
            obj.setSalvage(salvage);

            result.getProps().add(obj);

        }

        return result;

    }

}
