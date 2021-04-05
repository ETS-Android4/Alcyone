package com.pleiades.pleione.alcyone;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.Calendar;

import static android.content.Context.MODE_PRIVATE;

public class NotificationReceiverBoot extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
            SharedPreferences prefs = context.getSharedPreferences("prefs", MODE_PRIVATE);

            // auto release

            // set schedule notifications
            if (prefs.getBoolean("receiveNotification", false)) {
                ArrayList<Schedule> scheduleList = PrefsController.getScheduleListPrefs(context, "scheduleList");

                for (int i = 0; i < scheduleList.size(); i++) {
                    if (!scheduleList.get(i).completed && !scheduleList.get(i).time.equals("시간 설정 안 함")) {
                        String date = scheduleList.get(i).date;
                        String time = scheduleList.get(i).time;

                        int year = Integer.parseInt(date.substring(0, 4));
                        int month = Integer.parseInt(date.substring(6, 8));
                        int day = Integer.parseInt(date.substring(10, 12));
                        int hour = Integer.parseInt(time.substring(0, 2));
                        int minute = Integer.parseInt(time.substring(4, 6));

                        if (hour == 12)
                            hour = 0;
                        if (scheduleList.get(i).postMeridiem)
                            hour = hour + 12;
                        month--;

                        Calendar cal = Calendar.getInstance();

                        // get time now
                        int yearNow = cal.get(Calendar.YEAR);
                        int monthNow = cal.get(Calendar.MONTH);
                        int dayNow = cal.get(Calendar.DATE);
                        int hourNow = cal.get(Calendar.HOUR_OF_DAY);
                        int minuteNow = cal.get(Calendar.MINUTE);

                        if(LayoutSchedule.isEnteredTimeIsLater(year, month, day, hour, minute, yearNow, monthNow, dayNow, hourNow, minuteNow) == 1){
                            cal.set(Calendar.YEAR, year);
                            cal.set(Calendar.MONTH, month);
                            cal.set(Calendar.DATE, day);
                            cal.set(Calendar.HOUR_OF_DAY, hour);
                            cal.set(Calendar.MINUTE, minute);

                            ArrayList<NotificationRequest> notificationRequestList = new ArrayList<>();
                            NotificationRequest notificationRequest = new NotificationRequest();
                            notificationRequest.position = i;
                            notificationRequest.completed = false;
                            notificationRequestList.add(notificationRequest);
                            PrefsController.setNotificationRequestListPrefs(context, "notificationRequestList", notificationRequestList);

                            NotificationAlarm.setAlarm(context, cal, i); // request code is position
                        }

                    }
                }
            }
        }
    }
}