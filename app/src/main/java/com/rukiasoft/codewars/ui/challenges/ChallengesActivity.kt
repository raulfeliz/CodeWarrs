package com.rukiasoft.codewars.ui.challenges

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.Menu
import android.view.MenuItem
import com.rukiasoft.codewars.R
import com.rukiasoft.codewars.databinding.ActivityChallengesBinding
import com.rukiasoft.codewars.databinding.GlideBindingComponent
import com.rukiasoft.codewars.persistence.relations.ChallengeWithAllInfo
import com.rukiasoft.codewars.ui.common.BaseActivity
import com.rukiasoft.codewars.utils.Constants
import com.rukiasoft.codewars.vo.Status
import kotlinx.android.synthetic.main.activity_challenges.*
import timber.log.Timber

class ChallengesActivity : BaseActivity() {

    private lateinit var viewModel: ChallengesViewModel
    private lateinit var mBinding: ActivityChallengesBinding
    private lateinit var mRecyclerView: RecyclerView

    private var menuItem: MenuItem? = null
    protected var isRefreshing = false

    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.navigation_completed -> {
                viewModel.setCompleted()
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_authored -> {
                viewModel.setAuthored()
                return@OnNavigationItemSelectedListener true
            }
        }
        false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(ChallengesViewModel::class.java)

        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_challenges, GlideBindingComponent())

        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)

        if (intent.hasExtra(Constants.USER_NAME).not()) {
            finish()
        }

        setUpRecycler()

        viewModel.setUserName(intent.getStringExtra(Constants.USER_NAME))

        viewModel.numberOfChallenges.observe(this, Observer {
            it?.let {
                when (it.status) {

                    Status.SUCCESS -> hideRefresh()
                    Status.ERROR -> hideRefresh()
                    Status.LOADING -> showRefresh()
                }
            }
        })

        viewModel.challenges.observe(this, Observer {
            it?.let {
                (mRecyclerView.adapter as ChallengeAuthoredAdapter).submitList(it)

            }
        })

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_challenges_screen, menu)
        menuItem = menu?.findItem(R.id.action_refresh)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId

        if (id == R.id.action_refresh) {

            //force refresh
            return true
        }
        return super.onOptionsItemSelected(item)
    }


    private fun setUpRecycler() {

        //get the recycler
        mRecyclerView = mBinding.listChallenges

        // use a linear
        // layout manager
        val mLayoutManager = LinearLayoutManager(this.applicationContext, LinearLayoutManager.VERTICAL, false)
        mRecyclerView.layoutManager = mLayoutManager

        //add a divider decorator
        val dividerItemDecoration = DividerItemDecoration(mRecyclerView.context,
                DividerItemDecoration.VERTICAL)
        mRecyclerView.addItemDecoration(dividerItemDecoration)


        //add the adapter
        val adapter = ChallengeAuthoredAdapter(object : ChallengeAuthoredAdapter.ChallengeClickCallback {
            override fun onClick(challenge: ChallengeWithAllInfo?) {
                challenge?.let {
                    Timber.d("")
                }
            }
        })

        mRecyclerView.adapter = adapter


    }

    private fun showRefresh() {

        if (isRefreshing.not()) {
            isRefreshing = true
            //set ProgressBar in the MenuItem
            menuItem?.setActionView(R.layout.menu_item_refresh)

        }
    }

    private fun hideRefresh() {
        isRefreshing = false
        menuItem?.actionView = null

    }
}
