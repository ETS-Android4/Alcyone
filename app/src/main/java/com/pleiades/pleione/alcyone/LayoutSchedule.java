package com.pleiades.pleione.alcyone;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Iterator;

import static android.content.Context.MODE_PRIVATE;

public class LayoutSchedule extends Fragment {
    public static ArrayList<Schedule> scheduleList;
    private static ListAdapterSchedule adapter;

    @SuppressLint("StaticFieldLeak")
    private static ListView scheduleListView;

    @SuppressLint("StaticFieldLeak")
    private static Context context;

    @SuppressLint("StaticFieldLeak")
    private static View v;

    private static int editPos;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true);

        v = inflater.inflate(R.layout.activity_schedule, container, false);

        // context initialize
        context = getContext();

        // prefs control
        scheduleList = PrefsController.getScheduleListPrefs(context, "scheduleList");

        // checkbox initialize
        for (int i = 0; i < scheduleList.size(); i++) {
            scheduleList.get(i).checked = false;
        }

        sortSchedule();

        scheduleListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Schedule item = (Schedule) parent.getAdapter().getItem(position);

                Intent intent = new Intent(getActivity(), LayoutScheduleEdit.class);

                intent.putExtra("title", item.title);
                intent.putExtra("date", item.date);
                intent.putExtra("time", item.time);
                intent.putExtra("memo", item.memo);
                intent.putExtra("postMeridiem", item.postMeridiem);

                // remember position to edit
                editPos = position;

                startActivity(intent);
            }
        });

        return v;
    }

    // toolbar menu
    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.schedule_action_bar, menu);

        for (int i = 0; i < menu.size(); i++) {
            Drawable drawable = menu.getItem(i).getIcon();
            if (drawable != null) {
                drawable.mutate();
                drawable.setColorFilter(ContextCompat.getColor(context, R.color.colorAccent), PorterDuff.Mode.SRC_ATOP);
            }
        }
    }

    // handle button activities
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.addSchedule) {
            Intent intent = new Intent(getActivity(), LayoutScheduleAdd.class);
            startActivity(intent);
        } else if (id == R.id.deleteSchedule) {
            deleteDialog();
        } else if (id == R.id.completeSchedule) {
            completeDialog();
        }
        return super.onOptionsItemSelected(item);
    }

    public static void saveSchedule(String title, String date, String time, String memo, boolean postMeridiem) {
        Schedule newSchedule = new Schedule();

        newSchedule.title = title;
        newSchedule.date = date;
        newSchedule.time = time;
        newSchedule.memo = memo;
        newSchedule.postMeridiem = postMeridiem;

        scheduleList.add(newSchedule);

        sortSchedule();

        scheduleListView.setAdapter(adapter);

        // prefs control
        PrefsController.setScheduleListPrefs(context, "scheduleList", scheduleList);

        releaseScheduleNotifications();
        setScheduleNotifications();
    }

    public static void editSchedule(String title, String date, String time, String memo, boolean postMeridiem) {
        scheduleList.get(editPos).title = title;
        scheduleList.get(editPos).date = date;
        scheduleList.get(editPos).time = time;
        scheduleList.get(editPos).memo = memo;
        scheduleList.get(editPos).postMeridiem = postMeridiem;

        sortSchedule();

        // prefs control
        PrefsController.setScheduleListPrefs(context, "scheduleList", scheduleList);

        releaseScheduleNotifications();
        setScheduleNotifications();
    }

    // delete dialog
    public void deleteDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.AppTheme_AlertDialogOverlay);

        // cannot cancel
        builder.setCancelable(false);

        // builder.setTitle
        builder.setTitle("선택한 일정들을 삭제하시겠어요?");
        builder.setPositiveButton("삭제",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        releaseScheduleNotifications();

                        Iterator<Schedule> iterator = scheduleList.iterator();
                        while (iterator.hasNext()) {
                            Schedule item = iterator.next();

                            if (item.checked)
                                iterator.remove();
                        }

                        sortSchedule();

                        // prefs control
                        PrefsController.setScheduleListPrefs(context, "scheduleList", scheduleList);

                        setScheduleNotifications();
                    }
                });
        builder.setNegativeButton("취소",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });

        builder.show();
    }

    public static void deleteCompletedSchedules() {
        Iterator<Schedule> iterator = scheduleList.iterator();
        while (iterator.hasNext()) {
            Schedule item = iterator.next();

            if (item.completed)
                iterator.remove();
        }
        // prefs control
        PrefsController.setScheduleListPrefs(MainActivity.applicationContext, "scheduleList", scheduleList);
    }

    // complete dialog
    public void completeDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.AppTheme_AlertDialogOverlay);

        // cannot cancel
        builder.setCancelable(false);

        // builder.setTitle
        builder.setTitle("선택한 일정들을 완료하시겠어요?");
        builder.setPositiveButton("완료",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        releaseScheduleNotifications();

                        for (int i = 0; i < scheduleList.size(); i++) {
                            if (scheduleList.get(i).checked) {
                                scheduleList.get(i).completed = true;
                                scheduleList.get(i).checked = false;

                                SharedPreferences prefs = context.getSharedPreferences("prefs", MODE_PRIVATE);
                                SharedPreferences.Editor editor = prefs.edit();
                                LayoutAlcyone.scheduleCompletedNumberToday++;
                                editor.putInt("scheduleCompletedNumberToday", LayoutAlcyone.scheduleCompletedNumberToday);
                                editor.apply();
                            }
                        }

                        sortSchedule();

                        // prefs control
                        PrefsController.setScheduleListPrefs(context, "scheduleList", scheduleList);

                        setScheduleNotifications();
                    }
                });
        builder.setNegativeButton("취소",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });

        builder.show();
    }

    public static void sortSchedule() {
        Collections.sort(scheduleList);

        adapter = new ListAdapterSchedule(scheduleList);
        scheduleListView = v.findViewById(R.id.scheduleList);

        scheduleListView.setAdapter(adapter);
    }

    // add all notification reservation
    public static void setScheduleNotifications() {
        SharedPreferences prefs = MainActivity.applicationContext.getSharedPreferences("prefs", MODE_PRIVATE);

        if (prefs.getBoolean("receiveNotification", false)) {
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

                    if (isEnteredTimeIsLater(year, month, day, hour, minute, yearNow, monthNow, dayNow, hourNow, minuteNow) == 1) {
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
                        PrefsController.setNotificationRequestListPrefs(MainActivity.applicationContext, "notificationRequestList", notificationRequestList);

                        NotificationAlarm.setAlarm(MainActivity.applicationContext, cal, i); // request code is position
                    }

                }
            }
        }
    }

    public static int isEnteredTimeIsLater(int year, int month, int day, int hour, int minute, int yearNow, int monthNow, int dayNow, int hourNow, int minuteNow) {
        int result = Integer.compare(year, yearNow);

        if (result == 0)
            result = Integer.compare(month, monthNow);

        if (result == 0)
            result = Integer.compare(day, dayNow);

        if (result == 0)
            result = Integer.compare(hour, hourNow);

        if (result == 0)
            result = Integer.compare(minute, minuteNow);

        return result;
    }

    // remove all notification reservation
    public static void releaseScheduleNotifications() {
        SharedPreferences prefs = MainActivity.applicationContext.getSharedPreferences("prefs", MODE_PRIVATE);
        if (prefs.getBoolean("receiveNotification", false)) {
            for (int i = 0; i < scheduleList.size(); i++) {
                if (!scheduleList.get(i).completed)
                    NotificationAlarm.releaseAlarm(MainActivity.applicationContext, i);
            }
        }
    }

}
