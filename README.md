Introduction
You have been tasked with implementing a Library Loans Application. The application will assist a
user in managing their loans from a local library.

Assumptions 
App
1) There is no need for login details as it is assumed that the app on the device is used
for the owners own a library account.
Books
1) The details on store for books will be the Books ID, Title, Author and Genre.
2) While several copies of each title maybe in stock, each one must be uniquely
identifiable.
3) The details on store for unique identifier will be the ISBN, Status, Book ID.
4) 2 tables will be store in SQLite Database such as book and ISBN.
5) book table and ISBN are one to many relationship.
6) If a book is in cart or on loan, this must be reflected in some manner.
Cart, Loans and Returns
1) Once a book is loaned, added to cart or returned, this must be reflected somehow by
the system.
2) A person can only take 4 loans, but can add unlimited book into cart.