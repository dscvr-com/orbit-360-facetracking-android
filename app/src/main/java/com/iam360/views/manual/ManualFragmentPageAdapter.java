package com.iam360.views.manual;


import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * contaons the items
 * Created by Lotti on 4/8/2017.
 */

public class ManualFragmentPageAdapter extends FragmentPagerAdapter {
    private Map<Integer, ManualPageFragment> frags = new HashMap();
    public ManualFragmentPageAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        ManualPageFragment adpt = frags.get(position);
        if(adpt == null){
            adpt = ManualPageFragment.newInstance(position);
            frags.put(position,adpt);
        }
        return adpt;
    }

    @Override
    public int getCount() {
        return 0;
    }
}
