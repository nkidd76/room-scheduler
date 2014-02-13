package com.example.roomscheduler;

import android.app.ActionBar.LayoutParams;
import android.app.Activity;
import android.app.ListActivity;
import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.view.Gravity;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleCursorAdapter;

public class ChooseRoom extends ListActivity
implements LoaderManager.LoaderCallbacks<Cursor> {

// This is the Adapter being used to display the list's data
SimpleCursorAdapter mAdapter;

// These are the Contacts rows that we will retrieve
static final String[] PROJECTION = new String[] {CalendarContract.Calendars._ID,
	CalendarContract.Calendars.CALENDAR_DISPLAY_NAME};

// This is the select criteria
static final String SELECTION = "((" +
		CalendarContract.Calendars.CALENDAR_DISPLAY_NAME + " LIKE '%TestCal%'))";

static final String CALANDER_ID = "com.example.roomscheduler.ChooseRoom.CALENDAR_ID";
static final String CALANDER_DISPLAY_NAME = "com.example.roomscheduler.ChooseRoom.CALENDAR_DISPLAY_NAME";

@Override
protected void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	
	// Create a progress bar to display while the list loads
	ProgressBar progressBar = new ProgressBar(this);
	progressBar.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,
	        LayoutParams.WRAP_CONTENT, Gravity.CENTER));
	progressBar.setIndeterminate(true);
	getListView().setEmptyView(progressBar);
	
	// Must add the progress bar to the root of the layout
	ViewGroup root = (ViewGroup) findViewById(android.R.id.content);
	root.addView(progressBar);
	
	// For the cursor adapter, specify which columns go into which views
	//String[] fromColumns = {ContactsContract.Data.DISPLAY_NAME};
	String[] fromColumns = {CalendarContract.Calendars.CALENDAR_DISPLAY_NAME};
	int[] toViews = {android.R.id.text1}; // The TextView in simple_list_item_1
	
	// Create an empty adapter we will use to display the loaded data.
	// We pass null for the cursor, then update it in onLoadFinished()
	mAdapter = new SimpleCursorAdapter(this, 
	        android.R.layout.simple_list_item_1, null,
	        fromColumns, toViews, 0);
	setListAdapter(mAdapter);
	
	// Prepare the loader.  Either re-connect with an existing one,
	// or start a new one.
	getLoaderManager().initLoader(0, null, this);
}

// Called when a new Loader needs to be created
public Loader<Cursor> onCreateLoader(int id, Bundle args) {
	// Now create and return a CursorLoader that will take care of
	// creating a Cursor for the data being displayed.
	return new CursorLoader(this, CalendarContract.Calendars.CONTENT_URI,
	        PROJECTION, SELECTION, null, CalendarContract.Calendars.CALENDAR_DISPLAY_NAME);
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

@Override 
public void onListItemClick(ListView l, View v, int position, long id) {
	// Do something when a list item is clicked
	Cursor cursor = mAdapter.getCursor();
	cursor.moveToPosition(position);
	Uri calUri = ContentUris.withAppendedId(CalendarContract.Calendars.CONTENT_URI, id);
	Intent result = new Intent(this, RoomDetails.class);              
	result.setData(calUri);
	result.putExtra(CALANDER_ID, cursor.getString(0));
	result.putExtra(CALANDER_DISPLAY_NAME, cursor.getString(1));
	//setResult(Activity.RESULT_OK, result);
	startActivity(result);
}

@Override
public boolean onCreateOptionsMenu(Menu menu) {
	// Inflate the menu; this adds items to the action bar if it is present.
	getMenuInflater().inflate(R.menu.view_calendars, menu);
	return true;
}

}
