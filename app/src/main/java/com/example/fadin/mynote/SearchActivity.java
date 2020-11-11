package com.example.fadin.mynote;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class SearchActivity extends AppCompatActivity {
    private SearchView searchView;
    private ListView listView_notebook;
    private ListView listView_subrarea;
    private ListView listView_note;
    private TextView tv_empty;
    private TextView tv1;
    private TextView tv2;
    private TextView tv3;
    private MySQL mySQL;
    private SQLiteDatabase db;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        /*去除toorbar标题*/
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        /*左侧添加一个默认的返回图标*/
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        /*设置返回键可用*/
        getSupportActionBar().setHomeButtonEnabled(true);

        listView_notebook=findViewById(R.id.search_listview_notebook);
        listView_subrarea=findViewById(R.id.search_listview_subarea);
        listView_note=findViewById(R.id.search_listview_note);
        tv_empty=findViewById(R.id.search_listview_tv);
        tv1=findViewById(R.id.search_tv1);
        tv2=findViewById(R.id.search_tv2);
        tv3=findViewById(R.id.search_tv3);

        searchView=findViewById(R.id.search_view);
        /*------------------ SearchView有三种默认展开搜索框的设置方式，区别如下： ------------------*/
        //设置搜索框直接展开显示。左侧有放大镜(在搜索框中) 右侧有叉叉 可以关闭搜索框
        /*searchView.setIconified(false);*/
        //设置搜索框直接展开显示。左侧有放大镜(在搜索框外) 右侧无叉叉 有输入内容后有叉叉 不能关闭搜索框
        /*searchView.setIconifiedByDefault(false);*/
        //设置搜索框直接展开显示。左侧有无放大镜(在搜索框中) 右侧无叉叉 有输入内容后有叉叉 不能关闭搜索框
        searchView.onActionViewExpanded();
        // 设置该SearchView显示确认搜索按钮
        searchView.setSubmitButtonEnabled(true);
        //设置最大宽度
        /*searchView.setMaxWidth(1500);*/

        /*设置搜索文本监听事件*/
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            // 当点击搜索按钮时触发该方法
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }
            // 当搜索内容改变时触发该方法
            @Override
            public boolean onQueryTextChange(String newText) {
                tv_empty.setVisibility(View.GONE);
                tv1.setVisibility(View.GONE);
                tv2.setVisibility(View.GONE);
                tv3.setVisibility(View.GONE);
                listView_notebook.setAdapter(null);
                listView_subrarea.setAdapter(null);
                listView_note.setAdapter(null);
                if(!newText.isEmpty()) {
                    showListview(newText);
                }
                return false;
            }
        });

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id=item.getItemId();
        /*为toorbar返回箭头添加监听事件*/
        /*前面必须加android.*/
        if (id==android.R.id.home){
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showListview(String str){
        mySQL=new MySQL(getApplicationContext(),"mynote.db",null,1);
        db=mySQL.getReadableDatabase();

        boolean flag1=showListview_notebook(str);
        boolean flag2=showListview_subarea(str);
        boolean flag3=showListview_note(str);
        if(!flag1&&!flag2&&!flag3){
            tv_empty.setVisibility(View.VISIBLE);
        }
    }

    private boolean showListview_notebook(String str) {
        final List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        Map<String, Object> map = new HashMap<String, Object>();
        Cursor cursor = db.rawQuery("select _id,name from notebook", null);
        while (cursor.moveToNext()) {
            for (int i = 0; i < cursor.getCount(); i++) {
                cursor.moveToPosition(i);
                int _id = cursor.getInt(cursor.getColumnIndex("_id"));
                String name = cursor.getString(cursor.getColumnIndex("name"));
                if (Sunday(name, str) ) {
                    map = new HashMap<String, Object>();
                    map.put("_id", _id);
                    map.put("name", name);
                    list.add(map);
                }
            }
            if (list.isEmpty()){
                return false;
            }
            else {
                SimpleAdapter adapter_notebook = new SimpleAdapter(this, list,
                        R.layout.listview_search_notebook, new String[]{"name"}, new int[]{R.id.search_notebook});
                listView_notebook.setAdapter(adapter_notebook);

                tv1.setVisibility(View.VISIBLE);
                int i=adapter_notebook.getCount();
                String string="笔记本:("+i+")";
                tv1.setHint(string);

                listView_notebook.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        Intent intent=new Intent(SearchActivity.this,SubareaActivity.class);
                        Bundle bundle=new Bundle();
                        int _id= (int) list.get(position).get("_id");
                        String name= (String) list.get(position).get("name");
                        bundle.putInt("_id",_id);
                        bundle.putString("name",name);
                        intent.putExtras(bundle);
                        startActivity(intent);
                    }
                });

                return true;
            }
        }
        return false;
    }

    private boolean showListview_subarea(String str){
        final List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        Map<String, Object> map = new HashMap<String, Object>();
        final Cursor cursor = db.rawQuery("select * from subarea", null);
        while (cursor.moveToNext()) {
            for (int i = 0; i < cursor.getCount(); i++) {
                cursor.moveToPosition(i);
                int _id = cursor.getInt(cursor.getColumnIndex("_id"));
                String name = cursor.getString(cursor.getColumnIndex("name"));
                int notebook_id=cursor.getInt(cursor.getColumnIndex("notebook_id"));

                if (Sunday(name, str) ) {
                    map = new HashMap<String, Object>();
                    map.put("_id", _id);
                    map.put("name", name);
                    map.put("notebook_id",notebook_id);
                    map.put("address",subarea_address(_id));
                    list.add(map);
                }
            }
            if (list.isEmpty()){
                return false;
            }
            else {
                SimpleAdapter adapter_subarea = new SimpleAdapter(this, list,
                        R.layout.listview_search_subarea, new String[]{"name","address"}, new int[]{R.id.search_subarea,R.id.search_subarea_address});
                listView_subrarea.setAdapter(adapter_subarea);

                tv2.setVisibility(View.VISIBLE);
                int i=adapter_subarea.getCount();
                String string="分区:("+i+")";
                tv2.setHint(string);

                listView_subrarea.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        Intent intent=new Intent(SearchActivity.this,NoteActivity.class);
                        Bundle bundle=new Bundle();
                        int _id= (int) list.get(position).get("_id");
                        int notebook_id=(int)list.get(position).get("notebook_id");
                        String name= (String) list.get(position).get("name");
                        Cursor cursor1=db.rawQuery("select * from notebook where _id=?",new String[]{notebook_id+""});
                        cursor1.moveToFirst();
                        String notebook_name=cursor1.getString(cursor1.getColumnIndex("name"));
                        bundle.putInt("_id",_id);
                        bundle.putString("name",name);
                        bundle.putString("notebook_name",notebook_name);
                        intent.putExtras(bundle);
                        startActivity(intent);
                    }
                });

                return true;
            }
        }

        return false;
    }

    private String subarea_address(int _id){
        String address=new String();
        String name_subarea;
        String name_notebook;
        int notebook_id;
        mySQL=new MySQL(getApplicationContext(),"mynote.db",null,1);
        db=mySQL.getReadableDatabase();
        Cursor cursor=db.rawQuery("select * from subarea where _id=?",new String[]{_id+""});
        cursor.moveToFirst();
        name_subarea=cursor.getString(cursor.getColumnIndex("name"));
        notebook_id=cursor.getInt(cursor.getColumnIndex("notebook_id"));
        cursor=db.rawQuery("select * from notebook where _id=?",new String[]{notebook_id+""});
        cursor.moveToFirst();
        name_notebook=cursor.getString(cursor.getColumnIndex("name"));
        address=name_notebook+">>"+name_subarea;

        return address;
    }

    private boolean showListview_note(String str){
        final List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        Map<String, Object> map = new HashMap<String, Object>();
        Cursor cursor = db.rawQuery("select * from note", null);
        while (cursor.moveToNext()) {
            for (int i = 0; i < cursor.getCount(); i++) {
                cursor.moveToPosition(i);
                int _id = cursor.getInt(cursor.getColumnIndex("_id"));
                int subarea_id=cursor.getInt(cursor.getColumnIndex("subarea_id"));
                String name;
                name = cursor.getString(cursor.getColumnIndex("title"));
                if(name==null){
                    name="无标题页";
                }
                if (Sunday(name, str)) {
                    map = new HashMap<String, Object>();
                    map.put("_id", _id);
                    map.put("subarea_id",subarea_id);
                    map.put("name", name);
                    map.put("address", note_address(_id));
                    list.add(map);
                }
            }
            if (list.isEmpty()){
                return false;
            }
            else {
                SimpleAdapter adapter_note = new SimpleAdapter(this, list,
                        R.layout.listview_search_note, new String[]{"name","address"}, new int[]{R.id.search_note,R.id.search_note_address});
                listView_note.setAdapter(adapter_note);

                tv3.setVisibility(View.VISIBLE);
                int i=adapter_note.getCount();
                String string="页面:("+i+")";
                tv3.setHint(string);

                listView_note.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        Intent intent=new Intent(SearchActivity.this,AddnoteActivity.class);
                        int _id= (int) list.get(position).get("_id");
                        int subarea_id=(int)list.get(position).get("subarea_id");
                        intent.putExtra("key",200);
                        intent.putExtra("subarea_id",subarea_id);
                        intent.putExtra("_id",_id);
                        startActivity(intent);
                    }
                });

                return true;
            }
        }

        return false;
    }

    private String note_address(int _id){
        String address=new String();
        String name_note;
        String name_subarea;
        String name_notebook;
        int notebook_id;
        int subarea_id;
        mySQL=new MySQL(getApplicationContext(),"mynote.db",null,1);
        db=mySQL.getReadableDatabase();

        Cursor cursor=db.rawQuery("select * from note where _id=?",new String[]{_id+""});
        cursor.moveToFirst();
        name_note = cursor.getString(cursor.getColumnIndex("title"));
        if(name_note==null) {
            name_note="无标题页";
        }
        subarea_id=cursor.getInt(cursor.getColumnIndex("subarea_id"));

        cursor=db.rawQuery("select * from subarea where _id=?",new String[]{subarea_id+""});
        cursor.moveToFirst();
        name_subarea=cursor.getString(cursor.getColumnIndex("name"));
        notebook_id=cursor.getInt(cursor.getColumnIndex("notebook_id"));

        cursor=db.rawQuery("select * from notebook where _id=?",new String[]{notebook_id+""});
        cursor.moveToFirst();
        name_notebook=cursor.getString(cursor.getColumnIndex("name"));
        address=name_notebook+">>"+name_subarea+">>"+name_note;

        return address;
    }

    public boolean Sunday(String dest , String pattern){

        boolean flag=false;
        char[] destchars = dest.toCharArray();
        char[] patternchars = pattern.toCharArray();

        int i = 0;
        int j = 0;

        while(i <= (dest.length() - pattern.length() + j )  ){
            if( destchars[i] != patternchars[j] ){
                if(i == (dest.length() - pattern.length() + j )){
                    break;
                }
                int pos = contains(patternchars,destchars[i+pattern.length()-j]);
                if( pos== -1){
                    i = i + pattern.length() + 1 - j;
                    j = 0 ;
                }else{
                    i = i + pattern.length() - pos - j;
                    j = 0;
                }
            }else{
                if(j == (pattern.length() - 1)){
                    flag=true;
                    i = i - j + 1 ;
                    j = 0;
                }else{
                    i++;
                    j++;
                }
            }
        }
        return flag;
    }

    private int contains(char[] chars,char target){
        for(int i = chars.length-1 ; i >= 0; i--){
            if(chars[i] == target){
                return i ;
            }
        }
        return -1;
    }

}
