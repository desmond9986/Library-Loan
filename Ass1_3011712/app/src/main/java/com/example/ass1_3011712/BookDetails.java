/*
Student Number  : 30117112
Student Name    : Yi How Tan
 */

package com.example.ass1_3011712;

import java.util.ArrayList;

public class BookDetails {
    private String bookID, title, author, genre;
    private ArrayList<IsbnAndStatus> isbnS;

    public BookDetails(String bookID, String title,String author,String genre){
        this.bookID = bookID;
        this.title = title;
        this.author = author;
        this.genre = genre;
        isbnS = new ArrayList<IsbnAndStatus>();
    }

    public void addStock(IsbnAndStatus isbn){
        this.isbnS.add(isbn);
    }

    public String getAuthor() {
        return author;
    }

    public String getBookID(){
        return bookID;
    }

    public String getTitle(){
        return title;
    }

    public String getGenre() {
        return genre;
    }

    public  ArrayList<IsbnAndStatus> getIsbnS(){
        return isbnS;
    }

    public int getCheckedIn(){
        // get the number of books that available for loan
        int count = 0;
        for(int i = 0; i < isbnS.size(); i++){
            if(isbnS.get(i).getStatus().equals("checked in")){count++;}
        }
        return count;
    }

    public IsbnAndStatus checkOut(){
        // since this method will only be called when the book only have 1 isbn
        // so this method will only check the frist isbn in isbns list
        // check for the available book and loan
        // return actual IsbnAndStatus instance if successfully loan
        // return null if failed to loan
        if(isbnS.get(0).getStatus().equals("in cart")){
            isbnS.get(0).setStatus("checked out");
           return isbnS.get(0);
        }
        return null;
    }

    public IsbnAndStatus removeFromCart(){
        // since this method will only be called when the book only have 1 isbn
        // so this method will only check the frist isbn in isbns list
        // check for the available book and loan
        // return actual IsbnAndStatus instance if successfully loan
        // return null if failed to loan
        if(isbnS.get(0).getStatus().equals("in cart")){
            isbnS.get(0).setStatus("checked in");
            return isbnS.get(0);
        }
        return null;
    }

    public IsbnAndStatus checkIn(){
        // since this method will only be called when the book only have 1 isbn
        // so this method will only check the frist isbn in isbns list
        // check for the loaned book
        // return actual IsbnAndStatus instance if successfully return
        // return null if failed to return
        if(isbnS.get(0).getStatus().equals("checked out")){
            isbnS.get(0).setStatus("checked in");
            return isbnS.get(0);
        }
        return null;
    }

    public IsbnAndStatus addToCart(){
        // check for the available book and add to cart
        // return actual IsbnAndStatus instance if successfully add to cart
        // return null if failed to add to cart
        for(int i = 0; i < isbnS.size(); i++){
            if(isbnS.get(i).getStatus().equals("checked in")){
                isbnS.get(i).setStatus("in cart");
                return isbnS.get(i);
            }
        }
        return null;
    }

}
