package com.pranav.normcore.CustomClasses;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class FormatDateAndTime {

   public String getFormattedDate(long serverTime){

        long currentTime  = Calendar.getInstance().getTime().getTime();

        //If time spent is less than 24hours (24*60*60*1000 = 86400000 milliseconds)
        if(currentTime - serverTime< 86400000) {

            long minute = (currentTime - serverTime)/60000;
            String strDate = "----";
            if(minute < 60){

                if(minute == 1){
                    strDate = minute+" min ago";
                }
                else {
                    strDate = minute+" mins ago";
                }
            }
            else {
                int hour = (int) (minute/60);

                if(hour == 1){
                    strDate = hour+" hr ago";
                }
                else {
                    strDate = hour+" hrs ago";
                }
            }
            return strDate;
        }
        //If time spent is less than 48hours (2*24*60*60*1000 = 172800000 milliseconds)
        if(currentTime - serverTime< 172800000) {

            DateFormat dateFormat = new SimpleDateFormat("'Yesterday at' hh:mm a", Locale.getDefault());
            return dateFormat.format(serverTime);
        }
        //If time spent is less than a year
        else if(currentTime - serverTime< 365L * 86400000L){
            DateFormat dateFormat = new SimpleDateFormat("dd MMMM 'at' hh:mm a", Locale.getDefault());
            return dateFormat.format(serverTime);
        }
        // If time spent is more than a year
        else {
            DateFormat dateFormat = new SimpleDateFormat("dd MMMM yy 'at' hh:mm a", Locale.getDefault());
            return dateFormat.format(serverTime);
        }

    }
}
