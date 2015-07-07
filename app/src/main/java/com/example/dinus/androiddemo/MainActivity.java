package com.example.dinus.androiddemo;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Toast;

import com.example.dinus.androiddemo.contextmenu.ContextMenuFragment;
import com.example.dinus.androiddemo.contextmenu.MenuAdapter;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements MenuAdapter.OnMenuItemClickListener, SurfaceHolder.Callback{

    private FragmentManager fm;
    private ContextMenuFragment dialogFragment;
    private SurfaceView surfaceView ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        fm = getSupportFragmentManager();
        dialogFragment = ContextMenuFragment.newInstance(getMenuObjects());
        surfaceView = (SurfaceView) findViewById(R.id.surface);
        surfaceView.getHolder().addCallback(this);
    }

    private ArrayList<Integer> getMenuObjects() {
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
        super.onCreateOptionsMenu(menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.more_item:
                dialogFragment.setOuterMoreItem(findViewById(R.id.more_item));
                dialogFragment.setInnerMoreItemIconId(R.drawable.btn_add);
                if (fm.findFragmentByTag(ContextMenuFragment.TAG) == null) {
                    dialogFragment.show(fm, ContextMenuFragment.TAG);
                }
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onMenuItemClick(View clickedView, int position) {
        Toast.makeText(this, position + "position", Toast.LENGTH_LONG).show();
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Toast.makeText(this, "surfaceCreated", Toast.LENGTH_LONG).show();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Toast.makeText(this, "surfaceDestoryed", Toast.LENGTH_LONG).show();
    }
}
