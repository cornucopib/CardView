package com.cornucopia.welinktemplateoftab.widget.tab.adapter;

import android.view.View;
import android.view.ViewGroup;

import androidx.core.util.Pools;

import com.cornucopia.welinktemplateoftab.R;

import java.util.ArrayList;
import java.util.List;

/**
 * 列表型数据适配器，带有缓存功能。
 *
 * @author l84116371
 * @version 1.0
 * @since 2019/7/9
 */
public abstract class CacheListAdapter<T,V extends View> {

    private Pools.Pool<V> mCachePool;
    private List<T> mItemData = new ArrayList<>();
    private List<V> mViews = new ArrayList<>();
    private ViewGroup mParentView;

    public CacheListAdapter(ViewGroup parentView) {
        mParentView = parentView;
    }

    public void detach(int count) {
        int childCount = mViews.size();
        while (childCount > 0 && count > 0) {
            V view = mViews.remove(childCount - 1);
            if (mCachePool == null) {
                mCachePool = new Pools.SimplePool<>(12);
            }

            Object notCacheTag = view.getTag(R.id.welink_uibundle_tcl_can_not_cache_tag);
            if (notCacheTag == null || !(boolean) notCacheTag) {
                try {
                    mCachePool.release(view);
                } catch (Exception ignored) {
                }
            }

            mParentView.removeView(view);
            childCount--;
            count--;
        }
    }

    public void clear() {
        mItemData.clear();
        detach(mViews.size());
    }

    private V getView() {
        V v = mCachePool != null ? mCachePool.acquire() : null;
        if (v == null) {
            v = createView(mParentView);
        }
        return v;
    }

    protected abstract V createView(ViewGroup parentView);

    public CacheListAdapter<T, V> addItem(T item) {
        mItemData.add(item);
        return this;
    }

    public void setup() {
        int itemCount = mItemData.size();
        int childCount = mViews.size();
        int i;

        if (childCount > itemCount) {
            detach(childCount - itemCount);
        } else if (childCount < itemCount) {
            for (i = 0; i < itemCount - childCount; i++) {
                V view = getView();
                mParentView.addView(view);
                mViews.add(view);
            }
        }

        for (i = 0; i < itemCount; i++) {
            V view = mViews.get(i);
            T item = mItemData.get(i);
            bind(item, view, i);
        }
        mParentView.invalidate();
        mParentView.requestLayout();

    }

    public T getItem(int position) {
        if (mItemData == null) {
            return null;
        }
        if (position < 0 || position >= mItemData.size()) {
            return null;
        }
        return mItemData.get(position);
    }

    public void replaceItem(int position, T data) throws IllegalAccessException {
        if (position < mItemData.size() && position >= 0) {
            mItemData.set(position, data);
        } else {
            throw new IllegalAccessException("替换数据不存在");
        }

    }

    protected abstract void bind(T item, V view, int position);

    public List<V> getViews() {
        return mViews;
    }

    public int getSize() {
        if (mItemData == null) {
            return 0;
        }
        return mItemData.size();
    }

}
