package com.example.android.inventory.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.android.inventory.data.ProductContract.ProductEntry;


public class ProductDbHelper extends SQLiteOpenHelper {

    /**
     * Class log tag for debugging and logging
     */
    private static final String LOG_TAG = ProductDbHelper.class.getSimpleName();

    /**
     * Name of the store database
     */
    private static final String DATABASE_NAME = "store.db";
    /**
     * Version of the store database
     */
    private static final int DATABASE_VERSION = 1;

    /**
     * Constructor
     */
    public ProductDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * Overridden methods
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        // Construct the SQL CREATE TABLE statement for creating the products database
        // Supplier phone is made TINYTEXT for optimization purposes
        String SQL_CREATE_TABLE_PRODUCTS = "CREATE TABLE " + ProductEntry.TABLE_NAME + " ("
                + ProductEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + ProductEntry.COLUMN_PRODUCT_NAME + " TEXT NOT NULL, "
                + ProductEntry.COLUMN_PRODUCT_PRICE + " INTEGER NOT NULL DEFAULT 0, "
                + ProductEntry.COLUMN_PRODUCT_QUANTITY + " INTEGER NOT NULL DEFAULT 1, "
                + ProductEntry.COLUMN_SUPPLIER_NAME + " TEXT NOT NULL, "
                + ProductEntry.COLUMN_SUPPLIER_PHONE + " TINYTEXT NOT NULL);";

        // Perform the SQL command to create the products database
        db.execSQL(SQL_CREATE_TABLE_PRODUCTS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }
}
