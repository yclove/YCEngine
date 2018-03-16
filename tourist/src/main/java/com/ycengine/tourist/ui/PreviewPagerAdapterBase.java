package com.ycengine.tourist.ui;

import com.ycengine.tourist.model.CodeItem;

import java.util.List;

/**
 * https://blog.asamaru.net/2015/11/03/how-to-wait-for-android-runonuithread-to-be-finished/
 */
public class PreviewPagerAdapterBase {

}
/*

public class PreviewPagerAdapterBase<T extends CodeItem> extends PagerAdapter<T> {
    private final Runnable notifyDataSetChangedRunnalbe = new Runnable() {
        @Override
        public void run() {
            notifyDataSetChanged();
            synchronized (this) {
                this.notify();
            }
        }
    };

    public boolean setItems(List<T> items) {
        super.setItems(items);

        synchronized (notifyDataSetChangedRunnalbe) {
            Helper.runOnUiThread(notifyDataSetChangedRunnalbe);
            try {
                notifyDataSetChangedRunnalbe.wait();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return hasFirstItem;
    }
}
*/
