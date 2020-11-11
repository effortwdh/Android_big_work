package com.example.fadin.mynote;

import java.util.Calendar;

public class MyTime {
    private int year;
    private int month;
    private int day;
    private int hour;
    private int minute;
    private int second;
    public MyTime(){
        /*获取当前时间*/
        Calendar calendar = Calendar.getInstance();
        year = calendar.get(Calendar.YEAR);
        month = calendar.get(Calendar.MONTH) + 1;
        day = calendar.get(Calendar.DAY_OF_MONTH);
        hour = calendar.get(Calendar.HOUR_OF_DAY);
        minute = calendar.get(Calendar.MINUTE);
        second = calendar.get(Calendar.SECOND);
    }
    public MyTime(int year, int month, int day, int hour, int minute, int second){
        /*将给定时间赋值给对象中的参数*/
        this.year=year;
        this.month=month;
        this.day=day;
        this.hour=hour;
        this.minute=minute;
        this.second=second;
    }
    public MyTime(String key){
        /*解析标准字符串时间为参数并赋值*/
        String syear=key.substring(0,4);
        String smonth=key.substring(4,6);
        String sday=key.substring(6, 8);
        String shour=key.substring(8,10);
        String sminute=key.substring(10,12);
        String ssecond=key.substring(12);
        String[] t={smonth,sday,shour,sminute,ssecond};
        for(int i=0;i<5;i++)
        {
            if(t[i].compareTo("10")<0){
                t[i]=t[i].substring(1);
            }
        }
        year= Integer.parseInt(syear);
        month= Integer.parseInt(t[0]);
        day= Integer.parseInt(t[1]);
        hour= Integer.parseInt(t[2]);
        minute= Integer.parseInt(t[3]);
        second= Integer.parseInt(t[4]);
    }
    public int getYear(){
        return year;
    }
    public int getMonth(){
        return month;
    }
    public int getDay(){
        return day;
    }
    public int getHour(){
        return hour;
    }
    public int getMinute(){
        return minute;
    }
    public String getTime(){
        return hour+":"+minute+":"+second;
    }
    public String getData(){
        return year+"年"+month+"月"+day+"日";
    }
    public String getDataAndTime(){
        return getData()+' '+getTime();
    }
    public String getdateAndTimeNOSecond(){
        return getData()+' '+hour+":"+minute;
    }
    public String getStringDataAndTime(){
        /*将对象时间参数组合为标准字符串时间*/
        int[] t={month,day,hour,minute,second};
        String key= Integer.toString(year);
        for(int i=0;i<5;i++)
        {
            String temp= Integer.toString(t[i]);
            if(temp.length()==1)
            {
                temp="0"+temp;
                key+=temp;
            }
            else
                key+=temp;
        }
        return key;
    }
}
