package com.example.huhep.litepaltest;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.TabLayout;
import android.support.v7.view.menu.MenuBuilder;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;

public class CustomToolbar extends ConstraintLayout {
    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.toolbar_titleView)
    TextView titleTextView;

    @SuppressLint("RestrictedApi")
    public CustomToolbar(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        View view = LayoutInflater.from(context).inflate(R.layout.toolbar, this);
        ButterKnife.bind(this, view);
        toolbar.setOverflowIcon(getResources().getDrawable(R.drawable.ic_plus));
        ((MenuBuilder) toolbar.getMenu()).setOptionalIconsVisible(true);
    }

    public Toolbar getToolbar() {
        return toolbar;
    }

    public void setTitle(String title) {
        titleTextView.setText(title);
    }
}
