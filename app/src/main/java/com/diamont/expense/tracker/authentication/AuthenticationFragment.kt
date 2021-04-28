package com.diamont.expense.tracker.authentication

import androidx.biometric.BiometricPrompt
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_WEAK
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import com.diamont.expense.tracker.R
import com.diamont.expense.tracker.databinding.FragmentAuthenticationBinding
import com.diamont.expense.tracker.util.LocaleUtil

class AuthenticationFragment : Fragment() {
    /** Data binding */
    private lateinit var binding : FragmentAuthenticationBinding
    private lateinit var viewModel: AuthenticationFragmentViewModel

    /**
     * onCreateView()
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        /** Inflate the layout for this fragment */
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_authentication, container, false)
        binding.lifecycleOwner = this

        /**
         *  Create the view model using a view model factory
         */
        val application = requireNotNull(this.activity).application
        val localisedContext = LocaleUtil.getLocalisedContext(application)
        val viewModelFactory = AuthenticationFragmentViewModelFactory(localisedContext)

        viewModel = ViewModelProvider(this, viewModelFactory)
            .get(AuthenticationFragmentViewModel::class.java)

        binding.viewModel = viewModel

        /** Observe live data from PinCodeInputView */
        binding.pcivAuthentication.isInputComplete.observe(viewLifecycleOwner, Observer {
            if(it){
                /** If it changes to true we let the view model know */
                viewModel.onPinCodeEntered(binding.pcivAuthentication.pinCodeEntered)
            }
        })

        /** Observe live data from the view model */
        viewModel.isAuthenticationSuccessful.observe(viewLifecycleOwner, Observer {
            if(it){
                /** If authentication successful we navigate */
                navigateToApp()
            }else{
                /** Otherwise we reset the pin code input view */
                binding.pcivAuthentication.reset()
            }
        })

        if(viewModel.showBiometricPrompt){
            showBiometricPrompt()
        }

        /** Return the inflated layout */
        return binding.root
    }

    /**
     * Call this method to show the biometric prompt
     */
    private fun showBiometricPrompt(){
        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(context?.resources?.getString(R.string.biometric_prompt_title) ?:"")
            .setSubtitle(context?.resources?.getString(R.string.biometric_prompt_subtitle) ?:"")
            .setNegativeButtonText(context?.resources?.getString(R.string.use_pin_code) ?:"")
            .setAllowedAuthenticators(BIOMETRIC_STRONG or BIOMETRIC_WEAK)
            .build()

        val executor = ContextCompat.getMainExecutor(activity)
        val biometricPrompt = BiometricPrompt(this, executor, object : BiometricPrompt.AuthenticationCallback(){
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                navigateToApp()
            }

        })

        biometricPrompt.authenticate(promptInfo)
    }

    /**
     * Call this method to navigate to app
     */
    private fun navigateToApp(){
        activity?.findNavController(R.id.mainNavHostFragment)?.navigate(
            AuthenticationFragmentDirections.actionAuthenticationFragmentToMainAppFragment()
        )
    }

}