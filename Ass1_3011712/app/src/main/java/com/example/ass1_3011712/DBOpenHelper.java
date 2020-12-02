package com.example.ass1_3011712;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.TextView;

public class DBOpenHelper extends SQLiteOpenHelper {
    // constructor for the class here we just map onto the constructor of the
    // super class
    public DBOpenHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }
    // overridden method that is called when the database is to be created
    public void onCreate(SQLiteDatabase db) {
    // create the database
        db.execSQL(create_table_book);
        db.execSQL(create_table_isbn);
    }
    // overridden method that is called when the database is to be upgraded
    // note in this example we simply reconstruct the database not caring for
    // data loss ideally you should have a method for storing the data while you
    // are reconstructing the database
    public void onUpgrade(SQLiteDatabase db, int version_old, int version_new)
    {
    // drop the tables and recreate them
        db.execSQL(drop_table_book);
        db.execSQL(drop_table_isbn);
        db.execSQL(create_table_isbn);
        db.execSQL(create_table_book);
    }

    // method for inserting book to SQLite
    public boolean insertBook(String bookID, String title, String author, String genre){
        SQLiteDatabase sdb = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("BookID", bookID);
        cv.put("Title", title);
        cv.put("Author", author);
        cv.put("Genre", genre);
        // store result into a long variable for validation
        long result = sdb.insert("book",  null, cv);
        // result == -1 which means the data is not inserted
        if(result == -1)
            return false;
        else
            return true;
    }

    // method for inserting ISBN and status to SQLite
    public boolean insertISBN(String isbn, String status, String bookID){
        SQLiteDatabase sdb = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("ISBN", isbn);
        cv.put("Status", status);
        cv.put("BookID", bookID);
        // store result into a long variable for validation
        long result = sdb.insert("isbn",  null, cv);
        // result == -1 which means the data is not inserted
        if(result == -1)
            return false;
        else
            return true;
    }

    // method to get all book in database
    public Cursor getAllBook(){
        SQLiteDatabase sdb = this.getWritableDatabase();
        Cursor cursor = sdb.rawQuery("select*from book", null);
        return cursor;
    }

    // method to get all ISBN and status in database
    public Cursor getAllIsbn(){
        SQLiteDatabase sdb = this.getWritableDatabase();
        Cursor cursor = sdb.rawQuery("select*from isbn", null);
        return cursor;
    }

    // method to get ISBN by status
    public Cursor getIsbnByStatus(String status){
        SQLiteDatabase sdb = this.getWritableDatabase();
        // name of the table to query
        String table_name = "Isbn";
        // the columns that we wish to retrieve from the tables
        String[] columns = {"ISBN","Status", "BookID"};
        // where clause of the query. DO NOT WRITE WHERE IN THIS
        String where = "Status = ?";
        // arguments to provide to the where clause
        String where_args[] = {status};
        // group by clause of the query. DO NOT WRITE GROUP BY IN THIS
        String group_by = null;
        // having clause of the query. DO NOT WRITE HAVING IN THIS
        String having = null;
        // order by clause of the query. DO NOT WRITE ORDER BY IN THIS
        String order_by = null;
        // run the query. this will give us a cursor into the database
        // that will enable us to change the table row that we are working with
        Cursor cursor = sdb.query(table_name, columns, where, where_args, group_by, having, order_by);

        return cursor;
    }

    // a bunch of constant strings that will be needed to create and drop
    // databases
    private static final String create_table_book = "create table book(" +
            "BookID string primary key, " +
            "Title string, " +
            "Author string, " +
            "Genre string" +
            ")";

    private static final String create_table_isbn = "create table isbn(" +
            "ISBN string primary key, " +
            "Status string, " +
            "BookID string references book(BookID)" +
            ")";

    private static final String drop_table_book = "drop table book";
    private static final String drop_table_isbn = "drop table isbn";
}
