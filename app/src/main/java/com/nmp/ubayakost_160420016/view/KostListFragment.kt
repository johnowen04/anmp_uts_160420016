package com.nmp.ubayakost_160420016.view

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.nmp.ubayakost_160420016.R
import com.nmp.ubayakost_160420016.view.adapter.KostAdapter
import com.nmp.ubayakost_160420016.viewmodel.KostViewModel
import com.nmp.ubayakost_160420016.viewmodel.UserViewModel
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.drawer_header.view.*
import kotlinx.android.synthetic.main.fragment_kost_list.*

class KostListFragment : Fragment() {
    private lateinit var userViewModel: UserViewModel
    private lateinit var kostViewModel: KostViewModel

    private lateinit var adapter: KostAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        activity?.let {
            it.onBackPressedDispatcher.addCallback(requireActivity(), object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    activity?.moveTaskToBack(true)
                }
            })

            it.drawerButton.visibility = View.VISIBLE
            it.bottomNav.visibility = View.VISIBLE
            it.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
        }

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_kost_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        userViewModel = ViewModelProvider(this)[UserViewModel::class.java]

        if (!userViewModel.isLogin()) {
            val action = KostListFragmentDirections.actionHomeToLogin()
            Navigation.findNavController(view).navigate(action)
        }

        val user = userViewModel.getUserFromSharedPref()

        activity?.navView?.getHeaderView(0).let {
            it?.txtNameHeader?.text = if (user.firstName.isEmpty()) "No Name" else "${user.firstName} ${user.lastName}"
            it?.txtUsernameHeader?.text = user.username

            it?.btnLogOutHeader?.setOnClickListener {
                userViewModel.logout()
                Log.d("Logout Button", "Clicked")
                Toast.makeText(requireContext(), "Logout button header", Toast.LENGTH_SHORT).show()
                val action = KostListFragmentDirections.actionHomeToLogin()
                Navigation.findNavController(view).navigate(action)
            }

            it?.imgAvatar?.setOnClickListener { v2 ->
                Log.d("Avatar", "Clicked")
                Toast.makeText(requireContext(), "Avatar clicked", Toast.LENGTH_SHORT).show()
                activity?.drawerLayout?.closeDrawer(GravityCompat.START)
                val action = KostListFragmentDirections.actionHomeToProfile("home")
                Navigation.findNavController(view).navigate(action)
            }
        }

        kostViewModel = ViewModelProvider(this)[KostViewModel::class.java]
        kostViewModel.fetch() { }

        adapter = KostAdapter(arrayListOf(), "home") {
            kostViewModel.favorite(it) { }
        }

        rvKostHome.layoutManager = LinearLayoutManager(context)
        rvKostHome.adapter = adapter

        swipeRefreshHome.setOnRefreshListener {
            kostViewModel.fetch() { swipeRefreshHome.isRefreshing = !it }
            observeViewModel()
        }

        imgBtnFavorite.setOnClickListener {
            val action = KostListFragmentDirections.actionHomeToFavorite()
            Navigation.findNavController(it).navigate(action)
        }

        observeViewModel()
    }

    @SuppressLint("SetTextI18n")
    private fun observeViewModel() {
        kostViewModel.kostLiveData.observe(viewLifecycleOwner) {
            adapter.updateKostList(it)
        }

        userViewModel.user.observe(viewLifecycleOwner) { user ->
            activity?.navView.let {
                it?.txtNameHeader?.text = if (user.firstName.isEmpty()) "No Name" else "${user.firstName} ${user.lastName}"
                it?.txtUsernameHeader?.text = user.username
            }
        }
    }
}