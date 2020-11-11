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
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
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
/*分区页面*/
public class SubareaActivity extends AppCompatActivity {
    private int notebook_id;
    private String notebook_name;
    private ListView listView;
    private MySQL mySQL;
    private SQLiteDatabase db;
    private SimpleCursorAdapter simpleCursorAdapter;
    private int item_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subarea);

        Intent intent=getIntent();
        Bundle bundle=intent.getExtras();
        notebook_id =bundle.getInt("_id");
        notebook_name =bundle.getString("name");
        /*设置toolbar*/
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(notebook_name);
        setSupportActionBar(toolbar);
        /*toolbar.setNavigationIcon(R.mipmap.return0);*/
        /*左侧添加一个默认的返回图标*/
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        /*设置返回键可用*/
        getSupportActionBar().setHomeButtonEnabled(true);


        showListview();
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();*/
                showDialog_subarea();
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
        getMenuInflater().inflate(R.menu.menu_main, menu);
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
        /*前面必须加android.*/
        if (id == android.R.id.home) {
            finish();
            return true;
        }
        if (id == R.id.action_settings) {
            Intent intent=new Intent(SubareaActivity.this,SettingsActivity.class);
            startActivity(intent);
            return true;
        }
        if (id==R.id.action_refresh){
            recreate();
            return true;
        }
        if(id==R.id.action_search){
            Intent intent=new Intent(SubareaActivity.this,SearchActivity.class);
            startActivity(intent);
            return  true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
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
        menu.findItem(R.id.menu_protect).setVisible(true);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        Cursor cursor = simpleCursorAdapter.getCursor();
        cursor.moveToPosition(item_id);
        final int _id=cursor.getInt(cursor.getColumnIndex("_id"));
        switch (item.getItemId()) {
            case R.id.menu_delete:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("要删除分区吗？")
                        .setMessage("一旦删除，您将无法恢复此分区")
                        .setPositiveButton("确认", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Cursor cursor = simpleCursorAdapter.getCursor();
                                cursor.moveToPosition(item_id);
                                deleteData(_id);
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
                break;
            case R.id.menu_edit:
                showDialog_notebook_rename(_id);
                break;
        }
        return super.onContextItemSelected(item);
    }

    /*将数据库中的subarea表中的数据显示在listview中*/
    private void showListview(){
        listView=findViewById(R.id.subarea_listview);
        mySQL=new MySQL(getApplicationContext(),"mynote.db",null,1);
        db=mySQL.getReadableDatabase();
        String[] from=new String[]{"name"};
        int[] to=new int[]{R.id.listview_subarea_tv};
        simpleCursorAdapter=new SimpleCursorAdapter(this,R.layout.listview_subarea,null,from,to);
        listView.setAdapter(simpleCursorAdapter);
        refreshListview();
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent=new Intent(SubareaActivity.this,NoteActivity.class);
                Bundle bundle=new Bundle();
                Cursor mcursor=simpleCursorAdapter.getCursor();
                /*移动cursor光标到该item所对应的下标position*/
                mcursor.moveToPosition(position);
                /*获取对应数据项的id*/
                int _id=mcursor.getInt(mcursor.getColumnIndex("_id"));
                TextView textView=view.findViewById(R.id.listview_subarea_tv);
                /*将id传入下一个activity*/
                bundle.putInt("_id",_id);
                bundle.putString("notebook_name",notebook_name);
                bundle.putString("name",textView.getText().toString());
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });
        this.registerForContextMenu(listView);
    }

    /*显示弹出添加输入框*/
    private void showDialog_subarea(){
        LayoutInflater inflater=LayoutInflater.from(this);
        View view=inflater.inflate(R.layout.dialog_main,null);
        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        builder.setTitle("新建分区");
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
        editText.setHint("分区名称");
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
                /*Snackbar.make(MainActivity.this, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();*/
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
        builder.setTitle("重命名分区");
        builder.setView(view);
        final AlertDialog dialog=builder.create();
        dialog.show();

        /*设置dialog中的editext中 文本 为原名*/
        mySQL=new MySQL(getApplicationContext(),"mynote.db",null,1);
        db=mySQL.getWritableDatabase();
        Cursor cursor=db.rawQuery("select name from subarea where _id=?",new String[]{_id+""});
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
                /*Snackbar.make(MainActivity.this, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();*/
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

    /*刷新数据列表*/
    private void refreshListview(){
        Cursor Cursor = db.rawQuery("select * from subarea where notebook_id="+ notebook_id,null);
        simpleCursorAdapter.changeCursor(Cursor);
        setListViewHeight(listView);
    }

    /*notebook表插入操作*/
    private void insertData(String name){
        mySQL=new MySQL(getApplicationContext(),"mynote.db",null,1);
        db=mySQL.getWritableDatabase();
        db.execSQL("insert into subarea (name,notebook_id) values (?,?)",new Object[]{name,notebook_id});
    }
    /*删*/
    private void deleteData(int _id){
        mySQL=new MySQL(getApplicationContext(),"mynote.db",null,1);
        db=mySQL.getWritableDatabase();
        db.execSQL("delete from subarea where _id=?",new String[]{_id+""});
        refreshListview();
    }
    /*重命名*/
    private void reName(String name,int _id){
        mySQL=new MySQL(getApplicationContext(),"mynote.db",null,1);
        db=mySQL.getWritableDatabase();
        db.execSQL("update subarea set name=? where _id=?",new String[]{name,_id+""});
    }
}
