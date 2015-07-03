package com.example.dinus.androiddemo;

import android.app.Fragment;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;

import com.example.dinus.androiddemo.contextmenu.ContextMenuFragment;

import java.util.ArrayList;


public class MainActivity extends ActionBarActivity {

    private FragmentManager fm;
    private ContextMenuFragment dialogFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        fm = getSupportFragmentManager();
        dialogFragment = ContextMenuFragment.newInstance(getMenuObjects());

        
    }


    private ArrayList<Integer> getMenuObjects(){
        ArrayList<Integer> menuObjects = new ArrayList<>();
        menuObjects.add(R.drawable.icn_1);
        menuObjects.add(R.drawable.icn_2);
        menuObjects.add(R.drawable.icn_3);
        menuObjects.add(R.drawable.icn_4);

        return menuObjects;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_more:
                if (fm.findFragmentByTag(ContextMenuFragment.TAG) == null){
                    dialogFragment.show(fm, ContextMenuFragment.TAG);
                }
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (dialogFragment != null && dialogFragment.isAdded()) {
            dialogFragment.dismiss();
        } else{
            finish();
        }
    }
}
