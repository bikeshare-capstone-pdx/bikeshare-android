package edu.pdx.cs.bikeshare;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.views.overlay.OverlayItem;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

public class LoginActivity extends Activity {

	public static final String apiUrl = "http://api.bikeshare.cs.pdx.edu";
	public static final String tag = "LoginActivity";
	public static final String EXTRA_MESSAGE = "edu.pdx.cs.bikeshare.MESSAGE";
	private Spinner spinner;
	private List<String> user_spinner_list = new ArrayList<String>();
	private ArrayList<User> userList = new ArrayList<User>();
 
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
 
        spinner = (Spinner) findViewById(R.id.users_spinner);

        new LoginUser().execute();

        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, user_spinner_list);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinner.setAdapter(adapter);
    }
 
  
    // Called when the user clicks the Send button 
    public void login(View view) {
        String message = null;
        for (User u: userList){
    	  if(u.user_name.equals(spinner.getSelectedItem().toString())){  
    	    message = Integer.toString(u.user_id);
    	    break;
    	  }
        }
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra(EXTRA_MESSAGE, message);
        startActivity(intent);
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
	
    private class LoginUser extends AsyncTask<Void, Void, String> {
        private final static String route = "/REST/1.0/users/all";
        private final static String tag = "LoginUser";

		@Override
        protected String doInBackground(Void... params) {
            // Call REST API to login users
            HttpClient web = new DefaultHttpClient();
            String apiData = null;
            try {
                HttpResponse resp = web.execute(new HttpGet(apiUrl + route));
                StatusLine status = resp.getStatusLine();
                if (status.getStatusCode() == HttpStatus.SC_OK) {
                    // REST API returned success.
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    resp.getEntity().writeTo(out);
                    out.close();
                    apiData = out.toString();
                } else {
                    // REST API returned an error.
                    resp.getEntity().getContent().close();
                    Log.e(tag,"REST API returned error: " + status.getReasonPhrase());
                }
            } catch (ClientProtocolException e) {
                // HTTP barfed.
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return apiData;
        }
        
        @Override
        protected void onPostExecute(String result) {
            // If we received data from the API, parse the JSON.
            if (result != null) {
                try {
                	JSONObject jApiData = new JSONObject(result);
                    JSONArray jUsers = jApiData.getJSONArray("users");
                    for (int i = 0; i < jUsers.length(); i++) {
                        JSONObject jUser = jUsers.getJSONObject(i);
                        
                        // Construct a user object from the data.
                        User u = new User();
                        u.user_name = jUser.getString("USER_NAME");
                        u.user_id = jUser.getInt("USER_ID");
                        user_spinner_list.add(u.user_name);
                        userList.add(u);
                    }
                }
                catch (JSONException e) {
                    // Failed to parse the JSON.
                    e.printStackTrace();
                }
            } 
            else {
                // Failed to get data from the API.
            }
        }
    }
}
