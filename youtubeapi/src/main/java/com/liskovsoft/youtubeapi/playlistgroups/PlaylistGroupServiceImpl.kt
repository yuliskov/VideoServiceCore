package com.liskovsoft.youtubeapi.playlistgroups

import com.liskovsoft.mediaserviceinterfaces.data.ItemGroup
import com.liskovsoft.mediaserviceinterfaces.data.ItemGroup.Item
import com.liskovsoft.sharedutils.helpers.Helpers
import com.liskovsoft.sharedutils.rx.RxHelper
import com.liskovsoft.youtubeapi.channelgroups.models.ItemGroupImpl
import com.liskovsoft.youtubeapi.service.internal.MediaServicePrefs
import io.reactivex.disposables.Disposable

internal object PlaylistGroupServiceImpl : MediaServicePrefs.ProfileChangeListener {
    private const val PLAYLIST_GROUP_DATA = "playlist_group_data"
    private const val PERSIST_DELAY_MS: Long = 5_000
    private lateinit var mPlaylists: MutableList<ItemGroup>
    private var mPersistAction: Disposable? = null

    init {
        MediaServicePrefs.addListener(this)
        restoreData()
    }

    override fun onProfileChanged() {
        restoreData()
    }

    @JvmStatic
    fun addPlaylistGroup(group: ItemGroup) {
        // Move to the top
        mPlaylists.remove(group)
        mPlaylists.add(0, group)
        persistData()
    }

    @JvmStatic
    fun createPlaylistGroup(title: String, iconUrl: String?, items: List<Item>): ItemGroup {
        return ItemGroupImpl(title = title, iconUrl = iconUrl, items = items.toMutableList())
    }

    private fun restoreData() {
        val data = MediaServicePrefs.getData(PLAYLIST_GROUP_DATA)
        restoreData(data)
    }

    private fun restoreData(data: String?) {
        val split = Helpers.splitData(data)

        mPlaylists = Helpers.parseList(split, 0, ItemGroupImpl::fromString)
        mPlaylists.forEach {
            it as ItemGroupImpl
            it.onChange = { persistData() }
        }
    }

    private fun persistData() {
        RxHelper.disposeActions(mPersistAction)
        mPersistAction = RxHelper.runAsync({ persistDataReal() }, PERSIST_DELAY_MS)
    }

    private fun persistDataReal() {
        MediaServicePrefs.setData(PLAYLIST_GROUP_DATA, Helpers.mergeData(mPlaylists))
    }
}