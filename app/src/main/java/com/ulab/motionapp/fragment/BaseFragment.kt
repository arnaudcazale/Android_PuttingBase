package com.ulab.motionapp.fragment

import android.os.Bundle
import android.os.SystemClock
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.ulab.motionapp.R
import com.ulab.motionapp.activity.HomeActivity
import com.ulab.motionapp.common.Utils
import kotlinx.android.synthetic.main.header_back_with_setting.*

/**
 * Created by R.S. on 04/10/18
 */
abstract class BaseFragment : Fragment(), View.OnClickListener {

    protected val TAG: String = this.javaClass.simpleName
    private var lastClickedTime: Long = 0


    /**
     * Initialize the components for Fragment's view
     *
     * @param view A View inflated into Fragment
     */
    protected abstract fun initializeComponent(view: View) //to initialize the fragments components

    /**
     * Returns the resource id of the layout which will be used for setContentView() for the Activity
     *
     * @return resource id of the xml layout
     */
    protected abstract fun defineLayoutResource(): Int

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        return inflater.inflate(defineLayoutResource(), container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (header_ivBack != null) {
            header_ivBack.setOnClickListener(this)
        }

        initializeComponent(view)
    }

    override fun onClick(view: View) {
        Utils.hideSoftKeyBoard(activity, view)
        /*
         * Logic to Prevent the Launch of the Fragment Twice if User makes
         * the Tap(Click) very Fast.
         */
        if (SystemClock.elapsedRealtime() - lastClickedTime < Utils.MAX_CLICK_INTERVAL) {

            return
        }
        lastClickedTime = SystemClock.elapsedRealtime()

        when (view.id) {
            R.id.header_ivBack -> if (fragmentManager != null) {
                activity!!.onBackPressed()
            }
        }
    }

    /**
     * Adds the Fragment into layout container.
     *
     * @param container               Resource id of the layout in which Fragment will be added
     * @param currentFragment         Current loaded Fragment to be hide
     * @param nextFragment            New Fragment to be loaded into container
     * @param requiredAnimation       true if screen transition animation is required
     * @param commitAllowingStateLoss true if commitAllowingStateLoss is needed
     * @return true if new Fragment added successfully into container, false otherwise
     * @throws ClassCastException    Throws exception if getActivity() is not an instance of HomeActivity
     * @throws IllegalStateException Exception if Fragment transaction is invalid
     */
    @Throws(ClassCastException::class, IllegalStateException::class)
    protected fun addFragmentWithSharedElement(container: Int, currentFragment: Fragment, nextFragment: Fragment, requiredAnimation: Boolean, commitAllowingStateLoss: Boolean, iv: ImageView): Boolean {
        return if (activity != null) {
            if (activity is HomeActivity) {
                (activity as HomeActivity).addFragmentWithSharedElement(container, currentFragment, nextFragment, requiredAnimation, commitAllowingStateLoss, iv)
            } else {
                throw ClassCastException(HomeActivity::class.java.name + " can not be cast into " + activity!!::class.java.simpleName)
            }
        } else false
    }

    /**
     * Adds the Fragment into layout container.
     *
     * @param container               Resource id of the layout in which Fragment will be added
     * @param currentFragment         Current loaded Fragment to be hide
     * @param nextFragment            New Fragment to be loaded into container
     * @param requiredAnimation       true if screen transition animation is required
     * @param commitAllowingStateLoss true if commitAllowingStateLoss is needed
     * @return true if new Fragment added successfully into container, false otherwise
     * @throws ClassCastException    Throws exception if getActivity() is not an instance of HomeActivity
     * @throws IllegalStateException Exception if Fragment transaction is invalid
     */
    @Throws(ClassCastException::class, IllegalStateException::class)
    protected fun addFragment(container: Int, currentFragment: Fragment, nextFragment: Fragment, requiredAnimation: Boolean, commitAllowingStateLoss: Boolean): Boolean {
        return if (activity != null) {
            if (activity is HomeActivity) {
                (activity as HomeActivity).addFragment(container, currentFragment, nextFragment, requiredAnimation, commitAllowingStateLoss)
            } else {
                throw ClassCastException(HomeActivity::class.java.name + " can not be cast into " + activity!!::class.java.simpleName)
            }
        } else false
    }

    /**
     * Replaces the Fragment into layout container.
     *
     * @param container               Resource id of the layout in which Fragment will be added
     * @param nextFragment            New Fragment to be loaded into container
     * @param requiredAnimation       true if screen transition animation is required
     * @param commitAllowingStateLoss true if commitAllowingStateLoss is needed
     * @return true if new Fragment added successfully into container, false otherwise
     * @throws ClassCastException    Throws exception if getActivity() is not an instance of HomeActivity
     * @throws IllegalStateException Exception if Fragment transaction is invalid
     */
    @Throws(ClassCastException::class, IllegalStateException::class)
    protected fun replaceFragment(container: Int, nextFragment: Fragment, requiredAnimation: Boolean, commitAllowingStateLoss: Boolean): Boolean {
        return if (activity != null) {
            if (activity is HomeActivity) {
                (activity as HomeActivity).replaceFragment(container, nextFragment, requiredAnimation, commitAllowingStateLoss)
            } else {
                throw ClassCastException(HomeActivity::class.java.name + " can not be cast into " + activity!!::class.java.simpleName)
            }
        } else false
    }

    /**
     * Replaces the Fragment into layout container.
     *
     * @param container               Resource id of the layout in which Fragment will be added
     * @param fragmentManager         Activity fragment manager
     * @param nextFragment            New Fragment to be loaded into container
     * @param requiredAnimation       true if screen transition animation is required
     * @param commitAllowingStateLoss true if commitAllowingStateLoss is needed
     * @return true if new Fragment added successfully into container, false otherwise
     * @throws ClassCastException    Throws exception if getActivity() is not an instance of HomeActivity
     * @throws IllegalStateException Exception if Fragment transaction is invalid
     */
    @Throws(ClassCastException::class, IllegalStateException::class)
    protected fun replaceChildFragment(container: Int, fragmentManager: FragmentManager, nextFragment: Fragment, requiredAnimation: Boolean, commitAllowingStateLoss: Boolean): Boolean {
        return if (activity != null) {
            if (activity is HomeActivity) {
                (activity as HomeActivity).replaceChildFragment(container, fragmentManager, nextFragment, requiredAnimation, commitAllowingStateLoss)
            } else {
                throw ClassCastException(HomeActivity::class.java.name + " can not be cast into " + activity!!::class.java.simpleName)
            }
        } else false
    }
}