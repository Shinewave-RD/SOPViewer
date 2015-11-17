package com.shinewave.sopviewer;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.RelativeLayout;

import com.artifex.mupdfdemo.MuPDFCore;
import com.artifex.mupdfdemo.MuPDFPageAdapter;
import com.artifex.mupdfdemo.MuPDFReaderView;
import com.artifex.mupdfdemo.MuPDFView;
import com.artifex.mupdfdemo.OutlineActivityData;

public class PDFPlayActivity extends AppCompatActivity {

    private MuPDFCore        core;
    private String           mFileName;
    private MuPDFReaderView  mDocView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mFileName = getIntent().getStringExtra("FileName");

        core = openFile(mFileName);

        if(core != null)
        {
            mDocView = new MuPDFReaderView(this) {
                @Override
                protected void onMoveToChild(int i) {
                    //mPageNumberView.setText(String.format("%d / %d", i + 1,
                    //        core.countPages()));
                    //mPageSlider.setMax((core.countPages() - 1) * mPageSliderRes);
                    //mPageSlider.setProgress(i * mPageSliderRes);
                    super.onMoveToChild(i);
                }

                @Override
                protected void onTapMainDocArea() {
                }

                @Override
                protected void onDocMotion() {
                }

            };

            mDocView.setAdapter(new MuPDFPageAdapter(this, null, core));
        }

        RelativeLayout layout = new RelativeLayout(this);
        layout.addView(mDocView);
        setContentView(layout);

        //setContentView(R.layout.activity_pdfplay);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_pdfplay, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private MuPDFCore openFile(String path)
    {
        int lastSlashPos = path.lastIndexOf('/');
        mFileName = new String(lastSlashPos == -1
                ? path
                : path.substring(lastSlashPos+1));
        System.out.println("Trying to open " + path);
        try
        {
            core = new MuPDFCore(this, path);
            // New file: drop the old outline data
            OutlineActivityData.set(null);
        }
        catch (Exception e)
        {
            System.out.println(e);
            return null;
        }
        catch (java.lang.OutOfMemoryError e)
        {
            //  out of memory is not an Exception, so we catch it separately.
            System.out.println(e);
            return null;
        }
        return core;
    }
}
