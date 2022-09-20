package com.smart.planner.POJOs;

import com.google.firebase.firestore.Exclude;

public class List {
    private String listName ;
    private String listColor ;
    private String documentId;

    public List(){}

    public List(String name,String color) {
        this.listName = name;
        this.listColor = color ;
    }

    @Exclude
    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    public String getListName() {
        return listName;
    }

    public String getListColor() {
        return listColor;
    }
}
