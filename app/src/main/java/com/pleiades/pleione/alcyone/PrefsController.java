package com.pleiades.pleione.alcyone;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import static android.content.Context.MODE_PRIVATE;

public class PrefsController {

    // notification request
    public static void setNotificationRequestListPrefs(Context context, String key, ArrayList<NotificationRequest> value) {
        SharedPreferences prefs = context.getSharedPreferences("prefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        Gson gson = new Gson();
        String json = gson.toJson(value);

        editor.putString(key, json);
        editor.apply();
    }

    public static ArrayList<NotificationRequest> getNotificationRequestListPrefs(Context context, String key) {
        SharedPreferences prefs = context.getSharedPreferences("prefs", MODE_PRIVATE);

        Gson gson = new Gson();
        String json = prefs.getString(key, "");
        Type type = new TypeToken<ArrayList<NotificationRequest>>() {
        }.getType();

        return gson.fromJson(json, type);
    }

    // schedule
    public static void setScheduleListPrefs(Context context, String key, ArrayList<Schedule> value) {
        SharedPreferences prefs = context.getSharedPreferences("prefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        Gson gson = new Gson();
        String json = gson.toJson(value);

        editor.putString(key, json);
        editor.apply();
    }

    public static ArrayList<Schedule> getScheduleListPrefs(Context context, String key) {
        SharedPreferences prefs = context.getSharedPreferences("prefs", MODE_PRIVATE);

        Gson gson = new Gson();
        String json = prefs.getString(key, "");
        Type type = new TypeToken<ArrayList<Schedule>>() {
        }.getType();

        return gson.fromJson(json, type);
    }

    // story
    public static void setStoryListPrefs(Context context, String key, ArrayList<Story> value) {
        SharedPreferences prefs = context.getSharedPreferences("prefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        Gson gson = new Gson();
        String json = gson.toJson(value);

        editor.putString(key, json);
        editor.apply();
    }

    public static ArrayList<Story> getStoryListPrefs(Context context, String key) {
        SharedPreferences prefs = context.getSharedPreferences("prefs", MODE_PRIVATE);

        Gson gson = new Gson();
        String json = prefs.getString(key, "");
        Type type = new TypeToken<ArrayList<Story>>() {
        }.getType();

        return gson.fromJson(json, type);
    }

    // costume
    public static void setCostumeListPrefs(Context context, String key, ArrayList<Costume> value) {
        SharedPreferences prefs = context.getSharedPreferences("prefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        Gson gson = new Gson();
        String json = gson.toJson(value);

        editor.putString(key, json);
        editor.apply();
    }

    public static ArrayList<Costume> getCostumeListPrefs(Context context, String key) {
        SharedPreferences prefs = context.getSharedPreferences("prefs", MODE_PRIVATE);

        Gson gson = new Gson();
        String json = prefs.getString(key, "");
        Type type = new TypeToken<ArrayList<Costume>>() {
        }.getType();

        return gson.fromJson(json, type);
    }

    // last open date
    public static void setLastOpenDatePrefs(Context context, String key) {
        SharedPreferences prefs = context.getSharedPreferences("prefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH) + 1;
        int date = cal.get(Calendar.DATE);

        editor.putString(key, String.format(Locale.KOREAN, "%04d-%02d-%02d", year, month, date)); // lastOpenDate
        editor.apply();
    }

    // last open date
    public static void setLastOpenDateYesterdayPrefs(Context context, String key) {
        SharedPreferences prefs = context.getSharedPreferences("prefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -1);

        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH) + 1;
        int date = cal.get(Calendar.DATE);

        editor.putString(key, String.format(Locale.KOREAN, "%04d-%02d-%02d", year, month, date)); // lastOpenDate
        editor.apply();
    }

    public static void setDeveloperMode(Context context){
        int mainStoryProgress = 0;
        int storyProgress = 0;
        int costumeProgress = 1; // default costume

        // storyList initialize
        LayoutStory.storyList = new ArrayList<>();

        // story 0      : tutorial
        // story 1~30   : main story
        // story 31~35  : costume story
        for (int i = 0; i <= 30; i++) { // 31 main now
            Story item = new Story();
            item.unlocked = true;
            item.storyName = String.format(Locale.KOREAN, "main%02d", i);
            item.priority = 0;
            LayoutStory.storyList.add(item);

            mainStoryProgress++;
        }

        for (int i = 0; i < 2; i++) { // 2 special now
            Story item = new Story();
            item.unlocked = true;
            item.storyName = String.format(Locale.KOREAN, "costumeSpecial%02d", i);
            item.priority = 1;
            LayoutStory.storyList.add(item);
        }

        for (int i = 0; i < 3; i++) { // 3 pay now
            Story item = new Story();
            item.unlocked = true;
            item.storyName = String.format(Locale.KOREAN, "costumePay%02d", i);
            item.priority = 10;
            LayoutStory.storyList.add(item);
        }

        // story progress improve
        for (int i = 0; i < LayoutStory.storyList.size(); i++) {
            if (LayoutStory.storyList.get(i).unlocked)
                storyProgress++;
        }

        // set story list
        PrefsController.setStoryListPrefs(context, "storyList", LayoutStory.storyList);

        // costumeList initialize
        LayoutCostume.costumeList = new ArrayList<>();
        Costume item;

        item = new Costume();
        item.costumeName = "default";
        item.unlocked = true;
        item.priority = 0;
        LayoutCostume.costumeList.add(item);

        item = new Costume();
        item.costumeName = "school";
        item.unlocked = true;
        item.priority = 0;
        LayoutCostume.costumeList.add(item);

        item = new Costume();
        item.costumeName = "business";
        item.unlocked = true;
        item.priority = 0;
        LayoutCostume.costumeList.add(item);

        item = new Costume();
        item.costumeName = "training";
        item.unlocked = true;
        item.priority = 0;
        LayoutCostume.costumeList.add(item);

        item = new Costume();
        item.costumeName = "outside";
        item.unlocked = true;
        item.priority = 0;
        LayoutCostume.costumeList.add(item);

        item = new Costume();
        item.costumeName = "cheer";
        item.unlocked = true;
        item.priority = 0;
        LayoutCostume.costumeList.add(item);

        item = new Costume();
        item.costumeName = "wedding";
        item.unlocked = true;
        item.priority = 0;
        LayoutCostume.costumeList.add(item);

        item = new Costume();
        item.costumeName = "nurse";
        item.unlocked = true;
        item.priority = 1;
        LayoutCostume.costumeList.add(item);

        item = new Costume();
        item.costumeName = "pajamas";
        item.unlocked = true;
        item.priority = 1;
        LayoutCostume.costumeList.add(item);

        item = new Costume();
        item.costumeName = "bunny";
        item.unlocked = true;
        item.priority = 10;
        LayoutCostume.costumeList.add(item);

        item = new Costume();
        item.costumeName = "bloomer";
        item.unlocked = true;
        item.priority = 10;
        LayoutCostume.costumeList.add(item);

        item = new Costume();
        item.costumeName = "succubus";
        item.unlocked = true;
        item.priority = 10;
        LayoutCostume.costumeList.add(item);

        for (int i = 1; i < LayoutCostume.costumeList.size(); i++) { // i=1~ (except default costume)
            if (LayoutCostume.costumeList.get(i).unlocked)
                costumeProgress++;
        }

        PrefsController.setCostumeListPrefs(context, "costumeList", LayoutCostume.costumeList);

        MainActivity.storyProgress = storyProgress;
        MainActivity.costumeProgress = costumeProgress;

        MainActivity.setStoryProgressBar();
        MainActivity.setCostumeProgressBar();
    }
}