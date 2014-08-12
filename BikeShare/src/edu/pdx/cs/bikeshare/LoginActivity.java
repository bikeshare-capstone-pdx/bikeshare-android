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

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

public class LoginActivity extends Activity {

	public static final String apiUrl = "http://api.bikeshare.cs.pdx.edu";
	private final String signUpPath = "/REST/1.0/login/signup";
	public static final String tag = "LoginActivity";
	public static final String EXTRA_MESSAGE = "edu.pdx.cs.bikeshare.MESSAGE";
	private Spinner spinner;
 
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
 
        spinner = (Spinner) findViewById(R.id.users_spinner);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.user_array, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinner.setAdapter(adapter);
    }
 
  
    // Called when the user clicks the Send button 
    public void login(View view) {
       	String message = null;
    	int u_id = signUp();
    	if (u_id != -1){
    		message = Integer.toString(u_id);
    	}       
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra(EXTRA_MESSAGE, message);
        startActivity(intent);
    }

	private int signUp() {
		System.out.println("Starting to sign up");
		JSONObject result;
		HttpClient web = new DefaultHttpClient();
		HttpPost httppost = new HttpPost(apiUrl + signUpPath);
		
		String [] fullName = spinner.getSelectedItem().toString().split(" ");
		String first = fullName[0];
		String last = fullName[1];
		System.out.println(first);
		System.out.println(last);
		
		List<NameValuePair> params = new ArrayList<NameValuePair>(3);
		params.add(new BasicNameValuePair("first_name", first));
		params.add(new BasicNameValuePair("last_name", last));
		for(NameValuePair n: params){
			System.out.println(n);
		}
		
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
		    // Return user id
		    return result.getInt("USER_ID");
		    } 
		    else {
		    	System.out.println("Didn't get anything back");
		    	return -1;
		    }
		} 
		catch (ClientProtocolException e) {
		e.printStackTrace();
		} 
		catch (IOException e) {
		e.printStackTrace();
		} 
		catch (JSONException e) {
		e.printStackTrace();
		}
		return -1;
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.login, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	

}
