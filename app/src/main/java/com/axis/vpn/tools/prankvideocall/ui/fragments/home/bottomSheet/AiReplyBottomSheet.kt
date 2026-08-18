package com.axis.vpn.tools.prankvideocall.ui.fragments.home.bottomSheet

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import androidx.core.os.bundleOf
import com.axis.vpn.tools.prankvideocall.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView

class AiReplyBottomSheet : BottomSheetDialogFragment() {

    private var selectedOption = "manual"

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(savedInstanceState)

        selectedOption =
            arguments?.getString(MODE)
                ?: "manual"
    }
    companion object {

        private const val MODE = "mode"

        fun newInstance(
            mode: String
        ): AiReplyBottomSheet {

            return AiReplyBottomSheet().apply {
                arguments = bundleOf(
                    MODE to mode
                )
            }
        }
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(
            R.layout.bottom_sheet_ai_reply,
            container,
            false
        )
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)

        val cardManual =
            view.findViewById<MaterialCardView>(R.id.cardManual)

        val cardAi =
            view.findViewById<MaterialCardView>(R.id.cardAi)

        val cbManual =
            view.findViewById<CheckBox>(R.id.cbManual)

        val cbAi =
            view.findViewById<CheckBox>(R.id.cbAi)

        val btnContinue =
            view.findViewById<MaterialButton>(R.id.btnContinue)

        fun selectManual() {

            selectedOption = "manual"

            cbManual.isChecked = true
            cbAi.isChecked = false

            cardManual.strokeWidth = 4
            cardAi.strokeWidth = 0
        }

        fun selectAi() {

            selectedOption = "ai"

            cbManual.isChecked = false
            cbAi.isChecked = true

            cardManual.strokeWidth = 0
            cardAi.strokeWidth = 4
        }

        cardManual.setOnClickListener {
            selectManual()
        }

        cbManual.setOnClickListener {
            selectManual()
        }

        cardAi.setOnClickListener {
            selectAi()
        }

        cbAi.setOnClickListener {
            selectAi()
        }

        // Restore previous selection
        if (selectedOption == "ai") {
            selectAi()
        } else {
            selectManual()
        }

        btnContinue.setOnClickListener {

            parentFragmentManager.setFragmentResult(
                "ai_settings",
                bundleOf(
                    "mode" to selectedOption
                )
            )

            dismiss()
        }
    }
}