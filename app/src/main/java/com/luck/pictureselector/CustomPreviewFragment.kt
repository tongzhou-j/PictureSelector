package com.luck.pictureselector

import android.os.Bundle
import com.luck.picture.lib.PictureSelectorPreviewFragment
import com.luck.picture.lib.adapter.PicturePreviewAdapter

/**
 * @author：luck
 * @date：2022/2/21 4:15 下午
 * @describe：CustomPreviewFragment
 */
class CustomPreviewFragment : PictureSelectorPreviewFragment() {
    companion object {
        @JvmStatic
        fun newInstance(): CustomPreviewFragment {
            val fragment = CustomPreviewFragment()
            fragment.arguments = Bundle()
            return fragment
        }
    }

    override fun getFragmentTag(): String {
        return CustomPreviewFragment::class.java.simpleName
    }

    override fun createAdapter(): PicturePreviewAdapter {
        return CustomPreviewAdapter()
    }
}

