package com.swufe.notes;

import java.util.ArrayList;

import java.util.Collections;
import java.util.List;

import com.swufe.notes.adapter.MainAdapter;
import com.swufe.notes.SQList;

import com.swufe.notes.db.DatabaseOperation;
import com.swufe.notes.view.MyGridView;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
	private Button bt_add;// 添加按钮
	private SQLiteDatabase db;//数据库对象
	private DatabaseOperation dop;//自定义数据库类
	private MyGridView gv_notes;// 消息列表
	private TextView tv_note_id, tv_locktype, tv_lock;

	public AlarmManager am;// 消息管理者

	public MainAdapter adapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

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
//状态栏透明

		setContentView(R.layout.activity_main);
		bt_add = (Button) findViewById(R.id.bt_add);
		bt_add.setOnClickListener(new ClickEvent());

		// 数据库操作
		dop = new DatabaseOperation(this, db);
		gv_notes = (MyGridView) findViewById(R.id.lv_notes);
		if (am == null) {
			am = (AlarmManager) getSystemService(ALARM_SERVICE);//通过系统服务获取
		}

	}
	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		// 显示记事列表
		showNotesList();
		// 为记事列表添加监听器
		gv_notes.setOnItemClickListener(new ItemClickEvent());
		// 为记事列表添加长按事件
		gv_notes.setOnItemLongClickListener(new ItemLongClickEvent());
	}

	// 显示记事列表
	private void showNotesList() {
		// 创建或打开数据库 获取数据
		dop.create_db();
		//获取数据库内容
		Cursor cursor = dop.query_db();
		if (cursor.getCount() > 0) {
			List<SQList> list = new ArrayList<SQList>();//日记信息集合里
			while (cursor.moveToNext()) {// 光标移动成功
				// 把数据取出
				SQList list1 = new SQList();//创建数据库实体类
				//保存日记信息id到实体类
				list1.set_id("" + cursor.getInt(cursor.getColumnIndex("_id")));
				list1.setContext(cursor.getString(cursor
						.getColumnIndex("context")));//保存日记内容到实体类
				//保存日记标题到实体类
				list1.setTitle(cursor.getString(cursor.getColumnIndex("title")));
				//保存日记记录时间到实体类
				list1.setTime(cursor.getString(cursor.getColumnIndex("time")));
				//保存日记是否设置了日记锁到实体类
				list1.setLocktype(cursor.getString(cursor
						.getColumnIndex("locktype")));
				//保存日记锁秘密到实体类
				list1.setLock(cursor.getString(cursor.getColumnIndex("lock")));
				list.add(list1);//把保存日记信息实体类保存到日记信息集合里
			}
			//倒序显示数据
			Collections.reverse(list);
			adapter = new MainAdapter(list, this);//装载日记信息到首页
			gv_notes.setAdapter(adapter);//日记列表设置日记信息适配器
		}
		dop.close_db();//关闭数据库
	}
	// 记事列表单击监听器 实现点击功能
	class ItemClickEvent implements OnItemClickListener {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
								long id) {
			tv_note_id = (TextView) view.findViewById(R.id.tv_note_id);
			tv_locktype = (TextView) view.findViewById(R.id.tv_locktype);
			tv_lock = (TextView) view.findViewById(R.id.tv_lock);
			String locktype = tv_locktype.getText().toString();
			String lock = tv_lock.getText().toString();
			int item_id = Integer.parseInt(tv_note_id.getText().toString());
			if ("0".equals(locktype)) {//如果没加锁
				Intent intent = new Intent(MainActivity.this, AddActivity.class);
				//页面跳转到addActivity
				intent.putExtra("editModel", "update");
				intent.putExtra("noteId", item_id);
				startActivity(intent);
			} else {
				inputTitleDialog(lock, 0, item_id);//上锁的话弹出对话框
			}
		}
	}
	// 记事列表长按监听器 实现长按功能
	class ItemLongClickEvent implements OnItemLongClickListener {
		@Override
		public boolean onItemLongClick(AdapterView<?> parent, View view,
									   int position, long id) {
			//初始化日记id保存控件
			tv_note_id = (TextView) view.findViewById(R.id.tv_note_id);
			//初始化是否添加日记锁保存控件
			tv_locktype = (TextView) view.findViewById(R.id.tv_locktype);
			//初始化日记锁秘密保存信息
			tv_lock = (TextView) view.findViewById(R.id.tv_lock);
			//获取控件上是否设置日记锁信息
			String locktype = tv_locktype.getText().toString();
			//获取控件上日记密码信息
			String lock = tv_lock.getText().toString();
			//获取控件上id信息转换成int类型
			int item_id = Integer.parseInt(tv_note_id.getText().toString());
			//弹出选择操作框方法
			simpleList(item_id, locktype, lock);
			return true;
		}
	}
	// 简单列表对话框，用于选择操作
	public void simpleList(final int item_id, final String locktype,
						   final String lock) {
		//实例化AlertDialog弹窗
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this,
				R.style.custom_dialog);
		//设置弹窗标题
		alertDialogBuilder.setTitle("选择操作");
		//设置弹窗图片
		alertDialogBuilder.setIcon(R.mipmap.ic_launcher);
		//设置弹窗选项内容
		alertDialogBuilder.setItems(R.array.itemOperation,
				new android.content.DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						switch (which) {
							// 编辑
							case 0:
								if ("0".equals(locktype)) {//判断是否添加了秘密锁0没有
									Intent intent = new Intent(MainActivity.this,
											AddActivity.class);//跳转到添加日记页
									intent.putExtra("editModel", "update");//传递编辑信息
									intent.putExtra("noteId", item_id);//传递id信息
									startActivity(intent);//开始跳转
								} else {//有秘密锁
									// 弹出输入密码框
									inputTitleDialog(lock, 0, item_id);
								}
								break;
							// 删除
							case 1:
								if ("0".equals(locktype)) {// 判断是否是加密日记 0没有
									dop.create_db();// 打开数据库
									dop.delete_db(item_id);//删除数据
									dop.close_db();// 关闭数据库

									gv_notes.invalidate();
									// 刷新列表显示
									//在View发生改变时对UI进行重绘。你只需在Activity中
									//显式地调用View对象中的invalidate()方法即可。
									// 系统会自动调用 View的onDraw()方法。不使用多线程和双缓冲
									showNotesList();
								} else {//有秘密锁
									// 弹出输入密码框
									inputTitleDialog(lock, 1, item_id);
									// 刷新列表显示
									gv_notes.invalidate();
									//显示日记列表信息
									showNotesList();
								}
								break;
						}
					}
				});
		alertDialogBuilder.create();//创造弹窗
		alertDialogBuilder.show();//显示弹窗
	}

	// 加密日记打开弹出的输入密码框
	public void inputTitleDialog(final String lock, final int idtype,
								 final int item_id) {// 密码输入框
		final EditText inputServer = new EditText(this);
		inputServer.setInputType(InputType.TYPE_CLASS_TEXT
				| InputType.TYPE_TEXT_VARIATION_PASSWORD);
		inputServer.setFocusable(true);
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("请输入密码").setView(inputServer)
				.setNegativeButton("取消", null);
		builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {

			public void onClick(DialogInterface dialog, int which) {
				String inputName = inputServer.getText().toString();
				if ("".equals(inputName)) {
					Toast.makeText(MainActivity.this, "密码不能为空请重新输入！",
							Toast.LENGTH_LONG).show();
				} else {
					if (inputName.equals(lock)) {
						//如果是编辑
						if (0 == idtype) {
							Intent intent = new Intent(MainActivity.this,
									AddActivity.class);
							intent.putExtra("editModel", "update");
							intent.putExtra("noteId", item_id);
							startActivity(intent);
						}
						//如果是删除
						else if (1 == idtype) {
							dop.create_db();
							//删除数据
							dop.delete_db(item_id);
							dop.close_db();
							// 刷新列表显示
							gv_notes.invalidate();
							showNotesList();
						}
					} else {
						Toast.makeText(MainActivity.this, "密码不正确！",
								Toast.LENGTH_LONG).show();
					}
				}
			}
		});
		builder.show();
	}
	// 新建记事点击事件
	class ClickEvent implements OnClickListener {
		@Override
		public void onClick(View v) {
			//switch case 方法判断一个变量是否与系列变量相等
			switch (v.getId()) {
				// 添加记事
				case R.id.bt_add:
					Intent intent = new Intent(MainActivity.this, AddActivity.class);
					intent.putExtra("editModel", "newAdd");
					startActivity(intent);//intent传值，在addactivity里面处理
			}
		}
	}



}
