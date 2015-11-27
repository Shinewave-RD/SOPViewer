package com.shinewave.sopviewer;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
//import android.app.Fragment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.shinewave.sopviewer.dummy.DummyContent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Large screen devices (such as tablets) are supported by replacing the ListView
 * with a GridView.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnFragmentInteractionListener}
 * interface.
 */
public class PlayListManagerFragment extends Fragment implements AbsListView.OnItemClickListener {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private int mParam1;
    private String mParam2;

    private IFragmentInteraction mListener;

    private static final String FV_NAME = "PlayListName";
    private static final String FV_PLAYLIST = "fileObject";
    private PlayList info;
    private ArrayList<HashMap<String, Object>> list_Play = new ArrayList<HashMap<String, Object>>();

    /**
     * The fragment's ListView/GridView.
     */
    private AbsListView mListView;

    /**
     * The Adapter which will be used to populate the ListView/GridView with
     * Views.
     */
    private SimpleAdapter mAdapter;

    // TODO: Rename and change types of parameters
    public static PlayListManagerFragment newInstance(int param1, String param2) {
        PlayListManagerFragment fragment = new PlayListManagerFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public PlayListManagerFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mParam1 = getArguments().getInt(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

        // TODO: Change Adapter to display your content
        //mAdapter = new ArrayAdapter<DummyContent.DummyItem>(getActivity(),
        //        android.R.layout.simple_list_item_1, android.R.id.text1, DummyContent.ITEMS);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_playlistmanager, container, false);

        getPlayList();

        mAdapter = new SimpleAdapter(
                getActivity(),
                list_Play,
                R.layout.play_list_info_view,
                new String[]{FV_NAME},
                new int[]{R.id.lblPlayListName}
        );

        // Set the adapter
        mListView = (AbsListView) view.findViewById(android.R.id.list);
        ((AdapterView<ListAdapter>) mListView).setAdapter(mAdapter);

        mListView.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);
        // Set OnItemClickListener so we can be notified on item clicks
        mListView.setOnItemClickListener(this);

        Button btnCreate = (Button) view.findViewById(R.id.createBtn);
        btnCreate.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                showPlayItemActivity(true);
            }
        });

        Button btnEdit = (Button) view.findViewById(R.id.editBtn);
        btnEdit.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                showPlayItemActivity(false);
            }
        });

        Button btnDelete = (Button) view.findViewById(R.id.deleteBtn);
        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle("Alert!!");
                builder.setMessage("Are you sure to delete record");
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        deletePlayList(info.playListName);
                        getPlayList();
                        mListView.clearChoices();
                        mAdapter.notifyDataSetChanged();
                    }
                });

                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {

                    }
                });

                builder.create().show();
            }
        });

        Button btnPlay = (Button) view.findViewById(R.id.playBtn);
        btnPlay.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                //TODO:Call PDFPlayActivity to play
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

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (null != mListener) {
            // Notify the active callbacks interface (the activity, if the
            // fragment is attached to one) that an item has been selected.
            mListener.onFragmentInteraction(DummyContent.ITEMS.get(position).id);
        }
    }

    /**
     * The default content for this Fragment has a TextView that is shown when
     * the list is empty. If you would like to change the text, call this method
     * to supply the text it should use.
     */
    public void setEmptyText(CharSequence emptyText) {
        View emptyView = mListView.getEmptyView();

        if (emptyView instanceof TextView) {
            ((TextView) emptyView).setText(emptyText);
        }
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(String id);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        String message = data.getStringExtra("MESSAGE");
        if (resultCode == PlayItemActivity.RESULT_CODE_SAVE) {
            Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
        } else if (resultCode == PlayItemActivity.RESULT_CODE_CANCEL) {
            Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
        }
    }


    private void showPlayItemActivity(boolean isCreate) {
        int requestCode = isCreate ? 1 : 2;
        Intent intent = new Intent(getActivity(),
                PlayItemActivity.class);

        startActivityForResult(intent, requestCode);
    }

    private void getPlayList() {
        list_Play.clear();
        List<PlayList> list = DBManager.getPlayList();
        try {
            for (int i = 0; i < list.size(); i++) {

                PlayList info = list.get(i);
                HashMap<String, Object> cItem = new HashMap<String, Object>();

                cItem.put(FV_NAME, info.playListName);
                cItem.put(FV_PLAYLIST, info);

                list_Play.add(cItem);
            }
        } catch (Exception e) {
        }
    }

    private boolean insertPlayList(PlayList pList) {
        return DBManager.insertPlayList(pList);
    }

    private boolean deletePlayList(String pListName) {
        return DBManager.deletePlayList(pListName);
    }

}