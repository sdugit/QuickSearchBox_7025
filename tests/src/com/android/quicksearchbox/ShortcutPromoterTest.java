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

import android.test.AndroidTestCase;
import android.test.suitebuilder.annotation.MediumTest;

import java.util.ArrayList;

/**
 * Tests for {@link ShortcutPromoter}.
 *
 */
@MediumTest
public class ShortcutPromoterTest extends AndroidTestCase {

    private String mQuery;

    private SuggestionCursor mShortcuts;

    private ArrayList<SuggestionCursor> mSuggestions;

    private int mSuggestionCount;

    @Override
    protected void setUp() throws Exception {
        mQuery = "foo";
        mShortcuts = new MockShortcutRepository().getShortcutsForQuery(mQuery);
        mSuggestions = new ArrayList<SuggestionCursor>();
        mSuggestions.add(MockSource.SOURCE_1.getSuggestions(mQuery, 10));
        mSuggestions.add(MockSource.SOURCE_2.getSuggestions(mQuery, 10));
        mSuggestionCount = countSuggestions(mSuggestions);
    }

    @Override
    protected void tearDown() throws Exception {
        mQuery = null;
        mShortcuts.close();
        for (SuggestionCursor c : mSuggestions) {
            c.close();
        }
        mSuggestions = null;
    }

    public void testPickPromotedNoNext() {
        maxPromotedTestNoNext(0);
        maxPromotedTestNoNext(1);
        maxPromotedTestNoNext(2);
        maxPromotedTestNoNext(5);
    }

    public void testPickPromotedConcatNext() {
        maxPromotedTestConcatNext(0);
        maxPromotedTestConcatNext(1);
        maxPromotedTestConcatNext(2);
        maxPromotedTestConcatNext(6);
        maxPromotedTestConcatNext(7);
    }

    private void maxPromotedTestNoNext(int maxPromoted) {
        Promoter promoter = new ShortcutPromoter(null);
        int expectedCount = Math.min(maxPromoted, mShortcuts.getCount());
        ListSuggestionCursor promoted = new ListSuggestionCursor(mQuery);
        promoter.pickPromoted(mShortcuts, mSuggestions, maxPromoted, promoted);
        assertEquals(expectedCount, promoted.getCount());
        for (int i = 0; i < Math.min(maxPromoted, mShortcuts.getCount()); i++) {
            assertSuggestionEquals(new SuggestionPosition(promoted, i),
                    new SuggestionPosition(mShortcuts, i));
        }
    }

    private void maxPromotedTestConcatNext(int maxPromoted) {
        Promoter promoter = new ShortcutPromoter(new ConcatPromoter());
        int expectedCount = Math.min(maxPromoted, mShortcuts.getCount() + mSuggestionCount);
        ListSuggestionCursor promoted = new ListSuggestionCursor(mQuery);
        promoter.pickPromoted(mShortcuts, mSuggestions, maxPromoted, promoted);
        assertEquals(expectedCount, promoted.getCount());
        for (int i = 0; i < Math.min(maxPromoted, mShortcuts.getCount()); i++) {
            assertSuggestionEquals(new SuggestionPosition(promoted, i),
                    new SuggestionPosition(mShortcuts, i));
        }
        if (mShortcuts.getCount() < expectedCount) {
            assertSuggestionEquals(new SuggestionPosition(promoted, mShortcuts.getCount()),
                    new SuggestionPosition(mSuggestions.get(0), 0));
        }
    }

    private static int countSuggestions(ArrayList<SuggestionCursor> suggestions) {
        int count = 0;
        for (SuggestionCursor c : suggestions) {
            count += c.getCount();
        }
        return count;
    }

    private static void assertSuggestionEquals(SuggestionPosition x, SuggestionPosition y) {
        SuggestionCursor a = x.getSuggestion();
        SuggestionCursor b = y.getSuggestion();
        assertEquals(a.getSuggestionKey(), b.getSuggestionKey());
        assertEquals(a.getSuggestionDisplayQuery(), b.getSuggestionDisplayQuery());
    }
}
