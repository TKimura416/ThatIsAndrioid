package com.thatsit.android.adapter;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class ImageAdapter extends BaseAdapter {
	
	private TextView txtView;
	private final Context mContext;
	public static final String[] mThumbIds = { "\ud83d\ude00", "\ud83d\ude01",
			"\ud83d\ude02", "\ud83d\ude03", "\ud83d\ude04", "\ud83d\ude05",
			"\ud83d\ude06", "\ud83d\ude07", "\ud83d\ude08", "\ud83d\ude09",
			"\ud83d\ude0a", "\ud83d\ude0b", "\ud83d\ude0c", "\ud83d\ude0d",
			"\ud83d\ude0e", "\ud83d\ude0f", "\ud83d\ude10", "\ud83d\ude12",
			"\ud83d\ude13", "\ud83d\ude14", "\ud83d\ude16", "\ud83d\ude18",
			"\ud83d\ude1a", "\ud83d\ude1c", "\ud83d\ude1d", "\ud83d\ude1e",
			"\ud83d\ude20", "\ud83d\ude21", "\ud83d\ude22", "\ud83d\ude23",
			"\ud83d\ude24", "\ud83d\ude25", "\ud83d\ude28", "\ud83d\ude29",
			"\ud83d\ude2a", "\ud83d\ude2b", "\ud83d\ude2d", "\ud83d\ude30",
			"\ud83d\ude31", "\ud83d\ude32", "\ud83d\ude33", "\ud83d\ude35",
			"\ud83d\ude36", "\ud83d\ude37", "\ud83d\ude38", "\ud83d\ude39",
			"\ud83d\ude3a", "\ud83d\ude3b", "\ud83d\ude3c", "\ud83d\ude3d",
			"\ud83d\ude3e", "\ud83d\ude3f", "\ud83d\ude40", "\ud83d\ude45",
			"\ud83d\ude46", "\ud83d\ude47", "\ud83d\ude48", "\ud83d\ude49",
			"\ud83d\ude4a", "\ud83d\ude4b", "\ud83d\ude4c", "\ud83d\ude4d",
			"\ud83d\ude4e", "\ud83d\ude4f" };

	public static final String[] mThumbIdsForDispatch = { ":-83d:-e00", ":-83d:-e01", ":-83d:-e02",
		":-83d:-e03", ":-83d:-e04", ":-83d:-e05", ":-83d:-e06", ":-83d:-e07", ":-83d:-e08",
		":-83d:-e09", ":-83d:-e0a", ":-83d:-e0b", ":-83d:-e0c", ":-83d:-e0d", ":-83d:-e0e",
		":-83d:-e0f", ":-83d:-e10", ":-83d:-e12", ":-83d:-e13" ,":-83d:-e14",":-83d:-e16",":-83d:-e18",":-83d:-e1a"
		,":-83d:-e1c",":-83d:-e1d",":-83d:-e1e",":-83d:-e20",":-83d:-e21",":-83d:-e22",":-83d:-e23",":-83d:-e24"
		,":-83d:-e25",":-83d:-e28",":-83d:-e29",":-83d:-e2a",":-83d:-e2b",":-83d:-e2d",":-83d:-e30",":-83d:-e31"
		,":-83d:-e32",":-83d:-e33",":-83d:-e35",":-83d:-e36",":-83d:-e37",":-83d:-e38",":-83d:-e39",":-83d:-e3a"
		,":-83d:-e3b",":-83d:-e3c",":-83d:-e3d",":-83d:-e3e",":-83d:-e3f",":-83d:-e40",":-83d:-e45",":-83d:-e46"
		,":-83d:-e47",":-83d:-e48",":-83d:-e49",":-83d:-e4a",":-83d:-e4b",":-83d:-e4c",":-83d:-e4d",":-83d:-e4e",":-83d:-e4f"};
	
	
	
	
	public int getCount() {
		return mThumbIds.length;
	}

	public Object getItem(int position) {
		return mThumbIds[position];
	}

	public long getItemId(int position) {
		return 0;
	}

	public ImageAdapter(Context c) {
		mContext = c;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		
		try {
			if (convertView == null) {
				txtView = new TextView(mContext);
				txtView.setTextSize(40);
				txtView.setPadding(8, 8, 8, 8);
			} else {
				txtView = (TextView) convertView;
			}
			txtView.setText(mThumbIds[position]);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return txtView;
	}
}
