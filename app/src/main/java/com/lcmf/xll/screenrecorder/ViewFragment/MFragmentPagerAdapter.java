package com.lcmf.xll.screenrecorder.ViewFragment;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.ViewGroup;

import java.util.ArrayList;

/**
 * Created by Administrator on 2017/7/16 0016.
 */

public class MFragmentPagerAdapter extends FragmentPagerAdapter {
	//存放Fragment的数组
	private ArrayList<Fragment> fragmentsList;

	public MFragmentPagerAdapter(FragmentManager fm, ArrayList<Fragment> fragmentsList) {
		super(fm);
		this.fragmentsList = fragmentsList;
	}

	@Override
	public Fragment getItem(int position) {

		return fragmentsList.get(position);
	}

	@Override
	public int getCount() {
		return fragmentsList.size();
	}

	@Override
	public void destroyItem(ViewGroup container, int position, Object object) {
		super.destroyItem(container, position, object);
	}
}
