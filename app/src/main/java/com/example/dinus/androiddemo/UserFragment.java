package com.example.dinus.androiddemo;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class UserFragment extends Fragment {

    private List<Object> mListData = new ArrayList<>();
    private View hoverView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        for (int i = 0; i < 100; i++) {
            mListData.add("user" + i);
            mListData.add(R.mipmap.ic_launcher);
        }



        RecyclerView gridView = (RecyclerView) inflater.inflate(R.layout.fragment_list, null);
        LinearLayoutManager linearManager = new LinearLayoutManager(getActivity());
        linearManager.setOrientation(LinearLayoutManager.VERTICAL);
        gridView.setLayoutManager(linearManager);
        gridView.setAdapter(new UserAdapter());
        return gridView;
    }

   private class UserAdapter extends RecyclerViewAdapter{
       {
           addViewType(Integer.class, new ViewHolderFactory<ViewHolder>() {
               @Override
               public ViewHolder onCreateViewHolder(ViewGroup parent) {
                   return new IntegerHolder(parent);
               }
           });

           addViewType(String.class, new ViewHolderFactory<ViewHolder>() {
               @Override
               public ViewHolder onCreateViewHolder(ViewGroup parent) {
                   return new StringHolder(parent);
               }
           });

       }

       @Override
       public Object getItem(int position) {
           return mListData.get(position);
       }

       @Override
       public int getItemCount() {
           return mListData.size();
       }
   }

    private class IntegerHolder extends RecyclerViewAdapter.ViewHolder<Integer>{

        private ImageView imageView ;

        public IntegerHolder(ViewGroup parent){
            this(LayoutInflater.from(getActivity()).inflate(R.layout.item_recycler_view_1, parent, false));
        }

        public IntegerHolder(View itemView) {
            super(itemView);
            imageView = (ImageView) itemView.findViewById(R.id.imageView);
        }

        @Override
        public void bind(Integer item, int position) {
            imageView.setImageResource(item);
        }
    }
    private class StringHolder extends RecyclerViewAdapter.ViewHolder<String>{

        private TextView textView;

        public StringHolder(ViewGroup parent){
            this(LayoutInflater.from(getActivity()).inflate(R.layout.item_recycler_view, parent, false));
        }

        public StringHolder(View itemView) {
            super(itemView);
            textView = (TextView) itemView.findViewById(R.id.textView);
        }

        @Override
        public void bind(String item, int position) {
            textView.setText(item);
        }
    }
}
