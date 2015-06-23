package com.example.dinus.androiddemo;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public abstract class RecyclerViewAdapter<T, VH extends RecyclerViewAdapter.ViewHolder<T>> extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {

    private Map<Class<?>, Integer> mViewHolderTypeMappings = new HashMap<>();
    private Map<Integer, ViewHolderFactory> mViewHolderFactoryMappings = new HashMap<>();

    public <F> void addViewType(Class<? extends F> cls, ViewHolderFactory<? extends ViewHolder<? extends F>> factory) {
        int id = mViewHolderFactoryMappings.size();
        mViewHolderTypeMappings.put(cls, id);
        mViewHolderFactoryMappings.put(id, factory);
    }

    @Override
    public VH onCreateViewHolder(ViewGroup parent, int viewType) {
        if (mViewHolderFactoryMappings.size() > 0) {
            return (VH)mViewHolderFactoryMappings.get(viewType).onCreateViewHolder(parent);
        } else {
            return onCreateViewHolder(parent);
        }
    }

    public VH onCreateViewHolder(ViewGroup parent) {
        throw new RuntimeException("onCreateViewHolder(ViewGroup, int) is not implemented.");
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.bind(getItem(position), position);
    }

    @Override
    public int getItemViewType(int position) {
        if (mViewHolderTypeMappings.size() > 0) {
            Class<?> clazz = getItem(position).getClass();
            while (clazz != Object.class) {
                if (mViewHolderTypeMappings.containsKey(clazz)) {
                    return mViewHolderTypeMappings.get(clazz);
                }
            }
            throw new RuntimeException("Cannot resolve view type for (" + getItem(position) + ")");
        }
        return super.getItemViewType(position);
    }

    public abstract T getItem(int position);

    public static abstract class ViewHolder<T> extends RecyclerView.ViewHolder {

        public ViewHolder(View itemView) {
            super(itemView);
        }

        public abstract void bind(T item, int position);
    }

    public interface ViewHolderFactory<VH extends ViewHolder> {
        VH onCreateViewHolder(ViewGroup parent);
    }

}
