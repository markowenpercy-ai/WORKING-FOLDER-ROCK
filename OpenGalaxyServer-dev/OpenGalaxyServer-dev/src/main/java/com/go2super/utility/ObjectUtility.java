package com.go2super.utility;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.lang.reflect.Type;

public class ObjectUtility {

    public static <T> T copyObject(Object object) {

        Gson gson = new Gson();
        JsonObject jsonObject = gson.toJsonTree(object).getAsJsonObject();
        return gson.fromJson(jsonObject, (Type) object.getClass());
    }

}
