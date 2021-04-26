package com.diamont.expense.tracker.settingsFragment.chooseLanguageDialogFragment

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.diamont.expense.tracker.R
import com.diamont.expense.tracker.util.AppLocale
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class ChooseLanguageDialogFragment(
    private val selectedLanguageString: String,
    private val onPositiveButtonClicked: (selectedLangString: String) -> Unit
) : DialogFragment() {
    /**
     * The required views
     */
    private lateinit var layout : View
    private lateinit var dialog: AlertDialog
    private var radioButtonList: MutableList<RadioButton> = mutableListOf<RadioButton>()

    /**
     * onCreateDialog()
     */
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        /**
         * Inflate the layout
         */
        layout = View.inflate(requireContext(), R.layout.dialog_select_language, null)

        /**
         * We need to return the dialog we want to show()
         */
        dialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle(resources.getString(R.string.choose_language))
            .setView(layout)
            .setNegativeButton(resources.getString(R.string.cancel)) { _, _ -> }
            .setPositiveButton(resources.getString(R.string.choose)) { _, _ ->
                var selectedLang = ""

                for(i in radioButtonList.indices){
                    if(radioButtonList[i].isChecked){
                        selectedLang = AppLocale.supportedLocales[i].localeString
                    }
                }

                onPositiveButtonClicked(selectedLang)
            }
            .create()

        return dialog
    }

    /**
     * onCreateView()
     */
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rgContainer = layout.findViewById<RadioGroup>(R.id.rgSelectLangDialogRadioGroup)
        var isLanguageCheckBoxChecked: Boolean = false

        /**
         * Create the default radio button
         */
        val defaultRadioButton = RadioButton(context)
        defaultRadioButton.text = resources.getString(R.string.default_language)
        rgContainer.addView(defaultRadioButton)

        /**
         * Add a radio button for each locale
         */
        for(locale in AppLocale.supportedLocales){
            val langRadioButton = RadioButton(context)
            langRadioButton.text = resources.getString(locale.stringResId)

            if(locale.localeString == selectedLanguageString){
                //langRadioButton.isChecked = true
                isLanguageCheckBoxChecked = true
            }

            radioButtonList.add(langRadioButton)
            rgContainer.addView(langRadioButton)
        }

        if(!isLanguageCheckBoxChecked){
            //defaultRadioButton.isChecked = true
        }

        return layout
    }

    companion object{
        const val TAG: String = "ChooseLanguageDialogTag"
    }
}