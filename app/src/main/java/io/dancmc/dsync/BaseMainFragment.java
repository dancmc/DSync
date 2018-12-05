package io.dancmc.dsync;

import java.util.ArrayList;


import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

public class BaseMainFragment extends Fragment {

    ClickListeners clickListeners = new ClickListeners() {


    };

    private void changeFragment(BaseSubFragment fragment) {
        FragmentManager manager = getChildFragmentManager();
        FragmentTransaction tx = manager.beginTransaction();
        fragment.setClickListeners(clickListeners);
        tx.replace(R.id.fragment_overall_container, fragment, null);
        tx.addToBackStack(null);
        tx.commit();
    }

    public void clearBackStack(){
        int num = getChildFragmentManager().getBackStackEntryCount();
        if(num>0){

            getChildFragmentManager().popBackStack(getChildFragmentManager().getBackStackEntryAt(0).getId(), FragmentManager.POP_BACK_STACK_INCLUSIVE);
        }

    }

    // return true if did custom handle otherwise false
    public boolean handleBackPress(){
        FragmentManager childFm = getChildFragmentManager();
        if (childFm.getBackStackEntryCount() > 0) {
            beforeFragmentPopped();
            childFm.popBackStackImmediate();
            afterFragmentPopped();
            return true;
        }
        return false;
    }

    void beforeFragmentPopped(){

    }

    void afterFragmentPopped(){

    }

    public interface ClickListeners {


    }
}
