package com.pleiades.pleione.alcyone;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.Collections;

import static android.content.Context.MODE_PRIVATE;

public class LayoutCostume extends Fragment {
    @SuppressLint("StaticFieldLeak")
    private static Context context;

    public static ListAdapterCostume adapterCostume;
    public static ArrayList<Costume> costumeList;
    public static int clickedPosition;
    public static int storyRequest;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.activity_costume, container, false);
        context = getContext();

        costumeList = PrefsController.getCostumeListPrefs(context, "costumeList");
        Collections.sort(costumeList);

        final SharedPreferences prefs = context.getSharedPreferences("prefs", MODE_PRIVATE); // doesn't need context at activity onCreate
        final SharedPreferences.Editor editor = prefs.edit();

        adapterCostume = new ListAdapterCostume(costumeList);
        ListView costumeListView = v.findViewById(R.id.costumeList);
        costumeListView.setAdapter(adapterCostume);

        costumeListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (costumeList.get(position).unlocked) {
                    if (position != LayoutAlcyone.costumeSelectedPosition) {
                        // sleep
                        if (LayoutAlcyone.isAlcyoneSleep) {
                            // above snack bar
                            MainActivity.fabAboveSnackAnimation(false);

                            // snack bar initialize
                            Snackbar snackbar;
                            snackbar = Snackbar.make(view, "쉿..! 아키쨩이 자고 있어요!", Snackbar.LENGTH_SHORT);
                            View snackBarView = snackbar.getView();
                            snackBarView.setBackgroundColor(ContextCompat.getColor(context, R.color.colorDrawIcon));
                            TextView snackBarTextView = snackBarView.findViewById(com.google.android.material.R.id.snackbar_text);
                            snackBarTextView.setTextColor(ContextCompat.getColor(context, R.color.colorPrimary));
                            snackbar.addCallback(new Snackbar.Callback() {
                                @Override
                                public void onDismissed(Snackbar snackbar, int event) {
                                    MainActivity.fabAboveSnackAnimation(true);
                                }
                            });
                            snackbar.show();
                        } else {
                            if (!ConversationControl.inConversation && !ConversationControl.inConversationInstant) {
                                ConversationScript script1, script2, script3, script4;
                                switch (position) {
                                    case 0:
                                        break;
                                    case 1:
                                        script1 = new ConversationScript("어머니 교복.. 너무 작아서 조금 부끄럽네요...", "shy");
                                        ConversationControl.addConversation(script1);
                                        break;
                                    case 2:
                                        script1 = new ConversationScript("정장...", null);
                                        script2 = new ConversationScript(" 왠지 정식 비서가 된 느낌이네요.", "glad");
                                        ConversationControl.addConversation(script1, script2);
                                        break;
                                    case 3:
                                        script1 = new ConversationScript("운동이라도 갈 생각이신가요?", null);
                                        script2 = new ConversationScript("파이팅이에요. 항상 응원할게요!", "glad");
                                        ConversationControl.addConversation(script1, script2);
                                        break;
                                    case 4:
                                        script1 = new ConversationScript("외출할 때마다 항상 어머니가 신경쓰여요. 어머니가 아직 어리셔서..", "sad");
                                        script2 = new ConversationScript("가 아니고 걱정돼서요.", "shy");
                                        script3 = new ConversationScript("딱히 나이를 헷갈린 건 아니에요.", "shy");
                                        ConversationControl.addConversation(script1, script2, script3);
                                        break;
                                    case 5:
                                        script1 = new ConversationScript("치어리더는.. 왜 이렇게 부끄러운 옷을 입는 걸까요?", "shy");
                                        ConversationControl.addConversation(script1);
                                        break;
                                    case 6:
                                        script1 = new ConversationScript("어머니는 장난삼아 얘기하셨지만.. 그 뒤로 조금 신경쓰이네요.", "shy");
                                        script2 = new ConversationScript("입혀.. 주실 건가요?", "shy");
                                        script3 = new ConversationScript("..아니에요. 못 들은 걸로 해주세요.", "shy");
                                        ConversationControl.addConversation(script1, script2, script3);
                                        break;
                                    case 7:
                                        script1 = new ConversationScript("이 옷을 보면 그때가 생각나요.. 한동안 안 오셔서 얼마나 걱정했는지 아세요?", "sad");
                                        ConversationControl.addConversation(script1);
                                        break;
                                    case 8:
                                        script1 = new ConversationScript("이 파자마는 어머니가 사주셨어요.", null);
                                        script2 = new ConversationScript("부러우시죠? 게다가 엄청 포근하고 푹신푹신해요.", "sneer");
                                        script3 = new ConversationScript("네? 입어보고 싶으시다고요?", "surprised");
                                        script4 = new ConversationScript("그건 안 돼요.", "shy");
                                        ConversationControl.addConversation(script1, script2, script3, script4);
                                        break;
                                    case 9:
                                        script1 = new ConversationScript("앗 이 옷은..", "surprised");
                                        script2 = new ConversationScript("어머니께서 입지 말라고 하셨는데..", "shy");
                                        script3 = new ConversationScript("이번에만 입어드리는 거예요!", "shout");
                                        ConversationControl.addConversation(script1, script2, script3);
                                        break;
                                    case 10:
                                        script1 = new ConversationScript("이 브루마 말이에요.. 하마터면 반품할 뻔했지 뭐예요.", null);
                                        script2 = new ConversationScript("작아 보이니 입어보라고..", "sad");
                                        script3 = new ConversationScript("어머니께 딱 맞는 사이즈면 저한테 작은 게 당연한데 말이에요.", "sad");
                                        ConversationControl.addConversation(script1, script2, script3);
                                        break;
                                    case 11:
                                        script1 = new ConversationScript("짠! 할로윈 때 입었던 옷이에요. 서큐버스!", null);
                                        script2 = new ConversationScript("혹시 서큐버스가 이런 복장인 이유를 아시나요?", null);
                                        script3 = new ConversationScript("어머니께서는 제가 알기에 아직 이르다고 하시더라고요.", "sad");
                                        ConversationControl.addConversation(script1, script2, script3);
                                        break;
                                    default:
                                }
                            }
                            editor.putInt("costumeSelectedPosition", position);
                            editor.putString("costumeSelectedName", costumeList.get(position).costumeName);
                            editor.apply();
                            LayoutAlcyone.costumeSelectedPosition = position;
                            LayoutAlcyone.costumeSelectedName = costumeList.get(position).costumeName;
                            adapterCostume.notifyDataSetChanged();
                        }
                    }
                } else {
                    if (position < 9) {
                        // above snack bar
                        MainActivity.fabAboveSnackAnimation(false);

                        // snack bar initialize
                        Snackbar snackbar;

                        if (position > 6) {
                            snackbar = Snackbar.make(view, "아직 획득 조건을 만족시키지 못 하셨어요!", Snackbar.LENGTH_SHORT);
                        } else {
                            snackbar = Snackbar.make(view, "이 코스튬은 스토리 진행을 통해 획득하실 수 있어요!", Snackbar.LENGTH_SHORT);
                        }
                        View snackBarView = snackbar.getView();
                        snackBarView.setBackgroundColor(ContextCompat.getColor(context, R.color.colorDrawIcon));
                        TextView snackBarTextView = snackBarView.findViewById(com.google.android.material.R.id.snackbar_text);
                        snackBarTextView.setTextColor(ContextCompat.getColor(context, R.color.colorPrimary));
                        snackbar.addCallback(new Snackbar.Callback() {
                            @Override
                            public void onDismissed(Snackbar snackbar, int event) {
                                MainActivity.fabAboveSnackAnimation(true);
                            }
                        });
                        snackbar.show();
                    } else {
                        // buy costume
                        clickedPosition = position;

                        switch (position) {
                            case 9:
                                MainActivity.billingProcessor.purchase(getActivity(), "bunny");
                                break;
                            case 10:
                                MainActivity.billingProcessor.purchase(getActivity(), "bloomer");
                                break;
                            case 11:
                                MainActivity.billingProcessor.purchase(getActivity(), "succubus");
                                break;
                        }
                    }
                }
            }
        });

        return v;
    }

    public static void unlockProcess() {
        if (!LayoutCostume.costumeList.get(LayoutCostume.clickedPosition).unlocked) {
            LayoutCostume.costumeList.get(LayoutCostume.clickedPosition).unlocked = true;

            switch (LayoutCostume.clickedPosition) {
                case 9:
                    LayoutCostume.storyRequest = 33;
                    break;
                case 10:
                    LayoutCostume.storyRequest = 34;
                    break;
                case 11:
                    LayoutCostume.storyRequest = 35;
                    break;
            }
            LayoutStory.storyList.get(LayoutCostume.storyRequest).unlocked = true;

            LayoutCostume.adapterCostume.notifyDataSetChanged();

            MainActivity.costumeProgress++;
            MainActivity.storyProgress++;
            MainActivity.setCostumeProgressBar();
            MainActivity.setStoryProgressBar();

            PrefsController.setCostumeListPrefs(context, "costumeList", LayoutCostume.costumeList);
            PrefsController.setStoryListPrefs(context, "storyList", LayoutStory.storyList);

            getNewCostumeDialog();
        }
    }

    public static void billingErrorToast() {
        Toast.makeText(context, "칫.. 설렜네. =3=", Toast.LENGTH_SHORT).show();
    }

    private static void getNewCostumeDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.AppTheme_AlertDialogOverlay);
        builder.setTitle("새로운 코스튬을 획득하셨어요!");

        // cannot cancel
        builder.setCancelable(false);

        builder.setPositiveButton("스토리 열람",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        LayoutAlcyone.fromOtherLayout = 2; // 2 is to costume
                        LayoutAlcyone.storyRequest = storyRequest;
                        LayoutAlcyone.storyRequestContinue = -1;
                        Intent intent = new Intent(context, LayoutSplashStory.class);
                        intent.putExtra("request", 0);
                        Activity activity = (Activity) context;
                        activity.startActivityForResult(intent, 0); // request code 0
                        ((Activity) context).overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                    }
                });
        builder.setNegativeButton("취소",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
        builder.show();
    }
}

class Costume implements Comparable<Costume> {
    String costumeName;
    boolean unlocked = false;
    int priority;

    @Override
    public int compareTo(Costume costume) {
        return Integer.compare(this.priority, costume.priority);
    }
}