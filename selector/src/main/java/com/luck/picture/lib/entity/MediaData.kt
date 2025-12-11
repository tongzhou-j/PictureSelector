package com.luck.picture.lib.entity

/**
 * @author：luck
 * @date：2020-04-17 13:52
 * @describe：MediaData
 */
class MediaData {
    /**
     * Is there more
     */
    var isHasNextMore: Boolean = false

    /**
     * data
     */
    var data: ArrayList<LocalMedia?>? = null


    constructor() : super()

    constructor(isHasNextMore: Boolean, data: ArrayList<LocalMedia?>?) : super() {
        this.isHasNextMore = isHasNextMore
        this.data = data
    }
}
