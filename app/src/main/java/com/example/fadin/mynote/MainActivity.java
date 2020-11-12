package com.example.fadin.mynote;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.menu.MenuBuilder;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.reflect.Method;

import static android.media.CamcorderProfile.get;
/*初始页面*/
public class MainActivity extends AppCompatActivity {
    private ListView listView;
    private MySQL mySQL;
    private SQLiteDatabase db;
    private SimpleCursorAdapter simpleCursorAdapter;
    private int item_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        showListview();
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab_main);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDialog_notebook();
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
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            Intent intent=new Intent(MainActivity.this,SettingsActivity.class);
            startActivity(intent);
            return true;
        }
        if (id==R.id.action_refresh){
            recreate();
            return true;
        }
        if(id==R.id.action_search){
            Intent intent=new Intent(MainActivity.this,SearchActivity.class);
            startActivity(intent);
            return  true;
        }
        if(id==R.id.action_play){
            Intent intent=new Intent(MainActivity.this,PlayGameActivity.class);
            startActivity(intent);
            return  true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo)  {
        super.onCreateContextMenu(menu, v, menuInfo);
        /*获取当长按ListView的时候对应item的positon*/
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
        item_id = info.position;
        Cursor cursor=simpleCursorAdapter.getCursor();
        cursor.moveToPosition(item_id);
        String title=cursor.getString(cursor.getColumnIndex("name"));

        menu.setHeaderTitle(title);
        MenuInflater inflater=getMenuInflater();
        inflater.inflate(R.menu.menu_context,menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        Cursor cursor=simpleCursorAdapter.getCursor();
        cursor.moveToPosition(item_id);
        final int _id=cursor.getInt(cursor.getColumnIndex("_id"));
        switch (item.getItemId()){
            case R.id.menu_delete:
                AlertDialog.Builder builder=new AlertDialog.Builder(this);
                builder.setTitle("要删除笔记本吗？")
                        .setMessage("一旦删除，您将无法恢复此笔记本")
                        .setPositiveButton("确认", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                deleteData(_id);
                                Toast.makeText(getApplicationContext(),"删除成功",Toast.LENGTH_SHORT).show();
                            }
                        })
                        .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });
                builder.create().show();
                break;
            case  R.id.menu_edit:
                showDialog_notebook_rename(_id);
                break;
        }
        return super.onContextItemSelected(item);
    }

    /*将数据库中的notebook表中的数据显示在listview中*/
    private void showListview(){
        listView=findViewById(R.id.main_listview);
        mySQL=new MySQL(getApplicationContext(),"mynote.db",null,1);
        db=mySQL.getReadableDatabase();
        String[] from=new String[]{"name"};
        int[] to=new int[]{R.id.listview_main_textview};
        simpleCursorAdapter=new SimpleCursorAdapter(this,R.layout.listview_main,null,from,to);
        listView.setAdapter(simpleCursorAdapter);
        refreshListview();
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                /*获取点击list在数据库中所对应的id并传入下一个activity*/
                Intent intent=new Intent(MainActivity.this,SubareaActivity.class);
                Bundle bundle=new Bundle();
                Cursor mcursor=simpleCursorAdapter.getCursor();
                /*移动cursor光标到该item所对应的下标position*/
                mcursor.moveToPosition(position);
                /*获取对应数据项的id*/
                int _id=mcursor.getInt(mcursor.getColumnIndex("_id"));
                TextView textView=view.findViewById(R.id.listview_main_textview);
                /*将id传入下一个activity*/
                bundle.putInt("_id",_id);
                bundle.putString("name",textView.getText().toString());
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });
        this.registerForContextMenu(listView);
    }

    /**
     问题描述：
     1)  在android.support.v4.widget.NestedScrollView中直接嵌套ListView时出现的情况：listview显示不全只有一行或者两行。
     2) 如果listview的item中有根据id选择选项会导致选择错乱，如果listview中包含有switch控件，
     用来对该item是否可用进行判断，但是现在冲突导致的listview中位置错乱。
     2.解决方法：计算出每一个item的高度，此方法用在listview.setAdapter()后。
     */

    /*d动态设置listview高度*/
    public void setListViewHeight(ListView listView) {
        //获取listView的adapter
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null) {
            return;
        }
        int totalHeight = 0;
        //listAdapter.getCount()返回数据项的数目
        for (int i = 0,len = listAdapter.getCount(); i < len; i++) {
            View listItem = listAdapter.getView(i, null, listView);
            listItem.measure(0, 0);
            totalHeight += listItem.getMeasuredHeight();
        }
        // listView.getDividerHeight()获取子项间分隔符占用的高度
        // params.height最后得到整个ListView完整显示需要的高度
        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() *  (listAdapter .getCount() - 1));
        listView.setLayoutParams(params);
    }

    /*显示弹出添加输入框*/
    private void showDialog_notebook(){
        LayoutInflater inflater=LayoutInflater.from(this);
        View view=inflater.inflate(R.layout.dialog_main,null);
        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        builder.setTitle("新建笔记本");
        builder.setView(view);
        final AlertDialog dialog=builder.create();
        dialog.show();

        //只用下面这一行弹出对话框时需要点击输入框才能弹出软键盘
        dialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
        //加上下面这一行弹出对话框时软键盘随之弹出
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);

        /*取消button监听事件，退出dialog*/
        dialog.findViewById(R.id.dialog_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        /*注意获取dialog中layout控件时findViewById前要加dialog.*/
        final Button button=dialog.findViewById(R.id.dialog_ok);
        final EditText editText=dialog.findViewById(R.id.dialog_et);
        /*设置button不可点击*/
        button.setEnabled(false) ;
        /*设置button文本颜色*/
        button.setTextColor(getResources().getColor(R.color.color_gray_background));
        /*设置确认button监听事件*/
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String str=new String(editText.getText().toString());
                insertData(str);
                refreshListview();
                dialog.dismiss();
                Toast.makeText(getApplicationContext(), "创建成功",Toast.LENGTH_SHORT ).show();
            }
        });
        /*设置editview文本内容变化监听事件*/
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                button.setEnabled(true) ;
                button.setTextColor(getResources().getColor(R.color.colorAccent));
            }

            @Override
            public void afterTextChanged(Editable s) {
                if(editText.getText().toString().isEmpty()){
                    button.setEnabled(false) ;
                    button.setTextColor(getResources().getColor(R.color.color_gray_background));
                }
            }
        });
    }

    private void showDialog_notebook_rename(final int _id){
        LayoutInflater inflater=LayoutInflater.from(this);
        View view=inflater.inflate(R.layout.dialog_main,null);
        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        builder.setTitle("重命名笔记本");
        builder.setView(view);
        final AlertDialog dialog=builder.create();
        dialog.show();

        /*设置dialog中的editext中 文本 为原名*/
        mySQL=new MySQL(getApplicationContext(),"mynote.db",null,1);
        db=mySQL.getWritableDatabase();
        Cursor cursor=db.rawQuery("select name from notebook where _id=?",new String[]{_id+""});
        cursor.moveToFirst();
        String name= cursor.getString(cursor.getColumnIndex("name"));
        ((EditText)dialog.findViewById(R.id.dialog_et)).setText(name);
        /*设置editext文本全选状态*/
        ((EditText)dialog.findViewById(R.id.dialog_et)).selectAll();
        //只用下面这一行弹出对话框时需要点击输入框才能弹出软键盘
        dialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
        //加上下面这一行弹出对话框时软键盘随之弹出
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);

        /*取消button监听事件，退出dialog*/
        dialog.findViewById(R.id.dialog_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();

            }
        });
        /*注意获取dialog中layout控件时findViewById前要加dialog.*/
        final Button button=dialog.findViewById(R.id.dialog_ok);
        final EditText editText=dialog.findViewById(R.id.dialog_et);
        button.setText("确认");
        /*设置button不可点击*/
        button.setEnabled(false);
        /*设置button文本颜色*/
        button.setTextColor(getResources().getColor(R.color.color_gray_background));
        /*设置确认button监听事件*/
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String str=new String(editText.getText().toString());
                reName(str,_id);
                refreshListview();
                dialog.dismiss();
                Toast.makeText(getApplicationContext(), "修改成功",Toast.LENGTH_SHORT ).show();
            }
        });
        /*设置editview文本内容变化监听事件*/
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                button.setEnabled(true) ;
                button.setTextColor(getResources().getColor(R.color.colorAccent));
            }

            @Override
            public void afterTextChanged(Editable s) {
                if(editText.getText().toString().isEmpty()){
                    button.setEnabled(false) ;
                    button.setTextColor(getResources().getColor(R.color.color_gray_background));
                }
            }
        });
    }

    /*刷新数据列表*/
    private void refreshListview(){
        Cursor Cursor = db.rawQuery("select * from notebook",null);
        simpleCursorAdapter.changeCursor(Cursor);
        setListViewHeight(listView);
    }
    /*notebook表插入操作*/
    private void insertData(String name){
        mySQL=new MySQL(getApplicationContext(),"mynote.db",null,1);
        db=mySQL.getWritableDatabase();
        db.execSQL("insert into notebook (name) values (?)",new String[]{name});
    }
    /*删操作*/
    private void deleteData(int _id){
        mySQL=new MySQL(getApplicationContext(),"mynote.db",null,1);
        db=mySQL.getWritableDatabase();
        db.execSQL("delete from notebook where _id=?",new String[]{_id+""});
        refreshListview();
    }
    /*重命名*/
    private void reName(String name,int _id){
         mySQL=new MySQL(getApplicationContext(),"mynote.db",null,1);
         db=mySQL.getWritableDatabase();
         db.execSQL("update notebook set name=? where _id=?",new String[]{name,_id+""});
    }
}
