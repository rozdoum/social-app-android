package com.rozdoum.socialcomponents.model;

import com.rozdoum.socialcomponents.util.FormatterUtil;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Kristina on 10/28/16.
 */

public class Post {

    private String title;
    private String description;
    private long createdDate;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public long getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(long createdDate) {
        this.createdDate = createdDate;
    }

    public Map<String,Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();

        result.put("title", title);
        result.put("description", description);
        result.put("createdDate", createdDate);
        result.put("createdDateText", FormatterUtil.getFirebaseDateFormat().format(new Date(createdDate)));

        return result;
    }
}
