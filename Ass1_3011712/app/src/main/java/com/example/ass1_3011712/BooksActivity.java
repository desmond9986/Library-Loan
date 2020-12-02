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
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.ArrayList;
import java.util.Iterator;

public class BooksActivity extends AppCompatActivity {
    // private fields of the class
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
        setContentView(R.layout.activity_main);

        // get access to an SQLite database
        db = new DBOpenHelper(this, "library.db", null, 1);
        sdb = db.getWritableDatabase();

        // initiate all the variable
        lv_books = findViewById(R.id.lv_booklist);
        al_books = new ArrayList<BookDetails>();
        books_list_adapter = new CustomArrayAdapter(this, al_books);


        // set adapter for list view
        lv_books.setAdapter(books_list_adapter);
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


        // insert all the books into database for the first time of using this application
        Cursor test_cursor = db.getAllIsbn();
        if(test_cursor.getCount() == 0)
            insertData();


        lv_books.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // show a bottom sheet that comes with book details
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
                if(al_books.get(selected).getCheckedIn() != 0)
                    addCartDialog(selected);
                else
                    outOfStockDialog();
            }
        });

    }

    protected void onStart() {
        super.onStart();
        // use the function to retrieve all the ISBN and it's status from database
        Cursor isbn_cursor = db.getAllIsbn();

        isbn_cursor.moveToFirst();
        for(int i = 0; i < isbn_cursor.getCount(); i++){
            al_isbn.add(new IsbnAndStatus(isbn_cursor.getString(0),
                    isbn_cursor.getString(1),
                    isbn_cursor.getString(2)));
            isbn_cursor.moveToNext();
        }

        // get all books from database
        Cursor book_cursor = db.getAllBook();
        book_cursor.moveToFirst();
        for(int i = 0; i < book_cursor.getCount(); i++){
            al_books.add(new BookDetails(book_cursor.getString(0),
                    book_cursor.getString(1),
                    book_cursor.getString(2),
                    book_cursor.getString(3)));
            // add all the isbn into book
            for(int j = 0; j < al_isbn.size(); j++){
                if(al_isbn.get(j).getBookId().equals(book_cursor.getString(0))){
                    al_books.get(i).addStock(al_isbn.get(j));
                }
            }
            book_cursor.moveToNext();
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
        String isbnAndStatus = isbns.get(0).getIsbn() + " (" + isbns.get(0).getStatus() +")";
        for(int i = 1; i < isbns.size(); i++){
            isbnAndStatus += "\n" + isbns.get(i).getIsbn() + " (" + isbns.get(i).getStatus() +")";
        }

        bottom_sheet_title.setText(al_books.get(selected).getTitle());
        bottom_sheet_author.setText(al_books.get(selected).getAuthor());
        bottom_sheet_genre.setText(al_books.get(selected).getGenre());
        bottom_sheet_ISBN.setText(isbnAndStatus);
        bottom_sheet_button.setText("Add To Cart");
        dialog.show();
    }

    // a method that showing a dialog to tell user that the book is out of stock
    private void outOfStockDialog(){
        // we need a builder to create the dialog for us
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        // set the title and the message to be displayed on the dialog
        builder.setMessage("This book is out of stock.");

        // add in a positive button here
        builder.setPositiveButton("ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {}
        });

        // create the dialog and display it
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    // a method that showing a dialog to get confirmation of adding a book into cart
    private void addCartDialog(int position){
        // we need a builder to create the dialog for us
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        // set the title and the message to be displayed on the dialog
        builder.setMessage("Do you want to add to cart?");

        // add in a positive button here
        builder.setPositiveButton("yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // try to add a book into cart and get the isbn object to update the database
                IsbnAndStatus isbn = al_books.get(position).addToCart();
                if(isbn != null) {
                    ContentValues cv = new ContentValues();
                    cv.put("ISBN", isbn.getIsbn());
                    cv.put("Status", isbn.getStatus());
                    cv.put("BookID", isbn.getBookId());
                    int count = sdb.update("isbn", cv, "ISBN = ?", new String[]{isbn.getIsbn()});
                    if(count == 1) {
                        // if successfully updated the database then notify the adapter to update and make a toast
                        Toast.makeText(BooksActivity.this, "Successfully added to cart", Toast.LENGTH_SHORT).show();
                        books_list_adapter.notifyDataSetChanged();
                    }
                    else
                        // if failed to update the database then make a toast to tell user
                        Toast.makeText(BooksActivity.this, "Failed to add to cart", Toast.LENGTH_SHORT).show();
                }
                else
                    // if failed to update the database then make a toast to tell user
                    Toast.makeText(BooksActivity.this, "Failed to add to cart1", Toast.LENGTH_SHORT).show();
            }
        });
        // add in a negative button here
        builder.setNegativeButton("no", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) { }
        });
        // create the dialog and display it
        AlertDialog dialog = builder.create();
        dialog.show();

    }

    // this method is used for insert all the books into database for the first time of using this application
    private void insertData(){
        db.insertBook("B1","The alchemist", "Paulo Coelho", "Novel");
        db.insertISBN("9780722532935", "checked in", "B1");
        db.insertISBN("9780722532936", "checked in", "B1");
        db.insertISBN("9780722532937", "checked in", "B1");
        db.insertBook("B2","20 master plots(and how to build them)", "Ronald B. Tobias", "Fiction");
        db.insertISBN("9780898795950", "checked in", "B2");
        db.insertISBN("9780898795951", "checked in", "B2");
        db.insertBook("B3","Academy Street", "Mary Costello", "Fiction");
        db.insertISBN("9781782114208", "checked in", "B3");
        db.insertISBN("9781782114209", "checked in", "B3");
        db.insertBook("B4","Accordion Crimes", "E. Annie Proulx", "Fiction");
        db.insertISBN("9780684831541", "checked in", "B4");
        db.insertISBN("9780684831542", "checked in", "B4");
        db.insertBook("B5", "Another time, another life", "Leif G.W. Persson", "Mystery");
        db.insertISBN("9780385614191", "checked in", "B5");
        db.insertISBN("9780385614192", "checked in", "B5");
        db.insertBook("B6","Big sky", "Kate Atkinson", "Mystery");
        db.insertISBN("9780857526113", "checked in", "B6");
        db.insertISBN("9780857526114", "checked in", "B6");
        db.insertBook("B7","Ariel", "Sylvia Plath", "Poetry");
        db.insertISBN("9780571086269", "checked in", "B7");
        db.insertBook("B8","Birthday letters", "Ted Hughes", "Poetry");
        db.insertISBN("9780571194728", "checked in", "B8");
        db.insertISBN("9780571194729", "checked in", "B8");
        db.insertBook("B9","Anonymous motion picture", "Roland Emmerich", "Thriller");
        db.insertISBN("5035822075931", "checked in", "B9");
        db.insertISBN("5035822075932", "checked in", "B9");
        db.insertISBN("5035822075933", "checked in", "B9");
        db.insertBook("B10","The birds motion picture", "Alfred Hitchcock", "Thriller");
        db.insertISBN("90091020354", "checked in", "B10");
        db.insertISBN("90091020355", "checked in", "B10");
        db.insertBook("B11","America splendor motion picture", "Robert Pulcini", "Biography");
        db.insertISBN("90091014934", "checked in", "B11");
        db.insertISBN("90091014935", "checked in", "B11");
        db.insertBook("B12","Circe", "Madeline Miller", "Fantasy");
        db.insertISBN("9781408890042", "checked in", "B12");
        db.insertISBN("9781408890042", "checked in", "B12");
        db.insertBook("B13","A clash of kings", "George Martin", "Fantasy");
        db.insertISBN("9780006479895", "checked in", "B13");
        db.insertISBN("9780006479896", "checked in", "B13");
        db.insertBook("B14","Atonement", "Ian McEwan", "Historical Fiction");
        db.insertISBN("9780099507383", "checked in", "B14");
        db.insertISBN("9780099507384", "checked in", "B14");
        db.insertBook("B15","A boy in winter", "Rachel Seiffert", "Historical Fiction");
        db.insertISBN("9781844089994", "checked in", "B15");
    }

    @Override
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

            case R.id.action_returning:
                Intent returnIntent = new Intent(this, ReturnActivity.class);
                startActivity(returnIntent);
                return true;
            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);
        }
    }

}