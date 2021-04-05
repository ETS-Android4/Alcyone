package com.pleiades.pleione.alcyone;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class ListAdapterInfo extends BaseAdapter {
    private final ArrayList<Info> infoList;
    private final int listCount;

    public ListAdapterInfo(ArrayList<Info> newInfoList) {
        infoList = newInfoList;
        listCount = infoList.size();
    }

    @Override
    public int getCount() {
        return listCount;
    }

    @Override
    public Object getItem(int position) {
        return infoList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            final Context context = parent.getContext();
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.listview_info, parent, false);
        }

        TextView title = convertView.findViewById(R.id.infoTitle);
        title.setText(infoList.get(position).title);

        TextView contents = convertView.findViewById(R.id.infoContents);
        contents.setText(infoList.get(position).contents);

        return convertView;
    }
}