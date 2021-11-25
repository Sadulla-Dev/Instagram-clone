package com.example.instagram_clone.views

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.example.instagram_clone.R
import kotlinx.android.synthetic.main.password_dialog.view.*

class PasswordDialog: DialogFragment() {
    private lateinit var mListener: Listener

    interface Listener {
        fun onPasswordConfirm(password: String)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mListener = context as Listener
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = requireActivity().layoutInflater.inflate(R.layout.password_dialog, null)
        return AlertDialog.Builder(requireContext()).setView(view)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                mListener.onPasswordConfirm(view. password_input.text.toString())
            }.setNegativeButton(android.R.string.cancel) { _, _ ->

            }.setTitle(R.string.please_enter_password).create()
    }
}
