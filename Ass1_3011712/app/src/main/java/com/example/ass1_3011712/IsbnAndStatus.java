package com.example.ass1_3011712;

public class IsbnAndStatus {
    private String isbn, status, bookId;

    public IsbnAndStatus(String isbn, String status, String bookId){
        this.isbn = isbn;
        this.status = status;
        this.bookId = bookId;
    }

    public String getIsbn(){
        return isbn;
    }

    public String getStatus(){
        return status;
    }

    public String getBookId(){
        return bookId;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
