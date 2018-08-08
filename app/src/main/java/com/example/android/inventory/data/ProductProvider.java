package com.example.android.inventory.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.example.android.inventory.data.ProductContract.ProductEntry;


public class ProductProvider extends ContentProvider {

    private static final String LOG_TAG = ProductProvider.class.getSimpleName();
    private static final int PRODUCTS = 100;
    private static final int PRODUCT_ID = 101;

    private ProductDbHelper mDbHelper;
    private static UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        sUriMatcher.addURI(ProductContract.CONTENT_AUTHORITY, ProductContract.PATH_PRODUCTS, PRODUCTS);
        sUriMatcher.addURI(ProductContract.CONTENT_AUTHORITY, ProductContract.PATH_PRODUCTS + "/#", PRODUCT_ID);
    }


    /**
     * Overridden Methods
     */
    @Override
    public boolean onCreate() {
        mDbHelper = new ProductDbHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {

        SQLiteDatabase database = mDbHelper.getReadableDatabase();
        Cursor cursor;

        switch (sUriMatcher.match(uri)) {
            case PRODUCTS: {
                cursor = database.query(ProductEntry.TABLE_NAME, projection, selection,
                        selectionArgs, null, null, sortOrder);
                break;
            }
            case PRODUCT_ID: {
                selection = ProductEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                cursor = database.query(ProductEntry.TABLE_NAME, projection, selection,
                        selectionArgs, null, null, sortOrder);
                break;
            }
            default:
                throw new IllegalArgumentException("Can't query unknown uri: " + uri);
        }

        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PRODUCTS:
                return ProductEntry.CONTENT_LIST_TYPE;
            case PRODUCT_ID:
                return ProductEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues contentValues) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PRODUCTS:
                return insertProduct(uri, contentValues);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }

    private Uri insertProduct(Uri uri, ContentValues contentValues) {

        String name = contentValues.getAsString(ProductEntry.COLUMN_PRODUCT_NAME);
        if (name == null) {
            throw new IllegalArgumentException("Product requires a name");
        }
        Integer price = contentValues.getAsInteger(ProductEntry.COLUMN_PRODUCT_PRICE);
        if (price != null && price < 0) {
            throw new IllegalArgumentException("Product requires a valid price");
        }
        Integer quantity = contentValues.getAsInteger(ProductEntry.COLUMN_PRODUCT_QUANTITY);
        if (quantity != null && quantity < 0) {
            throw new IllegalArgumentException("Product requires a valid quantity");
        }

        SQLiteDatabase database = mDbHelper.getWritableDatabase();
        long newRowId = database.insert(ProductEntry.TABLE_NAME, null, contentValues);

        if (newRowId == -1) {
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;
        }

        getContext().getContentResolver().notifyChange(uri, null);

        return ContentUris.withAppendedId(uri, newRowId);
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {

        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        int rowsDeleted;

        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PRODUCTS: {
                rowsDeleted = database.delete(ProductEntry.TABLE_NAME, selection, selectionArgs);
                break;
            }
            case PRODUCT_ID: {
                // Delete a single row given by the ID in the URI
                selection = ProductEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                rowsDeleted = database.delete(ProductEntry.TABLE_NAME, selection, selectionArgs);
                break;
            }
            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }

        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return rowsDeleted;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues contentValues, @Nullable String selection, @Nullable String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PRODUCTS: {
                return updateProduct(uri, contentValues, selection, selectionArgs);
            }
            case PRODUCT_ID: {
                selection = ProductEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return updateProduct(uri, contentValues, selection, selectionArgs);
            }
            default: {
                throw new IllegalArgumentException("Update is not supported for " + uri);
            }
        }
    }

    private int updateProduct(Uri uri, ContentValues contentValues, String selection, String[] selectionArgs) {

        if (contentValues.containsKey(ProductEntry.COLUMN_PRODUCT_NAME)) {
            String name = contentValues.getAsString(ProductEntry.COLUMN_PRODUCT_NAME);
            if (name == null) {
                throw new IllegalArgumentException("Product requires a name");
            }
        }
        if (contentValues.containsKey(ProductEntry.COLUMN_PRODUCT_PRICE)) {
            Integer price = contentValues.getAsInteger(ProductEntry.COLUMN_PRODUCT_PRICE);
            if (price != null && price < 0) {
                throw new IllegalArgumentException("Product requires a valid price");
            }
        }
        if (contentValues.containsKey(ProductEntry.COLUMN_PRODUCT_QUANTITY)) {
            Integer quantity = contentValues.getAsInteger(ProductEntry.COLUMN_PRODUCT_QUANTITY);
            if (quantity != null && quantity < 0) {
                throw new IllegalArgumentException("Product requires a valid quantity");
            }
        }

        if (contentValues.size() == 0) {
            return 0;
        }

        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        int rowsUpdated = database.update(ProductEntry.TABLE_NAME, contentValues, selection, selectionArgs);
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return rowsUpdated;
    }
}
