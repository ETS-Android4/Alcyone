package com.pleiades.pleione.alcyone;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.os.Vibrator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.Guideline;
import androidx.fragment.app.Fragment;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;

import java.util.Calendar;
import java.util.Locale;
import java.util.Random;

import static android.content.Context.MODE_PRIVATE;
import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;

public class LayoutAlcyone extends Fragment {
    public static String costumeSelectedName;
    public static int costumeSelectedPosition;
    public static int scheduleCompletedNumberToday;
    public static int storyRequest = -1; // if positive integer, run story
    public static int storyRequestContinue = -1;
    public static int sceneCount = 0;
    public static int fromOtherLayout = 0;
    public static boolean isTodayFirstOpen; // default false but initialize at Main if first
    public static boolean isTodayScheduleClosed;
    public static boolean isAlcyoneSleep;
    public static boolean isFabOpened = false;
    public static boolean fastTalkSpeed = false;
    public static boolean lowEndMode = false;
    public static boolean timeSleeper = false;
    public static boolean nurseCount = false;
    public static boolean pajamasCount = false;
    public static FloatingActionButton scheduleCloseFab;

    public Context context;
    public View v;

    private ImageView alcyone;
    private ImageView alcyoneFace;
    private ImageView pleione;
    private ImageView conversationView;
    private TextView speaker;
    private TextView conversationText;
    private FloatingActionButton conversationFab;

    private int touchNumber = 0;
    private long mLastClickTime = 0;
    private Random random;
    private boolean businessUnlock = false;

    private int dailyConversationCount = 0;
    private final boolean[] dailyConversationExecuted = new boolean[8];

    private static int preSpeaker = -1;
    private static int tutorialCount = 0;
    private static Animation fabOpen;
    private static Animation fabClose;
    private static boolean isGreeted = false;
    private static boolean tmpStoryCostume = false;
    private static boolean inGreeting = false;
    private static boolean startingPoint = false;
    private static boolean inScheduleClosing;
    private static boolean getNewCostume = false;
    private static boolean getNewReview = false;
    private static ConversationScript conversationScript;
    private static String nickName;
    private static Handler handler;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.activity_alcyone, container, false);

        // view initialize
        alcyone = v.findViewById(R.id.alcyone);
        alcyoneFace = v.findViewById(R.id.alcyone_face);
        pleione = v.findViewById(R.id.pleione);
        speaker = v.findViewById(R.id.speaker);
        conversationView = v.findViewById(R.id.conversationView);
        conversationText = v.findViewById(R.id.conversationText);

        alcyone.clearColorFilter();
        alcyoneFace.clearColorFilter();
        pleione.clearColorFilter();
        alcyone.setVisibility(INVISIBLE);
        alcyoneFace.setVisibility(INVISIBLE);
        pleione.setVisibility(INVISIBLE);
        speaker.setVisibility(INVISIBLE);
        conversationView.setVisibility(INVISIBLE);

        context = getContext();
        costumeSelectedInitialize(context);

        if (storyRequest == -1 && isGreeted) {
            // alcyone sleep
            if (isAlcyoneSleep)
                changeCharacterResource(true, "pajamas", "sleep");
            else // set costume to selected one
                changeCharacterResource(true, costumeSelectedName, null);
        }

        fabOpen = AnimationUtils.loadAnimation(context, R.anim.fab_open);
        fabClose = AnimationUtils.loadAnimation(context, R.anim.fab_close);

        SharedPreferences prefs = context.getSharedPreferences("prefs", MODE_PRIVATE); // doesn't need context at activity onCreate

        fastTalkSpeed = prefs.getBoolean("fastTalkSpeed", false);
        lowEndMode = prefs.getBoolean("lowEndMode", false);

        MainActivity.changeFragmentFabControl(false);

        scheduleCloseFab = v.findViewById(R.id.scheduleCloseFab);
        scheduleCloseFab.setClickable(false);
        scheduleCloseFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                scheduleCloseFab.startAnimation(fabClose);
                scheduleCloseFab.setClickable(false);
                isFabOpened = false;
                scheduleCloseDialog();
            }
        });

        conversationFab = v.findViewById(R.id.conversationFab);
        conversationFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!ConversationControl.conversationLock) {
                    Calendar cal = Calendar.getInstance();
                    int hour = cal.get(Calendar.HOUR_OF_DAY);
                    if ((((hour >= 20) && (!LayoutAlcyone.isTodayScheduleClosed)) || isFabOpened) && !ConversationControl.inConversation && !ConversationControl.inConversationInstant) {
                        toggleFab();
                        MainActivity.fabCountControl();
                    } else {
                        fabPerformance();
                    }
                }
            }
        });

        ImageView alcyoneTouch = v.findViewById(R.id.alcyone_touch);
        alcyoneTouch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isAlcyoneSleep && (storyRequest == -1)) {
                    if (costumeSelectedName.equals("business")) {
                        if (businessUnlock) {
                            changeCharacterResource(true, "business", null);
                            businessUnlock = false;
                        } else {
                            changeCharacterResource(true, "business", "unlock");
                            businessUnlock = true;
                        }
                    }

                    if (!ConversationControl.inConversationInstant && !ConversationControl.inConversation)
                        touchNumber++;
                    if ((touchNumber >= 5) && (ConversationControl.conversationCount == 0)) {
                        ConversationScript script1 = new ConversationScript("왜 자꾸 만지시는 거죠..?", "shy");
                        ConversationControl.addConversationInstant(script1);
                        startInstantConversation();
                        touchNumber = 0;
                        businessUnlock = false;
                    }
                }
            }
        });

        if (!LayoutStory.storyList.get(0).unlocked) {
            isGreeted = true;
            storyRequest = 0;
            readStory();
        } else if (pajamasCount && (storyRequest == -1)) {
            isGreeted = true;
            pajamasCount = false;
            storyRequest = 32;
            readStory();
        } else if (nurseCount && (storyRequest == -1)) {
            isGreeted = true;
            nurseCount = false;
            storyRequest = 31;
            readStory();
        } else if (storyRequestContinue != -1) {
            finishInstantConversation();
        } else if (storyRequest == -1) {
            // interrupt conversation
            if (ConversationControl.inConversation || ConversationControl.inConversationInstant) {
                if (ConversationControl.inConversation) {
                    if (ConversationControl.conversationCount > 0)
                        ConversationControl.conversationCount--;
                    MainActivity.fabCountControl();
                    ConversationControl.inConversation = false;
                } else
                    ConversationControl.inConversationInstant = false;

                if (!isAlcyoneSleep) {
                    ConversationScript script1 = new ConversationScript("대화중에 자리를 떠나시다니.. 너무하세요...", "sad");
                    ConversationControl.addConversationInstant(script1);
                    startInstantConversation();
                }
            }
            // greeting (must under interrupt)
            else if (!isGreeted)
                greeting();
        } else {
            readStory();
        }

        return v;
    }

    public static void costumeSelectedInitialize(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("prefs", MODE_PRIVATE);

        costumeSelectedName = prefs.getString("costumeSelectedName", "default");
        for (int i = 0; i < LayoutCostume.costumeList.size(); i++) {
            if (LayoutCostume.costumeList.get(i).costumeName.equals(costumeSelectedName))
                costumeSelectedPosition = i;
        }
        //costumeSelectedPosition = prefs.getInt("costumeSelectedPosition", 0);
    }

    public void changeCharacterResource(boolean isAlcyone, String costumeName, String facialExpression) {
        String packName = context.getPackageName();
        String resName;
        int resID;

        if (isAlcyone) {
            // in story
            if (storyRequest != -1) {
                if (tmpStoryCostume) {
                    switch (storyRequest) {
                        case 9:
                            resName = "@drawable/alcyone_business";
                            break;
                        case 24:
                            resName = "@drawable/alcyone_cheer";
                            break;
                        case 30:
                            resName = "@drawable/alcyone_wedding";
                            break;
                        case 31:
                            resName = "@drawable/alcyone_nurse";
                            break;
                        case 32:
                            resName = "@drawable/alcyone_pajamas";
                            break;
                        case 33:
                            resName = "@drawable/alcyone_bunny";
                            break;
                        case 34:
                            resName = "@drawable/alcyone_bloomer";
                            break;
                        case 35:
                            resName = "@drawable/alcyone_succubus";
                            break;
                        default:
                            resName = "@drawable/alcyone_default";
                    }
                } else {
                    switch (storyRequest) {
                        case 4:
                            resName = "@drawable/alcyone_school";
                            break;
                        case 14:
                            resName = "@drawable/alcyone_training";
                            break;
                        case 19:
                            resName = "@drawable/alcyone_outside";
                            break;
                        default:
                            resName = "@drawable/alcyone_default";
                    }
                }

                resID = getResources().getIdentifier(resName, "drawable", packName);
                alcyone.setImageResource(resID);

                if (facialExpression == null) {
                    resName = "@drawable/alcyone_face";
                } else {
                    resName = "@drawable/alcyone_face_" + facialExpression;
                }
                resID = getResources().getIdentifier(resName, "drawable", packName);
                alcyoneFace.setImageResource(resID);
                alcyoneFace.setVisibility(VISIBLE);
            } else if (facialExpression != null && facialExpression.equals("unlock")) {
                resName = "@drawable/alcyone_business_unlock";
                resID = getResources().getIdentifier(resName, "drawable", packName);
                alcyone.setImageResource(resID);
                resName = "@drawable/alcyone_face_shy";
                resID = getResources().getIdentifier(resName, "drawable", packName);
                alcyoneFace.setImageResource(resID);
                alcyoneFace.setVisibility(VISIBLE);
            } else if (isAlcyoneSleep) {
                resName = "@drawable/alcyone_pajamas";
                resID = getResources().getIdentifier(resName, "drawable", packName);
                alcyone.setImageResource(resID);
                alcyoneFace.setVisibility(INVISIBLE);
            } else {
                resName = "@drawable/alcyone_" + costumeName;
                resID = getResources().getIdentifier(resName, "drawable", packName);
                alcyone.setImageResource(resID);

                if (facialExpression == null) {
                    resName = "@drawable/alcyone_face";
                } else {
                    resName = "@drawable/alcyone_face_" + facialExpression;
                }
                resID = getResources().getIdentifier(resName, "drawable", packName);
                alcyoneFace.setImageResource(resID);
                alcyoneFace.setVisibility(VISIBLE);
            }
            alcyone.setVisibility(VISIBLE);
        } else {
            // 표정 따로 할까 이것도? 일단 보류
            if (facialExpression == null)
                resName = "@drawable/pleione";
            else
                resName = "@drawable/pleione_" + facialExpression;
            resID = getResources().getIdentifier(resName, "drawable", packName);
            pleione.setImageResource(resID);

            pleione.setVisibility(VISIBLE);
        }
    }

    public void startAccumulatedConversation() {
        ConversationControl.inConversation = true;
        MainActivity.fabCountControl();
        conversationFab.setImageResource(R.drawable.ic_keyboard_arrow_right_white_24dp);
        conversationView.setVisibility(VISIBLE);

        ConversationControl.conversationBlock = ConversationControl.conversation.get(0);
        ConversationControl.conversation.remove(0);
        //PrefsController.setConversationPrefs(context, "conversation", ConversationControl.conversation);

        conversationText.setVisibility(VISIBLE);
        if (startingPoint) {
            handler = new Handler(context.getMainLooper());
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    talk(false);
                    startingPoint = false;
                }
            }, 150);
        } else
            talk(false);
    }

    public void startInstantConversation() {
        ConversationControl.inConversationInstant = true;
        MainActivity.fabCountControl();
        conversationFab.setImageResource(R.drawable.ic_keyboard_arrow_right_white_24dp);
        conversationView.setVisibility(VISIBLE);

        ConversationControl.conversationBlock = ConversationControl.conversationInstant.get(0); // 의심
        ConversationControl.conversationInstant.remove(0);

        conversationText.setVisibility(VISIBLE);
        if ((storyRequest != -1) || inGreeting) {
            startingPoint = true;
            inGreeting = false;
        }
        if (startingPoint) {
            handler = new Handler(context.getMainLooper());
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    talk(true);
                    startingPoint = false;
                }
            }, 150);
        } else
            talk(true);
    }

    public void talk(boolean isInstant) {
        ConversationControl.conversationLock = true;
        if (ConversationControl.inTutorial) {
            tutorialCount++;
            if (tutorialCount == 26) {
                nickNameDialog();
                return;
            }
        }
        conversationScript = ConversationControl.conversationBlock.block.get(0);
        ConversationControl.conversationBlock.block.remove(0);

        if (ConversationControl.inTutorial) {
            if (tutorialCount == 27)
                conversationScript.script = String.format("반가워요, %s님!", nickName);
            else if (tutorialCount == 40)
                conversationScript.script = String.format("%s님이 누르고 계신 상호작용 버튼은 상황에 따라 아이콘이 다르게 표시될 거예요.", nickName);
            else if (tutorialCount == 44)
                conversationScript.script = String.format("그럼 좋은 하루 보내세요, %s님.", nickName);
        }

        if (conversationScript.sceneChange) {
            storyRequestContinue = storyRequest;
            sceneCount++;
            Intent intent = new Intent(getActivity(), LayoutSplashStory.class);
            intent.putExtra("request", 2);
            startActivityForResult(intent, 2); // request code 2
            ((Activity) context).overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            return;
        }

        if (isInstant)
            ConversationControl.inTalkingInstant = true;
        else
            ConversationControl.inTalking = true;

        if (timeSleeper) {
            changeCharacterResource(conversationScript.isAlcyone, costumeSelectedName, conversationScript.facialExpression);
            speaker.setText("플레이오네");
        }
        // daily situation
        else if (storyRequest == -1) {
            changeCharacterResource(conversationScript.isAlcyone, costumeSelectedName, conversationScript.facialExpression);
            speaker.setText("알키오네");
        } else {
            if (conversationScript.isAlcyone) {
                if (conversationScript.visibilityAlcyone)
                    changeCharacterResource(true, costumeSelectedName, conversationScript.facialExpression);
                else {
                    alcyoneFace.setVisibility(INVISIBLE);
                    alcyone.setVisibility(INVISIBLE);
                }
                if (conversationScript.visibilityPleione)
                    pleione.setVisibility(VISIBLE);
                else
                    pleione.setVisibility(INVISIBLE);

                // alcyone start talk when preSpeaker is null
                if ((preSpeaker == -1) || ((preSpeaker == 1) && (lowEndMode))) {
                    bringToFrontAlcyone();
                    alcyone.setColorFilter(Color.argb(0, 0, 0, 0));
                    alcyoneFace.setColorFilter(Color.argb(0, 0, 0, 0));
                    pleione.setColorFilter(Color.argb(127, 0, 0, 0));
                } else if ((preSpeaker == 1) && (!lowEndMode))// when preSpeaker is pleione
                    characterAnimation(true);

                speaker.setText("알키오네");
                preSpeaker = 0; // alcyone
            } else if (conversationScript.noNamed) {
                alcyone.setColorFilter(Color.argb(127, 0, 0, 0));
                alcyoneFace.setColorFilter(Color.argb(127, 0, 0, 0));
                pleione.setColorFilter(Color.argb(127, 0, 0, 0));

                if (conversationScript.visibilityAlcyone) {
                    alcyone.setVisibility(VISIBLE);
                    alcyoneFace.setVisibility(VISIBLE);
                } else {
                    alcyoneFace.setVisibility(INVISIBLE);
                    alcyone.setVisibility(INVISIBLE);
                }

                if (conversationScript.visibilityPleione)
                    pleione.setVisibility(VISIBLE);
                else
                    pleione.setVisibility(INVISIBLE);

                speaker.setText("--");
                preSpeaker = -1; // noNamed
            } else {
                if (conversationScript.visibilityAlcyone) {
                    if (startingPoint)
                        changeCharacterResource(true, costumeSelectedName, null);
                    else {
                        alcyoneFace.setVisibility(VISIBLE);
                        alcyone.setVisibility(VISIBLE);
                    }
                } else {
                    alcyoneFace.setVisibility(INVISIBLE);
                    alcyone.setVisibility(INVISIBLE);
                }
                if (conversationScript.visibilityPleione)
                    changeCharacterResource(false, costumeSelectedName, conversationScript.facialExpression); // automatically visible
                else
                    pleione.setVisibility(INVISIBLE);

                // pleione start talk when preSpeaker is null
                if ((preSpeaker == -1) || ((preSpeaker == 0) && (lowEndMode))) {
                    bringToFrontPleione();
                    pleione.setColorFilter(Color.argb(0, 0, 0, 0));
                    alcyone.setColorFilter(Color.argb(127, 0, 0, 0));
                    alcyoneFace.setColorFilter(Color.argb(127, 0, 0, 0));
                } else if ((preSpeaker == 0) && (!lowEndMode))// when preSpeaker is alcyone
                    characterAnimation(false);

                speaker.setText("플레이오네");
                preSpeaker = 1; // pleione
            }
        }
        speaker.setVisibility(VISIBLE);

        textAnimation();
    }

    public void textAnimation() {
        int delayMillis = 25;
        if (fastTalkSpeed)
            delayMillis = 15;

        if (conversationScript.vibrate) {
            Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
            vibrator.vibrate(200);
        }

        for (int i = 1; i <= conversationScript.script.length(); i++) {
            final String subScript = conversationScript.script.substring(0, i);
            handler = new Handler(context.getMainLooper());
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    conversationText.setText(subScript);
                    if (subScript.equals(conversationScript.script)) {
                        ConversationControl.inTalkingInstant = false;
                        ConversationControl.inTalking = false;
                        ConversationControl.conversationLock = false;
                    }
                }
            }, delayMillis * i); // origin delayMillis * i
        }
    }

    public void characterAnimation(final boolean isAlcyone) {
        for (int i = 0; i < 128; i++) {
            final int iterator = i;
            handler = new Handler(context.getMainLooper());
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (isAlcyone) {
                        if (iterator == 64)
                            bringToFrontAlcyone();
                        alcyone.setColorFilter(Color.argb(127 - iterator, 0, 0, 0)); // 127 ~ 0
                        alcyoneFace.setColorFilter(Color.argb(127 - iterator, 0, 0, 0));
                        pleione.setColorFilter(Color.argb(iterator, 0, 0, 0)); // 0 ~ 127
                    } else {
                        if (iterator == 64)
                            bringToFrontPleione();
                        pleione.setColorFilter(Color.argb(127 - iterator, 0, 0, 0));
                        alcyone.setColorFilter(Color.argb(iterator, 0, 0, 0));
                        alcyoneFace.setColorFilter(Color.argb(iterator, 0, 0, 0));
                    }
                }
            }, i * 2);
        }
    }

    public void finishAccumulatedConversation() {
        conversationFab.setImageResource(R.drawable.ic_sentiment_very_satisfied_white_24dp);
        conversationView.setVisibility(INVISIBLE);
        conversationText.setText("");
        conversationText.setVisibility(INVISIBLE);
        speaker.setVisibility(INVISIBLE);

        changeCharacterResource(true, costumeSelectedName, null);

        if (ConversationControl.conversationCount > 0)
            ConversationControl.conversationCount--;

        ConversationControl.inConversation = false;
        MainActivity.fabCountControl();
    }

    public void finishInstantConversation() {
        if (storyRequestContinue != -1) {
            // between story, story continue
            startStoryContinue();
            return;
        } else if (storyRequest != -1 || timeSleeper) {
            if (getNewCostume) {
                getNewCostume = false;
                getNewCostumeDialog();
            } else {
                ConversationControl.conversationLock = true;
                storyRequest = -1;
                sceneCount = 0;
                tmpStoryCostume = false;

                if (fromOtherLayout != 0) {
                    handler = new Handler(context.getMainLooper());
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (fromOtherLayout == 1) {
                                fromOtherLayout = 0;
                                backToLayoutStory();
                            } else if (fromOtherLayout == 2) {
                                fromOtherLayout = 0;
                                backToLayoutCostume();
                            }
                        }
                    }, 400);
                } else {
                    handler = new Handler(context.getMainLooper());
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            timeSleeper = false;
                            Intent intent = new Intent(getActivity(), LayoutSplashStory.class);
                            intent.putExtra("request", 0);
                            startActivityForResult(intent, 0);
                            ((Activity) context).overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                        }
                    }, 400);
                }
            }
        } else {
            conversationFab.setImageResource(R.drawable.ic_sentiment_very_satisfied_white_24dp);
            conversationView.setVisibility(INVISIBLE);
            conversationText.setText("");
            conversationText.setVisibility(INVISIBLE);
            speaker.setVisibility(INVISIBLE);

            changeCharacterResource(true, costumeSelectedName, null);

            if (getNewReview) {
                getNewReview = false;
                getNewReviewDialog();
            }

            if (inScheduleClosing) {
                LayoutSchedule.deleteCompletedSchedules();
                SharedPreferences prefs = context.getSharedPreferences("prefs", MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();

                if (scheduleCompletedNumberToday != 0) {
                    isTodayScheduleClosed = true;
                    editor.putBoolean("isTodayScheduleClosed", true);
                    editor.apply();

                    if (MainActivity.mainStoryProgress <= 30) {
                        storyRequest = MainActivity.mainStoryProgress;
                        storyRequestContinue = -1;

                        Intent intent = new Intent(getActivity(), LayoutSplashStory.class);
                        intent.putExtra("request", 0);
                        startActivityForResult(intent, 0); // request code 0
                        ((Activity) context).overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                        return;
                    }
                } else {
                    isTodayScheduleClosed = true;
                    editor.putBoolean("isTodayScheduleClosed", true);
                    editor.apply();
                    inScheduleClosing = false;
                }
            }
        }
        ConversationControl.inConversationInstant = false;
        ConversationControl.inTutorial = false;
        MainActivity.fabCountControl();
    }

    public void toggleFab() {
        if (isFabOpened) {
            scheduleCloseFab.startAnimation(fabClose);
            scheduleCloseFab.setClickable(false);
            // default fab performance
            fabPerformance();
            isFabOpened = false;
        } else {
            scheduleCloseFab.startAnimation(fabOpen);
            scheduleCloseFab.setClickable(true);
            isFabOpened = true;
        }
    }

    public static void shutDownFab() {
        if (isFabOpened) {
            scheduleCloseFab.startAnimation(fabClose);
            scheduleCloseFab.setClickable(false);
            isFabOpened = false;
        }
    }

    public void fabPerformance() {
        if (ConversationControl.inTalkingInstant || ConversationControl.inTalkingInstant)
            return;

        // mis-clicking prevention, using threshold of 300 ms
        if (SystemClock.elapsedRealtime() - mLastClickTime < 300)
            return;
        mLastClickTime = SystemClock.elapsedRealtime();

        if ((!isAlcyoneSleep) || (storyRequest != -1)) {
            if (ConversationControl.conversationCount > 0) {
                if (ConversationControl.inConversation) {
                    if (ConversationControl.conversationBlock.block.size() > 0)
                        talk(false);
                    else {
                        finishAccumulatedConversation();
                    }
                } else {
                    // start conversation
                    startAccumulatedConversation();
                }
            }
            // greeting automatically perform this (count == 0)
            else {
                if (ConversationControl.inConversationInstant) {
                    if (ConversationControl.conversationBlock.block.size() > 0)
                        talk(true);
                    else {
                        finishInstantConversation();
                    }
                } else {
                    dailyConversation();
                }
            }
        } else {
            if (ConversationControl.inConversationInstant) {
                if (ConversationControl.conversationBlock.block.size() > 0)
                    talk(true);
                else {
                    finishInstantConversation();
                }
            } else {
                // Zzz
                ConversationScript script1 = new ConversationScript("Zzz...", null);
                ConversationControl.addConversationInstant(script1);
                startInstantConversation();
            }
        }
    }

    public void scheduleCloseDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.AppTheme_AlertDialogOverlay);
        builder.setTitle("금일 일정을 마감하시겠어요?");

        // cannot cancel
        builder.setCancelable(false);

        builder.setPositiveButton("마감",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        inScheduleClosing = true;

                        ConversationScript script1, script2, script3, script4, script5, script6;
                        script1 = new ConversationScript("오늘 완료한 일정의 개수는..", null);
                        script2 = new ConversationScript(String.format(Locale.KOREAN, "%d 개네요!", scheduleCompletedNumberToday), "glad");

                        switch (scheduleCompletedNumberToday) {
                            case 0:
                                script3 = new ConversationScript(".....", null);
                                script4 = new ConversationScript("..어? 제가 뭔가 빠트렸나요?! 이럴 리가 없는데!", "surprised");
                                script5 = new ConversationScript("혹시 제가 자고 있을 때 완료하셨다거나.. 근데 그렇다고 해도 0 개는..", "surprised");
                                script6 = new ConversationScript("..일정 관리를 소홀히 하시면 안 돼요!", "sad");
                                ConversationControl.addConversationInstant(script1, script2, script3, script4, script5, script6);
                                break;
                            case 1:
                            case 2:
                                script3 = new ConversationScript("비교적 한산한 하루를 보내셨군요?", null);
                                script4 = new ConversationScript("바쁘게 살아가는 것도 좋지만, 역시 적절한 휴식도 필요하다고 생각해요.", null);
                                script5 = new ConversationScript("오늘도 고생하셨어요.", "glad");
                                ConversationControl.addConversationInstant(script1, script2, script3, script4, script5);
                                break;
                            case 3:
                            case 4:
                                script3 = new ConversationScript("이대로라면 무엇을 목표로 하든 금방 달성하시겠는데요?", "glad");
                                script4 = new ConversationScript("물론 일정의 개수가 중요한 건 아니지만.. 이렇게 관리를 꼼꼼하게 하시니까요.", "glad");
                                script5 = new ConversationScript("오늘 하루도 고생 많으셨어요.", "glad");
                                ConversationControl.addConversationInstant(script1, script2, script3, script4, script5);
                                break;
                            default:
                                script3 = new ConversationScript(String.format(Locale.KOREAN, "..%d 개?! 정말 대단하세요!", scheduleCompletedNumberToday), "surprised");
                                script4 = new ConversationScript("설마.. 잔소리 듣기 싫어서 의미 없는 일정을 이것저것 완료한 건 아니시겠죠?", "playful");
                                script5 = new ConversationScript("농담이에요. 오늘 하루도 정말 고생 많으셨어요.", "glad");
                                script6 = new ConversationScript("푹 쉬세요.", "glad");
                                ConversationControl.addConversationInstant(script1, script2, script3, script4, script5, script6);
                                break;
                        }
                        startInstantConversation();
                    }
                });
        builder.setNegativeButton("취소",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
        builder.show();
    }

    private void getNewCostumeDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.AppTheme_AlertDialogOverlay);
        builder.setTitle("새로운 코스튬을 획득하셨어요!");

        // cannot cancel
        builder.setCancelable(false);

        builder.setPositiveButton("확인",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        ConversationControl.conversationLock = true;
                        storyRequest = -1;
                        sceneCount = 0;
                        tmpStoryCostume = false;

                        if (fromOtherLayout != 0) {
                            handler = new Handler(context.getMainLooper());
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    if (fromOtherLayout == 1) {
                                        fromOtherLayout = 0;
                                        backToLayoutStory();
                                    } else if (fromOtherLayout == 2) {
                                        fromOtherLayout = 0;
                                        backToLayoutCostume();
                                    }
                                }
                            }, 400);
                        } else {
                            handler = new Handler(context.getMainLooper());
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    Intent intent = new Intent(getActivity(), LayoutSplashStory.class);
                                    intent.putExtra("request", 0);
                                    startActivityForResult(intent, 0);
                                    ((Activity) context).overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                                }
                            }, 400);
                        }
                    }
                });
        builder.show();
    }

    private void getNewReviewDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.AppTheme_AlertDialogOverlay);
        builder.setTitle("리뷰를 작성해주시겠어요?");

        // cannot cancel
        builder.setCancelable(false);

        builder.setPositiveButton("작성",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        SharedPreferences prefs = context.getSharedPreferences("prefs", MODE_PRIVATE);
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putBoolean("isReviewed", true);
                        editor.apply();

                        String url = "https://play.google.com/store/apps/details?id=com.pleiades.pleione.alcyone";
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setData(Uri.parse(url));
                        startActivity(intent);
                    }
                });
        builder.setNegativeButton("취소",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
        builder.show();
    }

    public void greeting() {
        if (timeSleeper) {
            Guideline guidelineV1 = v.findViewById(R.id.guidelineV1);
            Guideline guidelineV1R = v.findViewById(R.id.guidelineV1R);
            guidelineV1.setGuidelinePercent((float) 0.3);
            guidelineV1R.setGuidelinePercent((float) 0.7);
            ConversationScript script1 = new ConversationScript("너...", "sneer", false, false, true);
            ConversationScript script2 = new ConversationScript("시간을 돌렸구나?", "sneer", false, false, true);
            ConversationScript script3 = new ConversationScript("괜찮아 괜찮아, 너처럼 호기심 많은 아이를 싫어하진 않으니까.", null, false, false, true);
            ConversationScript script4 = new ConversationScript("그래도 그런 짓은 하지 않는 게 좋을걸?", "playful", false, false, true);
            ConversationScript script5 = new ConversationScript("왜냐면..", "playful", false, false, true);
            ConversationScript script6 = new ConversationScript("버그 테스트를 안 했거든!", "heart", false, false, true);
            ConversationScript script7 = new ConversationScript("무슨 일이 일어나도 난 모른다?", "heart", false, false, true);
            ConversationControl.addConversationInstant(script1, script2, script3, script4, script5, script6, script7);
        } else if (!isAlcyoneSleep) {
            Calendar cal = Calendar.getInstance();
            int hour = cal.get(Calendar.HOUR_OF_DAY);
            ConversationScript script1, script2, script3;

            SharedPreferences prefs = context.getSharedPreferences("prefs", MODE_PRIVATE);
            String nickName = prefs.getString("nickName", "--");

            if (isTodayFirstOpen) {
                // alcyone sleep before 6
                switch (hour) {
                    case 6:
                    case 7:
                        script1 = new ConversationScript(String.format("%s님 오셨어요? 일찍 오셨네요?", nickName), "glad");
                        ConversationControl.addConversationInstant(script1);
                        break;
                    case 8:
                    case 9:
                        script1 = new ConversationScript(String.format("오셨어요, %s님?", nickName), "glad");
                        script2 = new ConversationScript("저도 마침 준비가 끝난 참이었어요.", null);
                        ConversationControl.addConversationInstant(script1, script2);
                        break;
                    case 10:
                    case 11:
                        script1 = new ConversationScript(String.format("좋은 아침이에요, %s님.", nickName), "glad");
                        script2 = new ConversationScript("...", null);
                        script3 = new ConversationScript("..아무튼 아침이에요.", "shy");
                        ConversationControl.addConversationInstant(script1, script2, script3);
                        break;
                    case 12:
                    case 13:
                    case 14:
                        script1 = new ConversationScript(String.format("%s님 오셨어요? 조금.. 늦으셨네요?", nickName), null);
                        script2 = new ConversationScript("오전에 뭔가 재미있는 일이라도 있으셨나봐요?", "playful");
                        ConversationControl.addConversationInstant(script1, script2);
                        break;
                    case 15:
                    case 16:
                    case 17:
                        script1 = new ConversationScript(String.format("설마 이제 일어나신 건 아니겠죠, %s님?", nickName), "sad");
                        script2 = new ConversationScript("아침이 훌쩍 지나가버린 기분이지만.. 오늘 하루도 힘내주세요.", null);
                        ConversationControl.addConversationInstant(script1, script2);
                        break;
                    case 18:
                    case 19:
                        script1 = new ConversationScript(String.format("어..? 오셨어요, %s님?", nickName), null);
                        script2 = new ConversationScript("너무 늦은 시간이라 오늘은 안 오시는 줄 알았다고요.", "sad");
                        ConversationControl.addConversationInstant(script1, script2);
                        break;
                    case 20:
                    case 21:
                    case 22:
                    case 23:
                        script1 = new ConversationScript(String.format("%s님..", nickName), "sad");
                        script2 = new ConversationScript("설마 일정만 마감하러 오신 건 아니겠죠?", "sad");
                        ConversationControl.addConversationInstant(script1, script2);
                        break;
                    default:
                }
            } else {
                // alcyone sleep before 6
                switch (hour) {
                    case 6:
                    case 7:
                        script1 = new ConversationScript(String.format("이른 시간에 또 뵙네요, %s님.", nickName), "glad");
                        script2 = new ConversationScript("깜박잊고 등록하지 않은 일정이라도 있으신가요?", null);
                        ConversationControl.addConversationInstant(script1, script2);
                        break;
                    case 8:
                    case 9:
                        script1 = new ConversationScript(String.format("조금 전에도 들르지 않으셨나요, %s님?", nickName), null);
                        script2 = new ConversationScript("설마 벌써 일정을 하나 완료하신 건가요? 대단해요!", "surprised");
                        ConversationControl.addConversationInstant(script1, script2);
                        break;
                    case 10:
                    case 11:
                        script1 = new ConversationScript(String.format("좋은 하루 보내고 계신가요, %s님?", nickName), null);
                        ConversationControl.addConversationInstant(script1);
                        break;
                    case 12:
                    case 13:
                    case 14:
                        script1 = new ConversationScript(String.format("%s님 안녕하세요, 점심 식사는 하셨나요?", nickName), "glad");
                        script2 = new ConversationScript("열심히 하시는 것도 좋지만, 건강도 생각해주세요.", null);
                        ConversationControl.addConversationInstant(script1, script2);
                        break;
                    case 15:
                    case 16:
                    case 17:
                        script1 = new ConversationScript(String.format("일정은 잘 진행되고 있으신가요, %s님?", nickName), "glad");
                        ConversationControl.addConversationInstant(script1);
                        break;
                    case 18:
                    case 19:
                        script1 = new ConversationScript(String.format("%s님 안녕하세요, 좋은 하루 보내셨나요?", nickName), "glad");
                        ConversationControl.addConversationInstant(script1);
                        break;
                    case 20:
                    case 21:
                    case 22:
                    case 23:
                        if (!isTodayScheduleClosed)
                            script1 = new ConversationScript(String.format("안녕하세요 %s님, 일정 마감하러 오신 건가요?", nickName), "glad");
                        else
                            script1 = new ConversationScript(String.format("일정은 아까 마감하셨는데.. 다른 일이 있으신가요, %s님?", nickName), null);
                        ConversationControl.addConversationInstant(script1);
                        break;
                    default:
                }
            }
        } else {
            ConversationScript script1 = new ConversationScript("Zzz...", null);
            ConversationControl.addConversationInstant(script1);
        }
        inGreeting = true;
        startInstantConversation();
        isGreeted = true;
    }

    public void dailyConversation() {
        SharedPreferences prefs = context.getSharedPreferences("prefs", MODE_PRIVATE);
        String nickName = prefs.getString("nickName", "--");
        ConversationScript script1, script2, script3, script4;

        // global variables : private bound = 8, dailyConversationCount = 0, boolean[] dailyConversationExecuted = new boolean[8];

        if (random == null)
            random = new Random();

        // consumed all conversation
        // daily conversation
        int bound = 8;
        if (dailyConversationCount == bound) {
            dailyConversationCount = 0;
            for (int i = 0; i < bound; i++)
                dailyConversationExecuted[i] = false;
        }

        // bound == 8 (0~7)
        int selector = random.nextInt(bound);
        for (int i = 0; i < bound; i++) {
            if (dailyConversationExecuted[selector])
                selector = random.nextInt(bound);
            else
                break;
        }

        switch (selector) {
            case 0:
                script1 = new ConversationScript("혹시 아이스크림 좋아하시나요?", null);
                script2 = new ConversationScript("저랑 저희 어머니께서는 아이스크림을 엄청 좋아해요!", "glad");
                script3 = new ConversationScript("저는 특히 체리X루를 좋아하는데.. 정말 여건만 되면 쌓아놓고 먹고 싶..", "lovely");
                script4 = new ConversationScript("..죄송해요. 흥분해버렸네요.", "shy");
                ConversationControl.addConversationInstant(script1, script2, script3, script4);
                break;
            case 1:
                script1 = new ConversationScript("제 나이가 궁금하신가요?", null);
                script2 = new ConversationScript("저는 22 살, 그리고 어머니는 23 살이에요.", null);
                script3 = new ConversationScript("너무 깊게 파고들지는 말아주세요...", "sad");
                ConversationControl.addConversationInstant(script1, script2, script3);
                break;
            case 2:
                script1 = new ConversationScript(String.format("Alcyone226. %s님, 제대로 기억해주세요.", nickName), "playful");
                script2 = new ConversationScript(String.format("%s님.. 제가 있으면 충분해요.", nickName), "shy");
                script3 = new ConversationScript("..어머니께서 제일 좋아하시는 게임 캐릭터의 대사예요.", null);
                ConversationControl.addConversationInstant(script1, script2, script3);
                break;
            case 3:
                script1 = new ConversationScript("저랑 어머니의 관계요?", null);
                script2 = new ConversationScript("..사랑하는 사이?", "shy");
                script3 = new ConversationScript("비록 혈연관계는 아니지만.. 서로를 누구보다 아끼고 있어요.", "glad");
                ConversationControl.addConversationInstant(script1, script2, script3);
                break;
            case 4:
                script1 = new ConversationScript("예전에는 대학이 정말 다니고 싶었는데.. 이젠 꼭 그렇지만은 않아요.", null);
                script2 = new ConversationScript("지금은 그저 어머니가 행복한 대학생활을 하셨으면 좋겠어요.", "glad");
                script3 = new ConversationScript(String.format("%s님은 어떤 학창시절을 보내셨나요?", nickName), "glad");
                ConversationControl.addConversationInstant(script1, script2, script3);
                break;
            case 5:
                script1 = new ConversationScript("좋은 냄새.. 킁킁.", "wily");
                script2 = new ConversationScript("어머니가 근처까지 오셨나..?", "wily");
                script3 = new ConversationScript(String.format("..%s님 언제부터 거기 계셨어요?!", nickName), "confused");
                ConversationControl.addConversationInstant(script1, script2, script3);
                break;
            case 6:
                script1 = new ConversationScript("저녁에 토스트.. 어떻게 생각하세요?", "shy");
                script2 = new ConversationScript("휴.. 저녁 메뉴 정하기가 너무 힘드네요.", "sad");
                script3 = new ConversationScript("점심은 혼자니까 대충 해결하면 되지만.. 어머니는 편식이 심하셔서요.", "sad");
                script4 = new ConversationScript(String.format("..설마 %s님도 파프리카 못 드시나요?", nickName), "confused");
                ConversationControl.addConversationInstant(script1, script2, script3, script4);
                break;
            case 7:
                if (prefs.getBoolean("isReviewed", false)) {
                    script1 = new ConversationScript(String.format("리뷰를 작성해주셔서 감사해요, %s님!", nickName), "heart");
                    script2 = new ConversationScript(String.format("%s님의 그런 마음씨가 항상 제게 큰 힘이 되고 있어요.", nickName), "glad");
                } else {
                    script1 = new ConversationScript(String.format("제 서비스는 만족스러우신가요, %s님?", nickName), "glad");
                    script2 = new ConversationScript("혹시 그러시다면, 리뷰.. 부탁드려도 될까요?", "shy");
                    getNewReview = true;
                }
                ConversationControl.addConversationInstant(script1, script2);
                break;
        }
        dailyConversationCount++;
        dailyConversationExecuted[selector] = true;
        startInstantConversation();
    }

    private void readStory() {
        if (storyRequest <= 30) {
            // main story 01~30, 0 is filtered at onCreate(tutorial)
            startMainStory();
        } else if (storyRequest <= 32) {
            // special story 31~32 (2 now)
            startSpecialStory();
        } else {
            // pay story 33~35 (3 now)
            startPayStory();
        }
    }

    private void startMainStory() {
//        // base to copy
//        scripts[i++] = new ConversationScript("", null, true);
//        scripts[i++] = new ConversationScript("", null, false);
//        scripts[i++] = new ConversationScript("", null, true, true, true); // visibility
//        scripts[i++] = new ConversationScript("", null, false, false, false); // visibility
//        scripts[i++] = new ConversationScript(true); // sceneChange
//        scripts[i++] = new ConversationScript("", true); // noNamed
//        scripts[i++] = new ConversationScript("", true, true, false); // noNamed visibility
//        scripts[i++] = new ConversationScript("", true, true); // vibrate
        ConversationScript[] scripts = new ConversationScript[40];
        int i = 0;
        switch (storyRequest) {
            case 0:
                ConversationControl.inTutorial = true;
                tutorialCount = 0;
                Guideline guidelineV1 = v.findViewById(R.id.guidelineV1);
                Guideline guidelineV1R = v.findViewById(R.id.guidelineV1R);
                guidelineV1.setGuidelinePercent((float) 0.3);
                guidelineV1R.setGuidelinePercent((float) 0.7);
                scripts[i++] = new ConversationScript("와! 안녕하세요! 플레이오네예요! 아키쨩을 다운로드해주셔서 정말 감사해요! >_<", null, false, false, true);
                scripts[i++] = new ConversationScript("앱 사용을 시작하기에 앞서 간단하게 앱을 소개해드릴게요!", null, false, false, true);
                scripts[i++] = new ConversationScript("어.. 아키쨩은 일정 관리 앱이에요! 다들 휴대폰에 하나씩은 탑재돼있는..", "surprised", false, false, true);
                scripts[i++] = new ConversationScript("네네! 메모장처럼 쓰는 그거요!", null, false, false, true);
                scripts[i++] = new ConversationScript("굳이 아키쨩을 쓸 이유가 있냐고 물으시면 할 말 없지만.. =3=", "hmm", false, false, true);
                scripts[i++] = new ConversationScript("저는 예전부터 AI 비서에 대한 로망이 있었거든요!", null, false, false, true);
                scripts[i++] = new ConversationScript("만화 같은 곳에 보면 자주 나오잖아요? 같이 대화하고 조언해주고..", "heart", false, false, true);
                scripts[i++] = new ConversationScript("저한테도 그런 친구가 있었으면 좋겠다고 항상 생각했어요!", null, false, false, true);
                scripts[i++] = new ConversationScript("아키쨩도 사실 제가 쓰려고 만든 거기도 하고.. 하하..", "sneer", false, false, true);
                scripts[i++] = new ConversationScript("뭐 아무튼! 계획적으로 생활하시는 데에 아키쨩이 동기부여가 됐으면 좋겠어요!", null, false, false, true);
                scripts[i++] = new ConversationScript("그럼, 시작할게요?", "heart", false, false, true);
                scripts[i] = new ConversationScript(true); // sceneChange
                break;
            case 1:
                scripts[i++] = new ConversationScript("아키쨩 나 왔어~", "heart", false);
                scripts[i++] = new ConversationScript("오셨어요, 어머니?", "glad", true);
                scripts[i++] = new ConversationScript("오늘 무슨 좋은 일 있으셨어요? 표정이 밝으시네요.", null, true);
                scripts[i++] = new ConversationScript("응응! 무슨 일 있었지!", "heart", false);
                scripts[i++] = new ConversationScript("오늘 실습보조 월급 나오는 날이잖아!", null, false);
                scripts[i++] = new ConversationScript("그래서 큰맘먹고 아이스크림 사왔어! 짠! 녹차X루!", null, false);
                scripts[i++] = new ConversationScript(".....", "sad", true);
                scripts[i++] = new ConversationScript(".....", "surprised", false);
                scripts[i++] = new ConversationScript("..아키쨩은 체리X루!", "heart", false);
                scripts[i++] = new ConversationScript("와! 체리X루!", "lovely", true);
                scripts[i++] = new ConversationScript("체리X루 아시는구나! 진.짜.겁.나.맛.있.습.니.다.", "lovely", true);
                scripts[i++] = new ConversationScript("..아키쨩.", "surprised", false);
                scripts[i++] = new ConversationScript("그건 무슨 말투야..?", "surprised", false);
                scripts[i] = new ConversationScript("..아무것도 아니에요.", "shy", true);
                break;
            case 2:
                scripts[i++] = new ConversationScript("흠...", "hmm", false);
                scripts[i++] = new ConversationScript("그럴 거면 tv를 사는 게 낫지 않아?", "hmm", false);
                scripts[i++] = new ConversationScript("티비는 수신료를 따로 내야 하는 거 아니에요? 지금 인터넷 요금은 관리비에 포함돼 있으니까..", "sad", true);
                scripts[i++] = new ConversationScript("차라리 큰 모니터를 사는 게 나을 것 같아요.", "sad", true);
                scripts[i++] = new ConversationScript("어차피 어머니 티비 볼 시간도 없으시잖아요?", "sad", true);
                scripts[i++] = new ConversationScript("그치만.. 아키쨩이 낮에 봐도 되고.. 아침에 일기예보 틀어놓고 양치도 하고...", "sad", false);
                scripts[i++] = new ConversationScript("안 돼요 낭비에요!", "sad", true);
                scripts[i++] = new ConversationScript("역시 모니터가 나아요. 그리고 예전부터 모니터가 하나라서 불편하다고 하셨잖아요?", null, true);
                scripts[i++] = new ConversationScript("음...", "hmm", false);
                scripts[i++] = new ConversationScript("그럼 둘 다 사지 말고 저금하자! 꼭 필요한 것도 아닌데.", null, false);
                scripts[i++] = new ConversationScript("..그래요, 그럼!", "glad", true);
                scripts[i++] = new ConversationScript("역시..", "hmm", false);
                scripts[i] = new ConversationScript("복권은 당첨된다 해도 고민이네!", null, false);
                break;
            case 3:
                scripts[i++] = new ConversationScript("안 씻으세요 어머니? 일찍 주무셔야죠..", "sad", true);
                scripts[i++] = new ConversationScript("앗.. 이거 오늘까지는 끝내고 싶어서..", "hmm", false);
                scripts[i++] = new ConversationScript("새벽까지 과제라니.. 역시 대학생은 힘드네요.", "sad", true);
                scripts[i++] = new ConversationScript(".....", "sad", false);
                scripts[i++] = new ConversationScript("미안해 아키쨩.. 내가 돈모아서 꼭 학교 다니게 해줄게...", "sad", false);
                scripts[i++] = new ConversationScript("네? 아니에요. 어머니도 장학금받고 다니시는데..", "surprised", true);
                scripts[i++] = new ConversationScript("제가 부족해서 그런거죠.", "sad", true);
                scripts[i++] = new ConversationScript("그런데 곧 제출 마감일인가 봐요?", null, true);
                scripts[i++] = new ConversationScript("별일이네요, 어머니께서 과제도 미루시고.", null, true);
                scripts[i++] = new ConversationScript("응응? 제출은.. 기말시험까진데...", "surprised", false);
                scripts[i++] = new ConversationScript(".....", "confused", true);
                scripts[i++] = new ConversationScript("어머니.", null, true);
                scripts[i++] = new ConversationScript("불 끌게요.", "wily", true);
                scripts[i] = new ConversationScript("미.. 미안해 아키쨩!", "surprised", false);
                break;
            case 4:
                scripts[i++] = new ConversationScript("아키쨩 나 왔..", null, false, false, true);
                scripts[i++] = new ConversationScript("꺄약!", null, true, false, true);
                scripts[i++] = new ConversationScript("어..어머니! 일찍 오셨네요!", "surprised", true);
                scripts[i++] = new ConversationScript("응응! 근데 왜 그렇게..", null, false);
                scripts[i++] = new ConversationScript("와!!! 아키쨩 그거 내 교복이야?", "heart", false);
                scripts[i++] = new ConversationScript("네.. 어머니 졸업앨범을 보다가 문득 생각나서.. 꺼내입어봤는데...", "shy", true);
                scripts[i++] = new ConversationScript("왠지 부끄럽네요.. 이렇게 치마가 짧은 줄이야.", "shy", true);
                scripts[i++] = new ConversationScript("상체쪽도 많이 조이고...", "shy", true);
                scripts[i++] = new ConversationScript("어..? 나는 평범하게 입었는데...", "surprised", false);
                scripts[i++] = new ConversationScript("졸업사진에도.. 이정도면 보통 아니야?", null, false);
                scripts[i++] = new ConversationScript("음.. 세탁하다가 옷이 줄어든 게 아닐까요?", "sad", true);
                scripts[i++] = new ConversationScript("그런가?", null, false);
                scripts[i++] = new ConversationScript("확실히 지금 아키쨩이 입은 거랑은 느낌이 많이 다르네..", "hmm", false);
                scripts[i++] = new ConversationScript("뭐 어때! 이제 입을 일도 없을 텐데.", null, false);
                scripts[i++] = new ConversationScript("그나저나 아키쨩! 너무 귀여워!!! 사진 찍어도 돼?!", "heart", false);
                scripts[i++] = new ConversationScript("..안 돼요.", "shy", true);
                scripts[i] = new ConversationScript("힝...", "crying", false);
                break;
            case 5:
                scripts[i++] = new ConversationScript("아키쨩.. 아키쨩...", "sad", false);
                scripts[i++] = new ConversationScript("왜 그러세요 어머니?", null, true);
                scripts[i++] = new ConversationScript("나 무서워.. 오늘 같이 자면 안 돼?", "crying", false);
                scripts[i++] = new ConversationScript("네? 오늘은 날씨도 좋은데...", "surprised", true);
                scripts[i++] = new ConversationScript("..방금 본 웹툰에 귀신이 너무 무서워서...", "crying", false);
                scripts[i++] = new ConversationScript("어머니, 또 공포 만화 보셨어요? 무서운 거 잘 보지도 못하시면서...", "sad", true);
                scripts[i++] = new ConversationScript("그치만 무서운 만화는 무서워해야 의미가 있는 거잖아..", "sad", false);
                scripts[i++] = new ConversationScript("오히려 그런 거 잘 보는 사람은 재미를 못 느끼지 않을까..?", "sad", false);
                scripts[i++] = new ConversationScript("그것도 그렇지만.. 어머니는 보통 사람들보다 2배는 겁이 많으시잖아요..", "sad", true);
                scripts[i++] = new ConversationScript("공포 만화는 조금 줄이셨으면 좋겠어요.", "sad", true);
                scripts[i++] = new ConversationScript("아무리 그래도 나 그 정도까지는 아니라구!", "hmm", false);
                scripts[i++] = new ConversationScript("그래요?", "wily", true);
                scripts[i++] = new ConversationScript("그럼 혼자 주무실 수 있죠?", "playful", true);
                scripts[i++] = new ConversationScript("어..?", "confused", false, false, true);
                scripts[i++] = new ConversationScript("아키쨩? 문 열어줘!", "surprised", false, false, true);
                scripts[i] = new ConversationScript("아키쨩!!!", "surprised", false, false, true);
                break;
            case 6:
                scripts[i++] = new ConversationScript("아키쨩, 아~", null, false);
                scripts[i++] = new ConversationScript("어머니.. 이제 제가 직접 까먹을게요...", "shy", true);
                scripts[i++] = new ConversationScript("안 돼, 아키쨩! 새우는 먹여주라고 있는 음식인걸!", "heart", false);
                scripts[i++] = new ConversationScript("그럼 그냥 머리만 떼서 주세요...", "shy", true);
                scripts[i++] = new ConversationScript("힝.. 전부 다 까주고 싶은 게 내 마음인데...", "crying", false);
                scripts[i++] = new ConversationScript("..알겠어요.", "shy", true);
                scripts[i++] = new ConversationScript("그럼 저도 먹여드릴게요.", null, true);
                scripts[i++] = new ConversationScript("응응! 아~", null, false);
                scripts[i++] = new ConversationScript("어머니 정말 새우 좋아하시네요.", "glad", true);
                scripts[i++] = new ConversationScript("응응! 새우는 구워도 맛있고 쪄도 맛있고 튀겨도 맛있고..", null, false);
                scripts[i++] = new ConversationScript("아, 그래도 제일 맛있는 건 역시 생새우초밥!", "heart", false);
                scripts[i++] = new ConversationScript("식감이 탱글해서 너무 좋은 것 같아.. 역시 나 맛보단 식감파인가?", "heart", false);
                scripts[i++] = new ConversationScript("샐러드도 아삭아삭해서 너무 좋아!", "heart", false);
                scripts[i++] = new ConversationScript("그 샐러드를 만드는 건 항상 저지만요.", "wily", true);
                scripts[i++] = new ConversationScript("헤헤..", null, false);
                scripts[i] = new ConversationScript("아키쨩 사랑해!!!", "heart", false);
                break;
            case 7:
                scripts[i++] = new ConversationScript("어? 웬 민트 초콜릿이에요, 어머니?", null, true);
                scripts[i++] = new ConversationScript("아 그거? 카페 직원분이 하나 주시더라구.", null, false);
                scripts[i++] = new ConversationScript("..내가 그렇게 시끄러웠나?", "hmm", false);
                scripts[i++] = new ConversationScript("네? 그게 무슨 말씀이세요?", "confused", true);
                scripts[i++] = new ConversationScript("아니 그냥.. 다음부터 오지 말라는 뜻인가 해서...", "sad", false);
                scripts[i++] = new ConversationScript("민트 초코.. 맛없잖아?", "sad", false);
                scripts[i++] = new ConversationScript("네????? 민트 초코가 얼마나 맛있는데!!!", "surprised", true);
                scripts[i++] = new ConversationScript("분명 그 직원 어머니께 관심 있어서 준 걸 거예요.", "wily", true);
                scripts[i++] = new ConversationScript("에이 설마.. 그랬으면 녹차 쿠키를 줬겠지.", "sneer", false);
                scripts[i++] = new ConversationScript("민트는 치약 맛인걸?", "sneer", false);
                scripts[i++] = new ConversationScript("민트가 치약 맛인 게 아니고 치약이 민트 맛인 거예요!", "shout", true);
                scripts[i++] = new ConversationScript("지.. 진정해 아키쨩!", "surprised", false);
                scripts[i++] = new ConversationScript("..민트 덕분에 초코가 깔끔해지는 건데...", "shy", true);
                scripts[i++] = new ConversationScript("그래도 치약 느낌이 지워지질 않아서.. 몸이 삼키기를 거부한다고 해야 하나?", "hmm", false);
                scripts[i++] = new ConversationScript("아무튼 별로야.", "sad", false);
                scripts[i++] = new ConversationScript("그리고 민트 녀석..", null, false);
                scripts[i++] = new ConversationScript("초코가 놀아주니까 기고만장해져선.. 요즘 안 보이는 곳이 없어!", "hmm", false);
                scripts[i++] = new ConversationScript("어머니! 얘가 다 듣겠어요!", "surprised", true);
                scripts[i++] = new ConversationScript("앗...", "surprised", false);
                scripts[i] = new ConversationScript("민트야 미안해...", "crying", false);
                break;
            case 8:
                scripts[i++] = new ConversationScript("아키쨩 아키쨩!", null, false);
                scripts[i++] = new ConversationScript("네, 어머니!", null, true);
                scripts[i++] = new ConversationScript("이거 봐! 이거 봐!", null, false);
                scripts[i++] = new ConversationScript("..안경?", null, true);
                scripts[i++] = new ConversationScript("응응!", null, false);
                scripts[i++] = new ConversationScript("또 어디에 렌즈 놔두고 오신 거예요?", "sad", true);
                scripts[i++] = new ConversationScript("아.. 아니라구!", "surprised", false);
                scripts[i++] = new ConversationScript("그게 아니라, 오늘 안경의 장점을 알아냈어!", null, false);
                scripts[i++] = new ConversationScript("음.. 뭔데요?", "sad", true);
                scripts[i++] = new ConversationScript("이렇게 과자를 털어먹을 때..", null, false);
                scripts[i++] = new ConversationScript("눈에 부스러기가 안 들어와!", "heart", false);
                scripts[i++] = new ConversationScript("(우물우물)", null, false);
                scripts[i++] = new ConversationScript("어머니...", "sad", true);
                scripts[i++] = new ConversationScript("..응응?", "confused", false);
                scripts[i++] = new ConversationScript("그냥 과자가 먹고싶으셨던 거죠?", null, true);
                scripts[i] = new ConversationScript("헤헤...", "heart", false);
                break;
            case 9:
                scripts[i++] = new ConversationScript("역시 이 정장 말이야, 아키쨩이 입는 게 나을 것 같아.", null, false);
                scripts[i++] = new ConversationScript("나는 IT 계열이라서 딱히 포멀하게 입을 일이 없잖아?", null, false);
                scripts[i++] = new ConversationScript("예전에 선물 받았던 거네요. 하긴 지금까지도 몇 번 안 입으셨고..", null, true);
                scripts[i++] = new ConversationScript("응응. 지금 한 번 입어볼래?", null, false);
                scripts[i] = new ConversationScript(true);
                break;
            case 10:
                scripts[i++] = new ConversationScript("아키쨩 아키쨩! 나 방학하면 우리 여행갈까?", null, false);
                scripts[i++] = new ConversationScript("네 좋아요.", "glad", true);
                scripts[i++] = new ConversationScript("어디 가고 싶은 곳이라도 생기셨어요?", null, true);
                scripts[i++] = new ConversationScript("응응! 나 제주도 가고 싶어! 가서 돌고래 볼래!", "heart", false);
                scripts[i++] = new ConversationScript("제주도..?", "confused", true);
                scripts[i++] = new ConversationScript("..힘드려나?", "sad", false);
                scripts[i++] = new ConversationScript("저희가 경제적으로 여유롭지는 않으니까요..", "sad", true);
                scripts[i++] = new ConversationScript("응응...", "sad", false);
                scripts[i++] = new ConversationScript("그리고 제주도에 가려면 배나 비행기를 타야 하는데..", null, true);
                scripts[i++] = new ConversationScript("어머니 멀미가 심하시잖아요? 괜찮으시겠어요?", "sad", true);
                scripts[i++] = new ConversationScript("..멀미는 성인 되고 다 나았어!", "confused", false);
                scripts[i++] = new ConversationScript("아마도...", "sad", false);
                scripts[i++] = new ConversationScript(".....", "sneer", true);
                scripts[i++] = new ConversationScript("졸업 전에 한 번이라도 여행 가고 싶어서..", "sad", false);
                scripts[i++] = new ConversationScript("취직하면 같이 놀러 갈 시간도 없을 테니까..", "sad", false);
                scripts[i++] = new ConversationScript("음..", null, true);
                scripts[i++] = new ConversationScript("그럼 부산은 어떠세요? 비슷한 풍경 아닐까요?", null, true);
                scripts[i++] = new ConversationScript("게다가 아쿠아리움에 가면.. 돌고래도 볼 수 있어요!", "lovely", true);
                scripts[i++] = new ConversationScript("와! 아키쨩 천재야 천재! 그럼 부산 가자!", "heart", false);
                scripts[i++] = new ConversationScript("대신 나 바다도 데려가 줘야 돼!", null, false);
                scripts[i] = new ConversationScript("네.", "glad", true);
                break;
            case 11:
                scripts[i++] = new ConversationScript("힝.. 나만 고양이 없어...", "sad", false);
                scripts[i++] = new ConversationScript("고양이 너무 귀여워! 학교뿌셔 지구뿌셔!!!", "heart", false);
                scripts[i++] = new ConversationScript("간지럽히고 쓰다듬고 같이 자고 싶어!", "heart", false);
                scripts[i++] = new ConversationScript("아키쨩, 우리도 고양이 키울까?", null, false);
                scripts[i++] = new ConversationScript("유튜브로 매일 고양이 영상 보시면서.. 그걸로는 만족이 안되세요?", "sad", true);
                scripts[i++] = new ConversationScript("직접 키우는 거랑은 다르니까...", "hmm", false);
                scripts[i++] = new ConversationScript("나도 고양이 문질문질.. 냥냥..", null, false);
                scripts[i++] = new ConversationScript("생명은 가벼운 마음으로 데려오는 게 아니에요, 어머니.", "sad", true);
                scripts[i++] = new ConversationScript("알고 있어.. 나는 그냥.. 너무 귀여워서..", "sad", false);
                scripts[i++] = new ConversationScript("..그리고 어머니도.. 귀여운 반려가 있으시잖아요?", "sad", true);
                scripts[i++] = new ConversationScript("응응? 그게 무슨 말이야?", null, false);
                scripts[i++] = new ConversationScript("냐옹...", "shy", true);
                scripts[i++] = new ConversationScript(".....", "confused", false);
                scripts[i++] = new ConversationScript("냥냥!", "shy", true);
                scripts[i++] = new ConversationScript(".....", "surprised", false);
                scripts[i++] = new ConversationScript("아키쨩...", "sneer", false);
                scripts[i] = new ConversationScript("귀여워!!!!!", "heart", false);
                break;
            case 12:
                scripts[i++] = new ConversationScript("아키쨩 나 왔어~", null, false);
                scripts[i++] = new ConversationScript("오셨어요, 어머니?", "glad", true);
                scripts[i++] = new ConversationScript("응응. 아키쨩, 밥은 챙겨 먹었어?", null, false);
                scripts[i++] = new ConversationScript("네 먹었죠~ 어머니께서 늦는다고 하셔서 혼자 먹느라 조금 외로웠다고요.", "wily", true);
                scripts[i++] = new ConversationScript("미안해.. 동아리방에 들렀다가 바로 오려고 했는데 어쩌다보니...", "sad", false);
                scripts[i++] = new ConversationScript("아니에요. 가끔은 다 같이 밥도 먹고 그래야죠.", null, true);
                scripts[i++] = new ConversationScript("대인관계도 중요하니까요.", null, true);
                scripts[i++] = new ConversationScript("아.. 다 같이는 아니구..", "surprised", false);
                scripts[i++] = new ConversationScript("졸업한 선배가 왔는데 잠시 얼굴 볼 수 있냐고 하셔서..", "sad", false);
                scripts[i++] = new ConversationScript("책도 반납할 겸 갔다가 밥도 얻어 먹었어!", null, false);
                scripts[i++] = new ConversationScript("네..?", "confused", true);
                scripts[i++] = new ConversationScript("응? 왜?", "surprised", false);
                scripts[i++] = new ConversationScript("여자 선배죠?", "sneer", true);
                scripts[i++] = new ConversationScript("아니? 남자 선배..인데?", null, false);
                scripts[i++] = new ConversationScript(".....", null, true);
                scripts[i++] = new ConversationScript("내일 아침밥은 어머니께서 직접 차려드세요.", null, true);
                scripts[i] = new ConversationScript("어.. 어째서!", "surprised", false);
                break;
            case 13:
                scripts[i++] = new ConversationScript("레몬타르트도 좋지만.. 역시 티라미수네요.", null, true);
                scripts[i++] = new ConversationScript("응응, 그렇지?", null, false);
                scripts[i++] = new ConversationScript("마카롱보단 치즈케이크고..", null, false);
                scripts[i++] = new ConversationScript("네네.", null, true);
                scripts[i++] = new ConversationScript("헉 그러면 결승전이 치즈케이크 vs 티라미수.. 둘 다 너무 맛있는데..!", "surprised", false);
                scripts[i++] = new ConversationScript("음.. 저는 치즈케이크라고 생각해요.", null, true);
                scripts[i++] = new ConversationScript("티라미수는 가루 때문에 깔끔하게 먹기가 힘드니까요.", null, true);
                scripts[i++] = new ConversationScript("맞아 맞아.. 그리고 작년에 먹었던 필라델X아 치즈케이크가 엄청 맛있었지.", null, false);
                scripts[i++] = new ConversationScript("아! 얼려먹었던 그거요?", "lovely", true);
                scripts[i++] = new ConversationScript("응응. 치즈케이크는 아는 맛이니까.. 우리 별로 기대를 안 했었잖아?", null, false);
                scripts[i++] = new ConversationScript("근데 그렇게 맛있을 줄이야.. 역시 비싼 거라 그런가?", "heart", false);
                scripts[i++] = new ConversationScript("확실히 그건 다른 치즈케이크랑은 달랐죠..", "glad", true);
                scripts[i++] = new ConversationScript("응응!", "heart", false);
                scripts[i++] = new ConversationScript("그러면 치즈케이크 고를게?", null, false);
                scripts[i++] = new ConversationScript("네!", null, true);
                scripts[i++] = new ConversationScript("그때 그 치즈케이크.. 또 먹고 싶다!", "heart", false);
                scripts[i++] = new ConversationScript("아, 그런데 어머니!", "surprised", true);
                scripts[i++] = new ConversationScript("응응?", null, false);
                scripts[i++] = new ConversationScript("비싼 치즈케이크가 그런 맛이면..", null, true);
                scripts[i++] = new ConversationScript("비싼 티라미수는 어떤 맛일까요..?", "confused", true);
                scripts[i] = new ConversationScript("어.. 그러게?! 그것까진 생각 못 했는데...", "surprised", false);
                break;
            case 14:
                scripts[i++] = new ConversationScript("흐읏.. 아키쨩 나 힘들어...", "shy", false);
                scripts[i++] = new ConversationScript("안 돼요 어머니. 아직 10분도 안 됐어요.", "sad", true);
                scripts[i++] = new ConversationScript("그치만.. 하아 하아..", "shy", false);
                scripts[i++] = new ConversationScript("학교 갈 때 빼면 밖에 잘 나가지도 않으시고..", "sad", true);
                scripts[i++] = new ConversationScript("운동 부족이에요...", "sad", true);
                scripts[i++] = new ConversationScript("아직 20 대인데, 벌써부터 이래서 괜찮으시겠어요?", "sad", true);
                scripts[i++] = new ConversationScript("하아.. 너도 20 대잖아.. 후우..", "shy", false);
                scripts[i++] = new ConversationScript("저는 계속 운동하잖아요..", "sad", true);
                scripts[i++] = new ConversationScript("나도.. 후.. 운동하는데...", "shy", false);
                scripts[i++] = new ConversationScript("산책은 운동이 아니에요.", "sad", true);
                scripts[i++] = new ConversationScript("너무해...", "crying", false);
                scripts[i++] = new ConversationScript("그럼 계속할까요?", null, true);
                scripts[i++] = new ConversationScript("싫어.. 하아.. 오늘은 그만할래...", "shy", false);
                scripts[i++] = new ConversationScript("흠...", "sad", true);
                scripts[i++] = new ConversationScript("그럼 이제부터 운동 꾸준히 하시는 거예요?", "sad", true);
                scripts[i] = new ConversationScript("응...", "hmm", false);
                break;
            case 15:
                scripts[i++] = new ConversationScript("아키쨩 같이 과자 먹자~", null, false);
                scripts[i++] = new ConversationScript("어머니 또 과자 사 오셨어요? 지난달 간식비도 아슬했는데..", "sad", true);
                scripts[i++] = new ConversationScript("잘 계산하고 계신 거 맞죠?", "sad", true);
                scripts[i++] = new ConversationScript("아니 산 게 아니고..", "surprised", false);
                scripts[i++] = new ConversationScript("내가 좋아할 맛이라면서 친구가 하나 주더라구..", null, false);
                scripts[i++] = new ConversationScript("진짜요..?", "wily", true);
                scripts[i++] = new ConversationScript("진짜라니까~ 아무튼 빨리 먹자!", "surprised", false);
                scripts[i++] = new ConversationScript("처음 먹어보는 거라서 무슨 맛일지 궁금해!", "heart", false);
                scripts[i++] = new ConversationScript("무슨 과자길래..", null, true);
                scripts[i++] = new ConversationScript("..도미 덮밥 맛 감자칩?", "confused", true);
                scripts[i++] = new ConversationScript("어..? 도미구이 맛 아니었어?", "confused", false);
                scripts[i++] = new ConversationScript("도미 덮밥 맛..이라고 적혀있네요..", "confused", true);
                scripts[i++] = new ConversationScript("도미 덮밥이 무슨 맛이죠?", null, true);
                scripts[i++] = new ConversationScript("글쎄... 먹어보면 알겠지?", "surprised", false);
                scripts[i++] = new ConversationScript("자 아키쨩, 아~", null, false);
                scripts[i++] = new ConversationScript("아~", "shy", true);
                scripts[i++] = new ConversationScript("(우물우물)", null, true);
                scripts[i++] = new ConversationScript("아!", "surprised", true);
                scripts[i++] = new ConversationScript("나도 나도!", null, false);
                scripts[i++] = new ConversationScript("(우물우물)", null, false);
                scripts[i++] = new ConversationScript("아?", "surprised", false);
                scripts[i++] = new ConversationScript("이거..", "confused", true);
                scripts[i++] = new ConversationScript("그 맛이네요..", "sneer", true);
                scripts[i++] = new ConversationScript("응응..", "sneer", false);
                scripts[i] = new ConversationScript("가다랑어 포!", null, false);
                break;
            case 16:
                scripts[i++] = new ConversationScript("어머니.", "playful", true);
                scripts[i++] = new ConversationScript("역시.. 노래 잘 부르는 사람은 너무 멋있는 것 같아요.", "glad", true);
                scripts[i++] = new ConversationScript("응응, 그렇긴 한데.. 갑자기..?", "surprised", false);
                scripts[i++] = new ConversationScript("오늘 낮에 인터넷에서 축가 부르는 영상을 봤거든요.", "glad", true);
                scripts[i++] = new ConversationScript("근데, 정장 입고 노래 부르는 모습이 너무 멋있더라고요.", "heart", true);
                scripts[i++] = new ConversationScript("게다가 선곡도 너무 좋았어요.", "lovely", true);
                scripts[i++] = new ConversationScript("뭐.. 축가 때는 대체로 아키쨩이 좋아할 만한 노래를 부르니까.", "sneer", false);
                scripts[i++] = new ConversationScript("결혼식 분위기도 있고..", null, false);
                scripts[i++] = new ConversationScript("그렇죠? 그러니까 저희 노래방 가요!", "heart", true);
                scripts[i++] = new ConversationScript("엥? 지금???", "surprised", false);
                scripts[i++] = new ConversationScript("네! 지금 가고 싶어요!", "glad", true);
                scripts[i++] = new ConversationScript("그.. 그래.", "surprised", false);
                scripts[i++] = new ConversationScript("그럼 학교 앞에 코인노래방 가자!", null, false);
                scripts[i] = new ConversationScript("네!", "glad", true);
                break;
            case 17:
                scripts[i++] = new ConversationScript("아키쨩, 요즘 일은 잘 돼가?", null, false);
                scripts[i++] = new ConversationScript("네, 어머니.", null, true);
                scripts[i++] = new ConversationScript("어려운 일도 아니고, 자택근무인데요 뭐.", null, true);
                scripts[i++] = new ConversationScript("그건 그런데.. 나는 역시 너한테 일 시키는 게 마음이 아파...", "sad", false);
                scripts[i++] = new ConversationScript("어머니.. 저희 서로 미안해하지 않기로 했잖아요...", "sad", true);
                scripts[i++] = new ConversationScript("어머니도 학교 다니면서 일하시는데, 저만 쉴 수도 없는 노릇이고요.", "sad", true);
                scripts[i++] = new ConversationScript("그리고 오히려 일 시작하고 좋은 점도 있는걸요?", null, true);
                scripts[i++] = new ConversationScript("그래..?", "hmm", false);
                scripts[i++] = new ConversationScript("네. 예전에는 집에서 어머니 오실 때까지 아무것도 안 하고 기다리기만 했잖아요?", "sad", true);
                scripts[i++] = new ConversationScript("그때 얼마나 심심했는데요.", "wily", true);
                scripts[i++] = new ConversationScript("미안해...", "crying", false);
                scripts[i++] = new ConversationScript("아이참.. 이래도 미안 저래도 미안.. 그만 미안해하세요!", "sad", true);
                scripts[i++] = new ConversationScript("응..", "sad", false);
                scripts[i++] = new ConversationScript("휴...", "sad", true);
                scripts[i++] = new ConversationScript("어머니, 운동은 꾸준히 하고 계신 거죠?", "wily", true);
                scripts[i++] = new ConversationScript("어.. 갑자기???", "surprised", false);
                scripts[i++] = new ConversationScript("자 시작해요.", "playful", true);
                scripts[i] = new ConversationScript("어???", "surprised", false);
                break;
            case 18:
                scripts[i++] = new ConversationScript("늦었잖아 darling~! 오늘은 뭐 할 거야? 난 뭐든 좋아!", true);
                scripts[i++] = new ConversationScript("헤헤...", "sneer", false);
                scripts[i++] = new ConversationScript("아키쨩, 얘 귀엽지 않아? 무료로 주는 캐릭터인데 별도 4 개다?", "heart", false);
                scripts[i++] = new ConversationScript("아니요. 하나도 안 귀여워요.", "sad", true);
                scripts[i++] = new ConversationScript("그.. 그래?", "confused", false);
                scripts[i++] = new ConversationScript("..귀여운데..", "hmm", false);
                scripts[i++] = new ConversationScript("나는 이 달링이라는 말 너무 좋은 것 같아!", null, false);
                scripts[i++] = new ConversationScript("뭔가 애정이 느껴지지 않아?", "heart", false);
                scripts[i++] = new ConversationScript("..달링.", "shy", true);
                scripts[i++] = new ConversationScript("응? 뭐라고 했어?", null, false);
                scripts[i++] = new ConversationScript("..게임 그만하시라고요!", "shy", true);
                scripts[i++] = new ConversationScript("요즘 매일 휴대폰으로 인형 모으는 게임만 쳐다보시고..", "sad", true);
                scripts[i++] = new ConversationScript("인형이 그렇게 좋으시면 인형이랑 사세요!", "shout", true);
                scripts[i++] = new ConversationScript("아니 아키쨩 그게 아니고..", "surprised", false);
                scripts[i++] = new ConversationScript("아니긴 뭐가요!", "sad", true);
                scripts[i++] = new ConversationScript("..미안해.", "sad", false);
                scripts[i++] = new ConversationScript("지울게...", "crying", false);
                scripts[i++] = new ConversationScript("그리고...", "shy", true);
                scripts[i++] = new ConversationScript("응..?", "crying", false);
                scripts[i++] = new ConversationScript("..그리고 저런 애들이 무서운 애들이에요! 겉모습에 속으시면 안 돼요!", "shy", true);
                scripts[i] = new ConversationScript("응응...", "sad", false);
                break;
            case 19:
                scripts[i++] = new ConversationScript("어머니 저 왔어요~", null, true);
                scripts[i++] = new ConversationScript("아키쨩!!! 보고 싶었어~!", "heart", false);
                scripts[i++] = new ConversationScript("내가 얼마나 기다렸는지 알아?", null, false);
                scripts[i++] = new ConversationScript("..어머니.", "confused", true);
                scripts[i++] = new ConversationScript("저 나간 지 한 시간도 안 지났는데요..?", "wily", true);
                scripts[i++] = new ConversationScript("그치만.. 그치만 나 아키쨩 없이는 10 분도 버티기 힘든걸!!!", "crying", false);
                scripts[i++] = new ConversationScript("학교는 어떻게 다니시는 거예요...", "sad", true);
                scripts[i++] = new ConversationScript("강의시간에는 몰래몰래 아키쨩 사진을 꺼내보고 있어!", null, false);
                scripts[i++] = new ConversationScript(".....", "sad", true);
                scripts[i++] = new ConversationScript(".....", "surprised", false);
                scripts[i++] = new ConversationScript("수업에 집중하셔야 돼요...", "sad", true);
                scripts[i++] = new ConversationScript("응응...", "sad", false);
                scripts[i++] = new ConversationScript("뭐 아무튼.. 저녁은 연어 덮밥이에요. 연어가 싸더라고요.", null, true);
                scripts[i++] = new ConversationScript("와 연어 덮밥! 맛있겠다!", null, false);
                scripts[i++] = new ConversationScript("그리고 오랜만에 아이스크림도 사 왔어요.", "playful", true);
                scripts[i++] = new ConversationScript("와!!! 녹차X루!!!!!", "heart", false);
                scripts[i++] = new ConversationScript("저보다 녹차X루를 더 반기시는 것 같은데요?", "sneer", true);
                scripts[i++] = new ConversationScript("에이 설마~ 아키쨩이 최고지!", null, false);
                scripts[i++] = new ConversationScript("사랑해, 아키쨩♡", "heart", false);
                scripts[i++] = new ConversationScript("저도요.", "glad", true);
                scripts[i++] = new ConversationScript("아이스크림은 꼭 식후에 드셔야 해요?", "playful", true);
                scripts[i] = new ConversationScript("응응!", "heart", false);
                break;
            case 20:
                scripts[i++] = new ConversationScript("휴.. 아키쨩, 나 양치 하고올게.. 너무 졸려...", "sad", false);
                scripts[i++] = new ConversationScript("네, 어머니.", null, true);
                scripts[i++] = new ConversationScript("....?", "confused", true);
                scripts[i++] = new ConversationScript("어.. 어머니?! 그거 클렌징폼이에요!", "surprised", true);
                scripts[i++] = new ConversationScript("어..?", "surprised", false);
                scripts[i++] = new ConversationScript("어.. 내가 왜 칫솔에 이걸...", "surprised", false);
                scripts[i++] = new ConversationScript("민트가 얼마나 싫으셨길래 이젠 치약까지..", "sneer", true);
                scripts[i++] = new ConversationScript("실수라구...", "shy", false);
                scripts[i++] = new ConversationScript("..근데 클렌징폼에 알갱이.. 씹으면 알밥 느낌 나지 않을까?", "hmm", false);
                scripts[i++] = new ConversationScript("의외로 맛있을지도..", null, false);
                scripts[i++] = new ConversationScript("입에 넣기만 해보세요...", "sad", true);
                scripts[i++] = new ConversationScript("아키쨩 무서워.. 장난인데..흑흑", "crying", false);
                scripts[i++] = new ConversationScript(".....", "sad", true);
                scripts[i++] = new ConversationScript(">_<", "heart", false);
                scripts[i++] = new ConversationScript(".....", "shy", true);
                scripts[i++] = new ConversationScript("아무튼 많이 피곤하신가봐요.. 오늘은 공부 그만하고 주무세요.", "shy", true);
                scripts[i] = new ConversationScript("응응.", null, false);
                break;
            case 21:
                scripts[i++] = new ConversationScript("어머니.", "wily", true);
                scripts[i++] = new ConversationScript("세트하신 카드, 블러핑이죠?", "sneer", true);
                scripts[i++] = new ConversationScript("무.. 무슨 소리야! 아직 패가 5장이나 있는데..", "surprised", false);
                scripts[i++] = new ConversationScript("다음에 한 번에 끝내려고 아끼는 거라구!", "surprised", false);
                scripts[i++] = new ConversationScript("그래요? 그럼 공격해도 괜찮으시죠?", "playful", true);
                scripts[i++] = new ConversationScript(".....", "sad", false);
                scripts[i++] = new ConversationScript("할게요?", "wily", true);
                scripts[i++] = new ConversationScript("..한 번만 봐주면 안 될까, 아키쨩?", "heart", false);
                scripts[i++] = new ConversationScript("안 돼요.", "playful", true);
                scripts[i++] = new ConversationScript("힝...", "crying", false);
                scripts[i] = new ConversationScript(true);
                break;
            case 22:
                scripts[i++] = new ConversationScript("아키쨩 나 왔어..", "sad", false);
                scripts[i++] = new ConversationScript("오셨어요, 어머니?", null, true);
                scripts[i++] = new ConversationScript("오늘따라 기운이 없어 보이세요.. 무슨 일 있으셨나요?", "sad", true);
                scripts[i++] = new ConversationScript("아, 응.. 친구한테 조금 안 좋은 일이 생겨서..", "sad", false);
                scripts[i++] = new ConversationScript("그러셨군요...", "sad", true);
                scripts[i++] = new ConversationScript("아무렇지 않은 척해도 힘들어하는 게 눈에 보이니까.. 나도 마음이 편치 않더라구...", "sad", false);
                scripts[i++] = new ConversationScript("아키쨩, 내 주변 사람들은.. 자꾸 힘든 일이 생기는 것 같아.", "sad", false);
                scripts[i++] = new ConversationScript("모두가 행복할 수는 없는 걸까?", "sad", false);
                scripts[i++] = new ConversationScript("어머니...", "sad", true);
                scripts[i++] = new ConversationScript("..누구에게나 고민은 있어요.", null, true);
                scripts[i++] = new ConversationScript("그런데도 어머니 주변 사람들이 힘들다고 털어놓는다는 건 그만큼..", null, true);
                scripts[i++] = new ConversationScript("어머니가 그분들께 의지가 된다는 뜻 아닐까요?", null, true);
                scripts[i++] = new ConversationScript("아무에게나 자기 고민을 말하지는 않으니까요.. 긍정적으로 생각하셨으면 좋겠어요.", "sad", true);
                scripts[i++] = new ConversationScript("응응.. 고마워.", "hmm", false);
                scripts[i++] = new ConversationScript("근데 나.. 이야기를 들으면서도 어떻게 위로해줘야 할지 잘 모르겠어서..", "sad", false);
                scripts[i++] = new ConversationScript("힘든 사람한테 막연하게 힘내라고만 할 수도 없으니까...", "hmm", false);
                scripts[i++] = new ConversationScript("어머니가 같이 있어주시는 것만으로도 위로가 될 거예요.", null, true);
                scripts[i++] = new ConversationScript("저도 어머니께 항상 위로받고 있고요.", null, true);
                scripts[i++] = new ConversationScript("그래..? 그렇다면 다행이지만...", "sad", false);
                scripts[i++] = new ConversationScript("그래도.. 다들 행복했으면 좋겠다.", "sad", false);
                scripts[i++] = new ConversationScript(".....", "hmm", false);
                scripts[i] = new ConversationScript("지금 행복하신가요?", "sad", false);
                break;
            case 23:
                scripts[i++] = new ConversationScript("어머니 어머니! 드디어 찾았어요!", "surprised", true);
                scripts[i++] = new ConversationScript("응응? 뭐를?", null, false);
                scripts[i++] = new ConversationScript("저희 이사할 때 잃어버렸던 손톱깎이요!", "confused", true);
                scripts[i++] = new ConversationScript("엥??? 벌써 몇 달이 지났는데 이제 와서?", "surprised", false);
                scripts[i++] = new ConversationScript("네! 저도 깜짝 놀랐다니까요?", "confused", true);
                scripts[i++] = new ConversationScript("어디서 나왔는지 맞춰보실래요?", "playful", true);
                scripts[i++] = new ConversationScript("음.. 아무리 찾아도 안 나왔으니까..", "hmm", false);
                scripts[i++] = new ConversationScript("화장실?", null, false);
                scripts[i++] = new ConversationScript("네..?", "confused", true);
                scripts[i++] = new ConversationScript("아니 뭔가.. 뜬금없는 곳에서 나왔을 것 같아서...", "surprised", false);
                scripts[i++] = new ConversationScript("확실히 뜬금없긴 했지만.. 화장실은 아니에요.", "sneer", true);
                scripts[i++] = new ConversationScript("그럼 어디서 나왔는데?", null, false);
                scripts[i++] = new ConversationScript("어머니께서 안 신으시는 양말에서요! 오늘 옷장 정리했거든요.", "playful", true);
                scripts[i++] = new ConversationScript("손톱깎이가.. 양말에서..?", "confused", false);
                scripts[i++] = new ConversationScript("어쩌다가 들어간 거지?!", "surprised", false);
                scripts[i++] = new ConversationScript("그러게요..?", "confused", true);
                scripts[i++] = new ConversationScript("없어진 줄 알고 결국 새로 샀었는데.. 조금 아까워요.", "sad", true);
                scripts[i++] = new ConversationScript("그래도 찾아서 다행이야!", null, false);
                scripts[i] = new ConversationScript("네.", "glad", true);
                break;
            case 24:
                scripts[i++] = new ConversationScript("어머니, 이번 학기에 테니스 수업도 듣고 계셨어요?", "confused", true);
                scripts[i++] = new ConversationScript("아니? 갑자기 테니스는 왜?", null, false);
                scripts[i++] = new ConversationScript("이 옷이요.. 그럼 코스프레용.. 인가?", "sad", true);
                scripts[i++] = new ConversationScript("아 그거? 고등학생 때 치어리더 동아리 하면서 입던 건데!", null, false);
                scripts[i++] = new ConversationScript("오랜만이야, 알렉산더!", "heart", false);
                scripts[i++] = new ConversationScript("알렉산더..?", "confused", true);
                scripts[i++] = new ConversationScript("아.. 애칭이야..", "surprised", false);
                scripts[i++] = new ConversationScript("아키쨩이 한번 입어볼래?", "heart", false);
                scripts[i++] = new ConversationScript("네에에?", "surprised", true);
                scripts[i] = new ConversationScript(true);
                break;
            case 25:
                scripts[i++] = new ConversationScript("음.. 미술관도 가고.. 하고 싶었던 공부도...", null, false);
                scripts[i++] = new ConversationScript("어머니, 뭐 하고 계세요?", null, true);
                scripts[i++] = new ConversationScript("응응? 방학 때 할 일들을 적어놓고 있어. 아직 이르긴 하지만.", null, false);
                scripts[i++] = new ConversationScript("저도 한 번 봐도 될까요?", null, true);
                scripts[i++] = new ConversationScript("응응, 자!", null, false);
                scripts[i++] = new ConversationScript("..미술관 가기, 양 꼬치 먹기, 그림 그리기, 유니티 공부하기.", "playful", true);
                scripts[i++] = new ConversationScript("오늘도 사랑스럽개 정주행.", "playful", true);
                scripts[i++] = new ConversationScript("영화 * 4 ?", "confused", true);
                scripts[i++] = new ConversationScript("여기 적힌 * 4는 뭐예요?", null, true);
                scripts[i++] = new ConversationScript("영화 보러 가자는 말을 딱 잘라 거절할 수가 없어서.. 방학하면 보자고 그랬거든...", "confused", false);
                scripts[i++] = new ConversationScript("계속 그러다 보니까 약속이 조금 쌓였네.. 하하..", "surprised", false);
                scripts[i++] = new ConversationScript("어머니.. 지키지 못 할 약속은 하시면 안 돼요.", "sad", true);
                scripts[i++] = new ConversationScript("나도 안다구.. 다 지킬 거니까..", "sad", false);
                scripts[i++] = new ConversationScript("미술관은 누구랑 가실 건데요?", "sad", true);
                scripts[i++] = new ConversationScript("응응? 당연히 나머지는 전부 아키쨩이랑..", "surprised", false);
                scripts[i++] = new ConversationScript(".....", "shy", true);
                scripts[i++] = new ConversationScript("왜 그래.. 싫어..?", "crying", false);
                scripts[i++] = new ConversationScript("아니요. 정답이에요.", "wily", true);
                scripts[i] = new ConversationScript("아키쨩은 가끔 잘 모르겠어..", "hmm", false);
                break;
            case 26:
                scripts[i++] = new ConversationScript("아키쨩 나 왔어~", null, false);
                scripts[i++] = new ConversationScript("다녀오셨어요, 어머니? 문자 보셨어요?", "glad", true);
                scripts[i++] = new ConversationScript("어.. 아니? 급한 일이야?", "confused", false);
                scripts[i++] = new ConversationScript("아니요 그런 건 아닌데, 저희 집 근처에 아쿠아리움이 생긴대요!", "glad", true);
                scripts[i++] = new ConversationScript("아쿠아리움???", "heart", false);
                scripts[i++] = new ConversationScript("네네! 분명 상어도 있겠죠?", "lovely", true);
                scripts[i++] = new ConversationScript("와! 상어!!!", null, false);
                scripts[i++] = new ConversationScript("아기- 상어 뚜 루루 뚜루~ 귀여운- 뚜루루 뚜루~ 바닷속- 뚜루루뚜루~ ", null, false);
                scripts[i++] = new ConversationScript("아기 상어!", "heart", false);
                scripts[i++] = new ConversationScript("어머니..", "shy", true);
                scripts[i++] = new ConversationScript("1.5 배속으로 들으면 더 신나!", "heart", false);
                scripts[i++] = new ConversationScript(".....", "confused", true);
                scripts[i++] = new ConversationScript(".....", "shy", false);
                scripts[i++] = new ConversationScript("..나는 거북이 있었으면 좋겠다!", "surprised", false);
                scripts[i++] = new ConversationScript("거북이..?", "confused", true);
                scripts[i++] = new ConversationScript("아! 그러고 보니 만화에 나오는 거북이를 보시곤, 어머니도 키우고 싶어 하셨죠!", "surprised", true);
                scripts[i++] = new ConversationScript("응응! 돌연변이 닌자 거북~ 돌연변이 닌자 거북~", null, false);
                scripts[i++] = new ConversationScript("..그 만화 아니에요.", "shy", true);
                scripts[i++] = new ConversationScript("..이거 아냐?", "shy", false);
                scripts[i++] = new ConversationScript("밴드 동아리 하는 만화요! 거북이 이름이 톤쨩..이었나?", null, true);
                scripts[i++] = new ConversationScript("아! 맞아맞아 톤쨩!!!", "heart", false);
                scripts[i++] = new ConversationScript("그리고 그때 어디서 기타를 빌려오셔선.. 손가락 아프다고 며칠 만에 그만두셨죠.", "playful", true);
                scripts[i++] = new ConversationScript("무.. 무슨 소리야 한 달은 열심히 쳤다구!", "surprised", false);
                scripts[i++] = new ConversationScript("그랬었나요?", "sneer", true);
                scripts[i++] = new ConversationScript("앞머리에 노란 핀 대신 탈색 브릿지 하시고..", "sneer", true);
                scripts[i] = new ConversationScript("아아아아아! 역사 왜곡이야, 아키쨩!", "surprised", false);
                break;
            case 27:
                scripts[i++] = new ConversationScript("아키쨩 아키쨩!", null, false);
                scripts[i++] = new ConversationScript("네네?", null, true);
                scripts[i++] = new ConversationScript("나..", "hmm", false);
                scripts[i++] = new ConversationScript("시원한 게 마시고 싶어!!!", "heart", false);
                scripts[i++] = new ConversationScript("그럼 잠시 쉴 겸 음료수라도 사러 갈까요?", "glad", true);
                scripts[i++] = new ConversationScript("응응! 같이 가자!", null, false);
                scripts[i] = new ConversationScript(true);
                break;
            case 28:
                scripts[i++] = new ConversationScript("아키쨩 나 왔어~", null, false);
                scripts[i++] = new ConversationScript("늦으셨네요, 어머니?", "sad", true);
                scripts[i++] = new ConversationScript("아, 응응. 지도 교수님이랑 상담하고 왔어.", null, false);
                scripts[i++] = new ConversationScript("흐응..", "sad", true);
                scripts[i++] = new ConversationScript("저한테는 왜 말씀 안 해주셨어요?", "sad", true);
                scripts[i++] = new ConversationScript("오늘 갑자기 생긴 일정이라서.. 미리 말을 못 했네.", "sad", false);
                scripts[i++] = new ConversationScript("그래도 연락은 해주셔야죠..", "sad", true);
                scripts[i++] = new ConversationScript("미안해..", "sad", false);
                scripts[i++] = new ConversationScript("그보다 아키쨩 이거 봐봐! 나 오늘 교수님한테 칭찬받았다?", null, false);
                scripts[i++] = new ConversationScript(".....", "sad", true);
                scripts[i++] = new ConversationScript(".....", "surprised", false);
                scripts[i++] = new ConversationScript("잘하셨어요.", "sneer", true);
                scripts[i++] = new ConversationScript("응.. 고마워.", "sad", false);
                scripts[i++] = new ConversationScript("어머니.", null, true);
                scripts[i++] = new ConversationScript("응응..?", null, false);
                scripts[i++] = new ConversationScript("아무리 바빠도 늦을 때 연락은 꼭 하셔야 돼요.", "sad", true);
                scripts[i++] = new ConversationScript(".....", "sad", false);
                scripts[i++] = new ConversationScript("밥은 드셨어요?", null, true);
                scripts[i++] = new ConversationScript("아니 아직..", "sad", false);
                scripts[i++] = new ConversationScript("우선 씻고 오세요. 밥 차려놓을게요.", "glad", true);
                scripts[i] = new ConversationScript("응...", "sad", false);
                break;
            case 29:
                scripts[i++] = new ConversationScript("아키쨩..", "sad", false);
                scripts[i++] = new ConversationScript("네 어머니.", null, true);
                scripts[i++] = new ConversationScript("저기...", "sad", false);
                scripts[i++] = new ConversationScript("말씀하세요.", "playful", true);
                scripts[i++] = new ConversationScript("나.. 알바 늘리고 싶은데.. 안 될까?", "sad", false);
                scripts[i++] = new ConversationScript("아르바이트를 또요..?", "confused", true);
                scripts[i++] = new ConversationScript("교내 근로도 하시잖아요.. 지금도 바쁘신데 왜..", "sad", true);
                scripts[i++] = new ConversationScript("역시 여윳돈을 좀 모아두는 게 좋지 않을까 해서..", "sad", false);
                scripts[i++] = new ConversationScript("..공부도 하셔야죠.", "sad", true);
                scripts[i++] = new ConversationScript("공부는 지금도 잘하고 있잖아.. 장학금도 계속 받도록 할 테니까.", "sad", false);
                scripts[i++] = new ConversationScript("여가시간을 조금이라도 쪼개서 일하고 싶은데...", "sad", false);
                scripts[i++] = new ConversationScript("안 돼요.. 저는 어머니께서 학업에 집중하셨으면 좋겠어요.", "sad", true);
                scripts[i++] = new ConversationScript("그리고 어차피 곧 졸업하시잖아요?", null, true);
                scripts[i++] = new ConversationScript("그치만.. 취업이 바로 된다는 보장도 없고.. 또..", "sad", false);
                scripts[i++] = new ConversationScript("어머니 마음은 충분히 이해해요. 저희가 경제적으로 여유롭지는 않으니까요.", "sad", true);
                scripts[i++] = new ConversationScript("그래도.. 못 버틸 정도는 아니잖아요?", "sad", true);
                scripts[i++] = new ConversationScript("그리고 당장에 생활이 어려워진다고 해도, 제가 일을 더 해야죠.. 왜 어머니 혼자 짊어지려고 하세요?", "sad", true);
                scripts[i++] = new ConversationScript("아키쨩은 내 딸이니까.. 힘들게 하고 싶지 않은걸..", "sad", false);
                scripts[i++] = new ConversationScript("어머니도.. 제 어머니니까.", "sad", true);
                scripts[i++] = new ConversationScript("저도 어머니를 힘들게 하고 싶지 않아요.", "sad", true);
                scripts[i] = new ConversationScript("응...", "sad", false);
                break;
            case 30:
                scripts[i++] = new ConversationScript("아키쨩, 나 왔어~", null, false);
                scripts[i++] = new ConversationScript("어머니.", "sad", true);
                scripts[i++] = new ConversationScript("응응..?", "surprised", false);
                scripts[i++] = new ConversationScript("오늘 일찍 오시는 날 아니에요?", "sad", true);
                scripts[i++] = new ConversationScript("그..렇지?", "surprised", false);
                scripts[i++] = new ConversationScript("그런데 왜 또 늦으신 거예요? 걱정했잖아요..", "sad", true);
                scripts[i++] = new ConversationScript("미안.. 일이 좀 있어서...", "sad", false);
                scripts[i++] = new ConversationScript("설마..", "sad", true);
                scripts[i++] = new ConversationScript("만나는 사람 생기셨어요?", "sad", true);
                scripts[i++] = new ConversationScript("에이 그럴 리가.. 그냥 회의가 길어졌어..", "sneer", false);
                scripts[i++] = new ConversationScript("이유가 어찌 됐든 늦으면 늦는다고 말을 해주셨어야죠.", "sad", true);
                scripts[i++] = new ConversationScript("연락도 안 되고..", "sad", true);
                scripts[i++] = new ConversationScript("예전엔 꼬박꼬박 문자 남기셨잖아요.. 요즘 왜 그러세요?", "sad", true);
                scripts[i++] = new ConversationScript("저기 아키쨩..", "sad", false);
                scripts[i++] = new ConversationScript("정말 미안한데 나 지금 너무 피곤해.. 우리 나중에 얘기하면 안 될까..?", "sad", false);
                scripts[i++] = new ConversationScript("안 돼요!", "sad", true);
                scripts[i++] = new ConversationScript(".....", "crying", false);
                scripts[i++] = new ConversationScript("자꾸 이런 식으로 그냥 넘어가려고 하시잖아요. 애초에..", "sad", true);
                scripts[i++] = new ConversationScript("아키쨩.. 미워...", "crying", false);
                scripts[i++] = new ConversationScript("어, 어머니! 어디 가세요!", "surprised", true, true, false);
                scripts[i] = new ConversationScript(true);
                break;
        }

        if ((MainActivity.mainStoryProgress == 0) || inScheduleClosing) {
            if (MainActivity.mainStoryProgress <= 30) {
                SharedPreferences prefs = context.getSharedPreferences("prefs", MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();

                // about story
                MainActivity.mainStoryProgress++;
                MainActivity.storyProgress++;
                MainActivity.setStoryProgressBar();
                editor.putInt("mainStoryProgress", MainActivity.mainStoryProgress);
                editor.apply();
                LayoutStory.storyList.get(storyRequest).unlocked = true;
                PrefsController.setStoryListPrefs(context, "storyList", LayoutStory.storyList);

                // about costume
                switch (storyRequest) {
                    case 4:
                    case 9:
                    case 14:
                    case 19:
                    case 24:
                    case 30:
                        MainActivity.costumeProgress++;
                        MainActivity.setCostumeProgressBar();
                        getNewCostume = true;
                        break;
                }
                switch (storyRequest) {
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
                PrefsController.setCostumeListPrefs(context, "costumeList", LayoutCostume.costumeList);

                inScheduleClosing = false;
            }
        }

        preSpeaker = -1;
        ConversationControl.addConversationInstant(scripts);
        startInstantConversation();
    }

    private void startStoryContinue() {
        SharedPreferences prefs = context.getSharedPreferences("prefs", MODE_PRIVATE);
        String nickName = prefs.getString("nickName", "--");
        ConversationScript[] scripts = new ConversationScript[40];
        int i = 0;
        switch (storyRequestContinue) {
            case 0:
                scripts[i++] = new ConversationScript("아키쨩! 나 왔어~", "heart", false);
                scripts[i++] = new ConversationScript("오셨어요, 어머니?", "glad", true);
                scripts[i++] = new ConversationScript("어..? 손님도 한 분 같이 오셨네요?", "confused", true);
                scripts[i++] = new ConversationScript("응응, 아키쨩 손님이야! 지난번에 말했던 그 일로 왔어.", null, false);
                scripts[i++] = new ConversationScript("아!", "surprised", true);
                scripts[i++] = new ConversationScript("제가 일정 관리를 도와드릴 분이시군요?", null, true);
                scripts[i++] = new ConversationScript("응! 맞아!", null, false);
                scripts[i++] = new ConversationScript("아키쨩 아키쨩! 나는 먼저 들어가 볼 테니 설명을 부탁할게!", null, false);
                scripts[i++] = new ConversationScript("오늘 완전 중요한 경기가 있는 날이거든!", "heart", false);
                scripts[i++] = new ConversationScript("네, 어머니. 쉬고 계세요.", "glad", true);
                scripts[i++] = new ConversationScript("응응!", null, false);
                scripts[i++] = new ConversationScript(".....", null, true, true, false);
                scripts[i++] = new ConversationScript("..혹시 성함이 어떻게 되시나요?", "glad", true, true, false);
                // dialog
                scripts[i++] = new ConversationScript(String.format("반가워요, %s님!", nickName), "glad", true, true, false);
                scripts[i++] = new ConversationScript("그럼 바로 저의 사용 방법을 알려드릴게요!", null, true, true, false);
                scripts[i++] = new ConversationScript("우선 앱 서랍의 일정 란에서 일정을 관리하실 수 있고, 오후 8시 이후로 저를 찾아오시면 그날의 일정을 마감하실 수 있어요.", null, true, true, false);
                scripts[i++] = new ConversationScript("일정을 마감하시면 완료된 일정들이 자동으로 삭제된 뒤에, 그 수가 하나 이상일 경우 스토리가 자동으로 재생돼요.", null, true, true, false);
                scripts[i++] = new ConversationScript("스토리라고 해봤자 저랑 어머니가 짧게 잡담을 나눌 뿐이지만요..", "wily", true, true, false);
                scripts[i++] = new ConversationScript("흠흠..!", "shy", true, true, false);
                scripts[i++] = new ConversationScript("열람했던 스토리들은 스토리 란에서 다시 확인 가능하세요.", null, true, true, false);
                scripts[i++] = new ConversationScript("특정 조건을 달성하거나 스토리를 진행하다 보면 코스튬을 획득하실 수 있으니, 매일 일정을 마감하셔야 해요. 아시겠죠?", "playful", true, true, false);
                scripts[i++] = new ConversationScript("아, 혹여나 제가 자고 있을 때 찾아오시면 일정을 완료해도 카운트가 되지 않는 등 기능에 제약이 생기게 돼요.", null, true, true, false);
                scripts[i++] = new ConversationScript("그러니 날이 바뀌기 전에 용무를 끝내시길 권장해드릴게요.", "playful", true, true, false);
                scripts[i++] = new ConversationScript("음, 또..", "confused", true, true, false);
                scripts[i++] = new ConversationScript("맞다!", null, true, true, false);
                scripts[i++] = new ConversationScript("마지막으로 상호작용 버튼에 대해 설명해드릴게요!", "glad", true, true, false);
                scripts[i++] = new ConversationScript(String.format("%s님이 누르고 계신 상호작용 버튼은 상황에 따라 아이콘이 다르게 표시될 거예요.", nickName), null, true, true, false);
                scripts[i++] = new ConversationScript("기본적으로는 저랑 대화를 시작하거나 진행하는 데에 쓰이지만, 다른 장소에서 저를 찾아오기 위해 사용하실 수도 있어요.", null, true, true, false);
                scripts[i++] = new ConversationScript("그리고 오후 8시 이후에 클릭하시면 추가로 마감 버튼이 표시되니, 일정을 마감하고 싶으실 때는 당황하지 말고 저를 불러주세요?", "glad", true, true, false);
                scripts[i++] = new ConversationScript("..설명은 이쯤에서 끝내도록 할까요?", null, true, true, false);
                scripts[i] = new ConversationScript(String.format("그럼 좋은 하루 보내세요, %s님.", nickName), "glad", true, true, false);
                storyRequestContinue = -1;
                break;
            case 9:
                tmpStoryCostume = true;
                scripts[i++] = new ConversationScript("좋네요! 뭔가 어머니 비서가 된 느낌?", "glad", true);
                scripts[i++] = new ConversationScript("와 비서 아키쨩!", "heart", false);
                scripts[i++] = new ConversationScript("흠흠 그럼 알키오네 씨, 남은 스케줄 브리핑해줄래?", "playful", false);
                scripts[i++] = new ConversationScript("네. 오늘 남은 일정이..", null, true);
                scripts[i++] = new ConversationScript("11시까지 공부, 그다음 취침이네요!", "glad", true);
                scripts[i++] = new ConversationScript("어..?", "surprised", false);
                scripts[i++] = new ConversationScript("감시할 거예요.", "wily", true);
                scripts[i++] = new ConversationScript("너무해!!!", "surprised", false);
                scripts[i] = new ConversationScript("그게 제 일인 걸요?", "playful", true);
                storyRequestContinue = -1;
                break;
            case 21:
                scripts[i++] = new ConversationScript("벌써 5:0이네요, 어머니.", "glad", true);
                scripts[i++] = new ConversationScript("왜 못 이기는 거야...", "hmm", false);
                scripts[i++] = new ConversationScript("아키쨩 혹시 카드게임의 천재..?", null, false);
                scripts[i++] = new ConversationScript("글쎄요?", null, true);
                scripts[i++] = new ConversationScript("꼭 카드가 아니더라도, 다른 게임들도 다 비슷한 결과였잖아요?", "wily", true);
                scripts[i++] = new ConversationScript("맞아, 예전부터 그랬었지...", "sad", false);
                scripts[i++] = new ConversationScript("그냥 내가 보드게임에 약한 걸까?", null, false);
                scripts[i++] = new ConversationScript("그렇다고 할 수도 있겠네요. 어머니는 표정에 다 드러나니까요.", null, true);
                scripts[i++] = new ConversationScript("응..? 뭐가?", "confused", false);
                scripts[i++] = new ConversationScript("감정이요.", "glad", true);
                scripts[i++] = new ConversationScript("어머니, 아까도 계속 불안한 표정이셨는걸요?", "glad", true);
                scripts[i++] = new ConversationScript("에이 설마 그럴 리가..", "surprised", false);
                scripts[i++] = new ConversationScript("난 언제나 도도하고 쿨한 포커페이스 신비주의 이미지인데?", "surprised", false);
                scripts[i++] = new ConversationScript("..아무도 그렇게 생각 안 해요.", "confused", true);
                scripts[i] = new ConversationScript(".....", "sad", false);
                storyRequestContinue = -1;
                break;
            case 24:
                tmpStoryCostume = true;
                scripts[i++] = new ConversationScript("역시 귀여워!!!", "heart", false);
                scripts[i++] = new ConversationScript("부끄럽네요.. 짧고.. 조이고...", "shy", true);
                scripts[i++] = new ConversationScript("음.. 아무래도 움직이기 편해야 하니까?", "surprised", false);
                scripts[i++] = new ConversationScript("그나저나 이젠 다 추억이네.. 이거 입을 땐 나도 어렸었는데..", "hmm", false);
                scripts[i++] = new ConversationScript("어머니.. 누가 들으면 30 대인 줄 알겠어요..", "wily", true);
                scripts[i++] = new ConversationScript("농담이지~", "playful", false);
                scripts[i++] = new ConversationScript("그래도 그때가 그립기도 해...", null, false);
                scripts[i++] = new ConversationScript("학교 행사 때 사진이라도 많이 찍어둘걸.", null, false);
                scripts[i++] = new ConversationScript("사진은 한 장도 안 남기신 거예요?", "sad", true);
                scripts[i++] = new ConversationScript("글쎄? 축제 영상은 남아있을 텐데.. 사진은 모르겠네.", null, false);
                scripts[i++] = new ConversationScript("영상이요?", "surprised", true);
                scripts[i++] = new ConversationScript("응응. 연습 열심히 했는데 기록이 하나도 없으면 아쉽잖아?", null, false);
                scripts[i++] = new ConversationScript("보여주세요!", "heart", true);
                scripts[i++] = new ConversationScript("어..? 좀 부끄러운데..", "surprised", false);
                scripts[i++] = new ConversationScript("빨리요!!!", "lovely", true);
                scripts[i] = new ConversationScript("으..응! 찾아볼게.", "surprised", false);
                storyRequestContinue = -1;
                break;
            case 27:
                scripts[i++] = new ConversationScript("그런데 어떤 거 드시려고요?", null, true);
                scripts[i++] = new ConversationScript("음.. 글쎄? 탄산음료는 입이 달아져서 별로구..", "hmm", false);
                scripts[i++] = new ConversationScript("그렇다고 이온음료를 마시기엔 뭔가 청량감이 부족해!", null, false);
                scripts[i++] = new ConversationScript("뭘 마시면 좋을까?", null, false);
                scripts[i++] = new ConversationScript("어머니는 술을 안 드시지만.. 그럴 땐 보통 맥주 아닐까요?", "wily", true);
                scripts[i++] = new ConversationScript("술은 맛없는걸...", "sad", false);
                scripts[i++] = new ConversationScript("그렇죠...", "sad", true);
                scripts[i++] = new ConversationScript("역시 커피가 최선일까?", null, false);
                scripts[i++] = new ConversationScript("스파클링 커피 말씀하시는 건 아니죠..?", "confused", true);
                scripts[i++] = new ConversationScript("앗 그건 좀..", "surprised", false);
                scripts[i++] = new ConversationScript("지난번에 나름대로 절충안이라고 생각해서 골랐다가..", "surprised", false);
                scripts[i++] = new ConversationScript("네...", "sad", true);
                scripts[i++] = new ConversationScript("그럼 다른 대체재가..", null, true);
                scripts[i++] = new ConversationScript("아! 탄산수는 어떠세요?", "surprised", true);
                scripts[i++] = new ConversationScript("맞다 탄산수!!! 왜 그 생각을 못 했지?", "heart", false);
                scripts[i] = new ConversationScript("아키쨩 역시 천재야!", null, false);
                storyRequestContinue = -1;
                break;
            case 30:
                switch (sceneCount) {
                    case 1:
                        scripts[i++] = new ConversationScript("(꽈당!)", true, true); // vibrate
                        scripts[i++] = new ConversationScript("어머니! 괜찮으세요?", "surprised", true);
                        scripts[i++] = new ConversationScript("아야...", "crying", false);
                        scripts[i++] = new ConversationScript("그러게 왜 갑자기 뛰쳐나가셔서.. 위험하잖아요...", "sad", true);
                        scripts[i++] = new ConversationScript(".....", "crying", false);
                        scripts[i++] = new ConversationScript("아! 어머니 휴대폰.. 화면이...", "surprised", true);
                        scripts[i++] = new ConversationScript(".....", "crying", false);
                        scripts[i++] = new ConversationScript("..휴대폰은 원래 그랬어.", "sad", false);
                        scripts[i++] = new ConversationScript("네?", "surprised", true);
                        scripts[i++] = new ConversationScript("언제부터요..?", "sad", true);
                        scripts[i++] = new ConversationScript("지난주.. 그때도 넘어져서..", "sad", false);
                        scripts[i++] = new ConversationScript("어머니.. 그럼 말씀해주셨어야죠..", "sad", true);
                        scripts[i++] = new ConversationScript("아키쨩은 새로 사라고 할 거니까...", "sad", false);
                        scripts[i++] = new ConversationScript("당연하죠! 내일 같이 가요.", "sad", true);
                        scripts[i++] = new ConversationScript("그치만!", "crying", false);
                        scripts[i++] = new ConversationScript("..우리 다른 돈 나갈 곳도 많고...", "crying", false);
                        scripts[i++] = new ConversationScript("나도 아키쨩도 지금 아껴 쓰고 있는데 갑자기 큰 지출은..", "crying", false);
                        scripts[i++] = new ConversationScript("아니요! 그래도 사야 해요! 서로 연락은 돼야죠!", "sad", true);
                        scripts[i++] = new ConversationScript(".....", "sad", false);
                        scripts[i++] = new ConversationScript("매일 안 된다 안 된다.. 왜 전부 안 된다고 해..?", "sad", false);
                        scripts[i++] = new ConversationScript("내 의사는 상관없는 거야? 왜..?", "crying", false);
                        scripts[i++] = new ConversationScript("왜...", "crying", false);
                        scripts[i++] = new ConversationScript("저는 그냥 어머니가 걱정돼서..", "sad", true);
                        scripts[i++] = new ConversationScript("연락.. 솔직히 안 해도 괜찮잖아..?", "sad", false);
                        scripts[i++] = new ConversationScript("내가 외박이라도 한 적 있어..? 이유 없이 늦기라도 해?", "sad", false);
                        //scripts[i++] = new ConversationScript("..언제 무슨 일이 일어날지 모르잖아요!", "sad", true);
                        scripts[i++] = new ConversationScript("학교 공부 하면서 교내 근로하고.. 알바하고..", "sad", false);
                        scripts[i++] = new ConversationScript("나는 나대로 힘든데 대체 아키쨩까지 왜 이러는 거야...", "crying", false);
                        //scripts[i++] = new ConversationScript("나도 성인이고 대학생인데.. 놀고 여행 다니면서 대학생활 즐기고 싶은데 참고 있잖아...", "crying", false);
                        scripts[i++] = new ConversationScript("남들만큼 쉬지도 못 하는데 너까지 매일 나한테 뭐라고 하고..", "crying", false);
                        scripts[i++] = new ConversationScript("집에서라도 편하게 있으면 안 돼..?", "crying", false);
                        scripts[i++] = new ConversationScript("이젠.. 힘들어...", "crying", false);
                        scripts[i++] = new ConversationScript(".....", "sad", true);
                        scripts[i++] = new ConversationScript("죄송해요, 어머니...", "sad", true);
                        scripts[i] = new ConversationScript(true);
                        break;
                    case 2:
                        scripts[i++] = new ConversationScript("(대충 샤워 끝내는 소리)", true); // noNamed
                        scripts[i++] = new ConversationScript(".....", "sad", false);
                        scripts[i++] = new ConversationScript("아키쨩..", "sad", false);
                        scripts[i++] = new ConversationScript("네.. 어머니...", "sad", true);
                        scripts[i++] = new ConversationScript("미안해.. 내가 오늘 많이 예민했나 봐...", "sad", false);
                        scripts[i++] = new ConversationScript("아니에요.. 오히려 제가 죄송해요..", "sad", true);
                        scripts[i++] = new ConversationScript("좀 더 어머니 입장을 고려했어야 하는데.. 앞으로 더 신경 쓰도록 할게요...", "sad", true);
                        scripts[i++] = new ConversationScript("응.. 고마워.", "sad", false);
                        scripts[i++] = new ConversationScript("그래도 급한 일이 생길 수 있으니까.. 휴대폰은 사셨으면 좋겠어요.", "sad", true);
                        scripts[i++] = new ConversationScript("응...", "sad", false);
                        scripts[i++] = new ConversationScript("대신 이제 연락은 따로 안 하셔도 돼요.. 집에 와서 왜 늦었는지만 말해주세요...", "sad", true);
                        scripts[i++] = new ConversationScript("응응, 알겠어.", "sad", false);
                        scripts[i] = new ConversationScript(true);
                        break;
                    case 3:
                        tmpStoryCostume = true;
                        scripts[i++] = new ConversationScript("어머니 저 부끄러워요...", "shy", true);
                        scripts[i++] = new ConversationScript("한 번만 찍자! 응? 아아~ 딱 한 번만!", null, false);
                        scripts[i++] = new ConversationScript(".....", "shy", true);
                        scripts[i++] = new ConversationScript("(찰칵)", true); // noNamed
                        scripts[i++] = new ConversationScript("역시 아키쨩.. 완전 귀여워!!!", "heart", false);
                        scripts[i++] = new ConversationScript("요즘 카메라 앱에는.. 이런 기능도 있네요.", "shy", true);
                        scripts[i++] = new ConversationScript("응응! 신기하다, 그치!", null, false);
                        scripts[i++] = new ConversationScript("그러네요.", "glad", true);
                        scripts[i++] = new ConversationScript("진짜 드레스 입은 모습도 언젠가 볼 수 있겠지?", null, false);
                        scripts[i++] = new ConversationScript("아키쨩이 떠난다고 생각하니까 벌써 슬퍼...", "sad", false);
                        scripts[i++] = new ConversationScript("글쎄요.. 결혼할 상대가 있을지 모르겠네요.", "sad", true);
                        scripts[i++] = new ConversationScript("흐응..?", "playful", false);
                        scripts[i++] = new ConversationScript("저기 있잖아? 매일 찾아오시는 누구씨!", "sneer", false);
                        scripts[i] = new ConversationScript("그치?", "heart", false);
                        storyRequestContinue = -1;
                        break;
                }
                break;
            case 31:
                tmpStoryCostume = true;
                scripts[i++] = new ConversationScript("오.. 오셨어요?", "shy", true);
                scripts[i++] = new ConversationScript("으악!!!", "surprised", false);
                scripts[i++] = new ConversationScript("너.. 너! 잠시 나가있어! 그리고 아키쨩 이리 와!", "surprised", false);
                scripts[i++] = new ConversationScript("왜.. 왜 그러세요, 어머니!", "confused", true);
                scripts[i++] = new ConversationScript("이 옷은 안 돼! 코스프레용이라구!!!", "surprised", false);
                scripts[i] = new ConversationScript("ㄴ..네?!", "surprised", true);
                storyRequestContinue = -1;
                break;
            case 32:
                tmpStoryCostume = true;
                scripts[i++] = new ConversationScript(String.format("어? %s님 오셨어요?", nickName), "surprised", true, true, false);
                scripts[i++] = new ConversationScript("많이 바쁘신가 봐요? 이 시간까지 일정 관리하시고..", "glad", true, true, false);
                scripts[i++] = new ConversationScript("..아무튼 편하게 볼일 보고 가세요. 저는 신경 안 쓰셔도 돼요.", "glad", true, true, false);
                scripts[i] = new ConversationScript("오히려 제가 잠옷 차림이라서.. 죄송해요.", "shy", true, true, false);
                storyRequestContinue = -1;
                break;
            case 33:
                tmpStoryCostume = true;
                scripts[i++] = new ConversationScript(".....", "shy", true, true, false);
                scripts[i++] = new ConversationScript("..이런 부끄러운 옷을..", "shy", true, true, false);
                scripts[i++] = new ConversationScript(String.format("%s님이 주신 거니까 입기야 하겠지만...", nickName), "shy", true, true, false);
                scripts[i++] = new ConversationScript("이건.. 너무...", "shy", true, true, false);
                scripts[i++] = new ConversationScript("아키쨩 나 왔어! 오늘 있지~", "heart", false);
                scripts[i++] = new ConversationScript(".....", "confused", false);
                scripts[i++] = new ConversationScript("..아키쨩?", "confused", false);
                scripts[i++] = new ConversationScript("어..어머니 이건..!", "confused", true);
                scripts[i++] = new ConversationScript("아키쨩! 아무리 힘들어도 사행성 게임은 안돼!!!", "surprised", false);
                scripts[i++] = new ConversationScript("네???", "surprised", true);
                scripts[i++] = new ConversationScript("ㄴ..나 그거 만화에서 봤어! 막 카드게임하고..", "surprised", false);
                scripts[i++] = new ConversationScript("아무튼 빨리 벗어!", "surprised", false);
                scripts[i++] = new ConversationScript("어..어머니! 지금은..", "surprised", true);
                scripts[i] = new ConversationScript("으악!", null, true, false, false);
                storyRequestContinue = -1;
                break;
            case 34:
                tmpStoryCostume = true;
                scripts[i++] = new ConversationScript(".....", "shy", true);
                scripts[i++] = new ConversationScript("조금 짧긴 하네요..", "shy", true);
                scripts[i++] = new ConversationScript("아키쨩..", "surprised", false);
                scripts[i++] = new ConversationScript("사진 찍어도 돼?", "heart", false);
                scripts[i++] = new ConversationScript(".....", "shy", true);
                scripts[i] = new ConversationScript("옷은 반품할게요...", "shy", true);
                storyRequestContinue = -1;
                break;
            case 35:
                scripts[i++] = new ConversationScript("서큐버스는 중세 유럽의 전설에서 남자들의 꿈속에 나타나 인간 여자로 둔갑하고..", null, false);
                scripts[i++] = new ConversationScript("..행위를...", null, false);
                scripts[i++] = new ConversationScript("네..?", null, true);
                scripts[i++] = new ConversationScript("날개는 허리 움직임을.. 보조..", null, false);
                scripts[i++] = new ConversationScript(".....", "shy", false);
                scripts[i++] = new ConversationScript("아키쨩한테 이런 건 아직 일러.", "shy", false);
                scripts[i++] = new ConversationScript("네??? 저도 성인이에요. 보여주세요!", "confused", true);
                scripts[i] = new ConversationScript("아.. 안 돼!", "surprised", false);
                storyRequestContinue = -1;
                break;
        }
        preSpeaker = -1;
        ConversationControl.addConversationInstant(scripts);
        startInstantConversation();
    }

    private void startSpecialStory() {
        ConversationScript[] scripts = new ConversationScript[40];
        int i = 0;
        switch (storyRequest) {
            case 31:
                scripts[i++] = new ConversationScript(".....", "sad", true);
                scripts[i++] = new ConversationScript(".....", "confused", false);
                scripts[i++] = new ConversationScript("아키쨩, 너무 초조해하지 마.. 바빠서 못 오는 걸 거야..", "surprised", false);
                scripts[i++] = new ConversationScript("그렇지만.. 벌써 사흘이 넘게 지났으니까요...", "sad", true);
                scripts[i++] = new ConversationScript("..역시 제가 뭔가 실수한 걸까요?", "sad", true);
                scripts[i++] = new ConversationScript("벌써 몇 번이나 확인했잖아.. 딱히 일정에 이상한 점은 없었는걸.", "sad", false);
                scripts[i++] = new ConversationScript("그럼.. 왜...", "sad", true);
                scripts[i++] = new ConversationScript("음.. 어디 아프기라도 한 걸까..?", "hmm", false);
                scripts[i++] = new ConversationScript("그런..가?", "sad", true);
                scripts[i++] = new ConversationScript("그렇구나! 분명 아프셨던 거예요!", "surprised", true);
                scripts[i++] = new ConversationScript("옆에 계셨으면 제가 조금이라도 도움이 됐을 텐데...", "sad", true);
                scripts[i++] = new ConversationScript("(똑똑)", true); // noNamed
                scripts[i++] = new ConversationScript("어?", "confused", false);
                scripts[i++] = new ConversationScript("오셨다!!!", "surprised", true);
                scripts[i++] = new ConversationScript("어.. 어?! 아키쨩!", "surprised", false, false, true); // visibility
                scripts[i++] = new ConversationScript("네?", null, true, false, true); // visibility
                scripts[i] = new ConversationScript(true);
                break;
            case 32:
                tmpStoryCostume = true;
                scripts[i++] = new ConversationScript("Zzz... Zzz...", "sleep", true, true, false); // facialExpression return null, so automatically face invisible
                scripts[i++] = new ConversationScript("(똑똑)", true, true, false); // noNamed
                scripts[i++] = new ConversationScript("흐아암..", "sleep", true, true, false);
                scripts[i++] = new ConversationScript("이 밤중에..?", "sad", true, true, false);
                scripts[i++] = new ConversationScript("음냐.. 아키쨩 사랑해.. ^p^", null, false, true, false);
                scripts[i++] = new ConversationScript(".....", "shy", true, true, false);
                scripts[i++] = new ConversationScript("누구세요?", null, true, true, false);
                scripts[i] = new ConversationScript(true);
                break;
        }

        if (!LayoutStory.storyList.get(storyRequest).unlocked) {
            // about story
            MainActivity.storyProgress++;
            MainActivity.setStoryProgressBar();

            LayoutStory.storyList.get(storyRequest).unlocked = true;
            PrefsController.setStoryListPrefs(context, "storyList", LayoutStory.storyList);

            // about costume
            MainActivity.costumeProgress++;
            MainActivity.setCostumeProgressBar();
            getNewCostume = true;

            // about costume
            switch (storyRequest) {
                case 31:
                    LayoutCostume.costumeList.get(7).unlocked = true;
                    break;
                case 32:
                    LayoutCostume.costumeList.get(8).unlocked = true;
                    break;
            }
            PrefsController.setCostumeListPrefs(context, "costumeList", LayoutCostume.costumeList);
        }

        preSpeaker = -1;
        ConversationControl.addConversationInstant(scripts);
        startInstantConversation();
    }

    private void startPayStory() {
        ConversationScript[] scripts = new ConversationScript[40];
        int i = 0;
        switch (storyRequest) {
            case 33:
                SharedPreferences prefs = context.getSharedPreferences("prefs", MODE_PRIVATE);
                String nickName = prefs.getString("nickName", "--");
                scripts[i++] = new ConversationScript("선물이요? 저한테 주시는 건가요?", null, true, true, false);
                scripts[i++] = new ConversationScript(String.format("감사합니다, %s님.", nickName), "glad", true, true, false);
                scripts[i] = new ConversationScript(true);
                break;
            case 34:
                scripts[i++] = new ConversationScript("어? 아키쨩, 택배 시켰어?", null, false);
                scripts[i++] = new ConversationScript("택배요..?", "confused", true);
                scripts[i++] = new ConversationScript("아 맞다! 체육복!", "surprised", true);
                scripts[i++] = new ConversationScript("세일하길래 하나 주문했어요. 어머니랑 같이 운동하려고요.", "wily", true);
                scripts[i++] = new ConversationScript("운동?!", "surprised", false);
                scripts[i++] = new ConversationScript("힘든 건 싫어..", "sad", false);
                scripts[i++] = new ConversationScript("게다가 나 이렇게 말랐는걸?", "sad", false);
                scripts[i++] = new ConversationScript("어머니.. 마른 건 좋은 게 아니에요..", "sad", true);
                scripts[i++] = new ConversationScript("지금 근육도 체력도 너무 부족하시다고요.", "sad", true);
                scripts[i++] = new ConversationScript("힝...", "crying", false);
                scripts[i++] = new ConversationScript("마침 옷도 왔으니 오늘부터 시작하죠!", null, true);
                scripts[i++] = new ConversationScript("오늘부터?! 난 아직 마음의 준비가!", "surprised", false);
                scripts[i++] = new ConversationScript("이제부터 준비하면 되죠! 자, 갈아입으세요!", "playful", true);
                scripts[i++] = new ConversationScript("..아, 아키쨩! 이거 바지가 너무 짧지 않아?", "surprised", false);
                scripts[i++] = new ConversationScript("어머니.. 그렇게 운동이 하기 싫으세요?", "sad", true);
                scripts[i++] = new ConversationScript("그게 아니라 진짜로 짧다구! 아키쨩이 입어봐!", "surprised", false);
                scripts[i] = new ConversationScript(true);
                break;
            case 35:
                scripts[i++] = new ConversationScript("어? 이거..", null, false);
                scripts[i++] = new ConversationScript("아키쨩이 할로윈 때 입었던 옷이다!", "heart", false);
                scripts[i++] = new ConversationScript("할로윈이요?", null, true);
                scripts[i++] = new ConversationScript("아! 그..", "confused", true);
                scripts[i++] = new ConversationScript("서큐버스...", "shy", true);
                scripts[i++] = new ConversationScript("응응!", null, false);
                scripts[i++] = new ConversationScript("역시.. 다시 봐도 부끄러운 디자인이네.", "shy", false);
                scripts[i++] = new ConversationScript("그러게요.. 그때는 어떻게 입고 다닌 건지...", "shy", true);
                scripts[i++] = new ConversationScript("뭐.. 다들 코스프레 하고 있었으니까.", "sneer", false);
                scripts[i++] = new ConversationScript("흐응...", "shy", true);
                scripts[i++] = new ConversationScript("그런데 왜 서큐버스는 이런 복장인 걸까요? 날개도 허리에 달려있고..", null, true);
                scripts[i++] = new ConversationScript("그러게? 보통 날개는 등에 달려있지 않나?", "surprised", false);
                scripts[i++] = new ConversationScript("한 번 찾아볼까?", null, false);
                scripts[i] = new ConversationScript(true);
                break;
        }

        LayoutStory.storyList.get(storyRequest).unlocked = true;
        PrefsController.setStoryListPrefs(context, "storyList", LayoutStory.storyList);

        preSpeaker = -1;
        ConversationControl.addConversationInstant(scripts);
        startInstantConversation();
    }

    private void backToLayoutStory() {
        Intent intent = new Intent(getActivity(), LayoutSplashStory.class);
        intent.putExtra("request", 1);
        startActivityForResult(intent, 1); // request code 1
        ((Activity) context).overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    private void backToLayoutCostume() {
        Intent intent = new Intent(getActivity(), LayoutSplashStory.class);
        intent.putExtra("request", 3);
        startActivityForResult(intent, 3); // request code 3
        ((Activity) context).overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    private void bringToFrontAlcyone() {
        alcyone.bringToFront();
        alcyoneFace.bringToFront();
        conversationView.bringToFront();
        conversationText.bringToFront();
        speaker.bringToFront();
        conversationFab.bringToFront();
    }

    private void bringToFrontPleione() {
        pleione.bringToFront();
        conversationView.bringToFront();
        conversationText.bringToFront();
        speaker.bringToFront();
        conversationFab.bringToFront();
    }

    public void nickNameDialog() {
        final SharedPreferences prefs = context.getSharedPreferences("prefs", Context.MODE_PRIVATE);
        final SharedPreferences.Editor editor = prefs.edit();

        // to get text from editText
        LayoutInflater inflater = this.getLayoutInflater();
        View v = inflater.inflate(R.layout.dialog_edit_text, null);
        final EditText eT = v.findViewById(R.id.dialogEditText);

        AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.AppTheme_AlertDialogOverlay);

        // cannot cancel
        builder.setCancelable(false);

        // set view
        builder.setView(v);

        if (ConversationControl.inTutorial) {
            builder.setTitle("성함을 알려주시겠어요?");
            builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    ConversationControl.conversationLock = false;
                    String newNickName = eT.getText().toString();
                    editor.putString("nickName", newNickName);
                    editor.apply();

                    View header = ((NavigationView) getActivity().findViewById(R.id.nav_view)).getHeaderView(0);
                    TextView tv = header.findViewById(R.id.nickName);
                    tv.setText(newNickName);
                    nickName = newNickName;

                    talk(true);
                }
            });
        }

        //builder.show();
        AlertDialog dialog = builder.create();
        dialog.show();

        // dialog editText request focus
        eT.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                eT.post(new Runnable() {
                    @Override
                    public void run() {
                        InputMethodManager inputMethodManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
                        inputMethodManager.showSoftInput(eT, InputMethodManager.SHOW_IMPLICIT);
                    }
                });
            }
        });
        eT.requestFocus();
    }
}
