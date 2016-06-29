package com.seasia.myquick.asyncTasks;

import android.content.Context;
import android.os.AsyncTask;
import com.seasia.myquick.controller.WebServiceClient;
import com.seasia.myquick.model.ValidateUserStatusID;
import com.thatsit.android.interfaces.ValidateUserStatusIdInterface;

public class UserStatusIdAsync extends AsyncTask<Void,Void,ValidateUserStatusID> {

    private Context context;
    private ValidateUserStatusIdInterface mValidateUserStatusIdInterface;

    public UserStatusIdAsync(Context context,ValidateUserStatusIdInterface mValidateUserStatusIdInterface){
        this.context = context;
        this.mValidateUserStatusIdInterface = mValidateUserStatusIdInterface;
    }
    @Override
    protected ValidateUserStatusID doInBackground(Void... params) {
        try {
            ValidateUserStatusID obj = new WebServiceClient(context)
                    .performUserStatusID();
            return obj;
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    protected void onPostExecute(ValidateUserStatusID result) {
        super.onPostExecute(result);
        mValidateUserStatusIdInterface.validateUserStatusId(result);
    }
}
