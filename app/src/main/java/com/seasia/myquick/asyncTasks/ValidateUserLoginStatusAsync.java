package com.seasia.myquick.asyncTasks;

import android.content.Context;
import android.os.AsyncTask;
import com.seasia.myquick.controller.WebServiceClient;
import com.seasia.myquick.model.ValidateUserLoginStatus;
import com.thatsit.android.Utility;
import com.thatsit.android.interfaces.ValidateUserLoginInterface;

public class ValidateUserLoginStatusAsync extends AsyncTask<Void, Void, ValidateUserLoginStatus> {
        private Context context;
        private ValidateUserLoginInterface mValidateUserLoginInterface;
        private String email;
        private String status;
        private String registrationId;
        private String statusID;

        public ValidateUserLoginStatusAsync(Context context,String email,String status,
                                            String registrationId,String statusID,
                                            ValidateUserLoginInterface mValidateUserLoginInterface) {

                this.context = context;
                this.email = email;
                this.status = status;
                this.registrationId = registrationId;
                this.statusID = statusID;
                this.mValidateUserLoginInterface = mValidateUserLoginInterface;
        }
        @Override
        protected ValidateUserLoginStatus doInBackground(Void... params) {

                try {
                        ValidateUserLoginStatus obj = new WebServiceClient(
                                context).validateUserLoginStatus(email,status,registrationId,statusID);

                        return obj;
                } catch (Exception e) {
                        Utility.stopDialog();
                        e.printStackTrace();
                        return null;
                }
        }

        @Override
        protected void onPostExecute(ValidateUserLoginStatus result) {
                super.onPostExecute(result);
                mValidateUserLoginInterface.validateUserLogin(context,result);
        }
}

