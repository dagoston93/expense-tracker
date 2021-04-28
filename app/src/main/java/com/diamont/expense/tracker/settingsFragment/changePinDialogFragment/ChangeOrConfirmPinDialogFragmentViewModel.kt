package com.diamont.expense.tracker.settingsFragment.changePinDialogFragment

import android.app.Application
import android.content.SharedPreferences
import android.content.res.Resources
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.diamont.expense.tracker.R
import com.diamont.expense.tracker.util.KEY_PREF_PIN_CODE

class ChangeOrConfirmPinDialogFragmentViewModel(
    private val resources: Resources,
    private val sharedPreferences: SharedPreferences,
    private val isConfirmMode: Boolean
) : ViewModel()  {
    /**
     * Setup some live data
     */
    private val _titleString = MutableLiveData<String>("")
    val titleString: LiveData<String>
        get() = _titleString

    private val _subtitleString = MutableLiveData<String>(resources.getString(R.string.enter_pin_code))
    val subtitleString: LiveData<String>
        get() = _subtitleString

    private val _errorString = MutableLiveData<String>("")
    val errorString: LiveData<String>
        get() = _errorString

    private val _isErrorStringVisible = MutableLiveData<Boolean>(false)
    val isErrorStringVisible: LiveData<Boolean>
        get() = _isErrorStringVisible

    private val _isOperationSuccessful = MutableLiveData<Boolean>(false)
    val isOperationSuccessful: LiveData<Boolean>
        get() = _isOperationSuccessful

    /**
     * Declare the required variables
     */
    private var pinCode: String = ""
    private var isCreateMode: Boolean = false
    private var isOldCodeEntered: Boolean = false
    private var firstPinEntered: String = ""

    /**
     * Constructor
     */
    init{
        /** Load current pin code */
        pinCode = sharedPreferences.getString(KEY_PREF_PIN_CODE, "") ?: ""

        /** If pin code not defined yet, we enter creation mode */
        if(pinCode == "") {
            isCreateMode = true
        }

        /** Set the title */
        if(isCreateMode){
            _titleString.value = resources.getString(R.string.create_pin_code)
        }else{
            if (isConfirmMode) {
                _titleString.value = resources.getString(R.string.confirm_pin_code)
            } else {
                _titleString.value = resources.getString(R.string.change_pin_code)
                _subtitleString.value = resources.getString(R.string.enter_old_pin)
            }
        }
    }

    /**
     * Call this method if input is complete
     */
    fun onPinCodeInputComplete(pinCodeEntered: String) {
        /**
         * Check whether we are in create, confirm or change mode
         */
        if(isCreateMode){
            /**
             * Create mode
             */
            if(firstPinEntered == ""){
                /**
                 * First entry
                 */
                firstPinEntered = pinCodeEntered
                _subtitleString.value = resources.getString(R.string.confirm_pin_code)
                _isErrorStringVisible.value = false
            }else{
                /**
                 * Second entry
                 */
                if(pinCodeEntered == firstPinEntered){
                    /**
                     * Codes match
                     */
                    _isErrorStringVisible.value = false

                    /**Save to shared prefs*/
                    with(sharedPreferences.edit()){
                        putString(KEY_PREF_PIN_CODE, pinCodeEntered)
                        apply()
                    }
                    _isOperationSuccessful.value = true
                }else{
                    /**
                     * Codes DON'T match
                     */
                    firstPinEntered = ""
                    _isErrorStringVisible.value = true
                    _errorString.value = resources.getString(R.string.pin_codes_do_not_match)
                    _subtitleString.value = resources.getString(R.string.enter_pin_code)
                }
            }
        }else{
            if(isConfirmMode){
                /**
                 * Confirm mode
                 */
                if(pinCodeEntered == pinCode){
                    /**
                     * Correct pin
                     */
                    _isErrorStringVisible.value = false
                    _isOperationSuccessful.value = true
                }else{
                    /**
                     * Incorrect pin
                     */
                    _errorString.value = resources.getString(R.string.invalid_pin)
                    _isErrorStringVisible.value = true
                }
            }else{
                /**
                 * Change mode
                 */
                if(isOldCodeEntered){
                    /**
                     * Waiting for new code
                     */
                    if(firstPinEntered == ""){
                        /**
                         * First entry
                         */
                        firstPinEntered = pinCodeEntered
                        _subtitleString.value = resources.getString(R.string.confirm_new_pin)
                        _isErrorStringVisible.value = false
                    }else{
                        /**
                         * Second entry
                         */
                        if(pinCodeEntered == firstPinEntered){
                            /**
                             * Codes match
                             */
                            _isErrorStringVisible.value = false

                            /** Save to shared prefs */
                            with(sharedPreferences.edit()){
                                putString(KEY_PREF_PIN_CODE, pinCodeEntered)
                                apply()
                            }
                            _isOperationSuccessful.value = true
                        }else{
                            /**
                             * Codes DON'T match
                             */
                            firstPinEntered = ""
                            _isErrorStringVisible.value = true
                            _errorString.value = resources.getString(R.string.pin_codes_do_not_match)
                            _subtitleString.value = resources.getString(R.string.enter_new_pin)
                        }
                    }
                }else{
                    /**
                     * Confirming old code before changing pin
                     */
                    if(pinCodeEntered == pinCode){
                        /**
                         * Correct pin code
                         */
                        _isErrorStringVisible.value = false
                        isOldCodeEntered = true
                        _subtitleString.value = resources.getString(R.string.enter_new_pin)
                    }else{
                        /**
                         * Incorrect pin code
                         */
                        _errorString.value = resources.getString(R.string.invalid_pin)
                        _isErrorStringVisible.value = true
                    }
                }
            }
        }
    }
}