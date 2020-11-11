package com.example.fadin.mynote;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class MySQL extends SQLiteOpenHelper {
    public MySQL(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    /*数据库创建自动调用onCreate函数*/
    @Override
    public void onCreate(SQLiteDatabase db) {
        MyTime myTime=new MyTime();
        db.execSQL("create table notebook (_id integer primary key autoincrement,name text not null)");
        db.execSQL("create table subarea (_id integer primary key autoincrement,name text not null,notebook_id integer,foreign key(notebook_id) references notebook(_id))");
        db.execSQL("create table note (_id integer primary key autoincrement,title text,content text,time_create text not null,time_revise text,subarea_id integer,foreign key(subarea_id) references subarea(_id))");
        db.execSQL("insert into notebook (_id,name) values (1,?)",new String[]{"我的笔记本"});
        db.execSQL("insert into subarea (_id,name,notebook_id) values (1,?,1)",new String[]{"快速分区"});
        db.execSQL("insert into note (_id,time_create,subarea_id) values (1,?,1)",new String[]{myTime.getStringDataAndTime()});
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        /*数据库版本更新时执行的操作*/
    }
}
