package com.liskovsoft.youtubeapi.rss

import com.liskovsoft.youtubeapi.common.helpers.tests.TestHelpersV2
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class RssServiceTest {
    @Test
    fun testGetFeed() {
        val feed = RssService.getFeed(TestHelpersV2.CHANNEL_ID_3)

        assertNotNull("Feed is empty", feed)
    }
}