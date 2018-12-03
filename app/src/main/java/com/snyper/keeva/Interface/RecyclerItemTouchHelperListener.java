package com.snyper.keeva.Interface;

import android.support.v7.widget.RecyclerView;

/**
 * Created by stephen snyper on 11/9/2018.
 */

public interface RecyclerItemTouchHelperListener {
    void onSwiped(RecyclerView.ViewHolder viewHolder, int direction, int position);
}
