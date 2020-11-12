package com.example.fadin.mynote;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Vector;


public class PlayGameActivity extends AppCompatActivity implements View.OnClickListener{
    private static final long VERSION = 1L;
    private int tt = 1;
    int a = 1;

    private String[] mycolor ={"#EEE4DA","#ECECC8","#F4A460",
            "#D2691E","#FF3030","#8B1A1A","#FFFF00"};
    private float x1 = 0;
    private float x2 = 0;
    private float y1 = 0;
    private float y2 = 0;

    private Button[] buttons;
    private Button restartButton;
    private Button saveButton;
    private Button loadButton;
    private TextView scoreText;
    private int[] values;              //用于标志每个位置的数值
    private int score;                 //用于记录分数
    private Vector<Integer> emptyItem = new Vector<>(); //用于记录空位置元素(0-15)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        /*去除toorbar标题*/
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        /*左侧添加一个默认的返回图标*/
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        /*设置返回键可用*/
        getSupportActionBar().setHomeButtonEnabled(true);


        initView();

        if(savedInstanceState != null){
            score = savedInstanceState.getInt("score");
            values = savedInstanceState.getIntArray("values");
        }else{
            score = 0;
            values = new int[16];
            initNums();
        }
        refresh();


        restartButton = (Button) findViewById(R.id.restartButton);
        loadButton = (Button) findViewById(R.id.loadButton);
        saveButton = (Button) findViewById(R.id.saveButton);
        restartButton.setOnClickListener(this);
        loadButton.setOnClickListener(this);
        saveButton.setOnClickListener(this);
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

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.restartButton:
                initNums();
                refresh();
                break;
            case R.id.saveButton:
                SharedPreferences sharedPreferences = getSharedPreferences("save",MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putInt("score",score);
                for(int i=0;i<16;i++){
                    editor.putInt("values"+Integer.toString(i),values[i]);
                }
                editor.commit();
                Toast.makeText(PlayGameActivity.this,"保存数据成功!",Toast.LENGTH_SHORT).show();
                break;
            case R.id.loadButton:
                SharedPreferences sharedPreferences1 = getSharedPreferences("save",MODE_PRIVATE);
                score = sharedPreferences1.getInt("score",0);
                for(int i=0;i<16;i++){
                    values[i] = sharedPreferences1.getInt("values"+Integer.toString(i),0);
                }
                refresh();
                Toast.makeText(PlayGameActivity.this,"载入数据成功!",Toast.LENGTH_SHORT).show();
                break;
            default:
                break;
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putIntArray("values",values);
        outState.putInt("score",score);
        Log.d("__main:saveInstance:","************");
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        values = savedInstanceState.getIntArray("values");
        score = savedInstanceState.getInt("score");
        Log.d("__main:restoreInstance:","************");
    }

    @Override
    protected void onPause() {
        super.onPause();
        SharedPreferences sharedPreferences = getSharedPreferences("save",MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("score",score);
        for(int i=0;i<16;i++){
            editor.putInt("values"+Integer.toString(i),values[i]);
        }
        editor.commit();
    }

    //初始化界面组件
    public void initView(){
        buttons = new Button[17];
        buttons[0] = (Button) findViewById(R.id.button1);
        buttons[1] = (Button) findViewById(R.id.button2);
        buttons[2] = (Button) findViewById(R.id.button3);
        buttons[3] = (Button) findViewById(R.id.button4);
        buttons[4] = (Button) findViewById(R.id.button5);
        buttons[5] = (Button) findViewById(R.id.button6);
        buttons[6] = (Button) findViewById(R.id.button7);
        buttons[7] = (Button) findViewById(R.id.button8);
        buttons[8] = (Button) findViewById(R.id.button9);
        buttons[9] = (Button) findViewById(R.id.button10);
        buttons[10] = (Button) findViewById(R.id.button11);
        buttons[11] = (Button) findViewById(R.id.button12);
        buttons[12] = (Button) findViewById(R.id.button13);
        buttons[13] = (Button) findViewById(R.id.button14);
        buttons[14] = (Button) findViewById(R.id.button15);
        buttons[15] = (Button) findViewById(R.id.button16);
        scoreText = (TextView) findViewById(R.id.scoreText);

        //注册子控件button的clickable为false,才能在检查到Button上滑动的效果
        for(int i=0;i<16;i++){
            buttons[i].setClickable(false);
        }
    }

    private int getMaxValue(){
        int max = 0;
        for(int i=0;i<16;i++){
            if(values[i] > max){
                max = values[i];
            }
        }
        return max;
    }

    //根据values设置emptyItem
    private void refreshEmptyItem(){
        emptyItem.clear();
        for(int i=0;i<16;i++){
            if(values[i] == 0){
                emptyItem.add(i);
            }
        }
    }

    //初始化游戏参数
    public void initNums(){
        for(int i=0;i<16;i++){
            values[i] = 0;
        }
        refreshEmptyItem();

        score = 0;
        int [] nums = new int[2];

        //生成两个0-15内的随机数,必须保证不相同(用于记录初始数值位置,初始产生两个数)
        Random random = new Random();
        nums[0] = random.nextInt(16);
        nums[1] = random.nextInt(16);
        while(nums[1] == nums[0]){
            nums[1] = random.nextInt(16);
        }

        //生成一个0-1随机数;0表示位置填充2,1表示位置填充4
        int a;
        a = random.nextInt(2);
        values[nums[0]] = 2*(a+1);
        a = random.nextInt(2);
        values[nums[1]] = 2*(a+1);
    }

    //如果有空白位置，则生成一个新的数(2或4),否则游戏结束
    public Boolean generateNew(){
        refreshEmptyItem();

        Random random = new Random();
        int a = random.nextInt(2);
        a = 2*(a+1);

        int countEmpty = emptyItem.size();
        if(countEmpty > 0){
            int loc = random.nextInt(countEmpty);
            loc = emptyItem.elementAt(loc);
            values[loc] = a;
            buttons[loc].setText(Integer.toString(a));
            buttons[loc].setBackgroundColor(Color.parseColor(mycolor[a/2-1]));
            refreshEmptyItem();
            return true;
        }else{
            return false;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(event.getAction() == MotionEvent.ACTION_DOWN){
            x1 = event.getX();
            y1 = event.getY();
        }
        if(event.getAction() == MotionEvent.ACTION_UP) {
            x2 = event.getX();
            y2 = event.getY();
            boolean flag = false;
            //判断方位
            if (y1 - y2 > 100) {
                flag = doUp();
            } else if (y2 - y1 > 100) {
                flag = doDown();
            } else if (x1 - x2 > 100) {
                flag = doLeft();
            } else if (x2 - x1 > 100) {
                flag = doRight();
            }
            //生成一个新的数
            if (flag) {
                generateNew();
            }
            refresh();
        }
        return super.onTouchEvent(event);
    }

    private Boolean isEnd(){
        for(int i=0;i<4;i++){
            for(int j=0;j<3;j++){
                if(values[4*i+j] == values[4*i+j+1]){
                    return false;
                }
            }
        }

        for(int i=0;i<4;i++){
            for(int j=0;j<3*4;j+=4){
                if(values[i+j] == values[i+j+4]){
                    return false;
                }
            }
        }

        return true;
    }

    //根据values数组更新界面,和emptyItem
    private void refresh(){
        //更新界面
        for(int i=0;i<16;i++){
            if(values[i] == 0){
                buttons[i].setText("");
                buttons[i].setBackgroundColor(Color.parseColor("#E8E8E8"));
            }else{
                buttons[i].setText(Integer.toString(values[i]));
                buttons[i].setBackgroundColor(getColorFor(values[i]));
            }
        }
        //更新emptyItem;
        refreshEmptyItem();
        //判断游戏是否结束
        if(emptyItem.size() == 0){
            if(isEnd()){
                AlertDialog.Builder builder = new AlertDialog.Builder(PlayGameActivity.this);
                builder.setMessage("游戏结束!你的分数是"+Integer.toString(score)+"!\n重新开始吗？");
                builder.setTitle("游戏结束");
                builder.setCancelable(false);
                builder.setPositiveButton("重新开始", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        initNums();
                        refresh();
                    }
                });
                builder.setNegativeButton("返回游戏", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                builder.create().show();
            }
        }
        scoreText.setText(Integer.toString(score));
    }

    private Boolean doUp(){
        Boolean isChanged = false;
        for (int i = 0; i < 4; i++) {
            Vector<Integer> loc = new Vector<>(4);
            Vector<Integer> val = new Vector<>(4);
            for (int j = i+3*4; j >= i; j-=4) {
                if (values[j] != 0) {
                    loc.add(j);
                    val.add(values[j]);
                }
            }
            if (loc.size() == 1) {
                if (i != loc.elementAt(0)) {
                    values[i] = val.elementAt(0);
                    values[loc.elementAt(0)] = 0;
                    isChanged = true;
                }
            } else if (loc.size() == 2) {
                if (val.elementAt(0).equals(val.elementAt(1))) {
                    values[loc.elementAt(0)] = 0;
                    values[loc.elementAt(1)] = 0;
                    values[i] = val.elementAt(0) * 2;
                    score += val.elementAt(0) * 2;
                    isChanged = true;
                } else {
                    values[loc.elementAt(0)] = 0;
                    values[loc.elementAt(1)] = 0;
                    values[i] = val.elementAt(1);
                    values[i + 4] = val.elementAt(0);
                    if(!loc.elementAt(0).equals(i+4)){
                        isChanged = true;
                    }
                }
            } else if (loc.size() == 3) {
                for (int j = 0; j < 3; j++) {
                    values[loc.elementAt(j)] = 0;
                }

                if (val.elementAt(2).equals(val.elementAt(1))) {
                    score += val.elementAt(2) * 2;
                    values[i] = val.elementAt(2) * 2;
                    values[i + 4] = val.elementAt(0);
                    isChanged = true;
                } else if (val.elementAt(1).equals(val.elementAt(0))){
                    score += val.elementAt(1) * 2;
                    values[i] = val.elementAt(2);
                    values[i + 4] = val.elementAt(1) * 2;
                    isChanged = true;
                } else {
                    values[i] = val.elementAt(2);
                    values[i + 4] = val.elementAt(1);
                    values[i + 2 * 4] = val.elementAt(0);
                    if(!loc.elementAt(0).equals(i+2*4)){
                        isChanged = true;
                    }
                }
            } else if (loc.size() == 4) {
                if (val.elementAt(3).equals(val.elementAt(2))) {
                    if (val.elementAt(1).equals(val.elementAt(0))) {
                        score = score + val.elementAt(3) * 2 + val.elementAt(1) * 2;
                        for (int j = 0; j < 4; j++) {
                            values[loc.elementAt(j)] = 0;
                        }

                        values[i] = val.elementAt(3) * 2;
                        values[i + 4] = val.elementAt(1) * 2;
                    } else {
                        values[loc.elementAt(0)] = 0;
                        values[i ] = val.elementAt(3) * 2;
                        values[i + 4] = val.elementAt(1);
                        values[i + 2 * 4] = val.elementAt(0);
                    }
                    isChanged = true;
                }else if(val.elementAt(2).equals(val.elementAt(1))){
                    score += val.elementAt(2)*2;
                    values[loc.elementAt(0)] = 0;
                    values[i + 4] = val.elementAt(2)*2;
                    values[i + 2 * 4] = val.elementAt(0);
                    isChanged = true;
                }else if(val.elementAt(1).equals(val.elementAt(0))){
                    score += val.elementAt(0)*2;
                    values[loc.elementAt(0)] = 0;
                    values[i + 2 * 4] = val.elementAt(0)*2;
                    isChanged = true;
                }
            }
        }
        refreshEmptyItem();
        return isChanged;
    }
    private Boolean doDown(){
        //isChanged用于判断当前方向操作是否发生变化,变化包括合并和移动
        Boolean isChanged = false;
        for (int i = 0; i < 4; i++) {
            Vector<Integer> loc = new Vector<>(4);
            Vector<Integer> val = new Vector<>(4);
            for (int j = i; j <= i + 3 * 4; j+=4) {
                if (values[j] != 0) {
                    loc.add(j);
                    val.add(values[j]);
                }
            }
            if (loc.size() == 1) {
                if (loc.elementAt(0) != (i + 3 * 4)) {
                    values[i + 3 * 4] = val.elementAt(0);
                    values[loc.elementAt(0)] = 0;
                    isChanged = true;
                }
            } else if (loc.size() == 2) {
                if (val.elementAt(0).equals(val.elementAt(1))) {
                    values[loc.elementAt(0)] = 0;
                    values[loc.elementAt(1)] = 0;
                    values[i + 3 * 4] = val.elementAt(0) * 2;
                    score += val.elementAt(0) * 2;
                    isChanged = true;
                } else {
                    values[loc.elementAt(0)] = 0;
                    values[loc.elementAt(1)] = 0;
                    values[i + 3 * 4] = val.elementAt(1);
                    values[i + 2 * 4] = val.elementAt(0);
                    if(!loc.elementAt(0).equals(i+2*4)){
                        isChanged = true;
                    }
                }
            } else if (loc.size() == 3) {
                for (int j = 0; j < 3; j++) {
                    values[loc.elementAt(j)] = 0;
                }

                if (val.elementAt(2).equals(val.elementAt(1))) {
                    score += val.elementAt(2) * 2;
                    values[i + 3 * 4] = val.elementAt(2) * 2;
                    values[i + 2 * 4] = val.elementAt(0);
                    isChanged = true;
                } else if (val.elementAt(1).equals(val.elementAt(0))){
                    score += val.elementAt(1) * 2;
                    values[i + 3 * 4] = val.elementAt(2);
                    values[i + 2 * 4] = val.elementAt(1) * 2;
                    isChanged = true;
                } else {
                    values[i + 3 * 4] = val.elementAt(2);
                    values[i + 2 * 4] = val.elementAt(1);
                    values[i + 4] = val.elementAt(0);
                    if(!loc.elementAt(0).equals(i+4)){
                        isChanged = true;
                    }
                }
            } else if (loc.size() == 4) {
                if (val.elementAt(3).equals(val.elementAt(2))) {
                    if (val.elementAt(1).equals(val.elementAt(0))) {
                        score = score + val.elementAt(3) * 2 + val.elementAt(1) * 2;
                        for (int j = 0; j < 4; j++) {
                            values[loc.elementAt(j)] = 0;
                        }

                        values[i + 3 * 4] = val.elementAt(3) * 2;
                        values[i + 2 * 4] = val.elementAt(1) * 2;
                    } else {
                        values[loc.elementAt(0)] = 0;
                        values[i + 3 * 4] = val.elementAt(3) * 2;
                        values[i + 2 * 4] = val.elementAt(1);
                        values[i + 4] = val.elementAt(0);
                    }
                    isChanged = true;
                }else if(val.elementAt(2).equals(val.elementAt(1))){
                    score += val.elementAt(2)*2;
                    values[loc.elementAt(0)] = 0;
                    values[i + 2 * 4] = val.elementAt(2)*2;
                    values[i + 4] = val.elementAt(0);
                    isChanged = true;
                }else if(val.elementAt(1).equals(val.elementAt(0))){
                    score += val.elementAt(0)*2;
                    values[loc.elementAt(0)] = 0;
                    values[i + 4] = val.elementAt(0)*2;
                    isChanged = true;
                }
            }
        }
        refreshEmptyItem();
        return isChanged;
    }
    private Boolean doLeft(){
        Boolean isChanged = false;
        for (int i = 0; i < 4; i++) {
            Vector<Integer> loc = new Vector<>(4);
            Vector<Integer> val = new Vector<>(4);
            for (int j = 3 + i * 4; j >= i * 4; j--) {
                if (values[j] != 0) {
                    loc.add(j);
                    val.add(values[j]);
                }
            }
            if (loc.size() == 1) {
                if (loc.elementAt(0) != ( i * 4)) {
                    values[i * 4] = val.elementAt(0);
                    values[loc.elementAt(0)] = 0;
                    isChanged = true;
                }
            } else if (loc.size() == 2) {
                if (val.elementAt(0).equals(val.elementAt(1))) {
                    values[loc.elementAt(0)] = 0;
                    values[loc.elementAt(1)] = 0;
                    values[i * 4] = val.elementAt(0) * 2;
                    score += val.elementAt(0) * 2;
                    isChanged = true;
                } else {
                    values[loc.elementAt(0)] = 0;
                    values[loc.elementAt(1)] = 0;
                    values[i * 4] = val.elementAt(1);
                    values[1 + i * 4] = val.elementAt(0);
                    if(!loc.elementAt(0).equals(1+i*4)){
                        isChanged = true;
                    }
                }
            } else if (loc.size() == 3) {
                for (int j = 0; j < 3; j++) {
                    values[loc.elementAt(j)] = 0;
                }

                if (val.elementAt(2).equals(val.elementAt(1))) {
                    score += val.elementAt(2) * 2;
                    values[i * 4] = val.elementAt(2) * 2;
                    values[1 + i * 4] = val.elementAt(0);
                    isChanged = true;
                } else if (val.elementAt(1).equals(val.elementAt(0))){
                    score += val.elementAt(1) * 2;
                    values[i * 4] = val.elementAt(2);
                    values[1 + i * 4] = val.elementAt(1) * 2;
                    isChanged = true;
                } else {
                    values[i * 4] = val.elementAt(2);
                    values[1 + i * 4] = val.elementAt(1);
                    values[2 + i * 4] = val.elementAt(0);
                    if(!loc.elementAt(0).equals(2+i*4)){
                        isChanged = true;
                    }
                }
            } else if (loc.size() == 4) {
                if (val.elementAt(3).equals(val.elementAt(2))) {
                    if (val.elementAt(1).equals(val.elementAt(0))) {
                        score = score + val.elementAt(3) * 2 + val.elementAt(1) * 2;
                        for (int j = 0; j < 4; j++) {
                            values[loc.elementAt(j)] = 0;
                        }

                        values[i * 4] = val.elementAt(3) * 2;
                        values[1 + i * 4] = val.elementAt(1) * 2;
                    } else {
                        values[loc.elementAt(0)] = 0;
                        values[i * 4] = val.elementAt(3) * 2;
                        values[1 + i * 4] = val.elementAt(1);
                        values[2 + i * 4] = val.elementAt(0);
                    }
                    isChanged = true;
                }else if(val.elementAt(2).equals(val.elementAt(1))){
                    score += val.elementAt(2)*2;
                    values[loc.elementAt(0)] = 0;
                    values[1 + i * 4] = val.elementAt(2)*2;
                    values[2 + i * 4] = val.elementAt(0);
                    isChanged = true;
                }else if(val.elementAt(1).equals(val.elementAt(0))){
                    score += val.elementAt(0)*2;
                    values[loc.elementAt(0)] = 0;
                    values[2 + i * 4] = val.elementAt(0)*2;
                    isChanged = true;
                }
            }
        }
        refreshEmptyItem();
        return isChanged;
    }

    private Boolean doRight() {
        Boolean isChanged = false;
        for (int i = 0; i < 4; i++) {
            Vector<Integer> loc = new Vector<>(4);
            Vector<Integer> val = new Vector<>(4);
            for (int j = i * 4; j < 4 + i * 4; j++) {
                if (values[j] != 0) {
                    loc.add(j);
                    val.add(values[j]);
                }
            }
            if (loc.size() == 1) {
                if (loc.elementAt(0) != (3 + i * 4)) {
                    values[3 + i * 4] = val.elementAt(0);
                    values[loc.elementAt(0)] = 0;
                    isChanged = true;
                }
            } else if (loc.size() == 2) {
                if (val.elementAt(0).equals(val.elementAt(1))) {
                    values[loc.elementAt(0)] = 0;
                    values[loc.elementAt(1)] = 0;
                    values[3 + i * 4] = val.elementAt(0) * 2;
                    score += val.elementAt(0) * 2;
                    isChanged = true;
                } else {
                    values[loc.elementAt(0)] = 0;
                    values[loc.elementAt(1)] = 0;
                    values[3 + i * 4] = val.elementAt(1);
                    values[2 + i * 4] = val.elementAt(0);
                    if(!loc.elementAt(0).equals(2+i*4)){
                        isChanged = true;
                    }
                }
            } else if (loc.size() == 3) {
                for (int j = 0; j < 3; j++) {
                    values[loc.elementAt(j)] = 0;
                }

                if (val.elementAt(2).equals(val.elementAt(1))) {
                    score += val.elementAt(2) * 2;
                    values[3 + i * 4] = val.elementAt(2) * 2;
                    values[2 + i * 4] = val.elementAt(0);
                    isChanged = true;
                } else if (val.elementAt(1).equals(val.elementAt(0))){
                    score += val.elementAt(1) * 2;
                    values[3 + i * 4] = val.elementAt(2);
                    values[2 + i * 4] = val.elementAt(1) * 2;
                    isChanged = true;
                } else {
                    values[3 + i * 4] = val.elementAt(2);
                    values[2 + i * 4] = val.elementAt(1);
                    values[1 + i * 4] = val.elementAt(0);
                    if(!loc.elementAt(0).equals(1+i*4)){
                        isChanged = true;
                    }
                }
            } else if (loc.size() == 4) {
                if (val.elementAt(3).equals(val.elementAt(2))) {
                    if (val.elementAt(1).equals(val.elementAt(0))) {
                        score = score + val.elementAt(3) * 2 + val.elementAt(1) * 2;
                        for (int j = 0; j < 4; j++) {
                            values[loc.elementAt(j)] = 0;
                        }

                        values[3 + i * 4] = val.elementAt(3) * 2;
                        values[2 + i * 4] = val.elementAt(1) * 2;
                    } else {
                        values[loc.elementAt(0)] = 0;
                        values[3 + i * 4] = val.elementAt(3) * 2;
                        values[2 + i * 4] = val.elementAt(1);
                        values[1 + i * 4] = val.elementAt(0);
                    }
                    isChanged = true;
                }else if(val.elementAt(2).equals(val.elementAt(1))){
                    score += val.elementAt(2)*2;
                    values[loc.elementAt(0)] = 0;
                    values[2 + i * 4] = val.elementAt(2)*2;
                    values[1 + i * 4] = val.elementAt(0);
                    isChanged = true;
                }else if(val.elementAt(1).equals(val.elementAt(0))){
                    score += val.elementAt(0)*2;
                    values[loc.elementAt(0)] = 0;
                    values[1 + i * 4] = val.elementAt(0)*2;
                    isChanged = true;
                }
            }
        }
        refreshEmptyItem();
        return isChanged;
    }

    private int getColorFor(int v){
        if(v/2 == 1){
            return Color.parseColor(mycolor[0]);
        }else if(v/4 == 1){
            return Color.parseColor(mycolor[1]);
        }else if(v/8 == 1){
            return Color.parseColor(mycolor[2]);
        }else if(v/16 == 1){
            return Color.parseColor(mycolor[3]);
        }else if(v/32 == 1){
            return Color.parseColor(mycolor[4]);
        }else if(v/64 == 1){
            return Color.parseColor(mycolor[5]);
        }else {
            return Color.parseColor(mycolor[6]);
        }
    }
}
