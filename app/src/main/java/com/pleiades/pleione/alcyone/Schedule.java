package com.pleiades.pleione.alcyone;

public class Schedule implements Comparable<Schedule> {

    public String title;

    public String date;
    public String time;

    public String memo;

    public boolean checked = false;
    public boolean completed = false;
    public boolean postMeridiem = false;


    // how to sort
    @Override
    public int compareTo(Schedule schedule) {
        int result = compare(this.completed, schedule.completed);

        if (result == 0)
            result = this.date.compareTo(schedule.date);

        if (result == 0)
            result = compare(this.postMeridiem, schedule.postMeridiem);

        if (result == 0)
            result = compareTime(this.time, schedule.time);
        //result = this.time.compareTo(schedule.time);

        return result;
    }

    public int compare(boolean x, boolean y) {
        if ((x && y) || (!x && !y))
            return 0;
        else if (x && !y)
            return 1;
        else
            return -1;
    }

    public int compareTime(String x, String y) {
        if(x.equals("시간 설정 안 함")){
            if(y.equals("시간 설정 안 함"))
                return 0;
            else
                return 1;
        }

        if(y.equals("시간 설정 안 함")){
            return -1;
        }

        int subX = Integer.parseInt(x.substring(0, 2));
        int subY = Integer.parseInt(y.substring(0, 2));

        if (subX == 12) {
            if (subY == 12)
                return x.compareTo(y);
            else
                return -1;
        }

        if (subY == 12) {
            // subX is always != 12
            return 1;
        }

        // subX and subY is not 12 both
        return x.compareTo(y);
    }

}
