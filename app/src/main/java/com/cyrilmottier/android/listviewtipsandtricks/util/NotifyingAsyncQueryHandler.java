/*
 * Copyright (C) 2015 Mohammad Hafiz bin Ismail (mypapit)
 *
 *   This software is provided 'as-is', without any express or implied
 *   warranty.  In no event will the authors be held liable for any damages
 *   arising from the use of this software.
 *
 *   Permission is granted to anyone to use this software for any purpose,
 *   including commercial applications, and to alter it and redistribute it
 *   freely, subject to the following restrictions:
 *
 *   1. The origin of this software must not be misrepresented; you must not
 *      claim that you wrote the original software. If you use this software
 *      in a product, an acknowledgment in the product documentation would be
 *      appreciated but is not required.
 *   2. Altered source versions must be plainly marked as such, and must not be
 *      misrepresented as being the original software.
 *   3. This notice may not be removed or altered from any source distribution.
 *
 *   Mohammad Hafiz bin Ismail
 *   mypapit@gmail.com
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
