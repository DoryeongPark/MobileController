package com.wonikrobotics.views;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Felix on 2016-08-03.
 */
public class PageViewAdapter extends PagerAdapter {

    private ArrayList<View> views;

    public PageViewAdapter(ArrayList<View> views){
        this.views = views;
    }

    @Override
    public int getCount(){
        return views.size();
    }

    @Override
    public Object instantiateItem(View container, int position) {
        View currentView = views.get(position);
        ((ViewPager)container).addView(currentView);
        return currentView;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        ((ViewPager)container).removeView((View)object);
    }
    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

}
