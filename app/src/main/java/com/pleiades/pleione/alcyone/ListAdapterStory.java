package com.pleiades.pleione.alcyone;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Locale;

public class ListAdapterStory extends BaseAdapter {
    private final ArrayList<Story> storyList;
    private final int listCount;

    public ListAdapterStory(ArrayList<Story> newStoryList) {
        storyList = newStoryList;
        listCount = storyList.size();
    }

    @Override
    public int getCount() {
        return listCount;
    }

    @Override
    public Object getItem(int position) {
        return storyList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        StoryViewHolder holder;

        if (convertView == null) {
            final Context context = parent.getContext();
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.listview_story, parent, false);

            // holder
            holder = new StoryViewHolder();
            holder.lockImage = convertView.findViewById(R.id.lockImageStory);
            holder.tv = convertView.findViewById(R.id.storyTitle);

            convertView.setTag(holder);
        } else {
            holder = (StoryViewHolder) convertView.getTag();
        }

        switch (storyList.get(position).storyName) {
            case "main00":
                holder.tv.setText(String.format(Locale.KOREAN, "스토리 %02d     튜토리얼", position));
                break;
            case "costumeSpecial00":
                holder.tv.setText(String.format(Locale.KOREAN, "스토리 %02d     간호복", position));
                break;
            case "costumeSpecial01":
                holder.tv.setText(String.format(Locale.KOREAN, "스토리 %02d     파자마", position));
                break;
            case "costumePay00":
                holder.tv.setText(String.format(Locale.KOREAN, "스토리 %02d     바니걸", position));
                break;
            case "costumePay01":
                holder.tv.setText(String.format(Locale.KOREAN, "스토리 %02d     브루마", position));
                break;
            case "costumePay02":
                holder.tv.setText(String.format(Locale.KOREAN, "스토리 %02d     서큐버스", position));
                break;
            default:
                String str = String.format(Locale.KOREAN, "스토리 %02d", position);
                holder.tv.setText(str);
        }

        if (storyList.get(position).unlocked)
            holder.lockImage.setVisibility(View.INVISIBLE);
        else
            holder.lockImage.setVisibility(View.VISIBLE);

        return convertView;
    }
}

class StoryViewHolder {
    ImageView lockImage;
    TextView tv;
}