package edu.pdx.cs.bikeshare;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

public class LoginActivity extends Activity{
	public static final String EXTRA_MESSAGE = "edu.pdx.cs.bikeshare.MESSAGE";
	public static final String apiUrl = "http://api.bikeshare.cs.pdx.edu";
	public static final String tag = "LoginActivity";
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
    public void login(View view) throws InterruptedException {
		Thread th = new Thread(new UserSignUp(spinner.getSelectedItem().toString()));
		th.start();
		th.join();
		String message = Integer.toString(UserSignUp.user_id);
    	if (!message.equals("-1")){
            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra(EXTRA_MESSAGE, message);
            startActivity(intent);
    	}
    	else {
    		System.out.println("Invalid user id");
    	}

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
