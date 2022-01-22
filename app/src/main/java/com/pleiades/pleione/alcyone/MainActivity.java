package com.pleiades.pleione.alcyone;

import static android.view.View.INVISIBLE;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;


public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{

    private static NavigationView navigationView;
    private static FragmentManager manager;

    @SuppressLint("StaticFieldLeak")
    private static TextView storyProgressTextView;
    @SuppressLint("StaticFieldLeak")
    private static TextView costumeProgressTextView;

    public static int maxStory = 36;
    public static int maxCostume = 12;

    public static int storyProgress = 0;
    public static int mainStoryProgress = 0;
    public static int costumeProgress = 1; // default costume

    @SuppressLint("StaticFieldLeak")
    public static TextView fabCount;
    @SuppressLint("StaticFieldLeak")
    public static ProgressBar pbStory, pbCostume;

    public static FloatingActionButton changeFragmentFab;

    public static Context applicationContext;
    public static boolean startPage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // set navigation color
        getWindow().setNavigationBarColor(Color.WHITE);

        SharedPreferences prefs = getSharedPreferences("prefs", MODE_PRIVATE); // doesn't need context at activity onCreate
        SharedPreferences.Editor editor = prefs.edit();
        applicationContext = getApplicationContext();

        manager = getSupportFragmentManager();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);

        View header = navigationView.getHeaderView(0);

        // nick name control at startup
        String nickName = prefs.getString("nickName", "--");
        TextView tv = header.findViewById(R.id.nickName);
        tv.setText(nickName);

        // is alcyone sleep?
        Calendar cal = Calendar.getInstance();
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        LayoutAlcyone.isAlcyoneSleep = (hour >= 0) && (hour < 6);

        // schedule, story, costume list initialize (include progress)
        listInitialize();

        // progress text control at startup
        storyProgressTextView = header.findViewById(R.id.storyProgressTextView);
        costumeProgressTextView = header.findViewById(R.id.costumeProgressTextView);

        // progressbar control at startup
        pbStory = header.findViewById(R.id.storyProgressBar);
        setStoryProgressBar();
        pbCostume = header.findViewById(R.id.costumeProgressBar);
        setCostumeProgressBar();

        // conversation initialize (includes conversation count initialize)
        conversationInitialize();

        // last open date
        String lastOpenDate = prefs.getString("lastOpenDate", null);
        if (lastOpenDate == null) {
            if (hour < 6) {
                // yesterday is last open date
                PrefsController.setLastOpenDateYesterdayPrefs(applicationContext, "lastOpenDate");
            } else {
                // today is last open date
                PrefsController.setLastOpenDatePrefs(applicationContext, "lastOpenDate");
            }
            lastOpenDate = prefs.getString("lastOpenDate", null);
        }

        @SuppressLint("SimpleDateFormat") SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        String dateBackup = lastOpenDate;
        Date lastDate, newDate;
        long dateDiff;

        try {
            lastDate = format.parse(lastOpenDate);

            // new open date
            PrefsController.setLastOpenDatePrefs(applicationContext, "lastOpenDate");
            lastOpenDate = prefs.getString("lastOpenDate", null);

            newDate = format.parse(lastOpenDate);

            // date difference
            dateDiff = newDate.getTime() - lastDate.getTime();
            dateDiff = dateDiff / (24 * 60 * 60 * 1000);

            // schedule completed number today
            LayoutAlcyone.scheduleCompletedNumberToday = prefs.getInt("scheduleCompletedNumberToday", 0);

            // is schedule closed
            LayoutAlcyone.isTodayScheduleClosed = prefs.getBoolean("isTodayScheduleClosed", false);

            if (dateDiff > 0) {

                if (hour >= 6) {
                    // is today first opened (day changed, alcyone not sleep)
                    LayoutAlcyone.isTodayFirstOpen = true;

                    // nurse
                    if (dateDiff >= 3) {
                        if (!LayoutStory.storyList.get(31).unlocked)
                            LayoutAlcyone.nurseCount = true;
                    }

                    PrefsController.setLastOpenDatePrefs(applicationContext, "lastOpenDate");
                    // lastOpenDate < today
                    LayoutAlcyone.isTodayScheduleClosed = false;
                    LayoutAlcyone.scheduleCompletedNumberToday = 0;
                    editor.putInt("scheduleCompletedNumberToday", 0);
                    editor.putBoolean("isTodayScheduleClosed", false);
                } else {
                    // is today first opened (day changed, alcyone sleep)
                    // like that user is not launch application

                    // pajamas
                    String pajamasCountDate = prefs.getString("pajamasCountDate", dateBackup);

                    if (!LayoutStory.storyList.get(32).unlocked && !(lastOpenDate.equals(pajamasCountDate))) {
                        int pajamasCount = prefs.getInt("pajamasCount", 0);
                        pajamasCount++;
                        editor.putInt("pajamasCount", pajamasCount);
                        editor.putString("pajamasCountDate", lastOpenDate);

                        if (pajamasCount > 3)
                            LayoutAlcyone.pajamasCount = true;
                    }
                    editor.putString("lastOpenDate", dateBackup);
                }
                editor.apply();
            } else if (dateDiff < 0) {
                LayoutAlcyone.timeSleeper = true;
            } else { // dateDiff == 0
                LayoutAlcyone.isTodayFirstOpen = false;
            }

        } catch (Exception e) {
            // ignore exception
        }

        // fab count control (After conversation count initialize)
        fabCount = findViewById(R.id.fabCount);
        fabCountControl();

        // costume selected position initialize
        LayoutAlcyone.costumeSelectedInitialize(applicationContext);

        // fab control
        changeFragmentFab = findViewById(R.id.changeFragmentFab);
        changeFragmentFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!ConversationControl.conversationLock) {
                    navigationView.setCheckedItem(R.id.nav_activity_alcyone);
                    onNavigationItemSelected(navigationView.getMenu().getItem(0));
                }
            }
        });

        // start page initialize
        startPage = prefs.getBoolean("startPage", false);

        // start page from notification
        String intentExtraPage = getIntent().getStringExtra("startPage");
        if ((intentExtraPage != null) && (intentExtraPage.equals("schedule")))
            startPage = true;

        // start page
        if (startPage) {
            navigationView.setCheckedItem(R.id.nav_activity_schedule);
            changeFragmentFabControl(true);
            manager.beginTransaction().replace(R.id.content_main, new LayoutSchedule()).commit();
        } else {
            navigationView.setCheckedItem(R.id.nav_activity_alcyone);
            onNavigationItemSelected(navigationView.getMenu().getItem(0));
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        applicationContext = getApplicationContext();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.main, menu);
//        return true;
//    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
//        int id = item.getItemId();

        /*
        if (id == R.id.action_settings) {
            return true;
        }
        */
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_activity_alcyone) {
            fabCountControl();
            LayoutAlcyone.shutDownFab();
            LayoutAlcyone.storyRequestContinue = -1;
            LayoutAlcyone.sceneCount = 0;
            //changeFragmentFabControl(false);
            manager.beginTransaction().replace(R.id.content_main, new LayoutAlcyone()).commit();
        } else if (id == R.id.nav_activity_schedule) {
            fabCountControl();
            LayoutAlcyone.shutDownFab();
            changeFragmentFabControl(true);
            manager.beginTransaction().replace(R.id.content_main, new LayoutSchedule()).commit();
        } else if (id == R.id.nav_activity_story) {
            fabCountControl();
            LayoutAlcyone.shutDownFab();
            changeFragmentFabControl(true);
            manager.beginTransaction().replace(R.id.content_main, new LayoutStory()).commit();
        } else if (id == R.id.nav_activity_costume) {
            fabCountControl();
            LayoutAlcyone.shutDownFab();
            changeFragmentFabControl(true);
            manager.beginTransaction().replace(R.id.content_main, new LayoutCostume()).commit();
        } else if (id == R.id.nav_activity_setting) {
            fabCountControl();
            LayoutAlcyone.shutDownFab();
            changeFragmentFabControl(true);
            manager.beginTransaction().replace(R.id.content_main, new LayoutSetting()).commit();
        } else if (id == R.id.nav_activity_info) {
            fabCountControl();
            LayoutAlcyone.shutDownFab();
            changeFragmentFabControl(true);
            manager.beginTransaction().replace(R.id.content_main, new LayoutInfo()).commit();
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public static void toAlcyone() {
        navigationView.setCheckedItem(R.id.nav_activity_alcyone);
        fabCountControl();
        LayoutAlcyone.shutDownFab();
        //changeFragmentFabControl(false);

        Handler handler = new Handler(applicationContext.getMainLooper());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                manager.beginTransaction().replace(R.id.content_main, new LayoutAlcyone()).commit();
            }
        }, 0);
    }

    public static void toStory() {
        navigationView.setCheckedItem(R.id.nav_activity_story);
        fabCountControl();
        LayoutAlcyone.shutDownFab();
        changeFragmentFabControl(true);

        Handler handler = new Handler(applicationContext.getMainLooper());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                manager.beginTransaction().replace(R.id.content_main, new LayoutStory()).commit();
            }
        }, 0);
    }

    public static void toCostume() {
        navigationView.setCheckedItem(R.id.nav_activity_costume);
        fabCountControl();
        LayoutAlcyone.shutDownFab();
        changeFragmentFabControl(true);

        Handler handler = new Handler(applicationContext.getMainLooper());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                manager.beginTransaction().replace(R.id.content_main, new LayoutCostume()).commit();
            }
        }, 0);
    }

    // set progress bar
    public static void setStoryProgressBar() {
        pbStory.setMax(maxStory);
        pbStory.setProgress(storyProgress);
        String str = String.format(Locale.KOREAN, "%02d", storyProgress);
        str = str + "/" + maxStory + " 스토리";
        storyProgressTextView.setText(str);
    }

    public static void setCostumeProgressBar() {
        pbCostume.setMax(maxCostume);
        pbCostume.setProgress(costumeProgress);
        String str = String.format(Locale.KOREAN, "%02d", costumeProgress);
        str = str + "/" + maxCostume + " 코스튬";
        costumeProgressTextView.setText(str);
    }

    public void listInitialize() {
        SharedPreferences prefs = getSharedPreferences("prefs", MODE_PRIVATE); // doesn't need context at MainAcvitivy

        // main story progress
        mainStoryProgress = prefs.getInt("mainStoryProgress", 0);

        // storyList initialize
        LayoutStory.storyList = PrefsController.getStoryListPrefs(applicationContext, "storyList");
        if (LayoutStory.storyList == null) {
            LayoutStory.storyList = new ArrayList<>();

            // story 0      : tutorial
            // story 1~30   : main story
            // story 31~35  : costume story
            for (int i = 0; i <= 30; i++) { // 31 main now
                Story item = new Story();
                item.unlocked = false;
                item.storyName = String.format(Locale.KOREAN, "main%02d", i);
                item.priority = 0;
                LayoutStory.storyList.add(item);
            }

            for (int i = 0; i < 2; i++) { // 2 special now
                Story item = new Story();
                item.unlocked = false;
                item.storyName = String.format(Locale.KOREAN, "costumeSpecial%02d", i);
                item.priority = 1;
                LayoutStory.storyList.add(item);
            }

            for (int i = 0; i < 3; i++) { // 3 pay now
                Story item = new Story();
                item.unlocked = false;
                item.storyName = String.format(Locale.KOREAN, "costumePay%02d", i);
                item.priority = 10;
                LayoutStory.storyList.add(item);
            }
        } else {
            // missing main story cover
            if (mainStoryProgress > 0) {
                for (int i = 0; i < mainStoryProgress; i++)
                    LayoutStory.storyList.get(i).unlocked = true;
            }

            storyProgress = 0;
            for (int i = 0; i < LayoutStory.storyList.size(); i++) {
                if (LayoutStory.storyList.get(i).unlocked)
                    storyProgress++;
            }
        }
        PrefsController.setStoryListPrefs(applicationContext, "storyList", LayoutStory.storyList);

        // costumeList initialize
        //ArrayList<Costume> costumeList = PrefsController.getCostumeListPrefs(applicationContext, "costumeList");
        LayoutCostume.costumeList = PrefsController.getCostumeListPrefs(applicationContext, "costumeList");

        if (LayoutCostume.costumeList == null) {
            LayoutCostume.costumeList = new ArrayList<>();
            Costume item;

            item = new Costume();
            item.costumeName = "default";
            item.unlocked = true;
            item.priority = 0;
            LayoutCostume.costumeList.add(item);

            item = new Costume();
            item.costumeName = "school";
            item.unlocked = false;
            item.priority = 0;
            LayoutCostume.costumeList.add(item);

            item = new Costume();
            item.costumeName = "business";
            item.unlocked = false;
            item.priority = 0;
            LayoutCostume.costumeList.add(item);

            item = new Costume();
            item.costumeName = "training";
            item.unlocked = false;
            item.priority = 0;
            LayoutCostume.costumeList.add(item);

            item = new Costume();
            item.costumeName = "outside";
            item.unlocked = false;
            item.priority = 0;
            LayoutCostume.costumeList.add(item);

            item = new Costume();
            item.costumeName = "cheer";
            item.unlocked = false;
            item.priority = 0;
            LayoutCostume.costumeList.add(item);

            item = new Costume();
            item.costumeName = "wedding";
            item.unlocked = false;
            item.priority = 0;
            LayoutCostume.costumeList.add(item);

            item = new Costume();
            item.costumeName = "nurse";
            item.unlocked = false;
            item.priority = 1;
            LayoutCostume.costumeList.add(item);

            item = new Costume();
            item.costumeName = "pajamas";
            item.unlocked = false;
            item.priority = 1;
            LayoutCostume.costumeList.add(item);

            item = new Costume();
            item.costumeName = "bunny";
            item.unlocked = false;
            item.priority = 10;
            LayoutCostume.costumeList.add(item);

            item = new Costume();
            item.costumeName = "bloomer";
            item.unlocked = false;
            item.priority = 10;
            LayoutCostume.costumeList.add(item);

            item = new Costume();
            item.costumeName = "succubus";
            item.unlocked = false;
            item.priority = 10;
            LayoutCostume.costumeList.add(item);
        } else {
            // missing costume cover
            if (mainStoryProgress > 0) {
                for (int i = 0; i < mainStoryProgress; i++) {
                    switch (i) {
                        case 4:
                            LayoutCostume.costumeList.get(1).unlocked = true;
                            break;
                        case 9:
                            LayoutCostume.costumeList.get(2).unlocked = true;
                            break;
                        case 14:
                            LayoutCostume.costumeList.get(3).unlocked = true;
                            break;
                        case 19:
                            LayoutCostume.costumeList.get(4).unlocked = true;
                            break;
                        case 24:
                            LayoutCostume.costumeList.get(5).unlocked = true;
                            break;
                        case 30:
                            LayoutCostume.costumeList.get(6).unlocked = true;
                            break;
                    }
                }
            }

            costumeProgress = 1;
            for (int i = 1; i < LayoutCostume.costumeList.size(); i++) { // i=1~ (except default costume)
                if (LayoutCostume.costumeList.get(i).unlocked)
                    costumeProgress++;
            }
        }
        PrefsController.setCostumeListPrefs(applicationContext, "costumeList", LayoutCostume.costumeList);
        // else if the number of costume < the number of costumes of new version ~

        LayoutSchedule.scheduleList = PrefsController.getScheduleListPrefs(applicationContext, "scheduleList");
        if (LayoutSchedule.scheduleList == null) {
            LayoutSchedule.scheduleList = new ArrayList<>();
            PrefsController.setScheduleListPrefs(applicationContext, "scheduleList", LayoutSchedule.scheduleList);
        }

    }

    public static void changeFragmentFabControl(boolean show) {
        if (show) {
            //changeFragmentFab.show();
            changeFragmentFab.setAlpha(1f);
            changeFragmentFab.setClickable(true);
        } else {
            fabCountControl();
            //changeFragmentFab.hide();
            changeFragmentFab.setAlpha(0f);
            changeFragmentFab.setClickable(false);
        }
    }

    public void conversationInitialize() {
        ConversationControl.conversation = new ArrayList<>();
        ConversationControl.conversationCount = 0;
        ConversationControl.conversationInstant = new ArrayList<>();
    }

    public static void fabCountControl() {
        if (ConversationControl.inConversation || ConversationControl.inConversationInstant) {
            fabCount.setVisibility(INVISIBLE);
        } else {
            Calendar cal = Calendar.getInstance();
            int hour = cal.get(Calendar.HOUR_OF_DAY);
            if ((hour >= 20) && (!LayoutAlcyone.isTodayScheduleClosed) && (!LayoutAlcyone.isFabOpened)) {
                fabCount.setText("!");
                fabCount.setVisibility(View.VISIBLE);
            } else {
                if (ConversationControl.conversationCount == 0) {
                    fabCount.setVisibility(INVISIBLE);
                } else {
                    fabCount.setText(Integer.toString(ConversationControl.conversationCount));
                    fabCount.setVisibility(View.VISIBLE);
                }
            }
        }
    }

    public static void fabAboveSnackAnimation(boolean isOpen) {
        Calendar cal = Calendar.getInstance();
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        if ((hour < 20) && ((LayoutAlcyone.isAlcyoneSleep) || (ConversationControl.conversationCount == 0))) {
            if (isOpen) {
                Animation fabOpen = AnimationUtils.loadAnimation(MainActivity.applicationContext, R.anim.fab_open);
                changeFragmentFab.startAnimation(fabOpen);
                changeFragmentFab.setClickable(true);
            } else {
                Animation fabClose = AnimationUtils.loadAnimation(applicationContext, R.anim.fab_close);
                changeFragmentFab.startAnimation(fabClose);
                changeFragmentFab.setClickable(false);
            }
        } else {
            if (isOpen) {
                Animation fabOpen = AnimationUtils.loadAnimation(MainActivity.applicationContext, R.anim.fab_open);
                changeFragmentFab.startAnimation(fabOpen);
                fabCount.startAnimation(fabOpen);
                fabCount.clearAnimation(); // great code!!
                changeFragmentFab.setClickable(true);
            } else {
                Animation fabClose = AnimationUtils.loadAnimation(applicationContext, R.anim.fab_close);
                changeFragmentFab.startAnimation(fabClose);
                fabCount.startAnimation(fabClose);
                fabCount.clearAnimation();
                changeFragmentFab.setClickable(false);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        ConversationControl.conversationLock = false;
        switch (resultCode) {
            // wanna go to alcyone
            case 0:
                toAlcyone();
                break;
            // wanna go to story
            case 1:
                toStory();
                break;
            // wanna show story continue
            case 2:
                toAlcyone();
                break;
            // wanna go to costume
            case 3:
                toCostume();
                break;
        }
    }
}
