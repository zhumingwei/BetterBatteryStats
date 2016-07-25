/*
 * Copyright (C) 2012 asksven
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
 * 
 * This file was contributed by two forty four a.m. LLC <http://www.twofortyfouram.com>
 * unter the terms of the Apache License, Version 2.0
 */

package cn.david.zhumingwei.betterbatterystats.localeplugin.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.asksven.android.common.privateapiproxies.BatteryStatsProxy;

import cn.david.zhumingwei.betterbatterystats.localeplugin.Constants;
import cn.david.zhumingwei.betterbatterystats.localeplugin.bundle.BundleScrubber;
import cn.david.zhumingwei.betterbatterystats.localeplugin.bundle.PluginBundleManager;
import cn.david.zhumingwei.betterbatterystats.services.WriteCustomReferenceService;
import cn.david.zhumingwei.betterbatterystats.services.WriteDumpfileService;

/**
 * This is the "fire" BroadcastReceiver for a Locale Plug-in setting.
 */
public final class FireReceiver extends BroadcastReceiver
{

	private static final String TAG = "FireReceiver";
	
    /**
     * @param context {@inheritDoc}.
     * @param intent the incoming {@link com.twofortyfouram.locale.Intent#ACTION_FIRE_SETTING} Intent. This should contain the
     *            {@link com.twofortyfouram.locale.Intent#EXTRA_BUNDLE} that was saved by {@link EditActivity} and later broadcast
     *            by Locale.
     */
    @Override
    public void onReceive(final Context context, final Intent intent)
    {
        /*
         * Always be sure to be strict on input parameters! A malicious third-party app could always send an empty or otherwise
         * malformed Intent. And since Locale applies settings in the background, the plug-in definitely shouldn't crash in the
         * background.
         */

        /*
         * Locale guarantees that the Intent action will be ACTION_FIRE_SETTING
         */
        if (!cn.david.zhumingwei.localepluginlib.Intent.ACTION_FIRE_SETTING.equals(intent.getAction()))
        {
            if (Constants.IS_LOGGABLE)
            {
                String log=String.format("Received unexpected %s", intent.getAction());
                Log.e(Constants.LOG_TAG, log); //$NON-NLS-1$
            }
            return;
        }

        /*
         * A hack to prevent a private serializable classloader attack
         */
        BundleScrubber.scrub(intent);
        BundleScrubber.scrub(intent.getBundleExtra(cn.david.zhumingwei.localepluginlib.Intent.EXTRA_BUNDLE));

        final Bundle bundle = intent.getBundleExtra(cn.david.zhumingwei.localepluginlib.Intent.EXTRA_BUNDLE);

        boolean saveRef = bundle.getBoolean(PluginBundleManager.BUNDLE_EXTRA_BOOL_SAVE_REF);
        boolean saveStat = bundle.getBoolean(PluginBundleManager.BUNDLE_EXTRA_BOOL_SAVE_STAT);
        boolean saveJson = bundle.getBoolean(PluginBundleManager.BUNDLE_EXTRA_BOOL_SAVE_JSON);

        String refFrom = bundle.getString(PluginBundleManager.BUNDLE_EXTRA_STRING_REF_NAME);
        
        Log.i(TAG, "Retrieved Bundle: " + bundle.toString());

		// make sure to flush cache
		BatteryStatsProxy.getInstance(context).invalidate();

        if (saveStat)
        {
        	Log.d(TAG, "Preparing to save a dumpfile");

			Intent serviceIntent = new Intent(context, WriteDumpfileService.class);
			serviceIntent.putExtra(WriteDumpfileService.STAT_TYPE_FROM, refFrom);
			context.startService(serviceIntent);

        }
        
        if (saveJson)
        {
        	Log.d(TAG, "Preparing to save a json dumpfile");

			Intent serviceIntent = new Intent(context, WriteDumpfileService.class);
			serviceIntent.putExtra(WriteDumpfileService.STAT_TYPE_FROM, refFrom);
			serviceIntent.putExtra(WriteDumpfileService.OUTPUT, "JSON");
			
			context.startService(serviceIntent);

        }
        if (saveRef)
        {
        	Log.d(TAG, "Preparing to save a custom ref");
			Intent serviceIntent = new Intent(context, WriteCustomReferenceService.class);
			context.startService(serviceIntent);

        }
        

    }
}