package com.shinewave.sopviewer;

import android.app.Activity;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
//import android.app.Fragment;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.RadioButton;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link IFragmentInteraction} interface
 * to handle interaction events.
 * Use the {@link ViewerSettingsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ViewerSettingsFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private int mParam1;
    private String mParam2;

    private IFragmentInteraction mListener;

    private RadioButton rbFitScreen;
    private RadioButton rbFitWidth;
    private RadioButton rbFitHeight;
    private CheckBox cbVerticalAutoScroll;
    private CheckBox cbHorizontalAutoScroll;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ViewerSettingsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ViewerSettingsFragment newInstance(int param1, String param2) {
        ViewerSettingsFragment fragment = new ViewerSettingsFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    public ViewerSettingsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getInt(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_viewer_settings, container, false);
        rbFitScreen = (RadioButton) view.findViewById(R.id.rbFitScreen);
        rbFitWidth = (RadioButton) view.findViewById(R.id.rbFitWidth);
        rbFitHeight = (RadioButton) view.findViewById(R.id.rbFitHeight);
        cbVerticalAutoScroll = (CheckBox) view.findViewById(R.id.cbVerticalAutoScroll);
        cbHorizontalAutoScroll = (CheckBox) view.findViewById(R.id.cbHorizontalAutoScroll);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        int value = sharedPreferences.getInt("ViewerSetting", 1);
        if(value == 1)
        {
            rbFitScreen.setChecked(true);
            cbVerticalAutoScroll.setChecked(sharedPreferences.getBoolean("VerticalAutoScroll",false));
            cbHorizontalAutoScroll.setChecked(sharedPreferences.getBoolean("HorizontalAutoScroll",false));
        }
        else if(value == 2)
        {
            rbFitWidth.setChecked(true);
            cbVerticalAutoScroll.setChecked(false);
            cbHorizontalAutoScroll.setChecked(sharedPreferences.getBoolean("HorizontalAutoScroll",false));
        }
        else if(value == 3)
        {
            rbFitWidth.setChecked(true);
            cbVerticalAutoScroll.setChecked(true);
            cbHorizontalAutoScroll.setChecked(sharedPreferences.getBoolean("HorizontalAutoScroll",false));
        }
        else if(value == 4)
        {
            rbFitHeight.setChecked(true);
            cbHorizontalAutoScroll.setChecked(false);
            cbVerticalAutoScroll.setChecked(sharedPreferences.getBoolean("VerticalAutoScroll",false));
        }
        else if(value == 5)
        {
            rbFitHeight.setChecked(true);
            cbHorizontalAutoScroll.setChecked(true);
            cbVerticalAutoScroll.setChecked(sharedPreferences.getBoolean("VerticalAutoScroll",false));
        }

        rbFitScreen.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                checkSelected();
            }
        });

        rbFitWidth.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                checkSelected();
            }
        });

        rbFitHeight.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                checkSelected();
            }
        });

        cbVerticalAutoScroll.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                checkSelected();
            }
        });

        cbHorizontalAutoScroll.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                checkSelected();
            }
        });

        return view;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (IFragmentInteraction) activity;
            ((MainActivity) activity).onSectionAttached(
                    getArguments().getInt(ARG_PARAM1));
            ((MainActivity) activity).restoreActionBar();
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    private void checkSelected()
    {
        int value = 1;
        boolean fitScreen = rbFitScreen.isChecked();
        boolean fitWidth = rbFitWidth.isChecked();
        boolean fitHeight = rbFitHeight.isChecked();
        boolean verticalAutoScroll = cbVerticalAutoScroll.isChecked();
        boolean horizontalAutoScroll = cbHorizontalAutoScroll.isChecked();
        if(fitScreen)                               //FitScreen = true
            value = 1;
        else if(fitWidth && !verticalAutoScroll)    //FitWidth = true & VerticalAutoScroll = false
            value = 2;
        else if(fitWidth && verticalAutoScroll)     //FitWidth = true & VerticalAutoScroll = true
            value = 3;
        else if(fitHeight && !horizontalAutoScroll) //FitHeight = true & HorizontalAutoScroll = false
            value = 4;
        else if(fitHeight && horizontalAutoScroll)  //FitHeight = true & HorizontalAutoScroll = true
            value = 5;

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        SharedPreferences.Editor edit = sharedPreferences.edit();
        edit.putInt("ViewerSetting", value);
        edit.putBoolean("VerticalAutoScroll", verticalAutoScroll);
        edit.putBoolean("HorizontalAutoScroll", horizontalAutoScroll);
        edit.commit();
    }
}
