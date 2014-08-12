package edu.pdx.cs.bikeshare;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

public class UserSignUp implements Runnable{


	private final String apiUrl = LoginActivity.apiUrl;
	private final String signUpPath = "/REST/1.0/login/signup";
	private String user_name = null;
	public static int user_id = -1;
	
	public UserSignUp(String string) {
		user_name = string;
	}

	public int signUp() {
		System.out.println("Starting to sign up");
		JSONObject result;
		HttpClient web = new DefaultHttpClient();
		HttpPost httppost = new HttpPost(apiUrl + signUpPath);
		
		String[] fullName = user_name.split(" ");
		String first = fullName[0];
		String last = fullName[1];
		
		List<NameValuePair> params = new ArrayList<NameValuePair>(3);
		params.add(new BasicNameValuePair("first_name", first));
		params.add(new BasicNameValuePair("last_name", last));
		
		try {
			httppost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
			HttpResponse response = web.execute(httppost);
			HttpEntity entity = response.getEntity();
			if (entity != null) {
				InputStream instream = entity.getContent();
				String resp = "";
				String line;
				BufferedReader rd = new BufferedReader(new InputStreamReader(instream));
				while ((line = rd.readLine()) != null) {
					resp += line;
				}
				result = new JSONObject(resp);
				System.out.println(resp);
				// Return full string
				return result.getInt("USER_ID");
			} 
			else {
				System.out.println("Didn't get anything back");
				return -1;
			}
		} 
		catch (ClientProtocolException e) {
		// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		catch (IOException e) {
		// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		catch (JSONException e) {
		// TODO Auto-generated catch block
			e.printStackTrace();
		}
			return -1;
	}
	
	@Override
	public void run() {
		android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);
		synchronized (this) {
			user_id = signUp();
			System.out.println(user_id);
		}
	}
}
