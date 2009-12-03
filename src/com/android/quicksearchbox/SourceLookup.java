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

import android.content.ComponentName;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Defines operations for looking up information about a {@link Source}.
 */
public interface SourceLookup {

    /**
     * Gets a suggestion source (or the current web search source) by component name.
     */
    Source getSourceByComponentName(ComponentName componentName);

    /**
     * Returns the web search source set in the preferences, or the default source
     * if no web search source has been selected.
     *
     * @return <code>null</code> only if there is no web search source available.
     */
    Source getSelectedWebSearchSource();

    /**
     * Checks if we trust the given source not to be spammy.
     */
    boolean isTrustedSource(Source source);

    /**
     * Gets all suggestion sources. This does not include any web search sources.
     *
     * @return A list of suggestion sources, including sources that are not enabled.
     *         Callers must not modify the returned collection.
     */
    Collection<Source> getSources();

    /**
     * Gets the sources that should be queried for the given query.
     */
    ArrayList<Source> getSourcesToQuery(String query);
}
