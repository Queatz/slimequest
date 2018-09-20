package com.slimequest.shared;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonSyntaxException;

import java.text.DateFormat;

public class Json {
    static public <T> T from(String json, Class<T> clazz) {
        try {
            return new GsonBuilder().setDateFormat(DateFormat.FULL, DateFormat.FULL).create().fromJson(json, clazz);
        } catch (JsonSyntaxException e) {
            e.printStackTrace();
            return null;
        }
    }

    static public <T> T from(JsonElement json, Class<T> clazz) {
        try {
            return new GsonBuilder().setDateFormat(DateFormat.FULL, DateFormat.FULL).create().fromJson(json, clazz);
        } catch (JsonSyntaxException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String to(Object object) {
        try {
            return new GsonBuilder().setDateFormat(DateFormat.FULL, DateFormat.FULL).create().toJson(object);
        } catch (JsonSyntaxException e) {
            e.printStackTrace();
            return null;
        }
    }
}
