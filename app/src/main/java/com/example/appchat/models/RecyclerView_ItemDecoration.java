package com.example.appchat.models;

import android.graphics.Rect;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class RecyclerView_ItemDecoration extends RecyclerView.ItemDecoration {
    int space;

    @Override
    public void getItemOffsets(@NonNull Rect outRect, int itemPosition, @NonNull RecyclerView parent) {
        super.getItemOffsets(outRect, itemPosition, parent);

        outRect.bottom = space;
    }

    public RecyclerView_ItemDecoration(int space) {
        this.space = space;
    }
}
