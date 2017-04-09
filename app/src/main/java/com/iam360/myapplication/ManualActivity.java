package com.iam360.myapplication;

import android.support.design.widget.TabLayout;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.iam360.views.manual.ManualFragmentPageAdapter;
import com.iam360.views.manual.ManualPageFragment;

public class ManualActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manual);
        ViewPager pager = (ViewPager) this.findViewById(R.id.photos_viewpager);
        PagerAdapter adapter = new ManualFragmentPageAdapter(getSupportFragmentManager());
        pager.setAdapter(adapter);

        TabLayout tabLayout = (TabLayout) this.findViewById(R.id.tab_layout);
        tabLayout.setupWithViewPager(pager);
    }
}
