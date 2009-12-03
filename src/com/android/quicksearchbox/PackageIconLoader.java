/*
 * Copyright (C) 2009 The Android Open Source Project
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

package com.android.quicksearchbox;

import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * Loads icons from other packages.
 *
 * Code partly stolen from {@link ContentResolver} and {@link android.app.SuggestionsAdapter}.
 *
 */
public class PackageIconLoader implements IconLoader {

    private static final boolean DBG = false;
    private static final String TAG = "QSB.PackageIconLoader";

    private final Context mPackageContext;

    /**
     * Creates a new icon loader.
     *
     * @param packageContext The context which will be used for finding icon resources.
     *        Resource IDs without an explicit package will be resolved against the package
     *        of this context.
     */
    public PackageIconLoader(Context packageContext) {
        mPackageContext = packageContext;
    }

    public Drawable getIcon(String drawableId) {
        if (DBG) Log.d(TAG, "getIcon(" + drawableId + ")");
        if (TextUtils.isEmpty(drawableId) || "0".equals(drawableId)) {
            return null;
        }
        try {
            // First, see if it's just an integer
            int resourceId = Integer.parseInt(drawableId);
            // If so, find it by resource ID
            return mPackageContext.getResources().getDrawable(resourceId);
        } catch (NumberFormatException nfe) {
            // It's not an integer, use it as a URI
            Uri uri = Uri.parse(drawableId);
            return getDrawable(uri);
        } catch (Resources.NotFoundException nfe) {
            // It was an integer, but it couldn't be found, bail out
            Log.w(TAG, "Icon resource not found: " + drawableId);
            return null;
        }
    }

    public Uri getIconUri(String drawableId) {
        if (TextUtils.isEmpty(drawableId) || "0".equals(drawableId)) {
            return null;
        }
        try {
            int resourceId = Integer.parseInt(drawableId);
            return new Uri.Builder()
                    .scheme(ContentResolver.SCHEME_ANDROID_RESOURCE)
                    .authority(mPackageContext.getPackageName())
                    .appendEncodedPath(String.valueOf(resourceId))
                    .build();
        } catch (NumberFormatException nfe) {
            return Uri.parse(drawableId);
        }
    }

    /**
     * Gets a drawable by URI.
     *
     * @return A drawable, or {@code null} if the drawable could not be loaded.
     */
    private Drawable getDrawable(Uri uri) {
        try {
            String scheme = uri.getScheme();
            if (ContentResolver.SCHEME_ANDROID_RESOURCE.equals(scheme)) {
                // Load drawables through Resources, to get the source density information
                OpenResourceIdResult r = getResourceId(uri);
                try {
                    return r.r.getDrawable(r.id);
                } catch (Resources.NotFoundException ex) {
                    throw new FileNotFoundException("Resource does not exist: " + uri);
                }
            } else {
                // Let the ContentResolver handle content and file URIs.
                InputStream stream = mPackageContext.getContentResolver().openInputStream(uri);
                if (stream == null) {
                    throw new FileNotFoundException("Failed to open " + uri);
                }
                try {
                    return Drawable.createFromStream(stream, null);
                } finally {
                    try {
                        stream.close();
                    } catch (IOException ex) {
                        Log.e(TAG, "Error closing icon stream for " + uri, ex);
                    }
                }
            }
        } catch (FileNotFoundException fnfe) {
            Log.w(TAG, "Icon not found: " + uri + ", " + fnfe.getMessage());
            return null;
        }
    }

    /**
     * A resource identified by the {@link Resources} that contains it, and a resource id.
     */
    public class OpenResourceIdResult {
        public Resources r;
        public int id;
    }

    /**
     * Resolves an android.resource URI to a {@link Resources} and a resource id.
     */
    public OpenResourceIdResult getResourceId(Uri uri) throws FileNotFoundException {
        String authority = uri.getAuthority();
        Resources r;
        if (TextUtils.isEmpty(authority)) {
            throw new FileNotFoundException("No authority: " + uri);
        } else {
            try {
                r = mPackageContext.getPackageManager().getResourcesForApplication(authority);
            } catch (NameNotFoundException ex) {
                throw new FileNotFoundException("No package found for authority: " + uri);
            }
        }
        List<String> path = uri.getPathSegments();
        if (path == null) {
            throw new FileNotFoundException("No path: " + uri);
        }
        int len = path.size();
        int id;
        if (len == 1) {
            try {
                id = Integer.parseInt(path.get(0));
            } catch (NumberFormatException e) {
                throw new FileNotFoundException("Single path segment is not a resource ID: " + uri);
            }
        } else if (len == 2) {
            id = r.getIdentifier(path.get(1), path.get(0), authority);
        } else {
            throw new FileNotFoundException("More than two path segments: " + uri);
        }
        if (id == 0) {
            throw new FileNotFoundException("No resource found for: " + uri);
        }
        OpenResourceIdResult res = new OpenResourceIdResult();
        res.r = r;
        res.id = id;
        return res;
    }
}
