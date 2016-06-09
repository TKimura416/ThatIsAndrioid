package com.seasia.myquick.controller;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import android.content.Context;
import android.util.Log;
import com.google.gson.Gson;
import com.seasia.myquick.model.AuthenticateUserServiceTemplate;
import com.seasia.myquick.model.CheckMessage_ChatPasswrd;
import com.seasia.myquick.model.CheckSubscriptionKeyValidity;
import com.seasia.myquick.model.FetchUserSettingTemplates;
import com.seasia.myquick.model.GenerateKeyIdResponse;
import com.seasia.myquick.model.GetSubscriptionHistoryTemplate;
import com.seasia.myquick.model.InsertUserResponseTemplate;
import com.seasia.myquick.model.SubcriptionFeeTemplate;
import com.seasia.myquick.model.UpdateMessagePaswordTemplate;
import com.seasia.myquick.model.UpdateSubsciptionTemplate;
import com.seasia.myquick.model.UpdateUserPasswordTemplate;
import com.seasia.myquick.model.UpdateUserSettingTemplate;
import com.seasia.myquick.model.ValidatePincode;
import com.seasia.myquick.model.ValidateThatsItID;
import com.seasia.myquick.model.ValidateUserLoginStatus;
import com.seasia.myquick.model.ValidateUserStatusID;

import java.security.KeyStore;

public class WebServiceClient {

	private final Context mContext;
	//private final String BASE_URL = "http://dotnetstg2.seasiaconsulting.com/Thatsit/Services/ThatsItService.svc/";
	private final String BASE_URL = "https://thatsitsrv.com/services/ThatsItService.svc/";
	private final String INSERT_USER_DATA = "InsertUserData";
	private final String GENERATE_ALPHANUMERIC_KEY = "GeneateAlphanumericKey";
	private final String CHECK_SUBSCRIPTION_VALIDITY = "CheckSubscription";
	private final String AUTHENTICATE_USER_SERVICE = "AuthenticateUserService";
	private final String UPDATE_MESSAGE_PASSWORD = "UpdateMessagePassword";
	private final String AUTHENTICATE_MESSAGE_PASSWORD = "CheckMessagePassword";
	private final String UPDATE_USER_SETTING = "UpdateUserSettings";
	private final String GET_SUBSCRIPTION_HISTORY = "GetSubscriptionHistory";
	private final String GET_REGISTRATION_DETAILS = "FetchUserSettings";
	private final String UPDATE_USER_PASSWORD = "UpdateUserPassword";
	private final String UPDATE_SUBSCRIPTION = "UpdateSubscription";
	private final String GET_SUBSCRIPTION_FEE = "GetSubscriptionFee";
	private final String VALIDATE_PINCODE = "ValidatePincode";
	private final String VALIDATE_THATSITID = "ValidateUserPincode";
	private final String VALIDATE_USER_LOGIN_STATUS = "ValidateUserLoginStatus";
	private final String VALIDATE_USER_STATUS_ID = "GeneateRandomNumber";

	private static final int TIMEOUT_CONNECTION = 50 * 1000;
	private static final int TIMEOUT_SOCKET = 50 * 1000;

	public WebServiceClient(Context parentRef) {
		mContext = parentRef;
	}

	// << POST definition;
	private Object performHttpPost(String payload, String method,Class<?> template) {
		try {
			HttpParams params = new BasicHttpParams();
			HttpConnectionParams.setConnectionTimeout(params, TIMEOUT_CONNECTION);
			HttpConnectionParams.setSoTimeout(params, TIMEOUT_SOCKET);
			//HttpClient httpClient = new DefaultHttpClient();
			HttpClient httpClient = getNewHttpClient();

			HttpPost httpPost = new HttpPost(BASE_URL + method);
			httpPost.setHeader("Content-type", "application/json;");
			httpPost.setParams(params);
			StringEntity requestParams = new StringEntity(payload,"UTF_8");
			httpPost.setEntity(requestParams);
			HttpResponse response = httpClient.execute(httpPost);

			HttpEntity getResponseEntity = response.getEntity();
			String ss = EntityUtils.toString(getResponseEntity);
			Gson gson = new Gson();
			Object object = gson.fromJson(ss, template);
			return object;
		} catch (Exception e) {
			return null;
		}
	}

	// << GET definition;
	private Object performHttpGet(String method, Class<?> template) {
		try {
			HttpParams params = new BasicHttpParams();
			HttpConnectionParams.setConnectionTimeout(params, TIMEOUT_CONNECTION);
			HttpConnectionParams.setSoTimeout(params, TIMEOUT_SOCKET);
			//HttpClient httpClient = new DefaultHttpClient();
			HttpClient httpClient = getNewHttpClient();

			HttpGet httpget = new HttpGet(BASE_URL + method);
			httpget.setParams(params);

			HttpResponse response = httpClient.execute(httpget);
			HttpEntity getResponseEntity = response.getEntity();
			String ss = EntityUtils.toString(getResponseEntity);

			Gson gson = new Gson();
			Object object = gson.fromJson(ss, template);
			return object;
		} catch (Exception e) {
			String error = e.toString();
			Log.e("Error in performHttpGet", error);
			return null;
		}
	}

	private static HttpClient getNewHttpClient() {
		try {
			KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
			trustStore.load(null, null);

			SSLSocketFactory sf = new MySSLSocketFactory(trustStore);
			sf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);

			HttpParams params = new BasicHttpParams();
			HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
			HttpProtocolParams.setContentCharset(params, HTTP.UTF_8);
			params.setIntParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, TIMEOUT_CONNECTION);
			params.setIntParameter(CoreConnectionPNames.SO_TIMEOUT, TIMEOUT_SOCKET);
			SchemeRegistry registry = new SchemeRegistry();
			registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
			registry.register(new Scheme("https", sf, 443));

			ClientConnectionManager ccm = new ThreadSafeClientConnManager(params, registry);
			return new DefaultHttpClient(ccm, params);
		}
		catch (Exception e) {
			return new DefaultHttpClient();
		}
	}


	/**
	 * register the user with the system
	 */

	public InsertUserResponseTemplate performUserResgisteration(String pin,
																String password, String subscriptionMode, String fee,
																String subscriptionAction, String Gender, String Country,
																String DeviceType, String City, String ProfileImage, String DOB,
																String MessagePassword, String EmailId) {

		String payload = "";
		try {
			payload = "{\"PinCode\":\"" + pin + "\"," + "\"Password\" :\""
					+ password + "\", " + "\"SubscriptionMode\":\""
					+ subscriptionMode + "\", " + "\"SubscriptionFee\":\""
					+ fee + "\"," + "\"SubscriptionAction\":\""
					+ subscriptionAction + "\"," + "\"Gender\":\"" + Gender
					+ "\"," + "\"Country\":\"" + Country + "\","
					+ "\"DeviceType\":\"" + DeviceType + "\"," + "\"City\":\"" + City
					+ "\"," + "\"ProfileImage\":\"" + ProfileImage + "\","
					+ "\"DOB\":\"" + DOB + "\"," + "\"MessagePassword\":\""
					+ MessagePassword + "\"," + "\"EmailId\":\"" + EmailId
					+ "\"}";

				} catch (Exception e) {
			e.printStackTrace();
		}
		return (InsertUserResponseTemplate) performHttpPost(payload,
				INSERT_USER_DATA, InsertUserResponseTemplate.class);
	}

	/**
	 * Update User Basic Settings
	 */
	public UpdateUserSettingTemplate change_UserBasicSettings(String UserId,
															  String Gender, String Country, String DeviceType, String City,
															  String ProfileImage, String DOB, String pseudoName,
															  String profileDescription, String EmailId) {
		String payload = "";
		try {
			payload = "{\"UserId\":\"" + UserId + "\"," + "\"Gender\" :\""
					+ Gender + "\", " + "\"Country\":\"" + Country + "\", "
					+ "\"DeviceType\":\"" + DeviceType + "\"," + "\"City\":\"" + City
					+ "\"," + "\"ProfileImage\":\"" + ProfileImage + "\","
					+ "\"DOB\":\"" + DOB + "\"," + "\"pseudoName\":\""
					+ pseudoName + "\"," + "\"profileDescription\":\""
					+ profileDescription + "\"," + "\"EmailId\":\"" + EmailId
					+ "\"}";
		} catch (Exception e) {
			e.printStackTrace();
		}
		return (UpdateUserSettingTemplate) performHttpPost(payload,
				UPDATE_USER_SETTING, UpdateUserSettingTemplate.class);

	}

	/**
	 * Update Chat Passwrd(chat settings)
	 */

	public UpdateMessagePaswordTemplate updateMessagePassword(String UserId,
															  String OldPassword, String NewPassword) {
		String payload = "";
		try {
			payload = "{\"UserId\":\"" + UserId + "\"," + "\"OldPassword\" :\""
					+ OldPassword + "\", " + "\"NewPassword\":\"" + NewPassword
					+ "\"}";
		} catch (Exception e) {
			e.printStackTrace();
		}
		return (UpdateMessagePaswordTemplate) performHttpPost(payload,
				UPDATE_MESSAGE_PASSWORD, UpdateMessagePaswordTemplate.class);

	}

	/**
	 * Update User Passwrd
	 */

	public UpdateUserPasswordTemplate updateUserPassword(String UserId,
														 String OldPassword, String NewPassword) {
		String payload = "";
		try {
			payload = "{\"UserId\":\"" + UserId + "\"," + "\"OldPassword\" :\""
					+ OldPassword + "\", " + "\"NewPassword\":\"" + NewPassword
					+ "\"}";

		} catch (Exception e) {

			e.printStackTrace();
		}
		return (UpdateUserPasswordTemplate) performHttpPost(payload,
				UPDATE_USER_PASSWORD, UpdateUserPasswordTemplate.class);

	}

	/**
	 * CHECK IF PROMOTION CODE EXISTS ON SERVER
	 */

	public ValidatePincode validatePincode(String pincode) {
		String payload = "";
		try {
			payload = "{\"PinCode\":\"" + pincode + "\"}";

		} catch (Exception e) {
			e.printStackTrace();
		}
		return (ValidatePincode) performHttpPost(payload,
				VALIDATE_PINCODE, ValidatePincode.class);

	}

	/**
	 * CHECK IF THAT'S IT ID from OPENFIRE EXISTS ON SERVER
	 */

	public ValidateThatsItID validateThatsItID(String pincode) {
		String payload = "";
		try {
			payload = "{\"PinCode\":\"" + pincode + "\"}";

		} catch (Exception e) {
			e.printStackTrace();
		}
		return (ValidateThatsItID) performHttpPost(payload,
				VALIDATE_THATSITID, ValidateThatsItID.class);
	}

	/**
	 * CHECK IF THATS IT ID IS LOGIN ON SERVER
	 */

	public ValidateUserLoginStatus validateUserLoginStatus(String EmailId,String UserLoginStatus,String registrationId,String StatusId) {
		String payload = "";
		try {
				payload = "{\"EmailId\":\"" + EmailId +"\","
				+ "\"UserLoginStatus\":\"" + UserLoginStatus +"\","
				+ "\"DeviceId\":\"" + registrationId +"\","
				+ "\"StatusId\":\"" + StatusId
				+ "\"}";

		} catch (Exception e) {
			e.printStackTrace();
		}
		return (ValidateUserLoginStatus) performHttpPost(payload,
				VALIDATE_USER_LOGIN_STATUS, ValidateUserLoginStatus.class);

	}

	public ValidateUserStatusID performUserStatusID() {
		return (ValidateUserStatusID) performHttpGet(
				VALIDATE_USER_STATUS_ID, ValidateUserStatusID.class);
	}
	/**
	 * Update Subsciption
	 */

	public UpdateSubsciptionTemplate updateSubsciption(String UserId,
													   String SubscriptionMode, String SubscriptionFee,
													   String SubscriptionAction, String SubscribeDate) {
		String payload = "";
		try {
			payload = "{\"UserId\":\"" + UserId + "\","
					+ "\"SubscriptionMode\" :\"" + SubscriptionMode + "\", "
					+ "\"SubscriptionFee\":\"" + SubscriptionFee + "\", "
					+ "\"SubscriptionAction\":\"" + SubscriptionAction + "\", "
					+ "\"SubscribeDate\":\"" + SubscribeDate + "\"}";

		} catch (Exception e) {

			e.printStackTrace();
		}
		return (UpdateSubsciptionTemplate) performHttpPost(payload,
				UPDATE_SUBSCRIPTION, UpdateSubsciptionTemplate.class);

	}

	/**
	 * GET USER KEY
	 */
	public GenerateKeyIdResponse performUserKey(String key) {
		return (GenerateKeyIdResponse) performHttpGet(
				GENERATE_ALPHANUMERIC_KEY, GenerateKeyIdResponse.class);
	}


	/**
	 * GET SUBSCRIPTION FEE
	 */
	public SubcriptionFeeTemplate getSubscriptionFee() {
		String GET_FEE = GET_SUBSCRIPTION_FEE;
		return (SubcriptionFeeTemplate) performHttpGet(GET_FEE,
				SubcriptionFeeTemplate.class);
	}

	/**
	 * GET REGISTRATION DETAILS
	 */
	public FetchUserSettingTemplates getRegistrationdetails(String userId) {
		String GET_DETAILS = GET_REGISTRATION_DETAILS + "/" + userId;
		return (FetchUserSettingTemplates) performHttpGet(GET_DETAILS,
				FetchUserSettingTemplates.class);
	}

	/**
	 * GET NO OF DAYS LEFT
	 */
	public CheckSubscriptionKeyValidity getUserValidity(String userId) {

		String Check_Validity = CHECK_SUBSCRIPTION_VALIDITY + "/" + userId;
		return (CheckSubscriptionKeyValidity) performHttpGet(Check_Validity,
				CheckSubscriptionKeyValidity.class);
	}

	/**
	 * GET SUBSCRIPTION HISTORY
	 */
	public GetSubscriptionHistoryTemplate getSubscriptionHistory(String userId) {

		String Check_Validity = GET_SUBSCRIPTION_HISTORY + "/" + userId;
		return (GetSubscriptionHistoryTemplate) performHttpGet(Check_Validity,
				GetSubscriptionHistoryTemplate.class);
	}

	/**
	 * Authenticate user chat password
	 */
	public CheckMessage_ChatPasswrd authenticateChatPasswd(String userId,
														   String password) {

		String Check_ChatPassword = AUTHENTICATE_MESSAGE_PASSWORD + "/"
				+ userId + "/" + password;
		return (CheckMessage_ChatPasswrd) performHttpGet(Check_ChatPassword,
				CheckMessage_ChatPasswrd.class);
	}

	/**
	 * Authenticate user service
	 */

	public AuthenticateUserServiceTemplate getUserSignIn(String EmailId,
														 String Password) {
		String Authenticate_User = AUTHENTICATE_USER_SERVICE + "/" + EmailId
				+ "/" + Password;

		return (AuthenticateUserServiceTemplate) performHttpGet(
				Authenticate_User, AuthenticateUserServiceTemplate.class);
	}
}

