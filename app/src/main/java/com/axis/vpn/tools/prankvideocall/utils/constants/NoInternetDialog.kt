package com.axis.vpn.tools.prankvideocall.utils.constants

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import com.axis.vpn.tools.prankvideocall.R

class NoInternetDialog(context: Context) : Dialog(context) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.dialog_no_internet)

        window?.setBackgroundDrawable(
            ColorDrawable(Color.TRANSPARENT)
        )

        setCancelable(false)
        setCanceledOnTouchOutside(false)
    }
}