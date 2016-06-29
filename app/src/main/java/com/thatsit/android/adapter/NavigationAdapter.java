package com.thatsit.android.adapter;

public class NavigationAdapter {
    String title;

    int icon;

    /**
     * @param title
     * @param icon
     */
    public NavigationAdapter(String title, int icon) {
        super();
        this.title = title;
        this.icon = icon;
    }

    /**
     * @return the title
     */
    public String getTitle() {
        return title;
    }

    /**
     * @param title
     *            the title to set
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * @return the icon
     */
    public int getIcon() {
        return icon;
    }

    /**
     * @param icon
     *            the icon to set
     */
    public void setIcon(int icon) {
        this.icon = icon;
    }
}
