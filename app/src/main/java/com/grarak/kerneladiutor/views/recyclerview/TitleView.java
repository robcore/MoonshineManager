/*
 * Copyright (C) 2015-2016 Willi Ye <williye97@gmail.com>
 *
 * This file is part of Kernel Adiutor.
 *
 * Kernel Adiutor is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Kernel Adiutor is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Kernel Adiutor.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.grarak.kerneladiutor.views.recyclerview;

import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.grarak.kerneladiutordonate.R;

/**
 * Created by willi on 30.04.16.
 */
public class TitleView extends RecyclerViewItem {

    private FrameLayout mRootView;
    private TextView mTitle;
    private View mMoreView;

    private View.OnClickListener mOnClickListener;
    private CharSequence mTitleText;

    @Override
    public int getLayoutRes() {
        return R.layout.rv_title_view;
    }

    @Override
    public void onCreateView(View view) {
        mRootView = (FrameLayout) view;
        mTitle = (TextView) view.findViewById(R.id.title);
        mMoreView = view.findViewById(R.id.more_view);

        setFullSpan(true);

        super.onCreateView(view);
    }

    public void setOnClickListener(View.OnClickListener onClickListener) {
        mOnClickListener = onClickListener;
        refresh();
    }

    public void setText(CharSequence text) {
        mTitleText = text;
        refresh();
    }

    @Override
    protected void refresh() {
        super.refresh();
        if (mRootView != null) {
            if (mOnClickListener != null) {
                mRootView.setClickable(true);
                mRootView.setOnClickListener(mOnClickListener);
                if (mMoreView != null) {
                    mMoreView.setVisibility(View.VISIBLE);
                }
            } else {
                mRootView.setClickable(false);
                if (mMoreView != null) {
                    mMoreView.setVisibility(View.GONE);
                }
            }
        }
        if (mTitle != null && mTitleText != null) {
            mTitle.setText(mTitleText);
        }
    }
}