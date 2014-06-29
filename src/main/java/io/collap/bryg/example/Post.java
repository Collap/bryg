package io.collap.bryg.example;

import java.util.ArrayList;
import java.util.List;

public class Post {

    private int id;
    private String title;
    private String content;
    private List<String> categories = new ArrayList<> ();

    public int getId () {
        return id;
    }

    public void setId (int id) {
        this.id = id;
    }

    public String getTitle () {
        return title;
    }

    public void setTitle (String title) {
        this.title = title;
    }

    public String getContent () {
        return content;
    }

    public void setContent (String content) {
        this.content = content;
    }

    public List<String> getCategories () {
        return categories;
    }

}
