package com.diamont.expense.tracker.historyFragment.filterDialogFragment

import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.diamont.expense.tracker.R
import com.diamont.expense.tracker.util.database.TransactionCategory
import com.diamont.expense.tracker.util.enums.TransactionType
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class FilterDialogFragment(
    private val categories: List<TransactionCategory>,
    private val filterTransactionTypes: MutableList<TransactionType>,
    private val filterTransactionCategoryId: MutableList<Int>,
    private val positiveButtonClickedCallback: (
        filteredTransactionTypes: MutableList<TransactionType>,
        filteredCategoryIds: MutableList<Int>,
        isFilterApplied: Boolean
    ) -> Unit
): DialogFragment() {
    /**
     * The required views
     */
    private lateinit var layout : View
    private lateinit var dialog: AlertDialog

    private lateinit var llCategories: LinearLayout
    private lateinit var llTransactionTypes: LinearLayout
    private lateinit var cbTransactionTypesAll: CheckBox
    private lateinit var cbCategoriesAll: CheckBox
    private lateinit var filterButton: Button

    private var categoryCheckBoxList: MutableList<CheckBox> = mutableListOf<CheckBox>()
    private var transactionTypeCheckBoxList: MutableList<CheckBox> = mutableListOf<CheckBox>()

    private var transactionTypes: List<TransactionType> = listOf(
        TransactionType.EXPENSE,
        TransactionType.INCOME,
        TransactionType.WITHDRAW,
        TransactionType.DEPOSIT
    )

    /**
     * onCreateDialog()
     */
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        /**
         * Inflate the layout
         */
        layout = View.inflate(requireContext(), R.layout.dialog_filter, null)

        /**
         * We need to return the dialog we want to show()
         */
        dialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle(resources.getString(R.string.filters))
            .setView(layout)
            .setNegativeButton(resources.getString(R.string.cancel)) { _, _ -> }
            .setPositiveButton(resources.getString(R.string.filter)) { _, _ ->
                /** Get the checked transaction types */
                val filteredTypeList = mutableListOf<TransactionType>()
                for(i in transactionTypes.indices){
                    if(transactionTypeCheckBoxList[i].isChecked){
                        filteredTypeList.add(transactionTypes[i])
                    }
                }

                /** Get the check categories */
                val filteredCategoryList = mutableListOf<Int>()
                for(i in categories.indices){
                    if(categoryCheckBoxList[i].isChecked){
                        filteredCategoryList.add(categories[i].categoryId)
                    }
                }

                var isFilterApplied: Boolean = false
                if(filteredTypeList.size != transactionTypes.size || filteredCategoryList.size != categories.size){
                    isFilterApplied = true
                }

                positiveButtonClickedCallback(filteredTypeList, filteredCategoryList, isFilterApplied)
            }
            .create()

        dialog.setOnShowListener {
            filterButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
        }

        return dialog
    }

    /**
     * onCreateView()
     */
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        llCategories = layout.findViewById<LinearLayout>(R.id.llFilterDialogCategories) as LinearLayout
        llTransactionTypes = layout.findViewById<LinearLayout>(R.id.llFilterTransactionTypes) as LinearLayout
        cbTransactionTypesAll = layout.findViewById<CheckBox>(R.id.cbFilterTransactionTypesAll) as CheckBox
        cbCategoriesAll = layout.findViewById<CheckBox>(R.id.cbFilterCategoriesAll) as CheckBox


        /**
         * Add transaction type checkboxes
         */
        for(transactionType in transactionTypes){
            val checkBox = CheckBox(context)
            checkBox.text = resources.getString(transactionType.stringId)
            checkBox.setOnClickListener {
                checkUncheckAllCheckBox(cbTransactionTypesAll, transactionTypeCheckBoxList)
                validateSelection()
            }

            transactionTypeCheckBoxList.add(checkBox)
            llTransactionTypes.addView(checkBox)
        }

        /**
         * Add category checkboxes
         */
        for(category in categories){
            val checkBox = CheckBox(context)
            checkBox.text = category.categoryName
            checkBox.setOnClickListener {
                checkUncheckAllCheckBox(cbCategoriesAll, categoryCheckBoxList)
                validateSelection()
            }
            categoryCheckBoxList.add(checkBox)
            llCategories.addView(checkBox)
        }

        /**
         * Check the required transaction type checkboxes
         */
        for(i in transactionTypes.indices){
            transactionTypeCheckBoxList[i].isChecked = filterTransactionTypes.contains(transactionTypes[i])
        }

        /**
         * Check the required category checkboxes
         */
        for(i in categories.indices){
            categoryCheckBoxList[i].isChecked = filterTransactionCategoryId.contains(categories[i].categoryId)
        }

        /** Check the all checkboxes if needed */
        checkUncheckAllCheckBox(cbTransactionTypesAll, transactionTypeCheckBoxList)
        checkUncheckAllCheckBox(cbCategoriesAll, categoryCheckBoxList)

        /**
         * Add onClickListener to the All checkboxes
         */
        cbTransactionTypesAll.setOnClickListener {
            onAllCheckboxClicked(it as CheckBox, transactionTypeCheckBoxList)
        }

        cbCategoriesAll.setOnClickListener {
            onAllCheckboxClicked(it as CheckBox, categoryCheckBoxList)
        }

        return layout
    }

    /**
     * Call this method to check/uncheck the "All" checkbox if all items are checked/unchecked
     */
    private fun checkUncheckAllCheckBox(cbAll: CheckBox, listCb: List<CheckBox>){
        var isChecked = true
        for(checkBox in listCb){
            if(!checkBox.isChecked){
                isChecked = false
            }
        }

        cbAll.isChecked = isChecked
    }

    /**
     * Call this method to check/uncheck sub-checkboxes
     */
    private fun onAllCheckboxClicked(cbAll: CheckBox, listCb: List<CheckBox>){
        for(checkBox in listCb){
            checkBox.isChecked = cbAll.isChecked
        }
        validateSelection()
    }

    /**
     * Call this button to disable filter button
     * if no type or no category selected and enable otherwise
     */
    private fun validateSelection(){
        var isValid:Boolean = true
        var numChecked: Int = 0

        for(checkBox in transactionTypeCheckBoxList){
            if(checkBox.isChecked){
                numChecked++
            }
        }

        if(numChecked == 0){
            isValid = false
        }

        numChecked = 0

        for(checkBox in categoryCheckBoxList){
            if(checkBox.isChecked){
                numChecked++
            }
        }

        if(numChecked == 0){
            isValid = false
        }

        filterButton.isEnabled = isValid
    }

    companion object{
        const val TAG: String = "FilterDialogTag"
    }
}