# Java 转 Kotlin 转换指南

## 文件统计

- **总文件数**: 337
- **模块数**: 6

### 按模块统计

- `app`: 29 个文件
- `camerax`: 30 个文件
- `compress`: 20 个文件
- `ijkplayer-java`: 28 个文件
- `selector`: 189 个文件
- `ucrop`: 41 个文件

## 转换方法

### 方法 1: 使用 Android Studio GUI（推荐）

1. 打开 Android Studio
2. 打开项目: `/Users/zt/workspace/PictureSelector`
3. 在 Project 视图中，选择要转换的文件或目录
4. 右键点击，选择 `Code` > `Convert Java File to Kotlin File`
5. 或使用快捷键:
   - Windows/Linux: `Ctrl+Alt+Shift+K`
   - Mac: `Cmd+Option+Shift+K`

### 方法 2: 批量转换单个目录

1. 在 Project 视图中选择整个包或目录
2. 右键选择 `Code` > `Convert Java File to Kotlin File`
3. Android Studio 会转换目录下所有 Java 文件

### 方法 3: 按模块转换（推荐顺序）

1. **compress** (20 个文件)
   - 路径: `/Users/zt/workspace/PictureSelector/compress/src/main/java/`
   - 建议: 先转换工具类，再转换业务类

2. **ijkplayer-java** (28 个文件)
   - 路径: `/Users/zt/workspace/PictureSelector/ijkplayer-java/src/main/java/`
   - 建议: 先转换工具类，再转换业务类

3. **app** (29 个文件)
   - 路径: `/Users/zt/workspace/PictureSelector/app/src/main/java/`
   - 建议: 先转换工具类，再转换业务类

4. **camerax** (30 个文件)
   - 路径: `/Users/zt/workspace/PictureSelector/camerax/src/main/java/`
   - 建议: 先转换工具类，再转换业务类

5. **ucrop** (41 个文件)
   - 路径: `/Users/zt/workspace/PictureSelector/ucrop/src/main/java/`
   - 建议: 先转换工具类，再转换业务类

6. **selector** (189 个文件)
   - 路径: `/Users/zt/workspace/PictureSelector/selector/src/main/java/`
   - 建议: 先转换工具类，再转换业务类

## 需要转换的文件列表

### 按模块分组

#### app

- `app/src/main/java/com/luck/pictureselector/App.java`
- `app/src/main/java/com/luck/pictureselector/CustomBottomNavBar.java`
- `app/src/main/java/com/luck/pictureselector/CustomCompleteSelectView.java`
- `app/src/main/java/com/luck/pictureselector/CustomLoadingDialog.java`
- `app/src/main/java/com/luck/pictureselector/CustomPreviewAdapter.java`
- `app/src/main/java/com/luck/pictureselector/CustomPreviewBottomNavBar.java`
- `app/src/main/java/com/luck/pictureselector/CustomPreviewFragment.java`
- `app/src/main/java/com/luck/pictureselector/CustomPreviewTitleBar.java`
- `app/src/main/java/com/luck/pictureselector/CustomTitleBar.java`
- `app/src/main/java/com/luck/pictureselector/ExoPlayerEngine.java`
- `app/src/main/java/com/luck/pictureselector/FullyGridLayoutManager.java`
- `app/src/main/java/com/luck/pictureselector/GlideEngine.java`
- `app/src/main/java/com/luck/pictureselector/IjkPlayerEngine.java`
- `app/src/main/java/com/luck/pictureselector/IjkPlayerView.java`
- `app/src/main/java/com/luck/pictureselector/ImageCacheUtils.java`
- `app/src/main/java/com/luck/pictureselector/ImageLoaderUtils.java`
- `app/src/main/java/com/luck/pictureselector/ImageUtil.java`
- `app/src/main/java/com/luck/pictureselector/InjectFragmentActivity.java`
- `app/src/main/java/com/luck/pictureselector/LineWrapRadioGroup.java`
- `app/src/main/java/com/luck/pictureselector/MainActivity.java`
- `app/src/main/java/com/luck/pictureselector/OnlyQueryDataActivity.java`
- `app/src/main/java/com/luck/pictureselector/PicassoEngine.java`
- `app/src/main/java/com/luck/pictureselector/PictureSelectorEngineImp.java`
- `app/src/main/java/com/luck/pictureselector/RoundedCornersTransform.java`
- `app/src/main/java/com/luck/pictureselector/SimpleActivity.java`
- `app/src/main/java/com/luck/pictureselector/VideoRequestHandler.java`
- `app/src/main/java/com/luck/pictureselector/adapter/GridImageAdapter.java`
- `app/src/main/java/com/luck/pictureselector/listener/DragListener.java`
- `app/src/main/java/com/luck/pictureselector/listener/OnItemLongClickListener.java`

#### camerax

- `camerax/src/main/java/com/luck/lib/camerax/CameraImageEngine.java`
- `camerax/src/main/java/com/luck/lib/camerax/CustomCameraConfig.java`
- `camerax/src/main/java/com/luck/lib/camerax/CustomCameraView.java`
- `camerax/src/main/java/com/luck/lib/camerax/PictureCameraActivity.java`
- `camerax/src/main/java/com/luck/lib/camerax/SimpleCameraX.java`
- `camerax/src/main/java/com/luck/lib/camerax/listener/CameraListener.java`
- `camerax/src/main/java/com/luck/lib/camerax/listener/CameraXOrientationEventListener.java`
- `camerax/src/main/java/com/luck/lib/camerax/listener/CameraXPreviewViewTouchListener.java`
- `camerax/src/main/java/com/luck/lib/camerax/listener/CaptureListener.java`
- `camerax/src/main/java/com/luck/lib/camerax/listener/ClickListener.java`
- `camerax/src/main/java/com/luck/lib/camerax/listener/IObtainCameraView.java`
- `camerax/src/main/java/com/luck/lib/camerax/listener/ImageCallbackListener.java`
- `camerax/src/main/java/com/luck/lib/camerax/listener/OnSimpleXPermissionDeniedListener.java`
- `camerax/src/main/java/com/luck/lib/camerax/listener/OnSimpleXPermissionDescriptionListener.java`
- `camerax/src/main/java/com/luck/lib/camerax/listener/TypeListener.java`
- `camerax/src/main/java/com/luck/lib/camerax/permissions/PermissionChecker.java`
- `camerax/src/main/java/com/luck/lib/camerax/permissions/PermissionResultCallback.java`
- `camerax/src/main/java/com/luck/lib/camerax/permissions/SimpleXPermissionUtil.java`
- `camerax/src/main/java/com/luck/lib/camerax/utils/BitmapUtils.java`
- `camerax/src/main/java/com/luck/lib/camerax/utils/CameraUtils.java`
- `camerax/src/main/java/com/luck/lib/camerax/utils/DateUtils.java`
- `camerax/src/main/java/com/luck/lib/camerax/utils/DensityUtil.java`
- `camerax/src/main/java/com/luck/lib/camerax/utils/DoubleUtils.java`
- `camerax/src/main/java/com/luck/lib/camerax/utils/FileUtils.java`
- `camerax/src/main/java/com/luck/lib/camerax/utils/SimpleXSpUtils.java`
- `camerax/src/main/java/com/luck/lib/camerax/widget/CaptureButton.java`
- `camerax/src/main/java/com/luck/lib/camerax/widget/CaptureLayout.java`
- `camerax/src/main/java/com/luck/lib/camerax/widget/FocusImageView.java`
- `camerax/src/main/java/com/luck/lib/camerax/widget/ReturnButton.java`
- `camerax/src/main/java/com/luck/lib/camerax/widget/TypeButton.java`

#### compress

- `compress/src/main/java/top/zibin/luban/Checker.java`
- `compress/src/main/java/top/zibin/luban/CompressionPredicate.java`
- `compress/src/main/java/top/zibin/luban/Engine.java`
- `compress/src/main/java/top/zibin/luban/InputStreamAdapter.java`
- `compress/src/main/java/top/zibin/luban/InputStreamProvider.java`
- `compress/src/main/java/top/zibin/luban/Luban.java`
- `compress/src/main/java/top/zibin/luban/LubanUtils.java`
- `compress/src/main/java/top/zibin/luban/OnCompressListener.java`
- `compress/src/main/java/top/zibin/luban/OnNewCompressListener.java`
- `compress/src/main/java/top/zibin/luban/OnRenameListener.java`
- `compress/src/main/java/top/zibin/luban/io/ArrayAdapterInterface.java`
- `compress/src/main/java/top/zibin/luban/io/ArrayPool.java`
- `compress/src/main/java/top/zibin/luban/io/ArrayPoolProvide.java`
- `compress/src/main/java/top/zibin/luban/io/BaseKeyPool.java`
- `compress/src/main/java/top/zibin/luban/io/BufferedInputStreamWrap.java`
- `compress/src/main/java/top/zibin/luban/io/ByteArrayAdapter.java`
- `compress/src/main/java/top/zibin/luban/io/GroupedLinkedMap.java`
- `compress/src/main/java/top/zibin/luban/io/IntegerArrayAdapter.java`
- `compress/src/main/java/top/zibin/luban/io/LruArrayPool.java`
- `compress/src/main/java/top/zibin/luban/io/PoolAble.java`

#### ijkplayer-java

- `ijkplayer-java/src/androidTest/java/tv/danmaku/ijk/media/player/ApplicationTest.java`
- `ijkplayer-java/src/main/java/tv/danmaku/ijk/media/player/AbstractMediaPlayer.java`
- `ijkplayer-java/src/main/java/tv/danmaku/ijk/media/player/AndroidMediaPlayer.java`
- `ijkplayer-java/src/main/java/tv/danmaku/ijk/media/player/IMediaPlayer.java`
- `ijkplayer-java/src/main/java/tv/danmaku/ijk/media/player/ISurfaceTextureHolder.java`
- `ijkplayer-java/src/main/java/tv/danmaku/ijk/media/player/ISurfaceTextureHost.java`
- `ijkplayer-java/src/main/java/tv/danmaku/ijk/media/player/IjkLibLoader.java`
- `ijkplayer-java/src/main/java/tv/danmaku/ijk/media/player/IjkMediaCodecInfo.java`
- `ijkplayer-java/src/main/java/tv/danmaku/ijk/media/player/IjkMediaMeta.java`
- `ijkplayer-java/src/main/java/tv/danmaku/ijk/media/player/IjkMediaPlayer.java`
- `ijkplayer-java/src/main/java/tv/danmaku/ijk/media/player/IjkTimedText.java`
- `ijkplayer-java/src/main/java/tv/danmaku/ijk/media/player/MediaInfo.java`
- `ijkplayer-java/src/main/java/tv/danmaku/ijk/media/player/MediaPlayerProxy.java`
- `ijkplayer-java/src/main/java/tv/danmaku/ijk/media/player/TextureMediaPlayer.java`
- `ijkplayer-java/src/main/java/tv/danmaku/ijk/media/player/annotations/AccessedByNative.java`
- `ijkplayer-java/src/main/java/tv/danmaku/ijk/media/player/annotations/CalledByNative.java`
- `ijkplayer-java/src/main/java/tv/danmaku/ijk/media/player/exceptions/IjkMediaException.java`
- `ijkplayer-java/src/main/java/tv/danmaku/ijk/media/player/ffmpeg/FFmpegApi.java`
- `ijkplayer-java/src/main/java/tv/danmaku/ijk/media/player/misc/AndroidMediaFormat.java`
- `ijkplayer-java/src/main/java/tv/danmaku/ijk/media/player/misc/AndroidTrackInfo.java`
- `ijkplayer-java/src/main/java/tv/danmaku/ijk/media/player/misc/IAndroidIO.java`
- `ijkplayer-java/src/main/java/tv/danmaku/ijk/media/player/misc/IMediaDataSource.java`
- `ijkplayer-java/src/main/java/tv/danmaku/ijk/media/player/misc/IMediaFormat.java`
- `ijkplayer-java/src/main/java/tv/danmaku/ijk/media/player/misc/ITrackInfo.java`
- `ijkplayer-java/src/main/java/tv/danmaku/ijk/media/player/misc/IjkMediaFormat.java`
- `ijkplayer-java/src/main/java/tv/danmaku/ijk/media/player/misc/IjkTrackInfo.java`
- `ijkplayer-java/src/main/java/tv/danmaku/ijk/media/player/pragma/DebugLog.java`
- `ijkplayer-java/src/main/java/tv/danmaku/ijk/media/player/pragma/Pragma.java`

#### selector

- `selector/src/main/java/com/luck/picture/lib/PictureOnlyCameraFragment.java`
- `selector/src/main/java/com/luck/picture/lib/PictureSelectorFragment.java`
- `selector/src/main/java/com/luck/picture/lib/PictureSelectorPreviewFragment.java`
- `selector/src/main/java/com/luck/picture/lib/PictureSelectorSystemFragment.java`
- `selector/src/main/java/com/luck/picture/lib/adapter/PictureAlbumAdapter.java`
- `selector/src/main/java/com/luck/picture/lib/adapter/PictureImageGridAdapter.java`
- `selector/src/main/java/com/luck/picture/lib/adapter/PicturePreviewAdapter.java`
- `selector/src/main/java/com/luck/picture/lib/adapter/holder/AudioViewHolder.java`
- `selector/src/main/java/com/luck/picture/lib/adapter/holder/BasePreviewHolder.java`
- `selector/src/main/java/com/luck/picture/lib/adapter/holder/BaseRecyclerMediaHolder.java`
- `selector/src/main/java/com/luck/picture/lib/adapter/holder/CameraViewHolder.java`
- `selector/src/main/java/com/luck/picture/lib/adapter/holder/ImageViewHolder.java`
- `selector/src/main/java/com/luck/picture/lib/adapter/holder/PreviewAudioHolder.java`
- `selector/src/main/java/com/luck/picture/lib/adapter/holder/PreviewGalleryAdapter.java`
- `selector/src/main/java/com/luck/picture/lib/adapter/holder/PreviewImageHolder.java`
- `selector/src/main/java/com/luck/picture/lib/adapter/holder/PreviewVideoHolder.java`
- `selector/src/main/java/com/luck/picture/lib/adapter/holder/VideoViewHolder.java`
- `selector/src/main/java/com/luck/picture/lib/animators/AlphaInAnimationAdapter.java`
- `selector/src/main/java/com/luck/picture/lib/animators/AnimationType.java`
- `selector/src/main/java/com/luck/picture/lib/animators/BaseAnimationAdapter.java`
- `selector/src/main/java/com/luck/picture/lib/animators/SlideInBottomAnimationAdapter.java`
- `selector/src/main/java/com/luck/picture/lib/animators/ViewHelper.java`
- `selector/src/main/java/com/luck/picture/lib/app/IApp.java`
- `selector/src/main/java/com/luck/picture/lib/app/PictureAppMaster.java`
- `selector/src/main/java/com/luck/picture/lib/basic/FragmentInjectManager.java`
- `selector/src/main/java/com/luck/picture/lib/basic/IBridgeLoaderFactory.java`
- `selector/src/main/java/com/luck/picture/lib/basic/IBridgePictureBehavior.java`
- `selector/src/main/java/com/luck/picture/lib/basic/IBridgeViewLifecycle.java`
- `selector/src/main/java/com/luck/picture/lib/basic/IPictureSelectorCommonEvent.java`
- `selector/src/main/java/com/luck/picture/lib/basic/IPictureSelectorEvent.java`
- `selector/src/main/java/com/luck/picture/lib/basic/InterpolatorFactory.java`
- `selector/src/main/java/com/luck/picture/lib/basic/PictureCommonFragment.java`
- `selector/src/main/java/com/luck/picture/lib/basic/PictureContentResolver.java`
- `selector/src/main/java/com/luck/picture/lib/basic/PictureContextWrapper.java`
- `selector/src/main/java/com/luck/picture/lib/basic/PictureFileProvider.java`
- `selector/src/main/java/com/luck/picture/lib/basic/PictureMediaScannerConnection.java`
- `selector/src/main/java/com/luck/picture/lib/basic/PictureSelectionCameraModel.java`
- `selector/src/main/java/com/luck/picture/lib/basic/PictureSelectionModel.java`
- `selector/src/main/java/com/luck/picture/lib/basic/PictureSelectionPreviewModel.java`
- `selector/src/main/java/com/luck/picture/lib/basic/PictureSelectionQueryModel.java`
- `selector/src/main/java/com/luck/picture/lib/basic/PictureSelectionSystemModel.java`
- `selector/src/main/java/com/luck/picture/lib/basic/PictureSelector.java`
- `selector/src/main/java/com/luck/picture/lib/basic/PictureSelectorSupporterActivity.java`
- `selector/src/main/java/com/luck/picture/lib/basic/PictureSelectorTransparentActivity.java`
- `selector/src/main/java/com/luck/picture/lib/config/Crop.java`
- `selector/src/main/java/com/luck/picture/lib/config/CustomIntentKey.java`
- `selector/src/main/java/com/luck/picture/lib/config/FileSizeUnit.java`
- `selector/src/main/java/com/luck/picture/lib/config/InjectResourceSource.java`
- `selector/src/main/java/com/luck/picture/lib/config/PermissionEvent.java`
- `selector/src/main/java/com/luck/picture/lib/config/PictureConfig.java`
- `selector/src/main/java/com/luck/picture/lib/config/PictureMimeType.java`
- `selector/src/main/java/com/luck/picture/lib/config/SelectLimitType.java`
- `selector/src/main/java/com/luck/picture/lib/config/SelectMimeType.java`
- `selector/src/main/java/com/luck/picture/lib/config/SelectModeConfig.java`
- `selector/src/main/java/com/luck/picture/lib/config/SelectorConfig.java`
- `selector/src/main/java/com/luck/picture/lib/config/SelectorProviders.java`
- `selector/src/main/java/com/luck/picture/lib/config/VideoQuality.java`
- `selector/src/main/java/com/luck/picture/lib/decoration/GridSpacingItemDecoration.java`
- `selector/src/main/java/com/luck/picture/lib/decoration/HorizontalItemDecoration.java`
- `selector/src/main/java/com/luck/picture/lib/decoration/ViewPage2ItemDecoration.java`
- `selector/src/main/java/com/luck/picture/lib/decoration/WrapContentLinearLayoutManager.java`
- `selector/src/main/java/com/luck/picture/lib/dialog/AlbumListPopWindow.java`
- `selector/src/main/java/com/luck/picture/lib/dialog/PhotoItemSelectedDialog.java`
- `selector/src/main/java/com/luck/picture/lib/dialog/PictureCommonDialog.java`
- `selector/src/main/java/com/luck/picture/lib/dialog/PictureLoadingDialog.java`
- `selector/src/main/java/com/luck/picture/lib/dialog/RemindDialog.java`
- `selector/src/main/java/com/luck/picture/lib/engine/CompressEngine.java`
- `selector/src/main/java/com/luck/picture/lib/engine/CompressFileEngine.java`
- `selector/src/main/java/com/luck/picture/lib/engine/CropEngine.java`
- `selector/src/main/java/com/luck/picture/lib/engine/CropFileEngine.java`
- `selector/src/main/java/com/luck/picture/lib/engine/ExtendLoaderEngine.java`
- `selector/src/main/java/com/luck/picture/lib/engine/ImageEngine.java`
- `selector/src/main/java/com/luck/picture/lib/engine/MediaPlayerEngine.java`
- `selector/src/main/java/com/luck/picture/lib/engine/PictureSelectorEngine.java`
- `selector/src/main/java/com/luck/picture/lib/engine/SandboxFileEngine.java`
- `selector/src/main/java/com/luck/picture/lib/engine/UriToFileTransformEngine.java`
- `selector/src/main/java/com/luck/picture/lib/engine/VideoPlayerEngine.java`
- `selector/src/main/java/com/luck/picture/lib/entity/LocalMedia.java`
- `selector/src/main/java/com/luck/picture/lib/entity/LocalMediaFolder.java`
- `selector/src/main/java/com/luck/picture/lib/entity/MediaData.java`
- `selector/src/main/java/com/luck/picture/lib/entity/MediaExtraInfo.java`
- `selector/src/main/java/com/luck/picture/lib/immersive/ImmersiveManager.java`
- `selector/src/main/java/com/luck/picture/lib/immersive/LightStatusBarUtils.java`
- `selector/src/main/java/com/luck/picture/lib/immersive/RomUtils.java`
- `selector/src/main/java/com/luck/picture/lib/interfaces/OnAlbumItemClickListener.java`
- `selector/src/main/java/com/luck/picture/lib/interfaces/OnBitmapWatermarkEventListener.java`
- `selector/src/main/java/com/luck/picture/lib/interfaces/OnCallbackIndexListener.java`
- `selector/src/main/java/com/luck/picture/lib/interfaces/OnCallbackListener.java`
- `selector/src/main/java/com/luck/picture/lib/interfaces/OnCameraInterceptListener.java`
- `selector/src/main/java/com/luck/picture/lib/interfaces/OnCustomLoadingListener.java`
- `selector/src/main/java/com/luck/picture/lib/interfaces/OnExternalPreviewEventListener.java`
- `selector/src/main/java/com/luck/picture/lib/interfaces/OnGridItemSelectAnimListener.java`
- `selector/src/main/java/com/luck/picture/lib/interfaces/OnInjectActivityPreviewListener.java`
- `selector/src/main/java/com/luck/picture/lib/interfaces/OnInjectLayoutResourceListener.java`
- `selector/src/main/java/com/luck/picture/lib/interfaces/OnItemClickListener.java`
- `selector/src/main/java/com/luck/picture/lib/interfaces/OnKeyValueResultCallbackListener.java`
- `selector/src/main/java/com/luck/picture/lib/interfaces/OnMediaEditInterceptListener.java`
- `selector/src/main/java/com/luck/picture/lib/interfaces/OnPermissionDeniedListener.java`
- `selector/src/main/java/com/luck/picture/lib/interfaces/OnPermissionDescriptionListener.java`
- `selector/src/main/java/com/luck/picture/lib/interfaces/OnPermissionsInterceptListener.java`
- `selector/src/main/java/com/luck/picture/lib/interfaces/OnPlayerListener.java`
- `selector/src/main/java/com/luck/picture/lib/interfaces/OnPreviewInterceptListener.java`
- `selector/src/main/java/com/luck/picture/lib/interfaces/OnQueryAlbumListener.java`
- `selector/src/main/java/com/luck/picture/lib/interfaces/OnQueryAllAlbumListener.java`
- `selector/src/main/java/com/luck/picture/lib/interfaces/OnQueryDataResultListener.java`
- `selector/src/main/java/com/luck/picture/lib/interfaces/OnQueryDataSourceListener.java`
- `selector/src/main/java/com/luck/picture/lib/interfaces/OnQueryFilterListener.java`
- `selector/src/main/java/com/luck/picture/lib/interfaces/OnRecordAudioInterceptListener.java`
- `selector/src/main/java/com/luck/picture/lib/interfaces/OnRecyclerViewPreloadMoreListener.java`
- `selector/src/main/java/com/luck/picture/lib/interfaces/OnRecyclerViewScrollListener.java`
- `selector/src/main/java/com/luck/picture/lib/interfaces/OnRecyclerViewScrollStateListener.java`
- `selector/src/main/java/com/luck/picture/lib/interfaces/OnRequestPermissionListener.java`
- `selector/src/main/java/com/luck/picture/lib/interfaces/OnResultCallbackListener.java`
- `selector/src/main/java/com/luck/picture/lib/interfaces/OnSelectAnimListener.java`
- `selector/src/main/java/com/luck/picture/lib/interfaces/OnSelectFilterListener.java`
- `selector/src/main/java/com/luck/picture/lib/interfaces/OnSelectLimitTipsListener.java`
- `selector/src/main/java/com/luck/picture/lib/interfaces/OnVideoThumbnailEventListener.java`
- `selector/src/main/java/com/luck/picture/lib/language/LanguageConfig.java`
- `selector/src/main/java/com/luck/picture/lib/language/LocaleTransform.java`
- `selector/src/main/java/com/luck/picture/lib/language/PictureLanguageUtils.java`
- `selector/src/main/java/com/luck/picture/lib/loader/IBridgeMediaLoader.java`
- `selector/src/main/java/com/luck/picture/lib/loader/LocalMediaLoader.java`
- `selector/src/main/java/com/luck/picture/lib/loader/LocalMediaPageLoader.java`
- `selector/src/main/java/com/luck/picture/lib/loader/SandboxFileLoader.java`
- `selector/src/main/java/com/luck/picture/lib/magical/BuildRecycleItemViewParams.java`
- `selector/src/main/java/com/luck/picture/lib/magical/MagicalView.java`
- `selector/src/main/java/com/luck/picture/lib/magical/MagicalViewWrapper.java`
- `selector/src/main/java/com/luck/picture/lib/magical/OnMagicalViewCallback.java`
- `selector/src/main/java/com/luck/picture/lib/magical/ViewParams.java`
- `selector/src/main/java/com/luck/picture/lib/manager/PictureCacheManager.java`
- `selector/src/main/java/com/luck/picture/lib/manager/SelectedManager.java`
- `selector/src/main/java/com/luck/picture/lib/obj/pool/ObjectPools.java`
- `selector/src/main/java/com/luck/picture/lib/permissions/PermissionChecker.java`
- `selector/src/main/java/com/luck/picture/lib/permissions/PermissionConfig.java`
- `selector/src/main/java/com/luck/picture/lib/permissions/PermissionResultCallback.java`
- `selector/src/main/java/com/luck/picture/lib/permissions/PermissionUtil.java`
- `selector/src/main/java/com/luck/picture/lib/photoview/Compat.java`
- `selector/src/main/java/com/luck/picture/lib/photoview/CustomGestureDetector.java`
- `selector/src/main/java/com/luck/picture/lib/photoview/OnGestureListener.java`
- `selector/src/main/java/com/luck/picture/lib/photoview/OnMatrixChangedListener.java`
- `selector/src/main/java/com/luck/picture/lib/photoview/OnOutsidePhotoTapListener.java`
- `selector/src/main/java/com/luck/picture/lib/photoview/OnPhotoTapListener.java`
- `selector/src/main/java/com/luck/picture/lib/photoview/OnScaleChangedListener.java`
- `selector/src/main/java/com/luck/picture/lib/photoview/OnSingleFlingListener.java`
- `selector/src/main/java/com/luck/picture/lib/photoview/OnViewDragListener.java`
- `selector/src/main/java/com/luck/picture/lib/photoview/OnViewTapListener.java`
- `selector/src/main/java/com/luck/picture/lib/photoview/PhotoView.java`
- `selector/src/main/java/com/luck/picture/lib/photoview/PhotoViewAttacher.java`
- `selector/src/main/java/com/luck/picture/lib/photoview/Util.java`
- `selector/src/main/java/com/luck/picture/lib/service/ForegroundService.java`
- `selector/src/main/java/com/luck/picture/lib/style/AlbumWindowStyle.java`
- `selector/src/main/java/com/luck/picture/lib/style/BottomNavBarStyle.java`
- `selector/src/main/java/com/luck/picture/lib/style/PictureSelectorStyle.java`
- `selector/src/main/java/com/luck/picture/lib/style/PictureWindowAnimationStyle.java`
- `selector/src/main/java/com/luck/picture/lib/style/SelectMainStyle.java`
- `selector/src/main/java/com/luck/picture/lib/style/TitleBarStyle.java`
- `selector/src/main/java/com/luck/picture/lib/thread/PictureThreadUtils.java`
- `selector/src/main/java/com/luck/picture/lib/utils/ActivityCompatHelper.java`
- `selector/src/main/java/com/luck/picture/lib/utils/AnimUtils.java`
- `selector/src/main/java/com/luck/picture/lib/utils/BitmapUtils.java`
- `selector/src/main/java/com/luck/picture/lib/utils/DateUtils.java`
- `selector/src/main/java/com/luck/picture/lib/utils/DensityUtil.java`
- `selector/src/main/java/com/luck/picture/lib/utils/DoubleUtils.java`
- `selector/src/main/java/com/luck/picture/lib/utils/DownloadFileUtils.java`
- `selector/src/main/java/com/luck/picture/lib/utils/FileDirMap.java`
- `selector/src/main/java/com/luck/picture/lib/utils/IntentUtils.java`
- `selector/src/main/java/com/luck/picture/lib/utils/MediaStoreUtils.java`
- `selector/src/main/java/com/luck/picture/lib/utils/MediaUtils.java`
- `selector/src/main/java/com/luck/picture/lib/utils/PSEglUtils.java`
- `selector/src/main/java/com/luck/picture/lib/utils/PictureFileUtils.java`
- `selector/src/main/java/com/luck/picture/lib/utils/SandboxTransformUtils.java`
- `selector/src/main/java/com/luck/picture/lib/utils/SdkVersionUtils.java`
- `selector/src/main/java/com/luck/picture/lib/utils/SortUtils.java`
- `selector/src/main/java/com/luck/picture/lib/utils/SpUtils.java`
- `selector/src/main/java/com/luck/picture/lib/utils/StyleUtils.java`
- `selector/src/main/java/com/luck/picture/lib/utils/ToastUtils.java`
- `selector/src/main/java/com/luck/picture/lib/widget/BottomNavBar.java`
- `selector/src/main/java/com/luck/picture/lib/widget/CompleteSelectView.java`
- `selector/src/main/java/com/luck/picture/lib/widget/MarqueeTextView.java`
- `selector/src/main/java/com/luck/picture/lib/widget/MediaPlayerView.java`
- `selector/src/main/java/com/luck/picture/lib/widget/MediumBoldTextView.java`
- `selector/src/main/java/com/luck/picture/lib/widget/PreviewBottomNavBar.java`
- `selector/src/main/java/com/luck/picture/lib/widget/PreviewTitleBar.java`
- `selector/src/main/java/com/luck/picture/lib/widget/RecyclerPreloadView.java`
- `selector/src/main/java/com/luck/picture/lib/widget/RoundCornerRelativeLayout.java`
- `selector/src/main/java/com/luck/picture/lib/widget/SlideSelectTouchListener.java`
- `selector/src/main/java/com/luck/picture/lib/widget/SlideSelectionHandler.java`
- `selector/src/main/java/com/luck/picture/lib/widget/SquareRelativeLayout.java`
- `selector/src/main/java/com/luck/picture/lib/widget/TitleBar.java`

#### ucrop

- `ucrop/src/main/java/com/yalantis/ucrop/OkHttpClientStore.java`
- `ucrop/src/main/java/com/yalantis/ucrop/UCrop.java`
- `ucrop/src/main/java/com/yalantis/ucrop/UCropActivity.java`
- `ucrop/src/main/java/com/yalantis/ucrop/UCropDevelopConfig.java`
- `ucrop/src/main/java/com/yalantis/ucrop/UCropFragment.java`
- `ucrop/src/main/java/com/yalantis/ucrop/UCropFragmentCallback.java`
- `ucrop/src/main/java/com/yalantis/ucrop/UCropGalleryAdapter.java`
- `ucrop/src/main/java/com/yalantis/ucrop/UCropImageEngine.java`
- `ucrop/src/main/java/com/yalantis/ucrop/UCropMultipleActivity.java`
- `ucrop/src/main/java/com/yalantis/ucrop/callback/BitmapCropCallback.java`
- `ucrop/src/main/java/com/yalantis/ucrop/callback/BitmapLoadCallback.java`
- `ucrop/src/main/java/com/yalantis/ucrop/callback/CropBoundsChangeListener.java`
- `ucrop/src/main/java/com/yalantis/ucrop/callback/OverlayViewChangeListener.java`
- `ucrop/src/main/java/com/yalantis/ucrop/decoration/GridSpacingItemDecoration.java`
- `ucrop/src/main/java/com/yalantis/ucrop/model/AspectRatio.java`
- `ucrop/src/main/java/com/yalantis/ucrop/model/CropParameters.java`
- `ucrop/src/main/java/com/yalantis/ucrop/model/CustomIntentKey.java`
- `ucrop/src/main/java/com/yalantis/ucrop/model/ExifInfo.java`
- `ucrop/src/main/java/com/yalantis/ucrop/model/ImageState.java`
- `ucrop/src/main/java/com/yalantis/ucrop/statusbar/ImmersiveManager.java`
- `ucrop/src/main/java/com/yalantis/ucrop/statusbar/LightStatusBarUtils.java`
- `ucrop/src/main/java/com/yalantis/ucrop/statusbar/RomUtils.java`
- `ucrop/src/main/java/com/yalantis/ucrop/task/BitmapCropTask.java`
- `ucrop/src/main/java/com/yalantis/ucrop/task/BitmapLoadTask.java`
- `ucrop/src/main/java/com/yalantis/ucrop/util/BitmapLoadUtils.java`
- `ucrop/src/main/java/com/yalantis/ucrop/util/CubicEasing.java`
- `ucrop/src/main/java/com/yalantis/ucrop/util/DensityUtil.java`
- `ucrop/src/main/java/com/yalantis/ucrop/util/EglUtils.java`
- `ucrop/src/main/java/com/yalantis/ucrop/util/FastBitmapDrawable.java`
- `ucrop/src/main/java/com/yalantis/ucrop/util/FileUtils.java`
- `ucrop/src/main/java/com/yalantis/ucrop/util/ImageHeaderParser.java`
- `ucrop/src/main/java/com/yalantis/ucrop/util/RectUtils.java`
- `ucrop/src/main/java/com/yalantis/ucrop/util/RotationGestureDetector.java`
- `ucrop/src/main/java/com/yalantis/ucrop/util/SelectedStateListDrawable.java`
- `ucrop/src/main/java/com/yalantis/ucrop/view/CropImageView.java`
- `ucrop/src/main/java/com/yalantis/ucrop/view/GestureCropImageView.java`
- `ucrop/src/main/java/com/yalantis/ucrop/view/OverlayView.java`
- `ucrop/src/main/java/com/yalantis/ucrop/view/TransformImageView.java`
- `ucrop/src/main/java/com/yalantis/ucrop/view/UCropView.java`
- `ucrop/src/main/java/com/yalantis/ucrop/view/widget/AspectRatioTextView.java`
- `ucrop/src/main/java/com/yalantis/ucrop/view/widget/HorizontalProgressWheelView.java`

## 转换后检查清单

- [ ] 编译项目，确保没有语法错误
- [ ] 检查 Kotlin 代码风格（使用 ktlint 或 detekt）
- [ ] 运行单元测试
- [ ] 运行应用，测试核心功能
- [ ] 检查性能是否有影响
- [ ] 更新相关文档
