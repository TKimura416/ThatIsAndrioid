package com.seasia.myquick.asyncTasks;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import com.seasia.myquick.controller.WebServiceClient;
import com.seasia.myquick.model.ValidateThatsItID;
import com.thatsit.android.interfaces.ValidateThatsItIdInterface;

public class Validate_ThatsItId_Async extends AsyncTask<Void, Void, ValidateThatsItID> {
    private final Context context;
    private final ValidateThatsItIdInterface mValidateThatsItIdInterface;
    private final String rosterEntry;
    private ProgressDialog pdDialog;

    public Validate_ThatsItId_Async(Context context,String rosterEntry,
                                    ValidateThatsItIdInterface mValidateThatsItIdInterface) {

        this.context = context;
        this.rosterEntry = rosterEntry;
        this.mValidateThatsItIdInterface = mValidateThatsItIdInterface;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        pdDialog = new ProgressDialog(context);
        pdDialog.setMessage("Please Wait");
        pdDialog.show();
        pdDialog.setCancelable(false);
    }

    @Override
    protected ValidateThatsItID doInBackground(Void... params) {

        try {
            ValidateThatsItID obj = new WebServiceClient(
                    context).validateThatsItID(rosterEntry);

            return obj;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    protected void onPostExecute(ValidateThatsItID result) {
        super.onPostExecute(result);
        pdDialog.dismiss();
        mValidateThatsItIdInterface.validateThatsItId(result);
    }
}
