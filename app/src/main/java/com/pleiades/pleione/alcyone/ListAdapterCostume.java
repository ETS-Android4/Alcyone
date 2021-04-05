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

public class ListAdapterCostume extends BaseAdapter {
    private final ArrayList<Costume> costumeList;
    private final int listCount;

    public ListAdapterCostume(ArrayList<Costume> newCostumeList) {
        costumeList = newCostumeList;
        listCount = costumeList.size();
    }

    @Override
    public int getCount() {
        return listCount;
    }

    @Override
    public Object getItem(int position) {
        return costumeList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        CostumeViewHolder holder;

        if (convertView == null) {
            final Context context = parent.getContext();
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.listview_costume, parent, false);

            // holder
            holder = new CostumeViewHolder();
            holder.lockImage = convertView.findViewById(R.id.lockImageCostume);
            holder.tv = convertView.findViewById(R.id.costumeTitle);
            holder.tv2 = convertView.findViewById(R.id.costumePrice);

            convertView.setTag(holder);
        } else {
            holder = (CostumeViewHolder) convertView.getTag();
        }

        String costumeNameKor;
        switch (costumeList.get(position).costumeName) {
            case "school":
                costumeNameKor = "교복";
                break;
            case "business":
                costumeNameKor = "정장";
                break;
            case "nurse":
                costumeNameKor = "간호복";
                break;
            case "pajamas":
                costumeNameKor = "파자마";
                break;
            case "training":
                costumeNameKor = "트레이닝복";
                break;
            case "outside":
                costumeNameKor = "외출복";
                break;
            case "wedding":
                costumeNameKor = "웨딩드레스";
                break;
            case "cheer":
                costumeNameKor = "치어리더";
                break;
            case "bunny":
                costumeNameKor = "바니걸";
                break;
            case "bloomer":
                costumeNameKor = "브루마";
                break;
            case "succubus":
                costumeNameKor = "서큐버스";
                break;
            default:
                costumeNameKor = "기본";
        }

        String str = String.format(Locale.KOREAN, "코스튬 %02d     %s", position, costumeNameKor);
        holder.tv.setText(str);

        if (position == LayoutAlcyone.costumeSelectedPosition) {
            holder.lockImage.setVisibility(View.VISIBLE);
            holder.lockImage.setImageResource(R.drawable.ic_check_black_24dp);
            holder.tv2.setVisibility(View.INVISIBLE);
        } else {
            if (costumeList.get(position).unlocked) {
                holder.lockImage.setVisibility(View.INVISIBLE);
                holder.lockImage.setImageResource(R.drawable.ic_lock_black_24dp);
                holder.tv2.setVisibility(View.INVISIBLE);
            } else {
                holder.lockImage.setVisibility(View.VISIBLE);
                holder.lockImage.setImageResource(R.drawable.ic_lock_black_24dp);
                if (costumeList.get(position).priority >= 10)
                    holder.tv2.setVisibility(View.VISIBLE);
                else
                    holder.tv2.setVisibility(View.INVISIBLE);
            }
        }

        return convertView;
    }
}

class CostumeViewHolder {
    ImageView lockImage;
    TextView tv, tv2;
}