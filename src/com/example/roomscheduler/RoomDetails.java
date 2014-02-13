package com.example.roomscheduler;

import java.text.DateFormat;
import java.util.Date;

import android.app.Activity;
import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.support.v4.app.NavUtils;
import android.text.format.DateUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

public class RoomDetails extends Activity 
 	implements LoaderManager.LoaderCallbacks<Cursor> 
{
	// This is the Adapter being used to display the list's data
	SimpleCursorAdapter mAdapter;

	// These are the Contacts rows that we will retrieve
	static final String[] PROJECTION = new String[] 
		{
		CalendarContract.Events.CALENDAR_ID,
		CalendarContract.Events.TITLE,
		CalendarContract.Events.DTSTART
		};

	static final int INSTANCE_LOADER = 0;
	static final int EVENT_LOADER = 1;
	
	// This is the select criteria
	//static final String SELECTION = "((" +
	//		CalendarContract.Calendars.CALENDAR_DISPLAY_NAME + " LIKE '%TestCal%'))";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_room_details);
		// Show the Up button in the action bar.
		setupActionBar();
		
		// Put the room name in the display.
		Intent intent = getIntent();
	    String roomName = intent.getStringExtra(ChooseRoom.CALANDER_DISPLAY_NAME);
	    TextView roomNameText = (TextView) findViewById(R.id.roomNameTextView);
	    roomNameText.setText(roomName);
	    
	    
	    String[] fromColumns = {CalendarContract.Instances.TITLE, 
	    		CalendarContract.Instances.BEGIN, CalendarContract.Instances.END};
		int[] toViews = {R.id.eventTitle, R.id.startTime, R.id.endTime };

	    // Create an empty adapter we will use to display the loaded data.
		// We pass null for the cursor, then update it in onLoadFinished()
		mAdapter = new SimpleCursorAdapter(this, 
		        R.layout.event_listitem_view, null,
		        fromColumns, toViews, 0);
		ListView eventListView = (ListView) findViewById(R.id.eventList);
		eventListView.setAdapter(mAdapter);
		
		SimpleCursorAdapter.ViewBinder binder = new SimpleCursorAdapter.ViewBinder() {
		    @Override
		    public boolean setViewValue(View view, Cursor cursor, int columnIndex) 
		    {
		    	// TODO: Put these in a more central place so they are not looked up each time.
		        int startTimeIndex = cursor.getColumnIndex(CalendarContract.Instances.BEGIN);
		    	int endTimeIndex = cursor.getColumnIndex(CalendarContract.Instances.END);
		    	if (columnIndex == startTimeIndex || columnIndex == endTimeIndex)
		    	{
			    	long now = new Date().getTime();
					TextView text = (TextView) view;
					if (text == null)
					{
						return false;
					}										
					text.setText(DateUtils.formatSameDayTime(cursor.getLong(columnIndex), now, DateFormat.SHORT, DateFormat.SHORT));
					return true;
		    	}
		    	else
		    	{
		    		return false;
		    	}
		    }
		};
		mAdapter.setViewBinder(binder);
		
		// Prepare the loader.  Either re-connect with an existing one,
		// or start a new one.
		getLoaderManager().initLoader(INSTANCE_LOADER, null, this);
	}

	// Called when a new Loader needs to be created
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		Intent intent = getIntent();
		long now = new Date().getTime();
		String selection = "(" + CalendarContract.Events.CALENDAR_ID + "=" + intent.getStringExtra(ChooseRoom.CALANDER_ID) +") AND ((" +
				CalendarContract.Instances.BEGIN + "<" + now + " AND " + CalendarContract.Instances.END + " > " + now + ") OR (" + 
				CalendarContract.Instances.BEGIN + ">" + now + " AND " + CalendarContract.Instances.BEGIN + "<" + (now + DateUtils.DAY_IN_MILLIS) + "))";
		String sortOrder = "begin DESC";
		
		Uri.Builder builder = CalendarContract.Instances.CONTENT_URI.buildUpon();
		
		ContentUris.appendId(builder, now - DateUtils.WEEK_IN_MILLIS);
		ContentUris.appendId(builder, now + DateUtils.WEEK_IN_MILLIS);
		
		return new CursorLoader(this, builder.build(), null, selection, null, sortOrder);
	}

	// Called when a previously created loader has finished loading
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		// Swap the new cursor in.  (The framework will take care of closing the
		// old cursor once we return.)
		mAdapter.swapCursor(data);
	}

	// Called when a previously created loader is reset, making the data unavailable
	public void onLoaderReset(Loader<Cursor> loader) {
		// This is called when the last Cursor provided to onLoadFinished()
		// above is about to be closed.  We need to make sure we are no
		// longer using it.
		mAdapter.swapCursor(null);
	}
	
	/**
	 * Set up the {@link android.app.ActionBar}.
	 */
	private void setupActionBar() {

		getActionBar().setDisplayHomeAsUpEnabled(true);

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.room_details, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			// This ID represents the Home or Up button. In the case of this
			// activity, the Up button is shown. Use NavUtils to allow users
			// to navigate up one level in the application structure. For
			// more details, see the Navigation pattern on Android Design:
			//
			// http://developer.android.com/design/patterns/navigation.html#up-vs-back
			//
			NavUtils.navigateUpFromSameTask(this);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

}
