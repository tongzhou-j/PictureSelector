package com.luck.picture.lib.basic

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.luck.picture.lib.config.PictureConfig
import com.luck.picture.lib.config.SelectMimeType
import com.luck.picture.lib.entity.LocalMedia
import java.lang.ref.SoftReference

/**
 * @author：luck
 * @date：2017-5-24 22:30
 * @describe：PictureSelector
 */
class PictureSelector private constructor(activity: Activity?, fragment: Fragment? = null) {
    private val mActivity: SoftReference<Activity?>
    private val mFragment: SoftReference<Fragment?>?

    private constructor(fragment: Fragment) : this(fragment.activity, fragment)

    init {
        mActivity = SoftReference<Activity?>(activity)
        mFragment = SoftReference<Fragment?>(fragment)
    }

    /**
     * @param chooseMode Select the type of images you want，all or images or video or audio
     * @return LocalMedia PictureSelectionModel
     * Use [SelectMimeType]
     */
    fun openGallery(chooseMode: Int): PictureSelectionModel {
        return PictureSelectionModel(this, chooseMode)
    }

    /**
     * @param chooseMode only use camera，images or video or audio
     * @return LocalMedia PictureSelectionModel
     * Use [SelectMimeType]
     */
    fun openCamera(chooseMode: Int): PictureSelectionCameraModel {
        return PictureSelectionCameraModel(this, chooseMode)
    }

    /**
     * @param chooseMode Select the type of images you want，all or images or video or audio
     * @return LocalMedia PictureSelectionSystemModel
     * Use [SelectMimeType]
     *
     *
     * openSystemGallery mode only supports some APIs
     *
     */
    fun openSystemGallery(chooseMode: Int): PictureSelectionSystemModel {
        return PictureSelectionSystemModel(this, chooseMode)
    }

    /**
     * @param selectMimeType query the type of images you want，all or images or video or audio
     * @return LocalMedia PictureSelectionQueryModel
     * Use [SelectMimeType]
     *
     *
     * only query [LocalMedia] data source
     *
     */
    fun dataSource(selectMimeType: Int): PictureSelectionQueryModel {
        return PictureSelectionQueryModel(this, selectMimeType)
    }

    /**
     * Preview mode to preview images or videos or audio
     *
     * @return
     */
    fun openPreview(): PictureSelectionPreviewModel {
        return PictureSelectionPreviewModel(this)
    }

    val activity: Activity?
        /**
         * @return Activity.
         */
        get() = mActivity.get()

    val fragment: Fragment?
        /**
         * @return Fragment.
         */
        get() = mFragment?.get()

    companion object {
        /**
         * Start PictureSelector for context.
         *
         * @param context
         * @return PictureSelector instance.
         */
        fun create(context: Context?): PictureSelector {
            return PictureSelector(context as Activity?)
        }

        /**
         * Start PictureSelector for Activity.
         *
         * @param activity
         * @return PictureSelector instance.
         */
        fun create(activity: AppCompatActivity?): PictureSelector {
            return PictureSelector(activity)
        }

        /**
         * Start PictureSelector for Activity.
         *
         * @param activity
         * @return PictureSelector instance.
         */
        fun create(activity: FragmentActivity?): PictureSelector {
            return PictureSelector(activity)
        }

        /**
         * Start PictureSelector for Fragment.
         *
         * @param fragment
         * @return PictureSelector instance.
         */
        fun create(fragment: Fragment): PictureSelector {
            return PictureSelector(fragment)
        }

        /**
         * set result
         *
         * @param data result
         * @return
         */
        fun putIntentResult(data: ArrayList<LocalMedia?>?): Intent {
            return Intent().putParcelableArrayListExtra(PictureConfig.EXTRA_RESULT_SELECTION, data)
        }

        /**
         * @param intent
         * @return get Selector  LocalMedia
         */
        fun obtainSelectorList(intent: Intent?): ArrayList<LocalMedia?> {
            if (intent == null) {
                return ArrayList<LocalMedia?>()
            }
            val result =
                intent.getParcelableArrayListExtra<LocalMedia?>(PictureConfig.EXTRA_RESULT_SELECTION)
            return if (result != null) result else ArrayList<LocalMedia?>()
        }
    }
}
