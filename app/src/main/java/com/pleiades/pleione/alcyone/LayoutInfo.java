package com.pleiades.pleione.alcyone;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;

import static com.pleiades.pleione.alcyone.MainActivity.applicationContext;

public class LayoutInfo extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.activity_info, container, false);

        ArrayList<Info> infoList = new ArrayList<>();
        infoList.add(new Info("플레이오네 이메일", "Pleione.P.Cluster@gmail.com"));

        try {
            PackageInfo packageInfo;
            packageInfo = applicationContext.getPackageManager().getPackageInfo(applicationContext.getPackageName(), 0);
            infoList.add(new Info("앱 버전", packageInfo.versionName));
        } catch (Exception e) {
            // ignore exception
        }

        ListAdapterInfo adapterInfo = new ListAdapterInfo(infoList);
        ListView infoListView = v.findViewById(R.id.infoList);
        infoListView.setAdapter(adapterInfo);

        infoListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // send email
                if (position == 0) {
                    Intent email = new Intent(Intent.ACTION_SEND);
                    email.setType("plain/text");
                    String[] address = {"Pleione.P.Cluster@gmail.com"};
                    email.putExtra(Intent.EXTRA_EMAIL, address);
//                  email.putExtra(Intent.EXTRA_SUBJECT,"email title");
//                  email.putExtra(Intent.EXTRA_TEXT,"emeil contents");
                    startActivity(email);
                } else if (position == 1) {
                    String url = "https://play.google.com/store/apps/details?id=com.pleiades.pleione.alcyone";
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse(url));
                    startActivity(intent);
                }
            }
        });

        return v;
    }
}

class Info {
    String title;
    String contents;

    public Info(String title, String contents) {
        this.title = title;
        this.contents = contents;
    }
}