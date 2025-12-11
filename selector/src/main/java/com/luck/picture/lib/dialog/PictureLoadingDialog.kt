package com.luck.picture.lib.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import com.luck.picture.lib.R


class PictureLoadingDialog(context: Context) : Dialog(context, R.style.Picture_Theme_AlertDialog) {
    init {
        setCancelable(true)
        setCanceledOnTouchOutside(false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.ps_alert_dialog)
        setDialogSize()
    }

    private fun setDialogSize() {
        val params = getWindow()!!.getAttributes()
        params.width = ViewGroup.LayoutParams.WRAP_CONTENT
        params.height = ViewGroup.LayoutParams.WRAP_CONTENT
        params.gravity = Gravity.CENTER
        getWindow()!!.setWindowAnimations(R.style.PictureThemeDialogWindowStyle)
        getWindow()!!.setAttributes(params)
    }
}