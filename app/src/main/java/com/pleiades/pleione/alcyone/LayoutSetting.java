package com.pleiades.pleione.alcyone;

import android.app.backup.BackupManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;

public class LayoutSetting extends Fragment {
    public static boolean settingErrorLock = false; // prevent listener accumulate scripts repeatedly

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.activity_setting, container, false);

        ArrayList<String> settingList = new ArrayList<>();
        settingList.add("시작 페이지를 일정으로 설정");
        settingList.add("빠른 대화 속도");
        settingList.add("일정 알림 수신");
        settingList.add("저사양 모드");
        settingList.add("이름 변경");
        settingList.add("데이터 백업 (테스트 중)");

        ListAdapterSetting adapterSetting = new ListAdapterSetting(settingList);
        ListView settingListView = v.findViewById(R.id.settingList);
        settingListView.setAdapter(adapterSetting);

        settingListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // change nick name
                if (position == 4) {
                    nickNameDialog();
                }
                if (position == 5){
                    backupRestoreDialog();
                }
            }
        });

        return v;
    }

    private void backupRestoreDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext(), R.style.AppTheme_AlertDialogOverlay);
        builder.setTitle("데이터를 백업하시겠어요?");
        builder.setMessage("(기기 변경 후 자동으로 복원됩니다.)");

        // cannot cancel
        builder.setCancelable(false);

        builder.setPositiveButton("백업",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        BackupManager backupManager = new BackupManager(getContext());
                        backupManager.dataChanged();

                        ConversationScript script1 = new ConversationScript("휴대폰을 새로 구매하셨나봐요?", null);
                        ConversationScript script2 = new ConversationScript("축하드려요!", "glad");
                        ConversationControl.addConversation(script1, script2);

                        Toast.makeText(getContext(), "구글 클라우드에 데이터 백업을 요청합니다.", Toast.LENGTH_LONG).show();
                    }
                });
        builder.setNegativeButton("취소",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
        builder.show();
    }

    private void nickNameDialog() {
        final SharedPreferences prefs = getContext().getSharedPreferences("prefs", Context.MODE_PRIVATE);
        final SharedPreferences.Editor editor = prefs.edit();

        // to get text from editText
        LayoutInflater inflater = this.getLayoutInflater();
        View v = inflater.inflate(R.layout.dialog_edit_text, null);
        final EditText eT = v.findViewById(R.id.dialogEditText);

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext(), R.style.AppTheme_AlertDialogOverlay);
        builder.setTitle("이름을 변경하시겠어요?");

        // cannot cancel
        builder.setCancelable(false);

        // set view
        builder.setView(v);

        builder.setPositiveButton("변경", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                String nickName = eT.getText().toString();
                editor.putString("nickName", nickName);
                editor.apply();

                // TODO dev pleione
                if(nickName.equals("dev_pleione")){
                    PrefsController.setDeveloperMode(getContext());
                }

                ConversationScript script1 = new ConversationScript(String.format("이제부터 %s님이라고 부르면 될까요?", nickName), "glad");
                ConversationControl.addConversation(script1);

                View header = ((NavigationView) getActivity().findViewById(R.id.nav_view)).getHeaderView(0);
                TextView tv = header.findViewById(R.id.nickName);
                tv.setText(nickName);
            }
        });
        builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            }
        });

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
                        InputMethodManager inputMethodManager= (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                        inputMethodManager.showSoftInput(eT, InputMethodManager.SHOW_IMPLICIT);
                    }
                });
            }
        });
        eT.requestFocus();
    }
}
