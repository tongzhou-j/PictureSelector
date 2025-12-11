package com.luck.picture.lib.config

import android.content.Context
import com.luck.picture.lib.PictureSelectorFragment
import com.luck.picture.lib.PictureSelectorPreviewFragment
import com.luck.picture.lib.R
import com.luck.picture.lib.adapter.PictureAlbumAdapter
import com.luck.picture.lib.adapter.PictureImageGridAdapter
import com.luck.picture.lib.adapter.PicturePreviewAdapter
import com.luck.picture.lib.adapter.holder.PreviewGalleryAdapter

/**
 * @author：luck
 * @date：2021/12/23 10:50 上午
 * @describe：InjectResourceSource
 */
object InjectResourceSource {
    /**
     * default layout
     */
    const val DEFAULT_LAYOUT_RESOURCE: Int = 0

    /**
     * [PictureSelectorFragment]  layout
     * [R.layout.ps_fragment_selector]
     */
    const val MAIN_SELECTOR_LAYOUT_RESOURCE: Int = 1

    /**
     * [PictureSelectorPreviewFragment] preview layout
     * [R.layout.ps_fragment_preview]
     */
    const val PREVIEW_LAYOUT_RESOURCE: Int = 2

    /**
     * [PictureImageGridAdapter]  image adapter item layout
     * [R.layout.ps_item_grid_image]
     */
    const val MAIN_ITEM_IMAGE_LAYOUT_RESOURCE: Int = 3

    /**
     * [PictureImageGridAdapter]  video adapter item layout
     * [R.layout.ps_item_grid_video]
     */
    const val MAIN_ITEM_VIDEO_LAYOUT_RESOURCE: Int = 4

    /**
     * [PictureImageGridAdapter]  audio adapter item layout
     * [R.layout.ps_item_grid_audio]
     */
    const val MAIN_ITEM_AUDIO_LAYOUT_RESOURCE: Int = 5

    /**
     * [PictureAlbumAdapter] adapter item layout
     * [R.layout.ps_album_folder_item]
     */
    const val ALBUM_ITEM_LAYOUT_RESOURCE: Int = 6

    /**
     * [PicturePreviewAdapter] preview adapter item layout
     * [R.layout.ps_preview_image]
     */
    const val PREVIEW_ITEM_IMAGE_LAYOUT_RESOURCE: Int = 7

    /**
     * [PicturePreviewAdapter] preview adapter item layout
     * [R.layout.ps_preview_video]
     */
    const val PREVIEW_ITEM_VIDEO_LAYOUT_RESOURCE: Int = 8

    /**
     * [PreviewGalleryAdapter] preview gallery adapter item layout
     * [R.layout.ps_preview_gallery_item]
     */
    const val PREVIEW_GALLERY_ITEM_LAYOUT_RESOURCE: Int = 9

    /**
     * [PicturePreviewAdapter] preview adapter item layout
     * [R.layout.ps_preview_audio]
     */
    const val PREVIEW_ITEM_AUDIO_LAYOUT_RESOURCE: Int = 10

    /**
     * getLayoutResource
     *
     * @param context
     * @param resourceSource [InjectResourceSource]
     * @return
     */
    fun getLayoutResource(
        context: Context?,
        resourceSource: Int,
        selectorConfig: SelectorConfig?
    ): Int {
        val listener = selectorConfig?.onLayoutResourceListener
        if (listener != null) {
            return listener.getLayoutResourceId(
                context,
                resourceSource
            )
        }
        return DEFAULT_LAYOUT_RESOURCE
    }
}
