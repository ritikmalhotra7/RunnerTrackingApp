package com.example.runnertrackingapp.ui.fragments

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.example.runnertrackingapp.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class DialogFragment() : DialogFragment() {

    private var yesListener: (() -> Unit)? = null

    fun setYesListener(listener:()->Unit){
        yesListener = listener
    }
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return MaterialAlertDialogBuilder(
            requireContext(),
            R.style.AlertDialogTheme
        ).setTitle("Cancel Run?")
            .setMessage("Are you sure to cancel run and delete all its data?")
            .setIcon(R.drawable.icons8_delete_1)
            .setPositiveButton("Yes") { _, _ -> yesListener }
            .setNegativeButton("No") { dialogInterface, _ -> dialogInterface.cancel() }
            .create()
        dialog?.show()

    }
}