package com.example.findyourselfinthephoto;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.util.ArrayList;
import java.util.Objects;

public class MyJson {

    private static ArrayList<String> subResult;
    private static String[] keys;

    public static ArrayList<String> getUrlArray(String json_string, String storageName) throws ParseException {

        if(Objects.equals(storageName, "YandexDisk")){
            subResult = new ArrayList<String>();
            keys = new String[]{"_embedded", "items", "preview"};
            return getYandexUrlArray(json_string, keys);
        }

        return null;
    }

    private static ArrayList<String> getYandexUrlArray(String json_string, String[] keys) throws ParseException {

        Object obj = new JSONParser().parse(json_string);
        JSONObject jo = (JSONObject) obj;

        jo = (JSONObject) jo.get(keys[0]);

        JSONArray tempArr = (JSONArray) jo.get(keys[1]);

        for (Object o : tempArr) {
            JSONObject test = (JSONObject) o;
            subResult.add(test.get(keys[2]).toString().replace("size=S", "size=2048x2048"));
        }

        return subResult;
    }
}