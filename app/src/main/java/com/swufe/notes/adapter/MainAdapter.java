package com.swufe.notes.adapter;

import android.content.Context;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.swufe.notes.SQList;
import com.swufe.notes.R;
import java.util.List;

public class MainAdapter extends BaseAdapter {   //继承BaseAdapter来 自动绘制view并且填充数据
	private List<SQList> list;
	private Context context;
	private LayoutInflater inflater;
	private DisplayMetrics dm;

	class Holder {
		public TextView tv_note_id, tv_note_title, tv_note_time,tv_locktype,tv_lock;
		public RelativeLayout ll_bg;
		public ImageView iv_imge;
	}

	public MainAdapter(List<SQList> list, Context context) {//动态装载日记信息到首页
		super();
		this.list = list;
		this.context = context;
		inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}
	@Override
	public int getCount() {
		return list == null ? 0 : list.size();
	}  //重写getcuont方法 ，显示的List数量
	@Override
	public Object getItem(int position) {
		return position;
	}
	@Override
	public long getItemId(int position) {
		return position;
	}
	@Override
	public View getView(final int position, View convertView, ViewGroup parent) { //重写getiew方法

		Holder holder;
		//把每一个控件都放在Holder中，当第一次创建convertView对象时，把这些控件找出来。
		//然后用convertView的setTag将viewHolder设置到Tag中，以便系统第二次绘制GridView时从Tag中取出。
		//当第二次重用convertView时，只需从convertView中getTag取出来就可以。
		if (convertView == null) {
			holder = new Holder();
			convertView = inflater.inflate(R.layout.note_item, null);
			holder.tv_note_id = (TextView) convertView
					.findViewById(R.id.tv_note_id);
			holder.tv_locktype= (TextView) convertView
					.findViewById(R.id.tv_locktype);
			holder.tv_lock= (TextView) convertView
					.findViewById(R.id.tv_lock);
			holder.tv_note_title = (TextView) convertView
					.findViewById(R.id.tv_note_title);
			holder.tv_note_time = (TextView) convertView
					.findViewById(R.id.tv_note_time);
			holder.iv_imge = (ImageView) convertView.findViewById(R.id.iv_imge);

			holder.ll_bg = (RelativeLayout) convertView
					.findViewById(R.id.ll_bg);
			convertView.setTag(holder);
		} else {
			holder = (Holder) convertView.getTag();
		}



		dm = context.getResources().getDisplayMetrics();//获得系统屏幕信息
		LinearLayout.LayoutParams imagebtn_params = new LinearLayout.LayoutParams(
				WindowManager.LayoutParams.WRAP_CONTENT,
				WindowManager.LayoutParams.WRAP_CONTENT);
		imagebtn_params.width = dm.widthPixels/2 ;//防止超出屏幕
		imagebtn_params.height = dm.widthPixels / 2;

		holder.ll_bg.setLayoutParams(imagebtn_params);
		holder.tv_note_id.setText(list.get(position).get_id());
		holder.tv_note_title.setText(list.get(position).getTitle());
		holder.tv_note_time.setText(list.get(position).getTime());
		holder.tv_locktype.setText(list.get(position).getLocktype());
		holder.tv_lock.setText(list.get(position).getLock());

      //对控件进行布局

		//判断是否上锁
		if ("1".equals(list.get(position).getLocktype())) {
			//判断是否上锁
			holder.tv_note_title.setVisibility(View.GONE);
			holder.iv_imge.setVisibility(View.VISIBLE);
			holder.iv_imge.setBackgroundResource(R.drawable.biglock);
		}

		return convertView;
	}

}
