package com.pleiades.pleione.alcyone;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.Collections;

public class LayoutStory extends Fragment {
    public static ArrayList<Story> storyList;
    private Context context;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.activity_story, container, false);
        context = getContext();

        storyList = PrefsController.getStoryListPrefs(context, "storyList");
        Collections.sort(storyList);

        ListAdapterStory adapterStory = new ListAdapterStory(storyList);
        ListView storyListView = v.findViewById(R.id.storyList);
        storyListView.setAdapter(adapterStory);

        storyListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (storyList.get(position).unlocked) {
                    readDialog(position);
                } else {
                    // above snack bar
                    MainActivity.fabAboveSnackAnimation(false);

                    // snack bar initialize
                    Snackbar snackbar;

                    if (position <= 30) {
                        snackbar = Snackbar.make(view, "일과를 마감할 때마다 하나씩 열람하실 수 있어요!", Snackbar.LENGTH_SHORT);
                    } else {
                        snackbar = Snackbar.make(view, "이 스토리는 코스튬을 획득해야 열람하실 수 있어요!", Snackbar.LENGTH_SHORT);
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
                }
            }
        });

        return v;
    }

    // read dialog
    public void readDialog(final int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.AppTheme_AlertDialogOverlay);

        builder.setTitle("스토리를 열람하시겠어요?");
        builder.setPositiveButton("열람",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        LayoutAlcyone.fromOtherLayout = 1; // 1 is to story
                        LayoutAlcyone.storyRequest = position;
                        LayoutAlcyone.storyRequestContinue = -1;
                        Intent intent = new Intent(context, LayoutSplashStory.class);
                        intent.putExtra("request", 0);
                        Activity activity = (Activity)context;
                        activity.startActivityForResult(intent, 0);
                        //startActivityForResult(intent, 0); // request code 0
                        ((Activity)context).overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
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

class Story implements Comparable<Story>{
    boolean unlocked = false;
    String storyName;
    int priority;

    @Override
    public int compareTo(Story story) {
        return Integer.compare(this.priority, story.priority);
    }
}