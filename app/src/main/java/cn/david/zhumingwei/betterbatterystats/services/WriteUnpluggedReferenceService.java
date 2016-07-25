/*
 * Copyright (C) 2011-14 asksven
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package cn.david.zhumingwei.betterbatterystats.services;

import android.app.IntentService;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.util.Log;

import com.asksven.android.common.utils.DateUtils;

import cn.david.zhumingwei.betterbatterystats.Wakelock;
import cn.david.zhumingwei.betterbatterystats.data.Reference;
import cn.david.zhumingwei.betterbatterystats.data.ReferenceStore;
import cn.david.zhumingwei.betterbatterystats.data.StatsProvider;
import cn.david.zhumingwei.betterbatterystats.widgetproviders.LargeWidgetProvider;

/**
 * @author sven
 *
 */
public class WriteUnpluggedReferenceService extends IntentService
{
	private static final String TAG = "WriteUnpluggedReferenceService";

	public WriteUnpluggedReferenceService()
	{
	    super("WriteUnpluggedReferenceService");
	}
	
	@Override
	public void onHandleIntent(Intent intent)
	{
		Log.i(TAG, "Called at " + DateUtils.now());
		try
		{
			// Store the "since unplugged ref
			Wakelock.aquireWakelock(this);
			StatsProvider.getInstance(this).setReferenceSinceUnplugged(0);

			Intent i = new Intent(ReferenceStore.REF_UPDATED).putExtra(Reference.EXTRA_REF_NAME, Reference.UNPLUGGED_REF_FILENAME);
		    this.sendBroadcast(i);

//			// save a new current ref
//			StatsProvider.getInstance(this).setCurrentReference(0);
//
//			i = new Intent(ReferenceStore.REF_UPDATED).putExtra(Reference.EXTRA_REF_NAME, Reference.CURRENT_REF_FILENAME);
//		    this.sendBroadcast(i);

			// check the battery level and if 100% the store "since charged" ref
			Intent batteryIntent = this.getApplicationContext().registerReceiver(null,
                    new IntentFilter(Intent.ACTION_BATTERY_CHANGED));

			int rawlevel = batteryIntent.getIntExtra("level", -1);
			double scale = batteryIntent.getIntExtra("scale", -1);
			double level = -1;
			if (rawlevel >= 0 && scale > 0)
			{
				// normalize level to [0..1]
			    level = rawlevel / scale;
			}

			Log.i(TAG, "Bettery level on uplug is " + level );

			if (level == 1)
			{
				try
				{
					Log.i(TAG, "Level was 100% at unplug, serializing 'since charged'");
					StatsProvider.getInstance(this).setReferenceSinceCharged(0);

					i = new Intent(ReferenceStore.REF_UPDATED).putExtra(Reference.EXTRA_REF_NAME, Reference.CHARGED_REF_FILENAME);
				    this.sendBroadcast(i);

					// save a new current ref
					StatsProvider.getInstance(this).setCurrentReference(0);

					i = new Intent(ReferenceStore.REF_UPDATED).putExtra(Reference.EXTRA_REF_NAME, Reference.CURRENT_REF_FILENAME);
				    this.sendBroadcast(i);

				}
				catch (Exception e)
				{
					Log.e(TAG, "An error occured: " + e.getMessage());
				}
				
			}
			// Build the intent to update the widget
			Intent intentRefreshWidgets = new Intent(LargeWidgetProvider.WIDGET_UPDATE);
			this.sendBroadcast(intentRefreshWidgets);
			

		}
		catch (Exception e)
		{
			Log.e(TAG, "An error occured: " + e.getMessage());
		}
		finally
		{
			Wakelock.releaseWakelock();
		}
		
		stopSelf();
	}

	@Override
	public IBinder onBind(Intent intent)
	{
		return null;
	}
	
	@Override
	public void onDestroy()
	{
		Log.e(TAG, "Destroyed at" + DateUtils.now());
		Wakelock.releaseWakelock();
	}

}