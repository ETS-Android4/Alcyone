package com.pleiades.pleione.alcyone;

import android.content.Context;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import java.util.ArrayList;

public class ListAdapterSchedule extends BaseAdapter {
    private final ArrayList<Schedule> scheduleList;
    private final int listCount;

    public ListAdapterSchedule(ArrayList<Schedule> newScheduleList) {
        scheduleList = newScheduleList;
        listCount = scheduleList.size();
    }

    @Override
    public int getCount() {
        return listCount;
    }

    @Override
    public Object getItem(int position) {
        return scheduleList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final int pos = position;
        ViewHolder holder;

        if (convertView == null) {
            final Context context = parent.getContext();
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.listview_schedule, parent, false);

            // checkbox holder
            holder = new ViewHolder();
            holder.checkBox = convertView.findViewById(R.id.listCheckBox);
            holder.tv1 = convertView.findViewById(R.id.listTitle);
            holder.tv2 = convertView.findViewById(R.id.listDate);
            holder.tv3 = convertView.findViewById(R.id.listTime);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.tv1.setText(scheduleList.get(position).title);
        holder.tv2.setText(scheduleList.get(position).date);
        holder.tv3.setText(scheduleList.get(position).time);

        holder.checkBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scheduleList.get(pos).checked = !scheduleList.get(pos).checked;
                notifyDataSetChanged();
            }
        });
        // set checkbox status
        holder.checkBox.setChecked(scheduleList.get(position).checked);

        // checkbox visibility
        if (scheduleList.get(position).completed) {
            holder.checkBox.setVisibility(View.INVISIBLE);

            holder.tv1.setPaintFlags(holder.tv1.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            holder.tv1.setTextColor(convertView.getResources().getColor(R.color.colorCompletedScheduleText));
            holder.tv2.setTextColor(convertView.getResources().getColor(R.color.colorCompletedScheduleText));
            holder.tv3.setTextColor(convertView.getResources().getColor(R.color.colorCompletedScheduleText));

            convertView.setBackgroundColor(convertView.getResources().getColor(R.color.colorDrawBackground));
        } else {
            holder.checkBox.setVisibility(View.VISIBLE);
            holder.tv1.setPaintFlags(holder.tv1.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
            holder.tv1.setTextColor(convertView.getResources().getColor(R.color.colorDrawText));
            holder.tv2.setTextColor(convertView.getResources().getColor(R.color.colorProgressTextView));
            holder.tv3.setTextColor(convertView.getResources().getColor(R.color.colorProgressTextView));

            convertView.setBackgroundColor(convertView.getResources().getColor(R.color.colorPrimary));
        }

        return convertView;
    }
}

class ViewHolder {
    TextView tv1, tv2, tv3;
    CheckBox checkBox;
}