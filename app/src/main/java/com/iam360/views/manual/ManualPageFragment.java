package com.iam360.views.manual;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.iam360.myapplication.R;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ManualPageFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ManualPageFragment extends Fragment {
    private static final String Page_NR_ARG = "pageNr";
    private int pageNr;


    public ManualPageFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param nrOfPage Parameter 1.
     * @return A new instance of fragment ManualPageFragment.
     */
    public static ManualPageFragment newInstance(int nrOfPage) {
        ManualPageFragment fragment = new ManualPageFragment();
        Bundle args = new Bundle();
        args.putInt(Page_NR_ARG, nrOfPage);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            pageNr = getArguments().getInt(Page_NR_ARG);
        }
        ManualAdapter adapter = new ManualAdapter(getContext());
        switch(pageNr){
            case 1:
                createPageOne(adapter);
                break;
            case 2:
                createPageTwo(adapter);
                break;
            case 3:
                createPageThree(adapter);
        }
        ListView view = (ListView) getView().findViewById(R.id.manual_list);
        view.setAdapter(adapter);
    }

    private void createPageThree(ManualAdapter adapter) {
        adapter.addTextItem(getContext().getString(R.string.manual_Page_three_p1));

    }

    private void createPageTwo(ManualAdapter adapter) {
        adapter.addTextItem(getContext().getString(R.string.manual_Page_two_p1));

    }

    private void createPageOne(ManualAdapter adapter) {
        adapter.addTextItem(getContext().getString(R.string.manual_Page_one_p1));

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_manual_page, container, false);
    }


}
