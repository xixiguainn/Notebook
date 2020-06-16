package com.swufe.notes;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import com.swufe.notes.db.DatabaseOperation;
import com.swufe.notes.view.LineEditText;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;



public class AddActivity extends Activity {
    private Button bt_back;
    private Button bt_save;
    private TextView tv_title;
    private SQLiteDatabase db;//数据库操作类
    private DatabaseOperation dop;//自定义数据库
    private LineEditText et_Notes;



    InputMethodManager imm;//控制手机键盘
    Intent intent;
    String editModel = null;
    int item_Id;
    String title;
    String time;
    String context;

    public String locktype = "0";// 判断是否打开密码锁
    public String lock = "0";// 密码
    private RelativeLayout datarl;
    private TextView datatv;
    private ScrollView sclv;
    // 记录editText中的图片，用于单击时判断单击的是那一个图片
    private List<Map<String, String>> imgList = new ArrayList<Map<String, String>>();
    private ImageButton ib_lk;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //状态栏透明
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
                    | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);
            window.setNavigationBarColor(Color.TRANSPARENT);
        }


        setContentView(R.layout.activity_add);
        bt_back = (Button) findViewById(R.id.bt_back);
        bt_back.setOnClickListener(new ClickEvent());
        bt_save = (Button) findViewById(R.id.bt_save);
        bt_save.setOnClickListener(new ClickEvent());

        et_Notes = (LineEditText) findViewById(R.id.et_note);
        datarl = (RelativeLayout) findViewById(R.id.datarl);
        sclv = (ScrollView) findViewById(R.id.sclv);
        ib_lk = (ImageButton) findViewById(R.id.ib_lk);

        dop = new DatabaseOperation(this, db);
        intent = getIntent();
        editModel = intent.getStringExtra("editModel");
        item_Id = intent.getIntExtra("noteId", 0);
        // 加载数据
        loadData();
    }

    // 加载数据
    private void loadData() {
        // 如果是新增记事模式，则将editText清空
        if (editModel.equals("newAdd")) {
            et_Notes.setText("");
        }
        // 如果编辑的是已存在的记事，则将数据库的保存的数据取出，并显示在EditText中
        else if (editModel.equals("update")) {

            dop.create_db();
            Cursor cursor = dop.query_db(item_Id);
            cursor.moveToFirst();
            // 取出数据库中相应的字段内容
            context = cursor.getString(cursor.getColumnIndex("context"));
            locktype = cursor.getString(cursor.getColumnIndex("locktype"));
            lock = cursor.getString(cursor.getColumnIndex("lock"));
            if ("0".equals(locktype)) {
                ib_lk.setBackgroundResource(R.drawable.unlock);
            } else {
                ib_lk.setBackgroundResource(R.drawable.lock);
            }
     //判断是否上锁来更改左下角
               // 小锁的图片



            int startIndex = 0;

            et_Notes.append(context.substring(startIndex, context.length()));
            dop.close_db();
        }
    }



    // 后退和保存按钮功能 实现
    class ClickEvent implements OnClickListener {

        @Override
        public void onClick(View v) {
            //switch case 方法判断一个变量是否与系列变量相等
            switch (v.getId()) {
                case R.id.bt_back:
                    // 当前Activity结束，则返回上一个Activity
                    AddActivity.this.finish();
                    break;
                // 将记事添加到数据库中
                case R.id.bt_save://保存按钮
                    // 取得EditText中的内容
                    context = et_Notes.getText().toString();
                    if (context.isEmpty()) {
                        Toast.makeText(AddActivity.this, "记事为空!", Toast.LENGTH_LONG)
                                .show();
                    } else {
                        // 取得当前时间
                        SimpleDateFormat formatter = new SimpleDateFormat(
                                "yyyy-MM-dd HH:mm");
                        Date curDate = new Date(System.currentTimeMillis());// 获取当前时间
                        time = formatter.format(curDate);
                        // 截取EditText中的前一部分作为标题保存到数据库中，用于显示在主页列表中
                        title = getTitle(context);
                        // 打开数据库
                        dop.create_db();
                        // 判断是更新还是新增记事
                        if (editModel.equals("newAdd")) {
                            // 将记事插入到数据库中
                            dop.insert_db(title, context, time,
                                    locktype, lock);
                            Log.i("run1","数据插入成功");

                        }
                        // 如果是编辑则更新记事即可
                        else if (editModel.equals("update")) {
                            dop.update_db(title, context, time,
                                    locktype, lock, item_Id);
                        }
                        dop.close_db();
                        // 结束当前activity
                        AddActivity.this.finish();
                    }
                    break;
            }
        }
    }

    // 使用subSring方法截取EditText中的前一部分作为标题
    // 保存进数据库中
    private String getTitle(String context) {

        String  title = context.substring(0,4);
        return title;
    }


    // 添加日记锁 取消日记锁
    public void onLOCK(View v) {
        if ("0".equals(locktype)) {//判断是否设置了密码
            inputlockDialog();//弹出设置密码弹窗
        } else {
            inputunlockDialog();//弹出取消密码弹窗
        }
    }

    //取消密码
    private void inputunlockDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);//创建弹出框
        builder.setTitle("是否取消密码")
                .setNegativeButton("取消", null);//在弹窗上设置标题设置取消按钮
        builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {//设置确认按钮
                locktype = "0";//设置没有设置密码
                lock = "0";//设置密码
                ib_lk.setBackgroundResource(R.drawable.unlock);
                Toast.makeText(AddActivity.this, "密码已取消",
                        Toast.LENGTH_LONG).show();
            }
        });
        builder.show();//弹出取消密码弹窗
    }

    //设置密码弹窗
    private void inputlockDialog() {
        final EditText inputServer = new EditText(this);//创建EditText输入框
        inputServer.setInputType(InputType.TYPE_CLASS_TEXT
                | InputType.TYPE_TEXT_VARIATION_PASSWORD);//设置输入框类型
        inputServer.setFocusable(true);//获取焦点
        AlertDialog.Builder builder = new AlertDialog.Builder(this);//创建弹出框
        builder.setTitle("设置密码").setView(inputServer)
                .setNegativeButton("取消", null);//在弹窗上设置标题添加输入框
        builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {//设置确认按钮
                String inputName = inputServer.getText().toString();
                if ("".equals(inputName)) {//判断输入框内容是否为空
                    Toast.makeText(AddActivity.this, "密码不能为空 请重新输入！",
                            Toast.LENGTH_LONG).show();
                } else {//输入框内容不为空　
                    lock = inputName;//密码
                    locktype = "1";//添加了密码锁
                    ib_lk.setBackgroundResource(R.drawable.lock);//设置添加锁图案
                    Toast.makeText(AddActivity.this, "密码设置成功！",
                            Toast.LENGTH_LONG).show();
                }
            }
        });
        builder.show();//弹出设置密码弹窗
    }



}
