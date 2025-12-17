package com.yalantis.ucrop

import android.annotation.TargetApi
import android.content.Intent
import android.graphics.ColorFilter
import android.graphics.drawable.Animatable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.text.TextUtils
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.view.animation.AnimationUtils
import android.view.animation.LayoutAnimationController
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.annotation.NonNull
import androidx.annotation.Nullable
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.graphics.BlendModeColorFilterCompat
import androidx.core.graphics.BlendModeCompat
import androidx.fragment.app.FragmentTransaction
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.yalantis.ucrop.R
import com.yalantis.ucrop.decoration.GridSpacingItemDecoration
import com.yalantis.ucrop.model.AspectRatio
import com.yalantis.ucrop.model.CustomIntentKey
import com.yalantis.ucrop.statusbar.ImmersiveManager
import com.yalantis.ucrop.util.DensityUtil
import com.yalantis.ucrop.util.FileUtils
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.util.ArrayList
import java.util.HashSet
import java.util.LinkedHashMap

/**
 * @author：luck
 * @date：2021/11/28 7:59 下午
 * @describe：UCropMultipleActivity
 */
class UCropMultipleActivity : AppCompatActivity(), UCropFragmentCallback {
    private var mToolbarTitle: String? = null
    private var mToolbarTitleSize: Int = 0
    // Enables dynamic coloring
    private var mToolbarColor: Int = 0
    private var mStatusBarColor: Int = 0
    @DrawableRes
    private var mToolbarCancelDrawable: Int = 0
    @DrawableRes
    private var mToolbarCropDrawable: Int = 0
    private var mToolbarWidgetColor: Int = 0
    private var mShowLoader: Boolean = false
    private val fragments = ArrayList<UCropFragment>()
    private var uCropCurrentFragment: UCropFragment? = null
    private var currentFragmentPosition: Int = 0
    private var uCropSupportList: ArrayList<String>? = null
    private var uCropNotSupportList: ArrayList<String>? = null
    private val uCropTotalQueue = LinkedHashMap<String, JSONObject>()
    private var outputCropFileName: String? = null
    private var galleryAdapter: UCropGalleryAdapter? = null
    private var isForbidCropGifWebp: Boolean = false
    private var isSkipCropForbid: Boolean = false
    private var aspectRatioList: ArrayList<AspectRatio>? = null
    private val filterSet = HashSet<String>()

    companion object {
        init {
            AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
        }
    }

    override fun onCreate(@Nullable savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        immersive()
        setContentView(R.layout.ucrop_activity_multiple)
        setupViews(intent)
        initCropFragments(intent)
    }

    private fun immersive() {
        val intent = intent
        val isDarkStatusBarBlack = intent.getBooleanExtra(UCrop.Options.EXTRA_DARK_STATUS_BAR_BLACK, false)
        mStatusBarColor = intent.getIntExtra(UCrop.Options.EXTRA_STATUS_BAR_COLOR, ContextCompat.getColor(this, R.color.ucrop_color_statusbar))
        ImmersiveManager.immersiveAboveAPI23(this, mStatusBarColor, mStatusBarColor, isDarkStatusBarBlack)
    }

    private fun initCropFragments(intent: Intent) {
        isSkipCropForbid = intent.getBooleanExtra(UCrop.Options.EXTRA_CROP_FORBID_SKIP, false)
        val totalCropData = intent.getStringArrayListExtra(UCrop.EXTRA_CROP_TOTAL_DATA_SOURCE)
        if (totalCropData == null || totalCropData.isEmpty()) {
            throw IllegalArgumentException("Missing required parameters, count cannot be less than 1")
        }
        uCropSupportList = ArrayList()
        uCropNotSupportList = ArrayList()
        for (i in totalCropData.indices) {
            val path = totalCropData[i]
            uCropTotalQueue[path] = JSONObject()
            val realPath = if (FileUtils.isContent(path)) {
                FileUtils.getPath(this, Uri.parse(path))
            } else {
                path
            }
            val mimeType = getPathToMimeType(path)
            if (FileUtils.isUrlHasVideo(realPath) || FileUtils.isHasVideo(mimeType) || FileUtils.isHasAudio(mimeType)) {
                // not crop type
                uCropNotSupportList!!.add(path)
            } else {
                uCropSupportList!!.add(path)
                val extras = intent.extras ?: continue
                val inputUri = if (FileUtils.isContent(path) || FileUtils.isHasHttp(path)) {
                    Uri.parse(path)
                } else {
                    Uri.fromFile(File(path))
                }
                val postfix = FileUtils.getPostfixDefaultJPEG(this, isForbidCropGifWebp, inputUri)
                val fileName = if (TextUtils.isEmpty(outputCropFileName)) {
                    FileUtils.getCreateFileName("CROP_${i + 1}") + postfix
                } else {
                    "${i + 1}${FileUtils.getCreateFileName()}_$outputCropFileName"
                }
                val destinationUri = Uri.fromFile(File(getSandboxPathDir(), fileName))
                extras.putParcelable(UCrop.EXTRA_INPUT_URI, inputUri)
                extras.putParcelable(UCrop.EXTRA_OUTPUT_URI, destinationUri)
                val aspectRatio = if (aspectRatioList != null && aspectRatioList!!.size > i) {
                    aspectRatioList!![i]
                } else {
                    null
                }
                aspectRatio?.let {
                    extras.putFloat(UCrop.EXTRA_ASPECT_RATIO_X, it.aspectRatioX)
                    extras.putFloat(UCrop.EXTRA_ASPECT_RATIO_Y, it.aspectRatioY)
                }
                val uCropFragment = UCropFragment.newInstance(extras)
                fragments.add(uCropFragment)
            }
        }

        if (uCropSupportList!!.isEmpty()) {
            throw IllegalArgumentException("No clipping data sources are available")
        }
        setGalleryAdapter()
        val uCropFragment = fragments[getCropSupportPosition()]
        switchCropFragment(uCropFragment, getCropSupportPosition())
        galleryAdapter!!.setCurrentSelectPosition(getCropSupportPosition())
    }

    /**
     * getCropSupportPosition
     *
     * @return
     */
    private fun getCropSupportPosition(): Int {
        var position = 0
        val intent = intent
        val extras = intent.extras ?: return position
        val skipCropMimeType = extras.getStringArrayList(UCrop.Options.EXTRA_SKIP_CROP_MIME_TYPE)
        if (skipCropMimeType != null && skipCropMimeType.isNotEmpty()) {
            position = -1
            filterSet.addAll(skipCropMimeType)
            for (i in uCropSupportList!!.indices) {
                val path = uCropSupportList!![i]
                val mimeType = getPathToMimeType(path)
                position++
                if (!filterSet.contains(mimeType)) {
                    break
                }
            }
            if (position == -1 || position > fragments.size) {
                position = 0
            }
        }
        return position
    }

    /**
     * getPathToMimeType
     *
     * @param path
     * @return
     */
    private fun getPathToMimeType(path: String): String {
        return if (FileUtils.isContent(path)) {
            FileUtils.getMimeTypeFromMediaContentUri(this, Uri.parse(path)) ?: ""
        } else {
            FileUtils.getMimeTypeFromMediaContentUri(this, Uri.fromFile(File(path))) ?: ""
        }
    }

    /**
     * switch crop fragment tab
     *
     * @param targetFragment target fragment
     * @param position       target index
     */
    private fun switchCropFragment(targetFragment: UCropFragment, position: Int) {
        val transaction = supportFragmentManager.beginTransaction()
        if (!targetFragment.isAdded) {
            uCropCurrentFragment?.let {
                transaction.hide(it)
            }
            transaction.add(R.id.fragment_container, targetFragment, "${UCropFragment.TAG}-$position")
        } else {
            transaction.hide(uCropCurrentFragment!!).show(targetFragment)
            targetFragment.fragmentReVisible()
        }
        currentFragmentPosition = position
        uCropCurrentFragment = targetFragment
        transaction.commitAllowingStateLoss()
    }

    private fun setGalleryAdapter() {
        val galleryRecycle = findViewById<RecyclerView>(R.id.recycler_gallery)
        val layoutManager = LinearLayoutManager(this)
        layoutManager.orientation = LinearLayoutManager.HORIZONTAL
        galleryRecycle.layoutManager = layoutManager
        if (galleryRecycle.itemDecorationCount == 0) {
            galleryRecycle.addItemDecoration(GridSpacingItemDecoration(Int.MAX_VALUE,
                DensityUtil.dip2px(this, 6f), true))
        }
        val animation = AnimationUtils
            .loadLayoutAnimation(this, R.anim.ucrop_layout_animation_fall_down)
        galleryRecycle.layoutAnimation = animation
        val galleryBarBackground = intent.getIntExtra(UCrop.Options.EXTRA_GALLERY_BAR_BACKGROUND,
            R.drawable.ucrop_gallery_bg)
        galleryRecycle.setBackgroundResource(galleryBarBackground)
        galleryAdapter = UCropGalleryAdapter(uCropSupportList!!)
        galleryAdapter!!.setOnItemClickListener(object : UCropGalleryAdapter.OnItemClickListener {
            override fun onItemClick(position: Int, view: View) {
                if (isSkipCropForbid) {
                    return
                }
                val path = uCropSupportList!![position]
                val mimeType = getPathToMimeType(path)
                if (filterSet.contains(mimeType)) {
                    Toast.makeText(applicationContext,
                        getString(R.string.ucrop_not_crop), Toast.LENGTH_SHORT).show()
                    return
                }
                if (galleryAdapter!!.getCurrentSelectPosition() == position) {
                    return
                }
                galleryAdapter!!.notifyItemChanged(galleryAdapter!!.getCurrentSelectPosition())
                galleryAdapter!!.setCurrentSelectPosition(position)
                galleryAdapter!!.notifyItemChanged(position)
                val uCropFragment = fragments[position]
                switchCropFragment(uCropFragment, position)
            }
        })
        galleryRecycle.adapter = galleryAdapter
    }

    /**
     * create crop output path dir
     *
     * @return
     */
    private fun getSandboxPathDir(): String {
        val customFile: File
        val outputDir = intent.getStringExtra(UCrop.Options.EXTRA_CROP_OUTPUT_DIR)
        customFile = if (outputDir == null || "" == outputDir) {
            File(getExternalFilesDir(Environment.DIRECTORY_PICTURES)!!.absolutePath, "Sandbox")
        } else {
            File(outputDir)
        }
        if (!customFile.exists()) {
            customFile.mkdirs()
        }
        return customFile.absolutePath + File.separator
    }

    private fun setupViews(@NonNull intent: Intent) {
        aspectRatioList = getIntent().getParcelableArrayListExtra(UCrop.Options.EXTRA_MULTIPLE_ASPECT_RATIO)
        isForbidCropGifWebp = intent.getBooleanExtra(UCrop.Options.EXTRA_CROP_FORBID_GIF_WEBP, false)
        outputCropFileName = intent.getStringExtra(UCrop.Options.EXTRA_CROP_OUTPUT_FILE_NAME)
        mStatusBarColor = intent.getIntExtra(UCrop.Options.EXTRA_STATUS_BAR_COLOR, ContextCompat.getColor(this, R.color.ucrop_color_statusbar))
        mToolbarColor = intent.getIntExtra(UCrop.Options.EXTRA_TOOL_BAR_COLOR, ContextCompat.getColor(this, R.color.ucrop_color_toolbar))

        mToolbarWidgetColor = intent.getIntExtra(UCrop.Options.EXTRA_UCROP_WIDGET_COLOR_TOOLBAR, ContextCompat.getColor(this, R.color.ucrop_color_toolbar_widget))
        mToolbarCancelDrawable = intent.getIntExtra(UCrop.Options.EXTRA_UCROP_WIDGET_CANCEL_DRAWABLE, R.drawable.ucrop_ic_cross)
        mToolbarCropDrawable = intent.getIntExtra(UCrop.Options.EXTRA_UCROP_WIDGET_CROP_DRAWABLE, R.drawable.ucrop_ic_done)
        mToolbarTitle = intent.getStringExtra(UCrop.Options.EXTRA_UCROP_TITLE_TEXT_TOOLBAR)
        mToolbarTitleSize = intent.getIntExtra(UCrop.Options.EXTRA_UCROP_TITLE_TEXT_SIZE_TOOLBAR, 18)
        mToolbarTitle = mToolbarTitle ?: resources.getString(R.string.ucrop_label_edit_photo)

        setupAppBar()
    }

    /**
     * Configures and styles both status bar and toolbar.
     */
    private fun setupAppBar() {
        setStatusBarColor(mStatusBarColor)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)

        // Set all of the Toolbar coloring
        toolbar.setBackgroundColor(mToolbarColor)
        toolbar.setTitleTextColor(mToolbarWidgetColor)

        val toolbarTitle = toolbar.findViewById<TextView>(R.id.toolbar_title)
        toolbarTitle.setTextColor(mToolbarWidgetColor)
        toolbarTitle.text = mToolbarTitle
        toolbarTitle.textSize = mToolbarTitleSize.toFloat()

        // Color buttons inside the Toolbar
        val stateButtonDrawable = AppCompatResources.getDrawable(this, mToolbarCancelDrawable)!!.mutate()
        val colorFilter = BlendModeColorFilterCompat.createBlendModeColorFilterCompat(mToolbarWidgetColor, BlendModeCompat.SRC_ATOP)
        stateButtonDrawable.colorFilter = colorFilter
        toolbar.navigationIcon = stateButtonDrawable
        setSupportActionBar(toolbar)
        val actionBar = supportActionBar
        actionBar?.setDisplayShowTitleEnabled(false)
    }

    /**
     * Sets status-bar color for L devices.
     *
     * @param color - status-bar color
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private fun setStatusBarColor(@ColorInt color: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val window = window
            window?.let {
                it.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
                it.statusBarColor = color
            }
        }
    }

    override fun loadingProgress(showLoader: Boolean) {
        mShowLoader = showLoader
        supportInvalidateOptionsMenu()
    }

    override fun onCropFinish(result: UCropFragment.UCropResult) {
        when (result.mResultCode) {
            RESULT_OK -> {
                val realPosition = currentFragmentPosition + uCropNotSupportList!!.size
                val realTotalSize = uCropNotSupportList!!.size + uCropSupportList!!.size - 1
                mergeCropResult(result.mResultData)
                if (realPosition == realTotalSize) {
                    onCropCompleteFinish()
                } else {
                    var nextFragmentPosition = currentFragmentPosition + 1
                    var path = uCropSupportList!![nextFragmentPosition]
                    var mimeType = getPathToMimeType(path)
                    var isCropCompleteFinish = false
                    while (filterSet.contains(mimeType)) {
                        if (nextFragmentPosition == realTotalSize) {
                            isCropCompleteFinish = true
                            break
                        } else {
                            nextFragmentPosition += 1
                            path = uCropSupportList!![nextFragmentPosition]
                            mimeType = getPathToMimeType(path)
                        }
                    }
                    if (isCropCompleteFinish) {
                        onCropCompleteFinish()
                    } else {
                        val uCropFragment = fragments[nextFragmentPosition]
                        switchCropFragment(uCropFragment, nextFragmentPosition)
                        galleryAdapter!!.notifyItemChanged(galleryAdapter!!.getCurrentSelectPosition())
                        galleryAdapter!!.setCurrentSelectPosition(nextFragmentPosition)
                        galleryAdapter!!.notifyItemChanged(galleryAdapter!!.getCurrentSelectPosition())
                    }
                }
            }
            UCrop.RESULT_ERROR -> {
                handleCropError(result.mResultData)
            }
        }
    }

    /**
     * onCropCompleteFinish
     */
    private fun onCropCompleteFinish() {
        val array = JSONArray()
        for ((_, value) in uCropTotalQueue) {
            array.put(value)
        }
        val intent = Intent()
        intent.putExtra(MediaStore.EXTRA_OUTPUT, array.toString())
        setResult(RESULT_OK, intent)
        finish()
    }

    /**
     * merge crop result
     *
     * @param intent
     */
    private fun mergeCropResult(intent: Intent) {
        try {
            val key = intent.getStringExtra(UCrop.EXTRA_CROP_INPUT_ORIGINAL)
            val uCropObject = uCropTotalQueue[key] ?: return
            val output = UCrop.getOutput(intent)
            uCropObject.put(CustomIntentKey.EXTRA_OUT_PUT_PATH, output?.path ?: "")
            uCropObject.put(CustomIntentKey.EXTRA_IMAGE_WIDTH, UCrop.getOutputImageWidth(intent))
            uCropObject.put(CustomIntentKey.EXTRA_IMAGE_HEIGHT, UCrop.getOutputImageHeight(intent))
            uCropObject.put(CustomIntentKey.EXTRA_OFFSET_X, UCrop.getOutputImageOffsetX(intent))
            uCropObject.put(CustomIntentKey.EXTRA_OFFSET_Y, UCrop.getOutputImageOffsetY(intent))
            uCropObject.put(CustomIntentKey.EXTRA_ASPECT_RATIO, UCrop.getOutputCropAspectRatio(intent))
            if (key != null) {
                uCropTotalQueue[key] = uCropObject
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @Suppress("ThrowableResultOfMethodCallIgnored")
    private fun handleCropError(@NonNull result: Intent) {
        val cropError = UCrop.getError(result)
        if (cropError != null) {
            Toast.makeText(this, cropError.message, Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(this, "Unexpected error", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroy() {
        UCropDevelopConfig.destroy()
        super.onDestroy()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.ucrop_menu_activity, menu)

        // Change crop & loader menu icons color to match the rest of the UI colors

        val menuItemLoader = menu.findItem(R.id.menu_loader)
        val menuItemLoaderIcon = menuItemLoader.icon
        menuItemLoaderIcon?.let {
            try {
                it.mutate()
                val colorFilter = BlendModeColorFilterCompat.createBlendModeColorFilterCompat(mToolbarWidgetColor, BlendModeCompat.SRC_ATOP)
                it.colorFilter = colorFilter
                menuItemLoader.icon = it
            } catch (e: IllegalStateException) {
                e.printStackTrace()
            }
            (menuItemLoader.icon as? Animatable)?.start()
        }

        val menuItemCrop = menu.findItem(R.id.menu_crop)
        val menuItemCropIcon = ContextCompat.getDrawable(this, mToolbarCropDrawable)
        menuItemCropIcon?.let {
            it.mutate()
            val colorFilter = BlendModeColorFilterCompat.createBlendModeColorFilterCompat(mToolbarWidgetColor, BlendModeCompat.SRC_ATOP)
            it.colorFilter = colorFilter
            menuItemCrop.icon = it
        }

        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        menu.findItem(R.id.menu_crop).isVisible = !mShowLoader
        menu.findItem(R.id.menu_loader).isVisible = mShowLoader
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_crop -> {
                if (uCropCurrentFragment != null && uCropCurrentFragment!!.isAdded) {
                    uCropCurrentFragment!!.cropAndSaveImage()
                }
            }
            android.R.id.home -> {
                onBackPressed()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
}

