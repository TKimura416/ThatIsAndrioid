package com.seasia.myquick.asyncTasks;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import com.seasia.myquick.controller.WebServiceClient;
import com.seasia.myquick.model.AuthenticateUserServiceTemplate;
import com.thatsit.android.interfaces.ValidateUserPauseStateInterface;

public class UserPauseStateAsync extends AsyncTask<Void, Void, AuthenticateUserServiceTemplate> {
    private Context context;
    private ValidateUserPauseStateInterface mValidateUserPauseStateInterface;
    private String rosterEntry;
    private ProgressDialog pdDialog;
    private String EmailId,ThatsItpassword;

    public UserPauseStateAsync(Context context,String EmailId,String ThatsItpassword,
                                    ValidateUserPauseStateInterface mValidateUserPauseStateInterface) {

        this.context = context;
        this.EmailId = EmailId;
        this.ThatsItpassword = ThatsItpassword;
        this.mValidateUserPauseStateInterface = mValidateUserPauseStateInterface;
    }


    @Override
    protected AuthenticateUserServiceTemplate doInBackground(Void... arg0) {

        try {
            AuthenticateUserServiceTemplate obj = new WebServiceClient(context)
                    .getUserSignIn(EmailId,ThatsItpassword);
            return obj;
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    protected void onPostExecute(AuthenticateUserServiceTemplate result) {
        super.onPostExecute(result);
       // pdDialog.dismiss();
        mValidateUserPauseStateInterface.validateUserPauseState(context,result);
    }
}

