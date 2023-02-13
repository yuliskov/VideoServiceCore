package com.liskovsoft.youtubeapi.browse.v2.impl

import com.liskovsoft.mediaserviceinterfaces.data.MediaGroup
import com.liskovsoft.mediaserviceinterfaces.data.MediaItem
import com.liskovsoft.youtubeapi.common.models.gen.ItemWrapper
import com.liskovsoft.youtubeapi.common.models.gen.isLive
import com.liskovsoft.youtubeapi.common.models.gen.isShorts
import com.liskovsoft.youtubeapi.common.models.gen.isUpcoming
import com.liskovsoft.youtubeapi.next.v2.impl.mediaitem.MediaItemImpl

abstract class MediaGroupImplBase(
    private val removeShorts: Boolean = true,
    private val removeLive: Boolean = false,
    private val removeUpcoming: Boolean = false
): MediaGroup {
    private val filter: ((ItemWrapper) -> Boolean) = {
        (removeShorts && it.isShorts() == true) || (removeLive && it.isLive() == true) || (removeUpcoming && it.isUpcoming() == true)
    }
    private var _titleItem: String? = null
        get() = field ?: titleItem
    private var _mediaItemList: List<MediaItem?>? = null
        get() = field ?: mediaItemList
    private var _nextPageKeyVal: String? = null
        get() = if (field == "") null else field ?: nextPageKeyVal
        set(value) { field = value ?: "" }

    private val titleItem by lazy { getTitleInt() }
    private val mediaItemList by lazy { getItemWrappersInt()
        ?.mapIndexedNotNull { index, it -> it
            ?.let { if (filter.invoke(it)) null else it }
            ?.let { MediaItemImpl(it).apply { playlistIndex = index } }
        }
    }
    private val nextPageKeyVal by lazy { getNextPageKeyInt() }

    abstract fun getItemWrappersInt(): List<ItemWrapper?>?
    abstract fun getNextPageKeyInt(): String?
    abstract fun getTitleInt(): String?

    override fun getId(): Int = title?.hashCode() ?: hashCode()

    override fun getType(): Int {
        return MediaGroup.TYPE_SUBSCRIPTIONS
    }

    override fun getMediaItems(): List<MediaItem?>? {
        return _mediaItemList
    }

    fun setMediaItems(list: List<MediaItem?>?) {
        _mediaItemList = list
    }

    override fun getTitle(): String? {
        return _titleItem
    }

    override fun setTitle(title: String?) {
        _titleItem = title
    }

    override fun getChannelId(): String? {
        return null
    }

    override fun getParams(): String? {
        return null
    }

    override fun getReloadPageKey(): String? {
        return null
    }

    override fun getNextPageKey(): String? {
        return _nextPageKeyVal
    }

    fun setNextPageKey(key: String?) {
        _nextPageKeyVal = key
    }

    override fun getChannelUrl(): String? {
        return null
    }

    override fun isEmpty(): Boolean {
        return mediaItemList.isNullOrEmpty()
    }
}