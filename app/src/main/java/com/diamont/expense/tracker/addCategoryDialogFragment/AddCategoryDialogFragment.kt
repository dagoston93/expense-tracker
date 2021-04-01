package com.diamont.expense.tracker.addCategoryDialogFragment

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.diamont.expense.tracker.R
import com.diamont.expense.tracker.util.database.TransactionDatabase
import com.diamont.expense.tracker.util.view.ColorPicker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class AddCategoryDialogFragment(
    private val editCategoryId: Int? = null,
    private val categoryListChangeCallBack: () -> Unit = {}) : DialogFragment(){
    /** Declare required variables */
    private lateinit var viewModel :AddCategoryDialogFragmentViewModel

    /**
     * The required views
     */
    private lateinit var viewColorIndicator : View
    private lateinit var layout : View
    private lateinit var colorPicker: ColorPicker
    private lateinit var etCategoryName: TextInputEditText
    private lateinit var tilCategoryName: TextInputLayout
    private lateinit var dialog: AlertDialog
    private var addButton: Button? = null

    /**
     * onCreateDialog()
     */
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        /**
         *  Create the view model using a view model factory
         */
        val application = requireNotNull(this.activity).application
        val databaseDao = TransactionDatabase.getInstance(application).transactionDatabaseDao

        /**
         * Create the view model
         */
        val viewModelFactory = AddCategoryDialogFragmentViewModelFactory(application, databaseDao, editCategoryId, categoryListChangeCallBack)
        viewModel = ViewModelProvider(this, viewModelFactory)
            .get(AddCategoryDialogFragmentViewModel::class.java)

        /**
         * Inflate the layout
         */
        layout = View.inflate(requireContext(), R.layout.dialog_add_or_edit_category, null)

        /**
         * Get the required views
         */
        viewColorIndicator = layout.findViewById<View>(R.id.viewAddCategoryColorIndicator) as View
        colorPicker = layout.findViewById<ColorPicker>(R.id.cpAddCategoryColorPicker) as ColorPicker
        etCategoryName = layout.findViewById<TextInputEditText>(R.id.etDialogAddCategoryName) as TextInputEditText
        tilCategoryName = layout.findViewById<TextInputLayout>(R.id.tilDialogAddCategoryName) as TextInputLayout

        /**
         * We need to return the dialog we want to show()
         */
        dialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle(
                if(editCategoryId == null)
                {
                    resources.getString(R.string.add_category_dialog_title)
                }else{
                    resources.getString(R.string.edit_category_dialog_title)
                }
            )
            .setView(layout)
            .setNegativeButton(resources.getString(R.string.cancel)) { _, _ -> }
            .setPositiveButton(
                if(editCategoryId == null) {
                    resources.getString(R.string.add)
                }else{
                    resources.getString(R.string.save)
                }
            ) { _, _ ->
                onAddButtonClicked()
            }
            .create()

        /** Disable the add button in the beginning */
        dialog.setOnShowListener {
            addButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            addButton?.isEnabled = false

            /**
             *  Sometimes textChange happens first when edit mode so then button remains disabled
             *  In that case we cannot change color without changing text first
             *  So if in edit mode call we call validate here as well
             * */
            if(editCategoryId != null) {
                validateCategoryName()
            }
        }

        /**
         * Text changed listener for the edit text
         */
        etCategoryName.addTextChangedListener {
            validateCategoryName()
        }

        return dialog
    }

    /**
     * onCreateView()
     *
     * We can observe live data here because in onCreateDialog
     * the viewLifecycleOwner is still null
     */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        /**
         * Observe the selected color live data
         */
        colorPicker.selectedColor.observe(viewLifecycleOwner, Observer {
            viewColorIndicator.setBackgroundResource(it)
        })

        /**
         * Observe category name (for edit)
         */
        viewModel.currentCategoryName.observe(viewLifecycleOwner, Observer {
            if(it.isNotEmpty()){
                etCategoryName.setText(it)
            }
        })

        /**
         * Observe category color (for edit)
         */
        viewModel.currentCategoryColorId.observe(viewLifecycleOwner, Observer {
            if(it != -1){
                colorPicker.setSelectedColorByResourceId(it)
            }
        })

        return layout
    }

    /**
     * onCLickListener for the positive button
     */
    private fun onAddButtonClicked(){
        viewModel.onPositiveButtonClick(
            etCategoryName.text.toString(),
            colorPicker.selectedColor.value ?: android.R.color.black
        )
    }

    /**
     * Validate category name
     */
    private fun validateCategoryName(){
        val error = viewModel.validateCategoryName(etCategoryName.text.toString())

        if(error == null){
            addButton?.isEnabled = true
            tilCategoryName.isErrorEnabled = false
            tilCategoryName.error = ""
        }else{
            addButton?.isEnabled = false
            tilCategoryName.isErrorEnabled = true
            tilCategoryName.error = error
        }
    }

    /**
     * Store the tag here which is needed when showing the dialog
     */
    companion object{
        const val TAG: String = "AddCategoryDialogTag"
    }
}