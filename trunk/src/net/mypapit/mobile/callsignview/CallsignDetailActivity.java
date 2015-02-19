package net.mypapit.mobile.callsignview;

/*
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License version 2 as
 *  published by the Free Software Foundation
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * MYCallsign 1.7.0 for Android <mypapit@gmail.com> (9w2wtf)
 * Copyright 2012-2015 Mohammad Hafiz bin Ismail. All rights reserved.
 *
 * Info url :
 * http://code.google.com/p/mycallsign-android/
 * http://kirostudio.com
 * http://m.ashamradio.com/
 * 
 * 
 * CallsignDetailActivity.java
 * For displaying details on Callsigns
 * Mobile Malaysian Callsign Search Application
 *
 *
 * Callsign data are taken from SKMM (MCMC) website http://www.skmm.gov.my/registers1/aa.asp?aa=AARadio
 * MYCallsign logo was created by piju (http://9m2pju.blogspot.com)
 * 
 * 
 */

import android.os.Bundle;
//import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
//import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
//import android.view.animation.BounceInterpolator;
//import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class CallsignDetailActivity extends CustomWindow implements
		OnClickListener {

	private CallsignInfo csinfo;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.details_page);
		csinfo = new CallsignInfo();

		csinfo = (CallsignInfo) getIntent()
				.getSerializableExtra("CallsignInfo");

		TextView tvcallsign = (TextView) findViewById(R.id.csdetails);
		TextView tvhandle = (TextView) findViewById(R.id.handledetails);
		TextView tvaa = (TextView) findViewById(R.id.AA);
		TextView tvExpiry = (TextView) findViewById(R.id.Expiry);

		tvcallsign.setText(csinfo.callsign);
		tvhandle.setText(csinfo.handle);
		tvaa.setText(csinfo.aa);
		tvExpiry.setText(csinfo.expire);

		Animation animation = AnimationUtils.loadAnimation(this,
				R.anim.slide_up);
		// BounceInterpolator bi = new BounceInterpolator();
		// bi.getInterpolation(2.0f);
		// animation.setInterpolator(bi);
		// animation.setInterpolator(new DecelerateInterpolator(2.0f));

		this.icon.setOnClickListener(this);
		this.btnAbout.setOnClickListener(this);
		this.icon.setImageResource(R.drawable.ic_menu_share);

		LinearLayout mainLayout = (LinearLayout) this
				.findViewById(R.id.details_page_layout);
		mainLayout.startAnimation(animation);

	}

	@Override
	public void onClick(View view) {
		// TODO Auto-generated method stub
		switch (view.getId()) {
		case R.id.btnAbout:
			try {
				final Dialog dialog = new Dialog(this);
				dialog.setContentView(R.layout.about_dialog);
				dialog.setTitle("About MYCallsign "
						+ getPackageManager().getPackageInfo(getPackageName(),
								0).versionName);
				dialog.setCancelable(true);

				// text
				TextView text = (TextView) dialog.findViewById(R.id.tvAbout);
				text.setText(R.string.txtLicense);

				// icon image
				ImageView img = (ImageView) dialog.findViewById(R.id.ivAbout);
				img.setImageResource(R.drawable.ic_launcher);

				dialog.show();

			} catch (NameNotFoundException nfe) {
				Toast.makeText(this, nfe.toString(), Toast.LENGTH_SHORT).show();

			}

			break;

		case R.id.btnIcon:
			this.icon.setImageResource(R.drawable.ic_menu_share_hover);

			Intent sharingIntent = new Intent(
					android.content.Intent.ACTION_SEND);
			sharingIntent.setType("text/plain");
			sharingIntent.putExtra(android.content.Intent.EXTRA_TITLE,
					"Callsign info");
			sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT,
					"MyCallsign Data");
			sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT,
					csinfo.callsign + "\n" + csinfo.handle + "\nAA: "
							+ csinfo.aa + "\nExpiry: " + csinfo.expire
							+ " \nhttp://goo.gl/BOrln  ");

			startActivity(Intent.createChooser(sharingIntent, "Share via"));

			break;
		}
		this.icon.setImageResource(R.drawable.ic_menu_share);
	}
}
