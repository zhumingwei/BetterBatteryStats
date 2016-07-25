/**
 * 
 */
package cn.david.zhumingwei.betterbatterystats;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import cn.david.zhumingwei.betterbatterysatats.R;
import cn.david.zhumingwei.betterbatterystats.services.WriteUnpluggedReferenceService;
import cn.david.zhumingwei.betterbatterystats.widgetproviders.LargeWidgetProvider;

/**
 * @author sven 
 */
public class FirstLaunch
{

	private static String TAG = "FirstLaunch";
	
	public static void app_launched(Activity ctx)
	{
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);

		boolean firstLaunch = !prefs.getBoolean("launched", false);

		if (firstLaunch)
		{
			Log.i(TAG, "Application was launched for the first time: create 'unplugged' reference");
			// Save that the app has been launched
			SharedPreferences.Editor editor = prefs.edit();
			editor.putBoolean("launched", true);
			editor.commit();
			
			showInfoDialog(ctx);
			
			// start service to persist reference
			Intent serviceIntent = new Intent(ctx, WriteUnpluggedReferenceService.class);
			ctx.startService(serviceIntent);
			
			// refresh widgets
			Intent intentRefreshWidgets = new Intent(LargeWidgetProvider.WIDGET_UPDATE);
			ctx.sendBroadcast(intentRefreshWidgets);

		}

		
	}

	public static void showInfoDialog(final Activity ctx)
	{		
        AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
        builder.setMessage(ctx.getString(R.string.message_first_launch))
               .setCancelable(false)
               .setTitle(ctx.getString(R.string.app_welcome, ctx.getString(R.string.app_name)))
               .setPositiveButton(ctx.getString(R.string.label_button_ok), new DialogInterface.OnClickListener()
               {
                   public void onClick(DialogInterface dialog, int id)
                   {		           
                	   dialog.dismiss();
                	   
//                	   // restart the app
//                	   Intent i = ctx.getBaseContext().getPackageManager()
//                	             .getLaunchIntentForPackage( ctx.getBaseContext().getPackageName() );
//                	i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                	ctx.startActivity(i);
                   }
               });
        builder.create().show();
	}
}
