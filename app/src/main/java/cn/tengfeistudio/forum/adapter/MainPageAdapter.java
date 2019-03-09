package cn.tengfeistudio.forum.adapter;


import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;



import java.util.List;

import cn.tengfeistudio.forum.module.base.BaseFragment;

/**
 * 主页切换Adapter
 */

public class MainPageAdapter extends FragmentStatePagerAdapter {

    private List<BaseFragment> fragments;

    public MainPageAdapter(FragmentManager fm, List<BaseFragment> fragments) {
        super(fm);
        this.fragments = fragments;
    }

    @Override
    public Fragment getItem(int position) {
        return fragments.get(position);
    }

    @Override
    public int getCount() {
        return fragments.size();
    }
}
