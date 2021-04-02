package com.diamont.expense.tracker.settingsFragment.changePinDialogFragment

import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.diamont.expense.tracker.R
import com.diamont.expense.tracker.util.boolToVisibility
import com.diamont.expense.tracker.util.view.PinCodeInputView

class ChangeOrConfirmPinDialogFragment(
    private val sharedPreferences: SharedPreferences,
    private val isConfirmMode: Boolean = false,
    private val onSuccessCallback: () -> Unit = {}
): DialogFragment() {
    /**
     * Required variables
     */
    private lateinit var viewModel: ChangeOrConfirmPinDialogFragmentViewModel

    /**
     * The required views
     */
    private lateinit var ivBackArrow: ImageView
    private lateinit var tvSubtitle: TextView
    private lateinit var tvTitle: TextView
    private lateinit var tvError: TextView
    private lateinit var pinCodeInput: PinCodeInputView

    /**
     * onCreate()
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        /** Set full screen dialog style */
        setStyle(STYLE_NORMAL, R.style.Theme_ExpenseTracker_Dialog_FullScreen)
    }

    /**
     * onCreateView()
     */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        /**
         * Inflate layout and get the required views
         */
        val layout = inflater.inflate(R.layout.dialog_change_or_confirm_pin, container, false)
        ivBackArrow = layout.findViewById<ImageView>(R.id.ivSettingsArrowBack) as ImageView
        tvSubtitle = layout.findViewById<TextView>(R.id.tvChangePinSubtitle) as TextView
        tvTitle = layout.findViewById<TextView>(R.id.tvChangePinDialogTitle) as TextView
        tvError = layout.findViewById<TextView>(R.id.tvChangePinError) as TextView
        pinCodeInput = layout.findViewById<PinCodeInputView>(R.id.pcivChangePin) as PinCodeInputView

        /**
         *  Create the view model using a view model factory
         */
        val application = requireNotNull(this.activity).application
        val viewModelFactory = ChangeOrConfirmPinDialogFragmentViewModelFactory(application, sharedPreferences, isConfirmMode)

        viewModel = ViewModelProvider(this, viewModelFactory)
            .get(ChangeOrConfirmPinDialogFragmentViewModel::class.java)

        /**
         * Set onClickListener for back arrow
         */
        ivBackArrow.setOnClickListener {
            dismiss()
        }

        /**
         * Observe UI live data
         */
        viewModel.titleString.observe(viewLifecycleOwner, Observer {
            tvTitle.text = it
        })

        viewModel.subtitleString.observe(viewLifecycleOwner, Observer {
            tvSubtitle.text = it
        })

        viewModel.isErrorStringVisible.observe(viewLifecycleOwner, Observer {
            tvError.visibility = boolToVisibility(it, false)
        })

        viewModel.errorString.observe(viewLifecycleOwner, Observer {
            tvError.text = it
        })

        /** Observe live data from PinCodeInputView */
        pinCodeInput.isInputComplete.observe(viewLifecycleOwner, Observer {
            if(it){
                /** If it changes to true we let the view model know */
                viewModel.onPinCodeInputComplete(pinCodeInput.pinCodeEntered)
                pinCodeInput.reset()
            }
        })

        /**
         * Observe if operation was successful
         */
        viewModel.isOperationSuccessful.observe(viewLifecycleOwner, Observer {
            /** If so, we call the callback and we dismiss dialog */
            if(it){
                onSuccessCallback()
                dismiss()
            }
        })

        return layout
    }

    /**
     * Store the tag here which is needed when showing the dialog
     */
    companion object{
        const val TAG: String = "ChangePinDialogFragmentTag"
    }
}