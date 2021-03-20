package com.diamont.expense.tracker.util.view

import android.content.Context
import android.content.res.ColorStateList
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.widget.ImageViewCompat
import androidx.core.widget.TextViewCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.diamont.expense.tracker.R
/** TODO: TAP ANIMATION!!! */
/**
 * PinCodeInputView
 *
 * This class is a custom view
 * that displays a numeric keyboard
 * and handles the user input of a PIN code.
 */
class PinCodeInputView(context: Context, attrs: AttributeSet) : LinearLayout(context, attrs) {

    /** Declare the required variables */
    private var inputLength : Int = 0
    private var delay : Long = 0
    private var isVibrationEnabled : Boolean = true
    private var _pinCodeEntered : String = ""
    val pinCodeEntered : String
        get() = _pinCodeEntered

    private var _inputStarsString : String = ""
    private val _isInputComplete = MutableLiveData<Boolean>(false)
    val isInputComplete : LiveData<Boolean>
        get() = _isInputComplete

    private val llNumberButtons : List<LinearLayout>
    private val llDelButton : LinearLayout
    private val tvButtonTexts : List<TextView>
    private val ivDelIcon : ImageView
    private val tvPinCodeInputStars : TextView
    private val tvPinLabel : TextView

    private var buttonTextAppearanceId : Int = 0
    private var pinTextAppearanceId : Int = 0
    private var starTextAppearanceId : Int = 0
    private var buttonColorVal : Int = 0

    private lateinit var vibrator : Vibrator

    /**
     * Constructor
     */
    init{
        /** Init vibrator */
        if(!isInEditMode){
            vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }

        /** Receive the attributes */
        context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.PinCodeInputView,
            R.attr.pinCodeInputStyle,
            R.style.Theme_ExpenseTracker_Widget_PinCodeInputView
        ).apply{
            try{
                inputLength = getInt(R.styleable.PinCodeInputView_pinCodeInputLength, DEF_INPUT_LENGTH)
                delay = getInt(R.styleable.PinCodeInputView_pinCodeInputDelayMillis, DEF_DELAY_MILLIS).toLong()
                isVibrationEnabled = getBoolean(R.styleable.PinCodeInputView_pinCodeInputVibrationEnabled, true)

                buttonTextAppearanceId = getResourceId(
                    R.styleable.PinCodeInputView_pinCodeInputButtonTextStyle,
                    R.style.TextAppearance_AppCompat_Large
                )

                pinTextAppearanceId = getResourceId(
                    R.styleable.PinCodeInputView_pinCodeInputPinTextStyle,
                    R.style.TextAppearance_AppCompat_Title
                )

                starTextAppearanceId = getResourceId(
                    R.styleable.PinCodeInputView_pinCodeInputStarsTextStyle,
                    R.style.TextAppearance_AppCompat_Title
                )

                buttonColorVal = getColor(R.styleable.PinCodeInputView_pinCodeInputButtonColor, 0x000000)
            } finally {
                recycle()
            }
        }

        /** Inflate the layout */
        val root : View = inflate(context, R.layout.view_pin_code_input, this)

        /** Get the required views */
        llNumberButtons = listOf(
            root.findViewById(R.id.llPinCodeInputView0) as LinearLayout,
            root.findViewById(R.id.llPinCodeInputView1) as LinearLayout,
            root.findViewById(R.id.llPinCodeInputView2) as LinearLayout,
            root.findViewById(R.id.llPinCodeInputView3) as LinearLayout,
            root.findViewById(R.id.llPinCodeInputView4) as LinearLayout,
            root.findViewById(R.id.llPinCodeInputView5) as LinearLayout,
            root.findViewById(R.id.llPinCodeInputView6) as LinearLayout,
            root.findViewById(R.id.llPinCodeInputView7) as LinearLayout,
            root.findViewById(R.id.llPinCodeInputView8) as LinearLayout,
            root.findViewById(R.id.llPinCodeInputView9) as LinearLayout
        )

        tvButtonTexts = listOf(
            root.findViewById(R.id.tvPinCodeInput0) as TextView,
            root.findViewById(R.id.tvPinCodeInput1) as TextView,
            root.findViewById(R.id.tvPinCodeInput2) as TextView,
            root.findViewById(R.id.tvPinCodeInput3) as TextView,
            root.findViewById(R.id.tvPinCodeInput4) as TextView,
            root.findViewById(R.id.tvPinCodeInput5) as TextView,
            root.findViewById(R.id.tvPinCodeInput6) as TextView,
            root.findViewById(R.id.tvPinCodeInput7) as TextView,
            root.findViewById(R.id.tvPinCodeInput8) as TextView,
            root.findViewById(R.id.tvPinCodeInput9) as TextView
        )

        ivDelIcon = root.findViewById(R.id.ivPinCodeInputViewDel) as ImageView
        tvPinCodeInputStars = root.findViewById(R.id.tvPinCodeInputStars) as TextView
        tvPinLabel = root.findViewById(R.id.tvPinCodeInputViewTitle) as TextView
        llDelButton = root.findViewById(R.id.llPinCodeInputViewDel) as LinearLayout

        /** Set text appearances and colors */
        for(i in tvButtonTexts.indices){
            TextViewCompat.setTextAppearance(tvButtonTexts[i], buttonTextAppearanceId)
            llNumberButtons[i].backgroundTintList = ColorStateList.valueOf(buttonColorVal)
        }

        TextViewCompat.setTextAppearance(tvPinCodeInputStars, starTextAppearanceId)
        TextViewCompat.setTextAppearance(tvPinLabel, pinTextAppearanceId)

        ImageViewCompat.setImageTintList(ivDelIcon, ColorStateList.valueOf(buttonColorVal))
        llDelButton.backgroundTintList = ColorStateList.valueOf(buttonColorVal)


        /** Add onClickListeners for the buttons */
        llNumberButtons[0].setOnClickListener{ addCharacter("0") }
        llNumberButtons[1].setOnClickListener{ addCharacter("1") }
        llNumberButtons[2].setOnClickListener{ addCharacter("2") }
        llNumberButtons[3].setOnClickListener{ addCharacter("3") }
        llNumberButtons[4].setOnClickListener{ addCharacter("4") }
        llNumberButtons[5].setOnClickListener{ addCharacter("5") }
        llNumberButtons[6].setOnClickListener{ addCharacter("6") }
        llNumberButtons[7].setOnClickListener{ addCharacter("7") }
        llNumberButtons[8].setOnClickListener{ addCharacter("8") }
        llNumberButtons[9].setOnClickListener{ addCharacter("9") }

        llDelButton.setOnClickListener { deleteLastCharacter() }
    }

    /** This method attaches a character to the entered PIN */
    private fun addCharacter(c : String){
        /** If max length reached we do nothing */
        if(inputLength == _pinCodeEntered.length) return

        /**
         *  Hide the previously entered digits.
         *  We need to do this because if the user enters the next digit before the
         *  last character is hidden, it remains visible
         */
        if(_pinCodeEntered.isNotEmpty()){
            _inputStarsString = ""
            for(i in _pinCodeEntered.indices){
                _inputStarsString += "*"
            }
        }

        /** Cancel runnable to restart time */
        tvPinCodeInputStars.removeCallbacks(hideLastCharRunnable)

        /** Append the character to the pin and show an extra star */
        _pinCodeEntered += c
        _inputStarsString += c
        tvPinCodeInputStars.text = _inputStarsString

        /** Show the last entered character, then hide it after a specified interval */
        tvPinCodeInputStars.postDelayed(
            hideLastCharRunnable,
            delay
        )

        /** If we reach max input length we change live data value */
        if(inputLength == _pinCodeEntered.length){
            _isInputComplete.value = true
        }

        vibrationEffect()
    }

    /** This runnable hides the last character of the PIN */
    private var hideLastCharRunnable = Runnable {
        _inputStarsString = removeLastChar(_inputStarsString)
        _inputStarsString += "*"
        tvPinCodeInputStars.text = _inputStarsString
    }

    /** This method deletes the last character of the entered PIN */
    private fun deleteLastCharacter(){
        if(_pinCodeEntered.isNotEmpty()){
            _pinCodeEntered = removeLastChar(_pinCodeEntered)
            _inputStarsString = removeLastChar(_inputStarsString)
            tvPinCodeInputStars.text = _inputStarsString

            /** Cancel the runnable otherwise if user deletes before char is hidden, a star appears after deleting char */
            tvPinCodeInputStars.removeCallbacks(hideLastCharRunnable)

            /** If we delete a char, we definitely don't have enough chars to continue */
            _isInputComplete.value = false

            vibrationEffect()
        }
    }

    /** This method removes the last char of a string */
    private fun removeLastChar(str : String ) : String{
        var s = ""
        if(str.isNotEmpty()){
            s = str.substring(0, str.length-1)
        }
        return s
    }

    /**
     *  This method vibrates the phone on key input
     */
    private fun vibrationEffect(){
        if(isVibrationEnabled){
            /** vibrate(Long) is deprecated in from API level 26 */
            if (Build.VERSION.SDK_INT >= 26) {
                vibrator.vibrate(VibrationEffect.createOneShot(VIBRATION_LENGTH, VibrationEffect.DEFAULT_AMPLITUDE))
            }else{
                vibrator.vibrate(VIBRATION_LENGTH)
            }
        }
    }

    /**
     * Call this method to reset the view
     */
    fun reset(){
        _inputStarsString = ""
        tvPinCodeInputStars.text = ""
        _pinCodeEntered = ""
        _isInputComplete.value = false

        /** Cancel runnable */
        tvPinCodeInputStars.removeCallbacks(hideLastCharRunnable)
    }

    companion object{
        const val DEF_INPUT_LENGTH : Int = 4
        const val DEF_DELAY_MILLIS : Int = 500
        const val VIBRATION_LENGTH : Long = 50
    }
}