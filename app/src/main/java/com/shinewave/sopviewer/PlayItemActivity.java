package com.shinewave.sopviewer;

/**
 * Created by user on 2015/11/24.
 */

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.app.Activity;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

public class PlayItemActivity extends Activity {


    private PlayItemAdapter adapter;
    private static Context ctext;
    public static final int RESULT_CODE_CANCEL = 0;
    public static final int RESULT_CODE_SAVE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ctext = PlayItemActivity.this;
        setContentView(R.layout.edit_play_item);

        setupListViewAdapter();

        setupAddButton();

        setupCancelButton();

        setupSaveButton();
    }

    public void removeClickHandler(View v) {
        PlayListItem itemToRemove = (PlayListItem) v.getTag();
        adapter.remove(itemToRemove);
    }

    private void setupListViewAdapter() {
        List<PlayListItem> tt = new ArrayList<>();
        tt.add(new PlayListItem(1, "HI", "HO", null, 3));
        tt.add(new PlayListItem(12, "HI2", "HO2", null, 32));
        adapter = new PlayItemAdapter(PlayItemActivity.this, R.layout.play_item_info_view, tt);
        ListView atomPaysListView = (ListView) findViewById(R.id.lst_play_item);
        atomPaysListView.setAdapter(adapter);
    }

    private void setupAddButton() {
        findViewById(R.id.btn_add).setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                adapter.add(new PlayListItem(0, "", "", null, 1));
            }
        });
    }

    private void setupCancelButton() {
        findViewById(R.id.btn_cancel).setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                String message = "Cancel";
                Intent intent = new Intent();
                intent.putExtra("MESSAGE", message);
                setResult(RESULT_CODE_CANCEL, intent);
                finish();
            }
        });
    }

    private void setupSaveButton() {
        findViewById(R.id.btn_save).setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                String message = "Save";
                Intent intent = new Intent();
                intent.putExtra("MESSAGE", message);
                setResult(RESULT_CODE_SAVE, intent);
                finish();
            }
        });
    }

    public static void show() {
        Toast.makeText(ctext, "File", Toast.LENGTH_SHORT).show();
    }
}
