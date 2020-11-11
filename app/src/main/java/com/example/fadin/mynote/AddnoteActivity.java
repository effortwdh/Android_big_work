package com.example.fadin.mynote;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.menu.MenuBuilder;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.reflect.Method;

import ren.qinc.edit.PerformEdit;
/*笔记本内容编辑页面*/
public class AddnoteActivity extends AppCompatActivity {
    private int _id;
    private int key;
    private int subarea_id;
    private TextView time;
    private EditText title;
    private EditText content;
    private PerformEdit mPerformEdit;
    private MySQL mySQL;
    private SQLiteDatabase db;
    private MyTime time_create;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_addnote);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        /*去除toorbar标题*/
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        /*左侧添加一个默认的返回图标*/
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        /*设置返回键可用*/
        getSupportActionBar().setHomeButtonEnabled(true);

        Intent intent = getIntent();
        key = intent.getIntExtra("key", -1);
        subarea_id = intent.getIntExtra("subarea_id", -1);
        setResult(RESULT_OK, null);

        time = findViewById(R.id.time);
        title = findViewById(R.id.title);
        content = findViewById(R.id.content);
        mPerformEdit = new PerformEdit(content);
        mySQL = new MySQL(getApplicationContext(), "mynote.db", null, 1);
        db = mySQL.getWritableDatabase();

        if (key == 100) {
            content.requestFocus();
            //只用下面这一行弹出对话框时需要点击输入框才能弹出软键盘
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
            //加上下面这一行弹出对话框时软键盘随之弹出
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);

            time_create = new MyTime();
            time.setText(time_create.getdateAndTimeNOSecond());
            /*创建新项*/
            insertData();
            /*获取新项id*/
            _id = queryData();
        }
        if(key==200){
            _id =intent.getIntExtra("_id",-1);
            Cursor cursor=db.rawQuery("select * from note where _id=?",new String[]{_id+""});
            cursor.moveToFirst();
            title.setText(cursor.getString(cursor.getColumnIndex("title")));
            MyTime time_create=new MyTime(cursor.getString(cursor.getColumnIndex("time_create")));
            time.setText(time_create.getdateAndTimeNOSecond());
            String string=cursor.getString(cursor.getColumnIndex("content"));
            if(string==null){
                string="";
            }
            mPerformEdit.setDefaultText(string);
        }

        title.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                updateData(title.getText().toString(),null);
            }
        });
        content.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                updateData(null,content.getText().toString());
            }
        });
    }

    /*重写onPrepareOptionsPanel(View view, Menu menu)方法，这样才能正常显示图标*/
    @Override
    protected boolean onPrepareOptionsPanel(View view, Menu menu) {
        if (menu != null) {
            if (menu.getClass() == MenuBuilder.class) {
                try {
                    Method m = menu.getClass().getDeclaredMethod("setOptionalIconsVisible", Boolean.TYPE);
                    m.setAccessible(true);
                    m.invoke(menu, true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return super.onPrepareOptionsPanel(view, menu);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_addnote, menu);
        MenuItem item2 = menu.findItem(R.id.action_return);
        MenuItem item1 = menu.findItem(R.id.action_return_2);

        item2.setVisible(true);
        item1.setVisible(true);
        if (key == 100) {
        }
        if (key == 200) {
            /* 设置撤回item为不可点击*//*
            item.setEnabled(false);*/
            /* 设置item颜色*//*
            Drawable icon = getResources().getDrawable( R.mipmap.return0 );
            String string="R.color.ppp";
            ColorFilter filter = new LightingColorFilter(getResources().getColor(R.color.ppp),getResources().getColor(R.color.ppp));
            icon.setColorFilter(filter);
            item.setIcon(icon);*/
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        /*为toorbar返回箭头添加监听事件*/
        if (id == android.R.id.home) {
            finish();
            return true;
        }
        if (id == R.id.action_settings) {
            return true;
        }
        if (id == R.id.action_refresh) {
            recreate();
            return true;
        }
        if (id == R.id.action_return) {
            mPerformEdit.undo();
            return true;
        }
        if (id == R.id.action_return_2) {
            mPerformEdit.redo();
            return true;
        }
        if(id==R.id.action_delete){
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("要删除页面吗？")
                    .setMessage("一旦删除，您将无法恢复此页面")
                    .setPositiveButton("确认", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            deleteData(_id);
                            finish();
                            Toast.makeText(getApplicationContext(), "删除成功", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
            builder.create().show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /*创建新项*/
    private void insertData(){
        /*创建新项*/
        /*String str= getText(R.string.large_text).toString();*/
        db.execSQL("insert into note (time_create,subarea_id) values (?,?)", new Object[]{
                time_create.getStringDataAndTime(), subarea_id});
    }
    /*删除note*/
    private void deleteData(int _id){
        mySQL=new MySQL(getApplicationContext(),"mynote.db",null,1);
        db=mySQL.getWritableDatabase();
        db.execSQL("delete from note where _id=?",new String[]{_id+""});
    }
    /*改*/
    private void updateData(String title, String content){
        MyTime myTime=new MyTime();
        if(title==null){
            db.execSQL("update note set content=? , time_revise=? where _id="+ _id,new String[]{content,myTime.getStringDataAndTime()});
        }
        if(content==null){
            db.execSQL("update note set title=? , time_revise=? where _id="+ _id,new String[]{title,myTime.getStringDataAndTime()});
        }

    }
    /*获取新项id*/
    private int queryData(){
        /*获取新项id*/
        Cursor cursor = db.rawQuery("select _id from note where time_create=? and subarea_id=?", new String[]{
                time_create.getStringDataAndTime(), subarea_id + ""});
        cursor.moveToFirst();
        return cursor.getInt(cursor.getColumnIndex("_id"));
    }
}
