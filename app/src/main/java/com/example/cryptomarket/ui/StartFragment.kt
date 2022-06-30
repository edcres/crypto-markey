package com.example.cryptomarket.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.example.cryptomarket.R
import com.example.cryptomarket.databinding.FragmentStartBinding

private const val fragmentTAG = "StartFrag__TAG"

class StartFragment : Fragment() {

    private var binding: FragmentStartBinding? = null
    private val vm: CoinsViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val fragmentBinding = FragmentStartBinding.inflate(inflater, container, false)
        binding = fragmentBinding
        return fragmentBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding?.apply {
            lifecycleOwner = viewLifecycleOwner
        }
        vm.startApplication(requireNotNull(this.activity).application)
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }
}