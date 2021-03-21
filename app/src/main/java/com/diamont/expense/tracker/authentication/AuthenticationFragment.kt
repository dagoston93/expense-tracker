package com.diamont.expense.tracker.authentication

import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import com.diamont.expense.tracker.R
import com.diamont.expense.tracker.databinding.FragmentAuthenticationBinding
import com.diamont.expense.tracker.initialSetup.InitialSetupFragmentDirections

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
        val viewModelFactory = AuthenticationFragmentViewModelFactory(application)

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
                activity?.findNavController(R.id.mainNavHostFragment)?.navigate(
                    AuthenticationFragmentDirections.actionAuthenticationFragmentToMainAppFragment()
                )
            }else{
                /** Otherwise we reset the pin code input view */
                binding.pcivAuthentication.reset()
            }
        })

        /** Return the inflated layout */
        return binding.root
    }

}