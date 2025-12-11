package com.luck.picture.lib.dialog

import android.app.Dialog
import android.content.Context
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.luck.picture.lib.R

/**
 * @author：luck
 * @date：2021/11/19 5:11 下午
 * @describe：RemindDialog
 */
class RemindDialog(context: Context, tips: String?) : Dialog(context, R.style.Picture_Theme_Dialog),
    View.OnClickListener {
    private lateinit var btnOk: TextView
    private lateinit var tvContent: TextView

    fun setButtonText(text: String?) {
        btnOk.text = text
    }

    fun setButtonTextColor(color: Int) {
        btnOk.setTextColor(color)
    }

    fun setContent(text: String?) {
        tvContent.text = text
    }

    fun setContentTextColor(color: Int) {
        tvContent.setTextColor(color)
    }

    private fun setDialogSize() {
        val params = window?.attributes
        if (params != null) {
            params.width = ViewGroup.LayoutParams.WRAP_CONTENT
            params.height = ViewGroup.LayoutParams.WRAP_CONTENT
            params.gravity = Gravity.CENTER
            window?.setWindowAnimations(R.style.PictureThemeDialogWindowStyle)
            window?.attributes = params
        }
    }

    override fun onClick(view: View) {
        val id = view.id
        if (id == R.id.btnOk) {
            if (listener != null) {
                listener!!.onClick(view)
            } else {
                dismiss()
            }
        }
    }

    private var listener: OnDialogClickListener? = null

    init {
        setContentView(R.layout.ps_remind_dialog)
        btnOk = findViewById<TextView>(R.id.btnOk)
        tvContent = findViewById<TextView>(R.id.tv_content)
        tvContent.text = tips
        btnOk.setOnClickListener(this)
        setDialogSize()
    }

    fun setOnDialogClickListener(listener: OnDialogClickListener?) {
        this.listener = listener
    }

    interface OnDialogClickListener {
        fun onClick(view: View?)
    }

    companion object {
        @Deprecated("")
        fun showTipsDialog(context: Context, tips: String?): Dialog {
            return RemindDialog(context, tips)
        }

        fun buildDialog(context: Context, tips: String?): RemindDialog {
            return RemindDialog(context, tips)
        }
    }
}
