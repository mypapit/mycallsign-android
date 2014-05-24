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
 * MYCallsign 1.6.1 for Android <mypapit@gmail.com> (9w2wtf)
 * Copyright 2012, 2014 Mohammad Hafiz bin Ismail. All rights reserved.
 *
 * Info url :
 * http://code.google.com/p/mycallsign-android/
 * http://kirostudio.com
 * http://m.ashamradio.com/
 * 
 * 
 * CallsignInfo.java
 * Serializable CallsignInfo class, for holding callsign data
 * Mobile Malaysian Callsign Search Application
 *
 *
 * Callsign data are taken from SKMM (MCMC) website http://www.skmm.gov.my/registers1/aa.asp?aa=AARadio
 * MYCallsign logo was created by piju (http://9m2pju.blogspot.com)
 * 
 * 
 */

import java.io.Serializable;

public class CallsignInfo implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5527936939732596098L;
	public String callsign, aa, handle, expire;

}
