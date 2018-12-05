package io.dancmc.dsync

import androidx.fragment.app.Fragment

open class BaseSubFragment: Fragment(){

    var clickListeners : BaseMainFragment.ClickListeners? = null

    open fun reload(){

    }

    open fun photosLoaded(imageDirectory: ArrayList<ImageDirectory>, photoList:ArrayList<PhotoObj>){

    }

}