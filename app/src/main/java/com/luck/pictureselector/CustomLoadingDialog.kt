package com.luck.pictureselector

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.view.WindowManager
import com.luck.pictureselector.R
import com.luck.picture.lib.R as PictureLibR

class CustomLoadingDialog(context: Context) : Dialog(context, PictureLibR.style.Picture_Theme_AlertDialog) {

    init {
        setCancelable(true)
        setCanceledOnTouchOutside(false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.custom_alert_dialog)
        setDialogSize()
    }

    private fun setDialogSize() {
        val params = window!!.attributes
        params.width = ViewGroup.LayoutParams.WRAP_CONTENT
        params.height = ViewGroup.LayoutParams.WRAP_CONTENT
        params.gravity = Gravity.CENTER
        window!!.setWindowAnimations(PictureLibR.style.PictureThemeDialogWindowStyle)
        window!!.attributes = params
    }
}

