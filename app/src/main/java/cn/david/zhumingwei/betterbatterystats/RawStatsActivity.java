/*
 * Copyright (C) 2011-2015 asksven
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *et
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package cn.david.zhumingwei.betterbatterystats;

/**
 * Shows alarms in a list
 * @author sven
 */

import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.asksven.android.common.privateapiproxies.BatteryInfoUnavailableException;
import com.asksven.android.common.privateapiproxies.BatteryStatsProxy;
import com.asksven.android.common.privateapiproxies.StatElement;
import com.asksven.android.common.utils.DateUtils;

import java.util.ArrayList;

import cn.david.zhumingwei.betterbatterysatats.R;
import cn.david.zhumingwei.betterbatterystats.adapters.StatsAdapter;
import cn.david.zhumingwei.betterbatterystats.data.StatsProvider;

public class RawStatsActivity extends ActionBarListActivity implements AdapterView.OnItemSelectedListener
{
	/**
	 * The logging TAG
	 */
	private static final String TAG = "KernelWakelocksActivity";
	
	/**
	 * The Stat to be displayed
	 */
	private int m_iStat = 0; 

	
	/**
	 * a progess dialog to be used for long running tasks
	 */
	ProgressDialog m_progressDialog;
	
	/**
	 * The ArrayAdpater for rendering the ListView
	 */
	private StatsAdapter m_listViewAdapter;
	
	/**
	 * @see android.app.Activity#onCreate(Bundle@SuppressWarnings("rawtypes")
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.raw_stats);
		
		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		toolbar.setTitle(getString(R.string.label_raw_stats));

	    setSupportActionBar(toolbar);
	    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
	    getSupportActionBar().setDisplayUseLogoEnabled(false);
		
		// Spinner for selecting the stat
		Spinner spinnerStat = (Spinner) findViewById(R.id.spinnerStat);
		
		ArrayAdapter spinnerStatAdapter = ArrayAdapter.createFromResource(
	            this, R.array.stats, R.layout.bbs_spinner_layout);
		spinnerStatAdapter.setDropDownViewResource(R.layout.bbs_spinner_dropdown_item);
	    
		spinnerStat.setAdapter(spinnerStatAdapter);
		// setSelection MUST be called after setAdapter
		spinnerStat.setSelection(m_iStat);
		spinnerStat.setOnItemSelectedListener(this);
		
		TextView tvSince = (TextView) findViewById(R.id.TextViewSince);

        long sinceMs = SystemClock.elapsedRealtime();

        if (sinceMs != -1)
        {
	        String sinceText = DateUtils.formatDuration(sinceMs);
	        
	        tvSince.setText(sinceText);
	    	Log.i(TAG, "Since " + sinceText);
        }
        else
        {
	        tvSince.setText("n/a ");
	    	Log.i(TAG, "Since: n/a ");
        	
        }

	}
	
	/* Request updates at startup */
	@Override
	protected void onResume()
	{
		super.onResume();
		new LoadStatData().execute(this);
	}

    /** 
     * Add menu items
     * 
     * @see android.app.Activity#onCreateOptionsMenu(Menu)
     */
    public boolean onCreateOptionsMenu(Menu menu)
    {  
    	MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.kernelwakelocks_menu, menu);
        return true;
    }  

    /** 
     * Define menu action
     * 
     * @see android.app.Activity#onOptionsItemSelected(MenuItem)
     */
    public boolean onOptionsItemSelected(MenuItem item)
    {  
        switch (item.getItemId())
        {  
	        case R.id.refresh:
            	// Refresh
	        	doRefresh();
            	break;

        }  
        return false;  
    }    

	
	private void doRefresh()
	{
		BatteryStatsProxy.getInstance(this).invalidate();
		new LoadStatData().execute(this);
		if (m_listViewAdapter != null)
		{
			m_listViewAdapter.notifyDataSetChanged();
		}
	}
	
	/**
	 * Take the change of selection from the spinners into account and refresh the ListView
	 * with the right data
	 */
	public void onItemSelected(AdapterView<?> parent, View v, int position, long id)
	{
		// this method is fired even if nothing has changed so we nee to find that out
		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);

		boolean bChanged = false;
		
		// id is in the order of the spinners, 0 is stat, 1 is stat_type
		if (parent == (Spinner) findViewById(R.id.spinnerStat))
		{
			int iNewStat = position;
			if ( m_iStat != iNewStat )
			{
				m_iStat = iNewStat;
				bChanged = true;
			}
			else
			{
				return;
			}
		}
		else
		{
    		Log.e(TAG, "RawStatsActivity.onItemSelected error. ID could not be resolved");
    		Toast.makeText(this, getString(R.string.info_unknown_state), Toast.LENGTH_SHORT).show();

		}

        if (bChanged)
        {
        	doRefresh();
        }
	}

	public void onNothingSelected(AdapterView<?> parent)
	{
		// do nothing
	}


	// @see http://code.google.com/p/makemachine/source/browse/trunk/android/examples/async_task/src/makemachine/android/examples/async/AsyncTaskExample.java
	// for more details
	private class LoadStatData extends AsyncTask<Object, Integer, StatsAdapter>
	{
		private Exception m_exception = null;
		@Override
	    protected StatsAdapter doInBackground(Object... refresh)
	    {
			m_listViewAdapter = null;
			try
			{
				Log.i(TAG, "LoadStatData: refreshing display for raw stats");
				
				ArrayList<StatElement> stats = null;
				StatsProvider provider = StatsProvider.getInstance(RawStatsActivity.this);
				// constants are related to arrays.xml string-array name="stats"
				switch (m_iStat)
				{
					case 0:
						stats = provider.getCurrentOtherUsageStatList(true, false, false);
						break;
					case 1:
						stats = provider.getCurrentKernelWakelockStatList(false, 0, 0);
						break;
					case 2:
						stats = provider.getCurrentWakelockStatList(false, 0, 0);
						break;
					case 3:
						stats = provider.getCurrentAlarmsStatList(false);
						break;
					case 4:
						stats = provider.getCurrentNetworkUsageStatList(false);
						break;
					case 5:
						stats = provider.getCurrentCpuStateList(false);
						break;
					case 6:
						stats = provider.getCurrentProcessStatList(false, 0);
						break;
				}
				m_listViewAdapter = new StatsAdapter(RawStatsActivity.this, stats, RawStatsActivity.this);
			}
			catch (BatteryInfoUnavailableException e)
			{
				//Log.e(TAG, e.getMessage(), e.fillInStackTrace());
				Log.e(TAG, "Exception: "+Log.getStackTraceString(e));
				m_exception = e;

			}
			catch (Exception e)
			{
				//Log.e(TAG, e.getMessage(), e.fillInStackTrace());
				Log.e(TAG, "Exception: "+Log.getStackTraceString(e));
				m_exception = e;

			}

	    	//StatsActivity.this.setListAdapter(m_listViewAdapter);
	        // getStatList();
	        return m_listViewAdapter;
	    }
		
//		@Override
		protected void onPostExecute(StatsAdapter o)
	    {
//			super.onPostExecute(o);
	        // update hourglass
			try
			{
		    	if (m_progressDialog != null)
		    	{
		    		m_progressDialog.dismiss(); //hide();
		    		m_progressDialog = null;
		    	}
			}
			catch (Exception e)
			{
				// nop
			}
			finally 
			{
				m_progressDialog = null;
			}
			
	    	if (m_exception != null)
	    	{
	    		if (m_exception instanceof BatteryInfoUnavailableException)
	    		{
	    			Snackbar
					  .make(findViewById(android.R.id.content), R.string.info_service_connection_error, Snackbar.LENGTH_LONG)
					  .show();
//	    			Toast.makeText(RawStatsActivity.this,
//	    					getString(R.string.info_service_connection_error),
//	    					Toast.LENGTH_LONG).show();

	    		}
	    		else
	    		{
	    			Snackbar
					  .make(findViewById(android.R.id.content), R.string.info_unknown_stat_error, Snackbar.LENGTH_LONG)
					  .show();
//
//	    			Toast.makeText(RawStatsActivity.this,
//	    					getString(R.string.info_unknown_stat_error),
//	    					Toast.LENGTH_LONG).show();
	    			
	    		}
	    	}
        	if (o != null)
        	{
        		o.setTotalTime(SystemClock.elapsedRealtime());
        		
        	}
	    	RawStatsActivity.this.setListAdapter(o);
	    }
//	    @Override
	    protected void onPreExecute()
	    {
	        // update hourglass
	    	// @todo this code is only there because onItemSelected is called twice
	    	if (m_progressDialog == null)
	    	{
	    		try
	    		{
			    	m_progressDialog = new ProgressDialog(RawStatsActivity.this);
			    	m_progressDialog.setMessage(getString(R.string.message_computing));
			    	m_progressDialog.setIndeterminate(true);
			    	m_progressDialog.setCancelable(false);
			    	m_progressDialog.show();
	    		}
	    		catch (Exception e)
	    		{
	    			m_progressDialog = null;
	    		}
	    	}
	    }
	}
	
}
