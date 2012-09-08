package net.mypapit.mobile.callsignview;

import android.app.Activity;
import android.os.Bundle;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

public class CustomWindow extends Activity {
	TextView title;
	ImageView icon, btnAbout;
	
	
	public void onCreate(Bundle bundle){
		super.onCreate(bundle);
		

        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
 
        setContentView(R.layout.main);
 
        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.window_title);
 
        title = (TextView) findViewById(R.id.windowTitle);
        icon  = (ImageView) findViewById(R.id.btnIcon);
        btnAbout = (ImageView) findViewById(R.id.btnAbout);
        
        icon.setClickable(true);
        btnAbout.setClickable(true);
        
		
		
	}

}
