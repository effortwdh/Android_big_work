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
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

import java.lang.reflect.Method;
/*快速分区页面*/
public class NoteActivity extends AppCompatActivity {
    private int subarea_id;
    private String notebook_name;
    private String subarea_name;
    private ListView listView;
    private MySQL mySQL;
    private SQLiteDatabase db;
    private SimpleCursorAdapter simpleCursorAdapter;
    private int item_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note);

        Intent intent=getIntent();
        Bundle bundle=intent.getExtras();
        subarea_id =bundle.getInt("_id");
        subarea_name =bundle.getString("name");
        notebook_name=bundle.getString("notebook_name");
        /*设置toolbar*/
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(subarea_name);
        toolbar.setSubtitle(notebook_name);
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
                Intent it=new Intent(NoteActivity.this,AddnoteActivity.class);
                it.putExtra("key",100);
                it.putExtra("subarea_id",subarea_id);
                startActivityForResult(it,100);

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
            Intent intent=new Intent(NoteActivity.this,SettingsActivity.class);
            startActivity(intent);
            return true;
        }
        if (id==R.id.action_refresh){
            recreate();
            return true;
        }
        if(id==R.id.action_search){
            Intent intent=new Intent(NoteActivity.this,SearchActivity.class);
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
        String title=cursor.getString(cursor.getColumnIndex("title"));
        if(title==null||title.isEmpty()){
            title="无标题页";
        }

        menu.setHeaderTitle(title);
        MenuInflater inflater=getMenuInflater();
        inflater.inflate(R.menu.menu_context,menu);
        menu.findItem(R.id.menu_edit).setTitle("修改");
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_delete:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("要删除页面吗？")
                        .setMessage("一旦删除，您将无法恢复此页面")
                        .setPositiveButton("确认", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Cursor cursor = simpleCursorAdapter.getCursor();
                                cursor.moveToPosition(item_id);
                                deleteData(cursor.getInt(cursor.getColumnIndex("_id")));
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
                Intent it=new Intent(NoteActivity.this,AddnoteActivity.class);
                Cursor cursor = simpleCursorAdapter.getCursor();
                cursor.moveToPosition(item_id);
                int _id=cursor.getInt(cursor.getColumnIndex("_id"));
                it.putExtra("key",200);
                it.putExtra("subarea_id",subarea_id);
                it.putExtra("_id",_id);
                startActivityForResult(it,200);
                break;
        }
        return super.onContextItemSelected(item);
    }

    /*将数据库中的subarea表中的数据显示在listview中*/
    private void showListview(){
        listView=findViewById(R.id.note_listview);
        mySQL=new MySQL(getApplicationContext(),"mynote.db",null,1);
        db=mySQL.getReadableDatabase();
        String[] from=new String[]{"title"};
        int[] to=new int[]{R.id.listview_note_tv};
        simpleCursorAdapter=new SimpleCursorAdapter(this,R.layout.listview_note,null,from,to);
        listView.setAdapter(simpleCursorAdapter);
        refreshListview();
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent it=new Intent(NoteActivity.this,AddnoteActivity.class);
                Cursor mcursor=simpleCursorAdapter.getCursor();
                /*移动cursor光标到该item所对应的下标position*/
                mcursor.moveToPosition(position);
                /*获取对应数据项的id*/
                int _id=mcursor.getInt(mcursor.getColumnIndex("_id"));
                it.putExtra("key",200);
                it.putExtra("subarea_id",subarea_id);
                it.putExtra("_id",_id);
                startActivityForResult(it,200);
            }
        });
        this.registerForContextMenu(listView);
    }

  /*  刷新数据列表*/
    private void refreshListview(){
        Cursor Cursor = db.rawQuery("select * from note where subarea_id="+ subarea_id,null);
        simpleCursorAdapter.changeCursor(Cursor);

        View view=findViewById(R.id.addnote_layout);
        View view1=findViewById(R.id.note_empty_layout);
        if (listView.getCount()==0){
            view.setVisibility(View.INVISIBLE);
            view1.setVisibility(View.VISIBLE);
        }
        else{
            view.setVisibility(View.VISIBLE);
            view1.setVisibility(View.INVISIBLE);
        }
    }
    /*删*/
    private void deleteData(int _id){
        mySQL=new MySQL(getApplicationContext(),"mynote.db",null,1);
        db=mySQL.getWritableDatabase();
        db.execSQL("delete from note where _id=?",new String[]{_id+""});
        refreshListview();
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==100||requestCode==200)
            if(resultCode==RESULT_OK)
               refreshListview();
    }
}
