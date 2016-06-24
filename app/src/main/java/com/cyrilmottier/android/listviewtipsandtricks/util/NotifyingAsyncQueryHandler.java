/*
 * This file is part of MYCallsign
 *
 * Copyright (c) 2016 Mohammad Hafiz bin Ismail <mypapit@gmail.com>
 * MYCallsign is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * Last Modified 2/23/15 8:13 PM
 *  Info url :
 *  http://code.google.com/p/mycallsign-android/
 *  http://blog.mypapit.net
 *  http://kirostudio.com
 *  http://mypapit.net
 */
package com.cyrilmottier.android.listviewtipsandtricks.util;

import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;

import java.lang.ref.WeakReference;

/**
 * A particular {@link AsyncQueryHandler} allowing clients to be notified via a
 * listener. The {@link NotifyingAsyncQueryHandler} also make sure no strong
 * reference is kept on the given listener (as it is often a Context).
 *
 * @author Cyril Mottier
 */
public class NotifyingAsyncQueryHandler extends AsyncQueryHandler {

    private WeakReference<NotifyingAsyncQueryListener> mListener;

    public NotifyingAsyncQueryHandler(ContentResolver resolver, NotifyingAsyncQueryListener listener) {
        super(resolver);
        setQueryListener(listener);
    }

    /**
     * Assign the given {@link NotifyingAsyncQueryListener}.
     */
    public void setQueryListener(NotifyingAsyncQueryListener listener) {
        mListener = (listener != null) ? new WeakReference<NotifyingAsyncQueryListener>(listener) : null;
    }

    public void clearQueryListener() {
        mListener = null;
    }

    public void startQuery(Uri uri, String[] projection) {
        startQuery(-1, null, uri, projection, null, null, null);
    }

    public void startQuery(Uri uri, String[] projection, String sortOrder) {
        startQuery(-1, null, uri, projection, null, null, sortOrder);
    }

    @Override
    protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
        final NotifyingAsyncQueryListener listener = (mListener == null) ? null : mListener.get();
        if (listener != null) {
            listener.onQueryComplete(token, cookie, cursor);
        } else if (cursor != null) {
            cursor.close();
        }
    }

    /**
     * Client may use this to listen to completed query operations.
     *
     * @author Cyril Mottier
     */
    public interface NotifyingAsyncQueryListener {
        void onQueryComplete(int token, Object cookie, Cursor cursor);
    }
}
