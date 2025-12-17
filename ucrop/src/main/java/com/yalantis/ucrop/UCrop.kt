package com.yalantis.ucrop

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Parcelable
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.annotation.FloatRange
import androidx.annotation.IntRange
import androidx.annotation.NonNull
import androidx.annotation.Nullable
import androidx.fragment.app.Fragment
import com.yalantis.ucrop.BuildConfig
import com.yalantis.ucrop.model.AspectRatio
import java.util.ArrayList
import java.util.Arrays
import java.util.Locale
import kotlin.jvm.JvmField
import kotlin.jvm.JvmStatic

/**
 * Created by Oleksii Shliama (https://github.com/shliama).
 * <p/>
 * Builder class to ease Intent setup.
 */
class UCrop private constructor(@NonNull source: Uri, @NonNull destination: Uri, totalSource: ArrayList<String>? = null) {
    companion object {
        const val REQUEST_CROP = 69
        const val RESULT_ERROR = 96
        const val MIN_SIZE = 10

        private val EXTRA_PREFIX = BuildConfig.LIBRARY_PACKAGE_NAME
        
        @JvmField
        val EXTRA_CROP_TOTAL_DATA_SOURCE = EXTRA_PREFIX + ".CropTotalDataSource"
        @JvmField
        val EXTRA_CROP_INPUT_ORIGINAL = EXTRA_PREFIX + ".CropInputOriginal"

        @JvmField
        val EXTRA_INPUT_URI = EXTRA_PREFIX + ".InputUri"
        @JvmField
        val EXTRA_OUTPUT_URI = EXTRA_PREFIX + ".OutputUri"
        @JvmField
        val EXTRA_OUTPUT_CROP_ASPECT_RATIO = EXTRA_PREFIX + ".CropAspectRatio"
        @JvmField
        val EXTRA_OUTPUT_IMAGE_WIDTH = EXTRA_PREFIX + ".ImageWidth"
        @JvmField
        val EXTRA_OUTPUT_IMAGE_HEIGHT = EXTRA_PREFIX + ".ImageHeight"
        @JvmField
        val EXTRA_OUTPUT_OFFSET_X = EXTRA_PREFIX + ".OffsetX"
        @JvmField
        val EXTRA_OUTPUT_OFFSET_Y = EXTRA_PREFIX + ".OffsetY"
        @JvmField
        val EXTRA_ERROR = EXTRA_PREFIX + ".Error"

        @JvmField
        val EXTRA_ASPECT_RATIO_X = EXTRA_PREFIX + ".AspectRatioX"
        @JvmField
        val EXTRA_ASPECT_RATIO_Y = EXTRA_PREFIX + ".AspectRatioY"

        @JvmField
        val EXTRA_MAX_SIZE_X = EXTRA_PREFIX + ".MaxSizeX"
        @JvmField
        val EXTRA_MAX_SIZE_Y = EXTRA_PREFIX + ".MaxSizeY"

        /**
         * This method creates new Intent builder and sets both source and destination image URIs.
         *
         * @param source      Uri for image to crop
         * @param destination Uri for saving the cropped image
         * @param totalSource crop data source for list
         */
        @JvmStatic
        fun of(@NonNull source: Uri, @NonNull destination: Uri, totalSource: ArrayList<String>): UCrop {
            if (totalSource.isEmpty()) {
                throw IllegalArgumentException("Missing required parameters, count cannot be less than 1")
            }
            return if (totalSource.size == 1) {
                UCrop(source, destination)
            } else {
                UCrop(source, destination, totalSource)
            }
        }

        /**
         * This method creates new Intent builder and sets both source and destination image URIs.
         *
         * @param source      Uri for image to crop
         * @param destination Uri for saving the cropped image
         */
        @JvmStatic
        fun <T> of(@NonNull source: Uri, @NonNull destination: Uri): UCrop {
            return UCrop(source, destination)
        }

        /**
         * Retrieve cropped image Uri from the result Intent
         *
         * @param intent crop result intent
         */
        @JvmStatic
        @Nullable
        fun getOutput(@NonNull intent: Intent): Uri? {
            return intent.getParcelableExtra(EXTRA_OUTPUT_URI)
        }

        /**
         * Retrieve the width of the cropped image
         *
         * @param intent crop result intent
         */
        @JvmStatic
        fun getOutputImageWidth(@NonNull intent: Intent): Int {
            return intent.getIntExtra(EXTRA_OUTPUT_IMAGE_WIDTH, -1)
        }

        /**
         * Retrieve the height of the cropped image
         *
         * @param intent crop result intent
         */
        @JvmStatic
        fun getOutputImageHeight(@NonNull intent: Intent): Int {
            return intent.getIntExtra(EXTRA_OUTPUT_IMAGE_HEIGHT, -1)
        }

        /**
         * Retrieve cropped image aspect ratio from the result Intent
         *
         * @param intent crop result intent
         * @return aspect ratio as a floating point value (x:y) - so it will be 1 for 1:1 or 4/3 for 4:3
         */
        @JvmStatic
        fun getOutputCropAspectRatio(@NonNull intent: Intent): Float {
            return intent.getFloatExtra(EXTRA_OUTPUT_CROP_ASPECT_RATIO, 0f)
        }

        /**
         * Retrieve the x of the cropped offset x
         *
         * @param intent crop result intent
         */
        @JvmStatic
        fun getOutputImageOffsetX(@NonNull intent: Intent): Int {
            return intent.getIntExtra(EXTRA_OUTPUT_OFFSET_X, 0)
        }

        /**
         * Retrieve the y of the cropped offset y
         *
         * @param intent crop result intent
         */
        @JvmStatic
        fun getOutputImageOffsetY(@NonNull intent: Intent): Int {
            return intent.getIntExtra(EXTRA_OUTPUT_OFFSET_Y, 0)
        }

        /**
         * Method retrieves error from the result intent.
         *
         * @param result crop result Intent
         * @return Throwable that could happen while image processing
         */
        @JvmStatic
        @Nullable
        fun getError(@NonNull result: Intent): Throwable? {
            return result.getSerializableExtra(EXTRA_ERROR) as? Throwable
        }
    }

    private val mCropIntent: Intent = Intent()
    private var mCropOptionsBundle: Bundle = Bundle()

    init {
        mCropOptionsBundle.putParcelable(EXTRA_INPUT_URI, source)
        mCropOptionsBundle.putParcelable(EXTRA_OUTPUT_URI, destination)
        totalSource?.let {
            mCropOptionsBundle.putStringArrayList(EXTRA_CROP_TOTAL_DATA_SOURCE, it)
        }
    }

    /**
     * Set Multiple Crop gallery Preview Image Engine
     *
     * @param engine
     * @return
     */
    fun setImageEngine(engine: UCropImageEngine?) {
        val dataSource = mCropOptionsBundle.getStringArrayList(EXTRA_CROP_TOTAL_DATA_SOURCE)
        val isUseBitmap = mCropOptionsBundle.getBoolean(Options.EXTRA_CROP_CUSTOM_LOADER_BITMAP, false)
        if ((dataSource != null && dataSource.size > 1) || isUseBitmap) {
            if (engine == null) {
                throw NullPointerException("Missing ImageEngine,please implement UCrop.setImageEngine")
            }
        }
        UCropDevelopConfig.imageEngine = engine
    }

    /**
     * Set an aspect ratio for crop bounds.
     * User won't see the menu with other ratios options.
     *
     * @param x aspect ratio X
     * @param y aspect ratio Y
     */
    fun withAspectRatio(x: Float, y: Float): UCrop {
        mCropOptionsBundle.putFloat(EXTRA_ASPECT_RATIO_X, x)
        mCropOptionsBundle.putFloat(EXTRA_ASPECT_RATIO_Y, y)
        return this
    }

    /**
     * Set an aspect ratio for crop bounds that is evaluated from source image width and height.
     * User won't see the menu with other ratios options.
     */
    fun useSourceImageAspectRatio(): UCrop {
        mCropOptionsBundle.putFloat(EXTRA_ASPECT_RATIO_X, 0f)
        mCropOptionsBundle.putFloat(EXTRA_ASPECT_RATIO_Y, 0f)
        return this
    }

    /**
     * Set maximum size for result cropped image. Maximum size cannot be less then {@value MIN_SIZE}
     *
     * @param width  max cropped image width
     * @param height max cropped image height
     */
    fun withMaxResultSize(@IntRange(from = MIN_SIZE.toLong()) width: Int, @IntRange(from = MIN_SIZE.toLong()) height: Int): UCrop {
        var finalWidth = width
        var finalHeight = height
        if (finalWidth < MIN_SIZE) {
            finalWidth = MIN_SIZE
        }

        if (finalHeight < MIN_SIZE) {
            finalHeight = MIN_SIZE
        }

        mCropOptionsBundle.putInt(EXTRA_MAX_SIZE_X, finalWidth)
        mCropOptionsBundle.putInt(EXTRA_MAX_SIZE_Y, finalHeight)
        return this
    }

    fun withOptions(@NonNull options: Options): UCrop {
        mCropOptionsBundle.putAll(options.optionBundle)
        return this
    }

    /**
     * Send the crop Intent from an Activity
     *
     * @param activity Activity to receive result
     */
    fun start(@NonNull activity: Activity) {
        start(activity, REQUEST_CROP)
    }

    /**
     * Send the crop Intent from an Activity with a custom request code
     *
     * @param activity    Activity to receive result
     * @param requestCode requestCode for result
     */
    fun start(@NonNull activity: Activity, requestCode: Int) {
        activity.startActivityForResult(getIntent(activity), requestCode)
    }

    /**
     * Send the crop Intent from a Fragment
     *
     * @param fragment Fragment to receive result
     */
    fun start(@NonNull context: Context, @NonNull fragment: Fragment) {
        start(context, fragment, REQUEST_CROP)
    }

    /**
     * Send the crop Intent with a custom request code
     *
     * @param fragment    Fragment to receive result
     * @param requestCode requestCode for result
     */
    fun start(@NonNull context: Context, @NonNull fragment: Fragment, requestCode: Int) {
        fragment.startActivityForResult(getIntent(context), requestCode)
    }

    /**
     * Send the crop Intent with a custom request code
     *
     * @param fragment    Fragment to receive result
     * @param requestCode requestCode for result
     */
    fun startEdit(@NonNull context: Context, @NonNull fragment: Fragment, requestCode: Int) {
        fragment.startActivityForResult(getIntent(context), requestCode)
    }

    /**
     * Get Intent to start {@link UCropActivity}
     *
     * @return Intent for {@link UCropActivity}
     */
    fun getIntent(@NonNull context: Context): Intent {
        val dataSource = mCropOptionsBundle.getStringArrayList(EXTRA_CROP_TOTAL_DATA_SOURCE)
        mCropIntent.setClass(context, if (dataSource != null && dataSource.size > 1) {
            UCropMultipleActivity::class.java
        } else {
            UCropActivity::class.java
        })
        mCropIntent.putExtras(mCropOptionsBundle)
        return mCropIntent
    }

    /**
     * Get Fragment {@link UCropFragment}
     *
     * @return Fragment of {@link UCropFragment}
     */
    fun getFragment(): UCropFragment {
        return UCropFragment.newInstance(mCropOptionsBundle)
    }

    fun getFragment(bundle: Bundle): UCropFragment {
        mCropOptionsBundle = bundle
        return getFragment()
    }

    /**
     * Class that helps to setup advanced configs that are not commonly used.
     * Use it with method {@link #withOptions(Options)}
     */
    class Options {
        companion object {
            @JvmField
            val EXTRA_COMPRESSION_FORMAT_NAME = EXTRA_PREFIX + ".CompressionFormatName"
            @JvmField
            val EXTRA_COMPRESSION_QUALITY = EXTRA_PREFIX + ".CompressionQuality"

            @JvmField
            val EXTRA_CROP_OUTPUT_DIR = EXTRA_PREFIX + ".CropOutputDir"

            @JvmField
            val EXTRA_CROP_OUTPUT_FILE_NAME = EXTRA_PREFIX + ".CropOutputFileName"

            @JvmField
            val EXTRA_CROP_FORBID_GIF_WEBP = EXTRA_PREFIX + ".ForbidCropGifWebp"

            @JvmField
            val EXTRA_CROP_FORBID_SKIP = EXTRA_PREFIX + ".ForbidSkipCrop"

            @JvmField
            val EXTRA_DARK_STATUS_BAR_BLACK = EXTRA_PREFIX + ".isDarkStatusBarBlack"

            @JvmField
            val EXTRA_DRAG_IMAGES = EXTRA_PREFIX + ".isDragImages"

            @JvmField
            val EXTRA_CROP_CUSTOM_LOADER_BITMAP = EXTRA_PREFIX + ".CustomLoaderCropBitmap"

            @JvmField
            val EXTRA_CROP_DRAG_CENTER = EXTRA_PREFIX + ".DragSmoothToCenter"

            @JvmField
            val EXTRA_ALLOWED_GESTURES = EXTRA_PREFIX + ".AllowedGestures"

            @JvmField
            val EXTRA_MAX_BITMAP_SIZE = EXTRA_PREFIX + ".MaxBitmapSize"
            @JvmField
            val EXTRA_MAX_SCALE_MULTIPLIER = EXTRA_PREFIX + ".MaxScaleMultiplier"
            @JvmField
            val EXTRA_IMAGE_TO_CROP_BOUNDS_ANIM_DURATION = EXTRA_PREFIX + ".ImageToCropBoundsAnimDuration"

            @JvmField
            val EXTRA_DIMMED_LAYER_COLOR = EXTRA_PREFIX + ".DimmedLayerColor"
            @JvmField
            val EXTRA_CIRCLE_STROKE_COLOR = EXTRA_PREFIX + ".CircleStrokeColor"
            @JvmField
            val EXTRA_CIRCLE_DIMMED_LAYER = EXTRA_PREFIX + ".CircleDimmedLayer"

            @JvmField
            val EXTRA_SHOW_CROP_FRAME = EXTRA_PREFIX + ".ShowCropFrame"
            @JvmField
            val EXTRA_CROP_FRAME_COLOR = EXTRA_PREFIX + ".CropFrameColor"
            @JvmField
            val EXTRA_CROP_FRAME_STROKE_WIDTH = EXTRA_PREFIX + ".CropFrameStrokeWidth"

            @JvmField
            val EXTRA_SHOW_CROP_GRID = EXTRA_PREFIX + ".ShowCropGrid"

            @JvmField
            val EXTRA_CROP_GRID_ROW_COUNT = EXTRA_PREFIX + ".CropGridRowCount"
            @JvmField
            val EXTRA_CROP_GRID_COLUMN_COUNT = EXTRA_PREFIX + ".CropGridColumnCount"
            @JvmField
            val EXTRA_CROP_GRID_COLOR = EXTRA_PREFIX + ".CropGridColor"
            @JvmField
            val EXTRA_CROP_GRID_STROKE_WIDTH = EXTRA_PREFIX + ".CropGridStrokeWidth"
            @JvmField
            val EXTRA_CIRCLE_STROKE_WIDTH_LAYER = EXTRA_PREFIX + ".CircleStrokeWidth"
            @JvmField
            val EXTRA_GALLERY_BAR_BACKGROUND = EXTRA_PREFIX + ".GalleryBarBackground"

            @JvmField
            val EXTRA_TOOL_BAR_COLOR = EXTRA_PREFIX + ".ToolbarColor"
            @JvmField
            val EXTRA_STATUS_BAR_COLOR = EXTRA_PREFIX + ".StatusBarColor"
            @JvmField
            val EXTRA_UCROP_COLOR_CONTROLS_WIDGET_ACTIVE = EXTRA_PREFIX + ".UcropColorControlsWidgetActive"

            @JvmField
            val EXTRA_UCROP_WIDGET_COLOR_TOOLBAR = EXTRA_PREFIX + ".UcropToolbarWidgetColor"
            @JvmField
            val EXTRA_UCROP_TITLE_TEXT_TOOLBAR = EXTRA_PREFIX + ".UcropToolbarTitleText"
            @JvmField
            val EXTRA_UCROP_TITLE_TEXT_SIZE_TOOLBAR = EXTRA_PREFIX + ".UcropToolbarTitleTextSize"
            @JvmField
            val EXTRA_UCROP_WIDGET_CANCEL_DRAWABLE = EXTRA_PREFIX + ".UcropToolbarCancelDrawable"
            @JvmField
            val EXTRA_UCROP_WIDGET_CROP_DRAWABLE = EXTRA_PREFIX + ".UcropToolbarCropDrawable"

            @JvmField
            val EXTRA_UCROP_LOGO_COLOR = EXTRA_PREFIX + ".UcropLogoColor"

            @JvmField
            val EXTRA_HIDE_BOTTOM_CONTROLS = EXTRA_PREFIX + ".HideBottomControls"
            @JvmField
            val EXTRA_FREE_STYLE_CROP = EXTRA_PREFIX + ".FreeStyleCrop"

            @JvmField
            val EXTRA_ASPECT_RATIO_SELECTED_BY_DEFAULT = EXTRA_PREFIX + ".AspectRatioSelectedByDefault"
            @JvmField
            val EXTRA_ASPECT_RATIO_OPTIONS = EXTRA_PREFIX + ".AspectRatioOptions"
            @JvmField
            val EXTRA_SKIP_CROP_MIME_TYPE = EXTRA_PREFIX + ".SkipCropMimeType"

            @JvmField
            val EXTRA_MULTIPLE_ASPECT_RATIO = EXTRA_PREFIX + ".MultipleAspectRatio"

            @JvmField
            val EXTRA_UCROP_ROOT_VIEW_BACKGROUND_COLOR = EXTRA_PREFIX + ".UcropRootViewBackgroundColor"
        }

        val optionBundle: Bundle = Bundle()

        /**
         * Set one of {@link android.graphics.Bitmap.CompressFormat} that will be used to save resulting Bitmap.
         */
        fun setCompressionFormat(@NonNull format: Bitmap.CompressFormat) {
            optionBundle.putString(EXTRA_COMPRESSION_FORMAT_NAME, format.name)
        }

        /**
         * Set one of {@link context.getExternalFilesDir()} The path that will be used to save
         * when clipping multiple drawings
         * Valid when multiple pictures are cropped
         */
        fun setCropOutputPathDir(@NonNull dir: String) {
            optionBundle.putString(EXTRA_CROP_OUTPUT_DIR, dir)
        }

        /**
         * File name after clipping output
         * Valid when multiple pictures are cropped
         * <p>
         * When multiple pictures are cropped, the front will automatically keep up with the timestamp
         * </p>
         */
        fun setCropOutputFileName(@NonNull fileName: String) {
            optionBundle.putString(EXTRA_CROP_OUTPUT_FILE_NAME, fileName)
        }

        /**
         * @param isForbidSkipCrop - It is forbidden to skip when cutting multiple drawings
         */
        fun isForbidSkipMultipleCrop(isForbidSkipCrop: Boolean) {
            optionBundle.putBoolean(EXTRA_CROP_FORBID_SKIP, isForbidSkipCrop)
        }

        /**
         * Get the bitmap of the uCrop resource using the custom loader
         *
         * @param isUseBitmap
         */
        fun isUseCustomLoaderBitmap(isUseBitmap: Boolean) {
            optionBundle.putBoolean(EXTRA_CROP_CUSTOM_LOADER_BITMAP, isUseBitmap)
        }

        /**
         * isDragCenter
         *
         * @param isDragCenter Crop and drag automatically center
         */
        fun isCropDragSmoothToCenter(isDragCenter: Boolean) {
            optionBundle.putBoolean(EXTRA_CROP_DRAG_CENTER, isDragCenter)
        }

        /**
         * @param isForbidCropGifWebp - Do you need to support clipping dynamic graphs gif or webp
         */
        fun isForbidCropGifWebp(isForbidCropGifWebp: Boolean) {
            optionBundle.putBoolean(EXTRA_CROP_FORBID_GIF_WEBP, isForbidCropGifWebp)
        }

        /**
         * Set compression quality [0-100] that will be used to save resulting Bitmap.
         */
        fun setCompressionQuality(@IntRange(from = 0) compressQuality: Int) {
            optionBundle.putInt(EXTRA_COMPRESSION_QUALITY, compressQuality)
        }

        /**
         * Choose what set of gestures will be enabled on each tab - if any.
         */
        fun setAllowedGestures(@UCropActivity.GestureTypes tabScale: Int,
                               @UCropActivity.GestureTypes tabRotate: Int,
                               @UCropActivity.GestureTypes tabAspectRatio: Int) {
            optionBundle.putIntArray(EXTRA_ALLOWED_GESTURES, intArrayOf(tabScale, tabRotate, tabAspectRatio))
        }

        /**
         * This method sets multiplier that is used to calculate max image scale from min image scale.
         *
         * @param maxScaleMultiplier - (minScale * maxScaleMultiplier) = maxScale
         */
        fun setMaxScaleMultiplier(@FloatRange(from = 1.0, fromInclusive = false) maxScaleMultiplier: Float) {
            optionBundle.putFloat(EXTRA_MAX_SCALE_MULTIPLIER, maxScaleMultiplier)
        }

        /**
         * This method sets animation duration for image to wrap the crop bounds
         *
         * @param durationMillis - duration in milliseconds
         */
        fun setImageToCropBoundsAnimDuration(@IntRange(from = MIN_SIZE.toLong()) durationMillis: Int) {
            optionBundle.putInt(EXTRA_IMAGE_TO_CROP_BOUNDS_ANIM_DURATION, durationMillis)
        }

        /**
         * Setter for max size for both width and height of bitmap that will be decoded from an input Uri and used in the view.
         *
         * @param maxBitmapSize - size in pixels
         */
        fun setMaxBitmapSize(@IntRange(from = MIN_SIZE.toLong()) maxBitmapSize: Int) {
            optionBundle.putInt(EXTRA_MAX_BITMAP_SIZE, maxBitmapSize)
        }

        /**
         * @param color - desired color of dimmed area around the crop bounds
         */
        fun setDimmedLayerColor(@ColorInt color: Int) {
            optionBundle.putInt(EXTRA_DIMMED_LAYER_COLOR, color)
        }

        /**
         * @param color - desired color of dimmed stroke area around the crop bounds
         */
        fun setCircleStrokeColor(@ColorInt color: Int) {
            optionBundle.putInt(EXTRA_CIRCLE_STROKE_COLOR, color)
        }

        /**
         * @param isCircle - set it to true if you want dimmed layer to have an circle inside
         */
        fun setCircleDimmedLayer(isCircle: Boolean) {
            optionBundle.putBoolean(EXTRA_CIRCLE_DIMMED_LAYER, isCircle)
        }

        /**
         * @param show - set to true if you want to see a crop frame rectangle on top of an image
         */
        fun setShowCropFrame(show: Boolean) {
            optionBundle.putBoolean(EXTRA_SHOW_CROP_FRAME, show)
        }

        /**
         * @param color - desired color of crop frame
         */
        fun setCropFrameColor(@ColorInt color: Int) {
            optionBundle.putInt(EXTRA_CROP_FRAME_COLOR, color)
        }

        /**
         * @param width - desired width of crop frame line in pixels
         */
        fun setCropFrameStrokeWidth(@IntRange(from = 0) width: Int) {
            optionBundle.putInt(EXTRA_CROP_FRAME_STROKE_WIDTH, width)
        }

        /**
         * @param show - set to true if you want to see a crop grid/guidelines on top of an image
         */
        fun setShowCropGrid(show: Boolean) {
            optionBundle.putBoolean(EXTRA_SHOW_CROP_GRID, show)
        }

        /**
         * @param count - crop grid rows count.
         */
        fun setCropGridRowCount(@IntRange(from = 0) count: Int) {
            optionBundle.putInt(EXTRA_CROP_GRID_ROW_COUNT, count)
        }

        /**
         * @param count - crop grid columns count.
         */
        fun setCropGridColumnCount(@IntRange(from = 0) count: Int) {
            optionBundle.putInt(EXTRA_CROP_GRID_COLUMN_COUNT, count)
        }

        /**
         * @param color - desired color of crop grid/guidelines
         */
        fun setCropGridColor(@ColorInt color: Int) {
            optionBundle.putInt(EXTRA_CROP_GRID_COLOR, color)
        }

        /**
         * @param width - desired width of crop grid lines in pixels
         */
        fun setCropGridStrokeWidth(@IntRange(from = 0) width: Int) {
            optionBundle.putInt(EXTRA_CROP_GRID_STROKE_WIDTH, width)
        }

        /**
         * @param width Set the circular clipping border
         */
        fun setCircleStrokeWidth(@IntRange(from = 0) width: Int) {
            optionBundle.putInt(EXTRA_CIRCLE_STROKE_WIDTH_LAYER, width)
        }

        /**
         * @param color - desired resolved color of the gallery bar background
         */
        fun setCropGalleryBarBackgroundResources(@ColorInt color: Int) {
            optionBundle.putInt(EXTRA_GALLERY_BAR_BACKGROUND, color)
        }

        /**
         * @param color - desired resolved color of the toolbar
         */
        fun setToolbarColor(@ColorInt color: Int) {
            optionBundle.putInt(EXTRA_TOOL_BAR_COLOR, color)
        }

        /**
         * @param color - desired resolved color of the statusbar
         */
        fun setStatusBarColor(@ColorInt color: Int) {
            optionBundle.putInt(EXTRA_STATUS_BAR_COLOR, color)
        }

        /**
         * @param Is the font of the status bar black
         */
        fun isDarkStatusBarBlack(isDarkStatusBarBlack: Boolean) {
            optionBundle.putBoolean(EXTRA_DARK_STATUS_BAR_BLACK, isDarkStatusBarBlack)
        }

        /**
         * Can I drag and drop images when crop
         *
         * @param isDragImages
         */
        fun isDragCropImages(isDragImages: Boolean) {
            optionBundle.putBoolean(EXTRA_DRAG_IMAGES, isDragImages)
        }

        /**
         * @param color - desired resolved color of the active and selected widget and progress wheel middle line (default is white)
         */
        fun setActiveControlsWidgetColor(@ColorInt color: Int) {
            optionBundle.putInt(EXTRA_UCROP_COLOR_CONTROLS_WIDGET_ACTIVE, color)
        }

        /**
         * @param color - desired resolved color of Toolbar text and buttons (default is darker orange)
         */
        fun setToolbarWidgetColor(@ColorInt color: Int) {
            optionBundle.putInt(EXTRA_UCROP_WIDGET_COLOR_TOOLBAR, color)
        }

        /**
         * @param text - desired text for Toolbar title
         */
        fun setToolbarTitle(@Nullable text: String?) {
            optionBundle.putString(EXTRA_UCROP_TITLE_TEXT_TOOLBAR, text)
        }

        /**
         * @param textSize - desired text for Toolbar title
         */
        fun setToolbarTitleSize(textSize: Int) {
            if (textSize > 0) {
                optionBundle.putInt(EXTRA_UCROP_TITLE_TEXT_SIZE_TOOLBAR, textSize)
            }
        }

        /**
         * @param drawable - desired drawable for the Toolbar left cancel icon
         */
        fun setToolbarCancelDrawable(@DrawableRes drawable: Int) {
            optionBundle.putInt(EXTRA_UCROP_WIDGET_CANCEL_DRAWABLE, drawable)
        }

        /**
         * @param drawable - desired drawable for the Toolbar right crop icon
         */
        fun setToolbarCropDrawable(@DrawableRes drawable: Int) {
            optionBundle.putInt(EXTRA_UCROP_WIDGET_CROP_DRAWABLE, drawable)
        }

        /**
         * @param color - desired resolved color of logo fill (default is darker grey)
         */
        fun setLogoColor(@ColorInt color: Int) {
            optionBundle.putInt(EXTRA_UCROP_LOGO_COLOR, color)
        }

        /**
         * @param hide - set to true to hide the bottom controls (shown by default)
         */
        fun setHideBottomControls(hide: Boolean) {
            optionBundle.putBoolean(EXTRA_HIDE_BOTTOM_CONTROLS, hide)
        }

        /**
         * @param enabled - set to true to let user resize crop bounds (disabled by default)
         */
        fun setFreeStyleCropEnabled(enabled: Boolean) {
            optionBundle.putBoolean(EXTRA_FREE_STYLE_CROP, enabled)
        }

        /**
         * Pass an ordered list of desired aspect ratios that should be available for a user.
         *
         * @param selectedByDefault - index of aspect ratio option that is selected by default (starts with 0).
         * @param aspectRatio       - list of aspect ratio options that are available to user
         */
        fun setAspectRatioOptions(selectedByDefault: Int, vararg aspectRatio: AspectRatio) {
            if (selectedByDefault >= aspectRatio.size) {
                throw IllegalArgumentException(String.format(Locale.US,
                    "Index [selectedByDefault = %d] (0-based) cannot be higher or equal than aspect ratio options count [count = %d].",
                    selectedByDefault, aspectRatio.size))
            }
            optionBundle.putInt(EXTRA_ASPECT_RATIO_SELECTED_BY_DEFAULT, selectedByDefault)
            optionBundle.putParcelableArrayList(EXTRA_ASPECT_RATIO_OPTIONS, ArrayList<Parcelable>(Arrays.asList(*aspectRatio)))
        }

        /**
         * Skip crop mimeType
         *
         * @param mimeTypes Use example {@link { image/gift or image/webp ... }}
         * @return
         */
        fun setSkipCropMimeType(vararg mimeTypes: String) {
            if (mimeTypes.isNotEmpty()) {
                optionBundle.putStringArrayList(EXTRA_SKIP_CROP_MIME_TYPE, ArrayList(Arrays.asList(*mimeTypes)))
            }
        }

        /**
         * @param color - desired background color that should be applied to the root view
         */
        fun setRootViewBackgroundColor(@ColorInt color: Int) {
            optionBundle.putInt(EXTRA_UCROP_ROOT_VIEW_BACKGROUND_COLOR, color)
        }

        /**
         * Set an aspect ratio for crop bounds.
         * User won't see the menu with other ratios options.
         *
         * @param x aspect ratio X
         * @param y aspect ratio Y
         */
        fun withAspectRatio(x: Float, y: Float) {
            optionBundle.putFloat(EXTRA_ASPECT_RATIO_X, x)
            optionBundle.putFloat(EXTRA_ASPECT_RATIO_Y, y)
        }

        /**
         * The corresponding crop scale of each graph in multi graph crop
         *
         * @param aspectRatio - The corresponding crop scale of each graph in multi graph crop
         */
        fun setMultipleCropAspectRatio(vararg aspectRatio: AspectRatio) {
            val aspectRatioX = optionBundle.getFloat(EXTRA_ASPECT_RATIO_X, 0f)
            val aspectRatioY = optionBundle.getFloat(EXTRA_ASPECT_RATIO_Y, 0f)
            if (aspectRatio.isNotEmpty() && aspectRatioX <= 0 && aspectRatioY <= 0) {
                withAspectRatio(aspectRatio[0].aspectRatioX, aspectRatio[0].aspectRatioY)
            }
            optionBundle.putParcelableArrayList(EXTRA_MULTIPLE_ASPECT_RATIO, ArrayList<Parcelable>(Arrays.asList(*aspectRatio)))
        }

        /**
         * Set an aspect ratio for crop bounds that is evaluated from source image width and height.
         * User won't see the menu with other ratios options.
         */
        fun useSourceImageAspectRatio() {
            optionBundle.putFloat(EXTRA_ASPECT_RATIO_X, 0f)
            optionBundle.putFloat(EXTRA_ASPECT_RATIO_Y, 0f)
        }

        /**
         * Set maximum size for result cropped image.
         *
         * @param width  max cropped image width
         * @param height max cropped image height
         */
        fun withMaxResultSize(@IntRange(from = MIN_SIZE.toLong()) width: Int, @IntRange(from = MIN_SIZE.toLong()) height: Int) {
            optionBundle.putInt(EXTRA_MAX_SIZE_X, width)
            optionBundle.putInt(EXTRA_MAX_SIZE_Y, height)
        }
    }
}

