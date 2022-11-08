package com.example.findyourselfinthephoto;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.util.ArrayList;
import java.util.Objects;

public class MyJson {

    private static ArrayList<ArrayList<String>> subResult;
    private static String[] keys;

    public static ArrayList<ArrayList<String>> getUrlArray(String json_string, String storageName) throws ParseException {

        if(Objects.equals(storageName, "YandexDisk")){
            subResult = new ArrayList<>();
            subResult.add(new ArrayList<>());
            subResult.add(new ArrayList<>());

            keys = new String[]{"_embedded", "items", "preview", "file"};
            return getYandexUrlArray(json_string, keys);
        }

        return null;
    }

    public static int getTotal(String json_string, String storageName) throws ParseException{
        if(Objects.equals(storageName, "YandexDisk")){
            keys = new String[]{"_embedded", "total"};
            return getYandexTotalSize(json_string);
        }
        return -1;
    }

    private static int getYandexTotalSize(String json_string) throws ParseException{
        Object obj = new JSONParser().parse(json_string);
        JSONObject jo = (JSONObject) obj;

        jo = (JSONObject) jo.get(keys[0]);
        int size = ((Long) jo.get(keys[1])).intValue();
        return size;
    }

    private static ArrayList<ArrayList<String>> getYandexUrlArray(String json_string, String[] keys) throws ParseException {

        Object obj = new JSONParser().parse(json_string);
        JSONObject jo = (JSONObject) obj;

        jo = (JSONObject) jo.get(keys[0]);

        JSONArray tempArr = (JSONArray) jo.get(keys[1]);

        for (Object o : tempArr) {
            JSONObject test = (JSONObject) o;
            subResult.get(0).add(test.get(keys[2]).toString().replace("size=S", "size=2048x2048"));
            subResult.get(1).add(test.get(keys[3]).toString());
        }

        return subResult;
    }
}