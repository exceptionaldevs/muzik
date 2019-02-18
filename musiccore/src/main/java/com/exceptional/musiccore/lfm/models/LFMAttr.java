package com.exceptional.musiccore.lfm.models;

public class LFMAttr {

    private String user;
    private String page;
    private String perPage;
    private String totalPages;
    private String total;

    public String getUser() {
        return user;
    }

    public int getPage() {
        return Integer.parseInt(page);
    }

    public int getPerPage() {
        return Integer.parseInt(perPage);
    }

    public int getTotalPages() {
        return Integer.parseInt(totalPages);
    }

    public int getTotal() {
        return Integer.parseInt(total);
    }

    public float getProgress() {
        return getPage() * 1.0f / getTotalPages();
    }
}
