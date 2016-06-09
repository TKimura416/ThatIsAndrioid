package com.myquick.filebrowser;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.thatsit.android.R;
import com.thatsit.android.Utility;
import com.thatsit.android.activities.MUCActivity;
import com.thatsit.android.activities.SelectAudioActivity;
import com.thatsit.android.fragement.FragmentChatScreen;

public class AndroidExplorer extends ListActivity {


	public static boolean isPromtAllowed=true;
	private List<String> item = null;
	private List<String> path = null;
	private String root="/";
	private TextView myPath;

	/** Called when the activity is first created. */

	@Override

	public void onCreate(Bundle savedInstanceState) {
		Utility.allowAuthenticationDialog=false;
		
		super.onCreate(savedInstanceState);
		
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_fileexplorer);
		myPath = (TextView)findViewById(R.id.path);
		getDir(root);
		FragmentChatScreen.progressDialog.dismiss();
	}

	@Override
	protected void onResume() {
		super.onResume();
		if(Utility.allowAuthenticationDialog==true){
			Utility.showLock(AndroidExplorer.this);
		}
	}
	
	@Override
	public void onBackPressed() {
		super.onBackPressed();
		Utility.allowAuthenticationDialog=false;
	}
	@Override
	protected void onStop() {
		super.onStop();
		Utility.taskPromtOnStop(isPromtAllowed , AndroidExplorer.this);
	}
	
	private void getDir(String dirPath){
		myPath.setText("Location: " + dirPath);
		item = new ArrayList<>();
		path = new ArrayList<>();
		File f = new File(dirPath);
		File[] files = f.listFiles();
		if(!dirPath.equals(root)){
			item.add(root);
			path.add(root);
			item.add("../");
			path.add(f.getParent());
		}
		for(int i=0; i < files.length; i++)
		{
			File file = files[i];

			path.add(file.getPath());
			if(file.isDirectory())
				item.add(file.getName() + "/");

			else
				item.add(file.getName());
		}

		ArrayAdapter<String> fileList = new ArrayAdapter<>(this, R.layout.file_explorer_row, item);
		setListAdapter(fileList);
	}

	@Override

	protected void onListItemClick(ListView l, View v, int position, long id) {

		File file = new File(path.get(position));
		if (file.isDirectory())
		{
			if(file.canRead())
				getDir(path.get(position));

			else
			{
				new AlertDialog.Builder(this)
				.setTitle("[" + file.getName() + "] folder can't be read!")
				.setPositiveButton("OK",new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
					}
				}).show();
			}
		}
		else{
			Intent returnIntent = new Intent();
			returnIntent.putExtra("path",file.getPath());
			setResult(RESULT_OK,returnIntent);
			Utility.allowAuthenticationDialog=false;
			finish();
		}
	}
	
	
	
}


