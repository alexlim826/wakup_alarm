package app.tabsample;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;

import alarmModels.AlarmSetting;

public class AlarmMainActivity extends Activity {

    public static TextView txtAdd;
    private TextView txtBack;

    private SimpleDateFormat mFormatter = new SimpleDateFormat("yyyy-MM-dd hh:mm aa");

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.alarm_main);

        txtAdd = (TextView)findViewById(R.id.txtAdd);
        txtBack = (TextView)findViewById(R.id.txtBack);
        txtAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Fragment frag;
                if(AlarmSetting.bool_edit_flag == 1){
                    if(!checkAlarmParams()) {
                        Toast.makeText(getApplicationContext(), "Please correct insert alarm informations!!!", Toast.LENGTH_LONG).show();
                        return;
                    }
                    txtBack.setText("");
                    txtAdd.setText("Add");
                    AlarmSetting.bool_edit_flag = 0;
                    addAlarm();
                    frag = new AlarmListFragment();

                }else if(AlarmSetting.bool_edit_flag == 0){
                    AlarmSetting.bool_edit_flag = 1;
                    txtBack.setText("Back");
                    txtAdd.setText("Done");
                    frag = new AlarmEditFragment();

                }else{
                    AlarmSetting.bool_edit_flag = 1;
                    frag = new AlarmEditFragment();
                }
                FragmentManager fm = getFragmentManager();
                FragmentTransaction fragmentTransaction = fm.beginTransaction();
                fragmentTransaction.replace(R.id.alarm_fragment, frag);
                fragmentTransaction.commit();
            }
        });
        txtBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Fragment frag;
                if(AlarmSetting.bool_edit_flag == 1){
                    AlarmSetting.bool_edit_flag = 0;
                    txtBack.setText("");
                    txtAdd.setText("Add");
                    frag = new AlarmListFragment();
                }else{
                    txtAdd.setVisibility(View.VISIBLE);
                    AlarmSetting.bool_edit_flag = 1;
                    frag = new AlarmEditFragment();
                }
                FragmentManager fm = getFragmentManager();
                FragmentTransaction fragmentTransaction = fm.beginTransaction();
                fragmentTransaction.replace(R.id.alarm_fragment,frag);
                fragmentTransaction.commit();
            }
        });
    }
    private int scheduleNotification(Notification notification, long mili) {

        final int _id = (int) System.currentTimeMillis();

        Intent notificationIntent = new Intent(this, NotificationPublisher.class);

        notificationIntent.putExtra(NotificationPublisher.NOTIFICATION_ID, _id);
        notificationIntent.putExtra(NotificationPublisher.NOTIFICATION, notification);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, _id, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        long futureInMillis = mili;
        AlarmManager alarmManager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
//        alarmManager.set(AlarmManager.RTC_WAKEUP, futureInMillis, pendingIntent);
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, futureInMillis, 1000 * 5, pendingIntent);//1000 * 60 * 60 * 24 * 7
        return _id;
    }

    private Notification getNotification(String title, String content) {
        Notification.Builder builder = new Notification.Builder(this);
        builder.setContentTitle(title);
        builder.setContentText(content);
        builder.setSmallIcon(R.drawable.icon);
        builder.setDefaults(Notification.DEFAULT_SOUND);
        builder.setAutoCancel(true);
        return builder.build();
    }

    private void addAlarm(){

        int flag = 1;
        AlarmSetting.alarm_state = flag;

        String[] intervals = getIntervals();
        for(int i = 0; i < 7; i++){
            if(intervals[i] != ""){
                ParsePosition pos = new ParsePosition(0);
                Date date1 = mFormatter.parse(intervals[i] + " " + AlarmSetting.strAlarmTime, pos);
                System.out.println("in milliseconds: " + (int)date1.getTime());
                int s_id = scheduleNotification(getNotification("my alarm title (Wake up)", "alarm content" + AlarmSetting.strAlarmTime), date1.getTime());
                AlarmSetting.alarm_ids[i] = s_id;
            }else{
                AlarmSetting.alarm_ids[i] = 0;
            }
        }
        AlarmSetting.saveAlarm(getApplicationContext());
    }

    String[] getIntervals(){
        SimpleDateFormat df;
        Date today = new Date();
//        int[] weekflags = AlarmSetting.getWeek_flag();
        String[] dateAry = new String[7];

        df = new SimpleDateFormat("yyyy-MM");
        String YM = df.format(today);
        df = new SimpleDateFormat("dd");
        String day = df.format(today);
        df = new SimpleDateFormat("EEE");
        String weekday = df.format(today);
        int curWeek = getDayofWeek(weekday);
        for(int i = 0; i < 7; i++){
            if(AlarmSetting.week_flag[i] == 1){
                if(curWeek > i){
                    dateAry[i] = YM + "-" + (Integer.parseInt(day) + 7 - curWeek + i);
                }else{
                    dateAry[i] = YM + "-" + (Integer.parseInt(day) - curWeek + i);
                }
            }else{
                dateAry[i] = "";
            }
        }
        return dateAry;
    }
    int getDayofWeek(String day){
        if(day.equals("Mon"))
            return 0;
        else if(day.equals("Tue"))
            return 1;
        else if(day.equals("Wed"))
            return 2;
        else if(day.equals("Thu"))
            return 3;
        else if(day.equals("Fri"))
            return 4;
        else if(day.equals("Sat"))
            return 5;
        else return 6;
    }
    void initSharedReference(){
        SharedPreferences sharedPreference = getApplicationContext().getSharedPreferences(AlarmSetting.LocalTmpPath, 0);
        SharedPreferences.Editor editor = sharedPreference.edit();
        editor.clear();
        editor.commit();
    }
    Boolean checkAlarmParams(){
        if(AlarmSetting.strAlarmName == " " || AlarmSetting.strAlarmTime == " "|| !AlarmSetting.checkWeek())
            return false;
        else
            return true;
    }
}