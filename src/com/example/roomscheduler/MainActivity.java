package com.example.roomscheduler;

import java.util.HashSet;
import java.util.Set;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

public class MainActivity extends Activity {

	public final static String EXTRA_MESSAGE = "com.example.myfirstapp.MESSAGE";
	private static final int PICK_ROOM_REQUEST = 1;
	public static final String PREFS_NAME = "MyPrefsFile";
	
	
    @Override
    protected void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        //SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        setDefaultPreferences(settings);
    }
    
    // Sets up default preferences for anything that is not already defined.
    private void setDefaultPreferences(SharedPreferences prefs)
    {
    	if (!prefs.contains(SettingsActivity.CALENDAR_NAMES))
    	{
    		SharedPreferences.Editor editor = prefs.edit();
    		Set<String> calNames = new HashSet<String>() {{  
    			  add("Nate TestCal 1"); add("Nate TestCal 2"); 
    			}}; 
    		editor.putStringSet(SettingsActivity.CALENDAR_NAMES, calNames);
    		editor.commit();
    	}
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    
    public boolean onOptionsItemSelected(MenuItem item) 
    {
        // Handle item selection
        switch (item.getItemId()) {
        case R.id.action_settings:
        	Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }
    
    /** Called when the user clicks the Send button */
    public void sendMessage(View view) {
        // Do something in response to button
    	Intent intent = new Intent(this, DisplayMessageActivity.class);
    	EditText editText = (EditText) findViewById(R.id.edit_message);
    	String message = editText.getText().toString();
    	intent.putExtra(EXTRA_MESSAGE, message);
    	startActivity(intent);
    }
    
    public void chooseRoom(View view) {
        // Do something in response to button
    	Intent intent = new Intent(this, ChooseRoom.class);
    	startActivity(intent);
    }
}
