package com.example.ass1_3011712;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.ArrayList;

public class ReturnActivity extends AppCompatActivity {
    private DBOpenHelper db;
    private SQLiteDatabase sdb;
    private ListView lv_books;
    private ArrayList<BookDetails> al_books;
    private ArrayList<IsbnAndStatus> al_isbn;
    private CustomArrayAdapter books_list_adapter;
    private View dialogView;
    private BottomSheetDialog dialog;
    private TextView bottom_sheet_title, bottom_sheet_author, bottom_sheet_genre, bottom_sheet_ISBN;
    private Button bottom_sheet_button;
    private int selected;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_return);
        setTitle("My Loans");

        // get access to an SQLite database
        db = new DBOpenHelper(this, "library.db", null, 1);
        sdb = db.getWritableDatabase();

        // initiate all the variable
        lv_books = findViewById(R.id.lv_booklist);
        al_books = new ArrayList<BookDetails>();
        al_isbn = new ArrayList<IsbnAndStatus>();

        // set up bottom sheet dialog and views
        dialogView = getLayoutInflater().inflate(R.layout.bottom_sheet, null);
        dialog = new BottomSheetDialog(this);
        dialog.setContentView(dialogView);
        bottom_sheet_title = dialogView.findViewById(R.id.bottom_sheet_title);
        bottom_sheet_author = dialogView.findViewById(R.id.bottom_sheet_author);
        bottom_sheet_genre = dialogView.findViewById(R.id.bottom_sheet_genre);
        bottom_sheet_genre = dialogView.findViewById(R.id.bottom_sheet_genre);
        bottom_sheet_ISBN = dialogView.findViewById(R.id.bottom_sheet_ISBN);
        bottom_sheet_button = dialogView.findViewById(R.id.bottom_sheet_button);

        // set adapter for list view
        books_list_adapter = new CustomArrayAdapter(this, al_books);
        lv_books.setAdapter(books_list_adapter);



        lv_books.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            // show a dialog to confirm of returning book
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selected = position;
                bottomSheetDialog();
            }
        });

        // add listener for bottom sheet button
        bottom_sheet_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                // show a dialog to confirm of adding a book to cart if there is at least a book available
                returnBookDialog(selected);
            }
        });
    }

    protected void onStart() {
        super.onStart();

        // use the function to retrieve all the checked out ISBN and it's status from database
        Cursor isbn_cursor = db.getIsbnByStatus("checked out");
        isbn_cursor.moveToFirst();
        for(int i = 0; i < isbn_cursor.getCount(); i++){
            al_isbn.add(new IsbnAndStatus(isbn_cursor.getString(0),
                    isbn_cursor.getString(1),
                    isbn_cursor.getString(2)));
            isbn_cursor.moveToNext();
        }

        // create a tempo array list contains all the books
        ArrayList<BookDetails> temp_al_books = new ArrayList<BookDetails>();

        // get all books from database and store inside tempo nook array list
        Cursor book_cursor = db.getAllBook();
        book_cursor.moveToFirst();
        for(int i = 0; i < book_cursor.getCount(); i++){
            temp_al_books.add(new BookDetails(book_cursor.getString(0),
                    book_cursor.getString(1),
                    book_cursor.getString(2),
                    book_cursor.getString(3)));
            book_cursor.moveToNext();
        }

        // match the checked out isbn with the book by using bookID
        // save all the matched books into al_books
        for(int i = 0; i < al_isbn.size(); i++){
            for(int j = 0; j < temp_al_books.size(); j++){
                if(al_isbn.get(i).getBookId().equals(temp_al_books.get(j).getBookID())){
                    al_books.add(new BookDetails(temp_al_books.get(j).getBookID(),
                            temp_al_books.get(j).getTitle(),
                            temp_al_books.get(j).getAuthor(),
                            temp_al_books.get(j).getGenre()));
                    al_books.get(i).addStock(al_isbn.get(i));
                }
            }
        }

        //Notify list data changed.
        books_list_adapter.notifyDataSetChanged();
    }

    // remove all data
    protected void onStop() {
        super.onStop();

        al_books.clear();
        al_isbn.clear();
    }

    // generate a bottom Sheet Dialog
    private void bottomSheetDialog(){
        ArrayList<IsbnAndStatus> isbns = al_books.get(selected).getIsbnS();
        String isbn = isbns.get(0).getIsbn();

        bottom_sheet_title.setText(al_books.get(selected).getTitle());
        bottom_sheet_author.setText(al_books.get(selected).getAuthor());
        bottom_sheet_genre.setText(al_books.get(selected).getGenre());
        bottom_sheet_ISBN.setText(isbn);
        bottom_sheet_button.setText("Return This Book");
        dialog.show();
    }

    // a method that showing a dialog to get confirmation of returning book from user
    private void returnBookDialog(int position){
        // we need a builder to create the dialog for us
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        // set the title and the message to be displayed on the dialog
        builder.setMessage("Confirm to return this book?");

        // add in a positive button here
        builder.setPositiveButton("yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // try to check in a book and get the isbn object to update the database
                IsbnAndStatus isbn = al_books.get(position).checkIn();
                if(isbn != null) {
                    ContentValues cv = new ContentValues();
                    cv.put("ISBN", isbn.getIsbn());
                    cv.put("Status", isbn.getStatus());
                    cv.put("BookID", isbn.getBookId());
                    int count = sdb.update("isbn", cv, "ISBN = ?", new String[]{isbn.getIsbn()});
                    if(count == 1) {
                        // if successfully updated the database then remove the book from list view and make a toast
                        al_books.remove(position);
                        Toast.makeText(ReturnActivity.this, "Successfully returned this book", Toast.LENGTH_SHORT).show();
                        books_list_adapter.notifyDataSetChanged();
                    }
                    else
                        // if failed to update the database then make a toast to tell user
                        Toast.makeText(ReturnActivity.this, "Failed to return this book", Toast.LENGTH_SHORT).show();
                }
                else
                    // if failed to update the database then make a toast to tell user
                    Toast.makeText(ReturnActivity.this, "Failed to return this book", Toast.LENGTH_SHORT).show();
            }
        });
        // add in a negative button here
        builder.setNegativeButton("no", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        // create the dialog and display it
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_loaning:
                Intent loanIntent = new Intent(this, LoanActivity.class);
                startActivity(loanIntent);
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);
        }
    }
}