package com.liskovsoft.youtubeapi.service;

import com.liskovsoft.mediaserviceinterfaces.yt.ServiceManager;
import com.liskovsoft.mediaserviceinterfaces.yt.data.MediaGroup;
import com.liskovsoft.mediaserviceinterfaces.yt.data.MediaItem;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(RobolectricTestRunner.class)
public class YouTubeServiceManagerTest {
    private ServiceManager mService;

    @Before
    public void setUp() {
        // fix issue: No password supplied for PKCS#12 KeyStore
        // https://github.com/robolectric/robolectric/issues/5115
        System.setProperty("javax.net.ssl.trustStoreType", "JKS");

        mService = YouTubeServiceManager.instance();
    }

    /**
     * <a href="https://www.ibm.com/developerworks/ru/library/j-5things5/index.html">More info about concurrent utils...</a>
     */
    @Test
    public void testThatSearchNotEmpty() {
        MediaGroup mediaGroup = mService.getContentService().getSearch("hello world");

        List<MediaItem> list = new ArrayList<>();

        MediaItem mediaItem = mediaGroup.getMediaItems().get(0);
        list.add(mediaItem);
        assertNotNull(mediaItem);

        assertTrue("Has media items", list.size() > 0);
    }
    
    @Test
    public void testThatRecommendedNotEmpty() {
        MediaGroup mediaGroup = mService.getContentService().getRecommended();

        assertTrue("Has media items", !mediaGroup.isEmpty());

        List<MediaItem> list = new ArrayList<>();

        MediaItem mediaItem = mediaGroup.getMediaItems().get(0);
        list.add(mediaItem);
        assertNotNull(mediaItem);

        assertTrue("Has media items", list.size() > 0);
    }
}