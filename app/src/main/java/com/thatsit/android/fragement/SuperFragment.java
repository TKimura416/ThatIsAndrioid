package com.thatsit.android.fragement;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.thatsit.android.MainService;

public class SuperFragment extends Fragment{

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		checkServiceStart();
		return super.onCreateView(inflater, container, savedInstanceState);
		
	}
	
	protected void checkServiceStart(){

		if(MainService.mService==null ){
			getActivity().startService(new Intent(getActivity(),MainService.class));
		}
	}
}
