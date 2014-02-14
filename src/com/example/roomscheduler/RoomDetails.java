package com.example.roomscheduler;

import java.text.DateFormat;
import java.util.Date;

import android.app.Activity;
import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.support.v4.app.NavUtils;
import android.text.format.DateUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

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
	
	static long currentTime = 0;
	
	// This is the select criteria
	//static final String SELECTION = "((" +
	//		CalendarContract.Calendars.CALENDAR_DISPLAY_NAME + " LIKE '%TestCal%'))";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_room_details);
		// Show the Up button in the action bar.
		setupActionBar();
		
		// Set the time that this page was last refreshed.
		currentTime = new Date().getTime();
		TextView dateTime = (TextView) findViewById(R.id.roomDetailsDateTime);
		DateFormat dateFormat = java.text.DateFormat.getDateTimeInstance();
		dateTime.setText(dateFormat.format(new Date().getTime()));	
		
		// Put the room name in the display.
		Intent intent = getIntent();
	    String roomName = intent.getStringExtra(ChooseRoom.CALANDER_DISPLAY_NAME);
	    TextView roomNameText = (TextView) findViewById(R.id.roomDetailsRoomName);
	    roomNameText.setText(roomName);
	    
	    
	    String[] fromColumns = {CalendarContract.Instances.TITLE, 
	    		CalendarContract.Instances.BEGIN, CalendarContract.Instances.END};
		int[] toViews = {R.id.eventTitle, R.id.startTime, R.id.endTime };

	    // Create an empty adapter we will use to display the loaded data.
		// We pass null for the cursor, then update it in onLoadFinished()
		mAdapter = new SimpleCursorAdapter(this, 
		        R.layout.event_listitem_view, null,
		        fromColumns, toViews, 0);
		ListView eventListView = (ListView) findViewById(R.id.roomDetailsEventList);
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
					TextView text = (TextView) view;
					if (text == null)
					{
						return false;
					}										
					text.setText(DateUtils.formatSameDayTime(cursor.getLong(columnIndex), currentTime, DateFormat.SHORT, DateFormat.SHORT));
					return true;
		    	}
		    	else
		    	{
		    		return false;
		    	}
		    }
		};
		mAdapter.setViewBinder(binder);
		
		// If user selects an event in the list, invoke a calendar service to view the event.
		eventListView.setOnItemClickListener(new OnItemClickListener() {
			  @Override
			  public void onItemClick(AdapterView<?> parent, View view,
			    int position, long id) 
			  {
			    Cursor cursor = mAdapter.getCursor();
			    cursor.moveToPosition(position);
			    int eventIdColumnIndex = cursor.getColumnIndex(CalendarContract.Instances.EVENT_ID);
			    long eventId = cursor.getLong(eventIdColumnIndex);
			    Uri uri = ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, eventId);
			    Intent intent = new Intent(Intent.ACTION_VIEW)
			       .setData(uri);
			    startActivity(intent);
			    
			  }
			});
		
		// Prepare the loader.  Either re-connect with an existing one,
		// or start a new one.
		getLoaderManager().initLoader(INSTANCE_LOADER, null, this);
	}

	// Called when a new Loader needs to be created
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		Intent intent = getIntent();
		String selection = "(" + CalendarContract.Events.CALENDAR_ID + "=" + intent.getStringExtra(ChooseRoom.CALANDER_ID) +") AND ((" +
				CalendarContract.Instances.BEGIN + "<" + currentTime + " AND " + CalendarContract.Instances.END + " > " + currentTime + ") OR (" + 
				CalendarContract.Instances.BEGIN + ">" + currentTime + " AND " + CalendarContract.Instances.BEGIN + "<" + (currentTime + DateUtils.DAY_IN_MILLIS) + "))";
		String sortOrder = "begin";
		
		Uri.Builder builder = CalendarContract.Instances.CONTENT_URI.buildUpon();
		
		ContentUris.appendId(builder, currentTime - DateUtils.WEEK_IN_MILLIS);
		ContentUris.appendId(builder, currentTime + DateUtils.WEEK_IN_MILLIS);
		
		return new CursorLoader(this, builder.build(), null, selection, null, sortOrder);
	}

	// Called when a previously created loader has finished loading
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) 
	{
		// Update the room status
		TextView roomStatus = (TextView) findViewById(R.id.roomDetailsRoomStatus);
		if (data.getCount() < 1)
		{
			roomStatus.setText(R.string.room_status_free);
		}
		else
		{
			data.moveToFirst();
	    	// TODO: Put these in a more central place so they are not looked up each time.
			int startTimeIndex = data.getColumnIndex(CalendarContract.Instances.BEGIN);
	    	int endTimeIndex = data.getColumnIndex(CalendarContract.Instances.END);
	    	long startTime = data.getLong(startTimeIndex);
	    	long endTime = data.getLong(endTimeIndex);
	    	Resources res = getResources();
	    	DateFormat dateFormat = java.text.DateFormat.getTimeInstance(DateFormat.SHORT);
	    	if (startTime < currentTime)
	    	{
	    		// Room is currently in use.  List status as "Busy until..."	    		
	    		// TODO: Check to see if there are other events right after this one
	    		// and adjust the free time accordingly.
	    		roomStatus.setText(String.format(res.getString(R.string.room_status_busy_until), 
	    				dateFormat.format(endTime)));
	    	}
	    	else
	    	{
	    		// Event is coming up later.  List status as "Free until..."
	    		roomStatus.setText(String.format(res.getString(R.string.room_status_free_until), 
	    				dateFormat.format(startTime)));
	    	}
		}
		
		// Update the list of events in the room:
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
