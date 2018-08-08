package com.example.android.inventory;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.NavUtils;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.example.android.inventory.data.ProductContract.ProductEntry;

import butterknife.BindView;
import butterknife.ButterKnife;


public class EditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String LOG_TAG = EditorActivity.class.getSimpleName();
    private static final int EXISTING_PRODUCT_LOADER = 0;

    private Uri mCurrentProductUri;

    private boolean mProductDataChanged = false;

    @BindView(R.id.editor_product_name_field)
    EditText mProductNameET;
    @BindView(R.id.editor_product_price_field)
    EditText mProductPriceET;
    @BindView(R.id.editor_product_quantity_field)
    EditText mProductQuantityET;
    @BindView(R.id.editor_supplier_name_field)
    EditText mSupplierNameET;
    @BindView(R.id.editor_supplier_phone_field)
    EditText mSupplierPhoneET;
    @BindView(R.id.editor_fab)
    FloatingActionButton mFab;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        ButterKnife.bind(this);

        Intent openingIntent = getIntent();
        mCurrentProductUri = openingIntent.getData();

        if (mCurrentProductUri == null) {
            setTitle(getString(R.string.editor_activity_title_add_product));
        } else {
            setTitle(getString(R.string.editor_activity_title_edit_product));
            getSupportLoaderManager().initLoader(EXISTING_PRODUCT_LOADER, null, this);
        }

        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveProduct();
            }
        });
        setupFieldTouchDetection();
    }

    @Override
    public void onBackPressed() {
        if (!mProductDataChanged) {
            super.onBackPressed();
            return;
        } else {
            DialogInterface.OnClickListener discardButtonClickListener =
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    };

            showUnsavedChangesDialog(discardButtonClickListener);
        }
    }

    /**
     * Loader Callbacks
     */
    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
        String[] projection = {
                ProductEntry._ID,
                ProductEntry.COLUMN_PRODUCT_NAME,
                ProductEntry.COLUMN_PRODUCT_PRICE,
                ProductEntry.COLUMN_PRODUCT_QUANTITY,
                ProductEntry.COLUMN_SUPPLIER_NAME,
                ProductEntry.COLUMN_SUPPLIER_PHONE};

        return new CursorLoader(this, mCurrentProductUri, projection,
                null, null, null);
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor cursor) {
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }

        if (cursor.moveToFirst()) {
            int productNameColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_NAME);
            int productQuantityColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_QUANTITY);
            int productPriceColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_PRICE);
            int supplierNameColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_SUPPLIER_NAME);
            int supplierPhoneColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_SUPPLIER_PHONE);

            String productName = cursor.getString(productNameColumnIndex);
            int productPrice = cursor.getInt(productPriceColumnIndex);
            int productQuantity = cursor.getInt(productQuantityColumnIndex);
            String supplierName = cursor.getString(supplierNameColumnIndex);
            String supplierPhone = cursor.getString(supplierPhoneColumnIndex);

            mProductNameET.setText(productName);
            mProductPriceET.setText(String.valueOf(productPrice));
            mProductQuantityET.setText(String.valueOf(productQuantity));
            mSupplierNameET.setText(supplierName);
            mSupplierPhoneET.setText(supplierPhone);
        }
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
        mProductNameET.setText("");
        mProductPriceET.setText("");
        mProductQuantityET.setText("");
        mSupplierNameET.setText("");
        mSupplierPhoneET.setText("");
    }


    /**
     * Other overridden methods
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: {
                if (!mProductDataChanged) {
                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    return true;
                } else {
                    DialogInterface.OnClickListener discardButtonClickListener =
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
                                }
                            };

                    showUnsavedChangesDialog(discardButtonClickListener);
                    return true;
                }
            }
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Other helper methods
     */
    private void setupFieldTouchDetection() {
        View.OnTouchListener onTouchListener = new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                mProductDataChanged = true;
                return false;
            }
        };

        mProductNameET.setOnTouchListener(onTouchListener);
        mProductPriceET.setOnTouchListener(onTouchListener);
        mProductQuantityET.setOnTouchListener(onTouchListener);
        mSupplierNameET.setOnTouchListener(onTouchListener);
        mSupplierPhoneET.setOnTouchListener(onTouchListener);
    }

    private void saveProduct() {
        String productName = mProductNameET.getText().toString().trim();
        String productPriceStr = mProductPriceET.getText().toString().trim();
        String productQuantityStr = mProductQuantityET.getText().toString().trim();
        String supplierName = mSupplierNameET.getText().toString().trim();
        String supplierPhone = mSupplierPhoneET.getText().toString().trim();

        if (mCurrentProductUri == null &&
                TextUtils.isEmpty(productName) && TextUtils.isEmpty(productPriceStr) &&
                TextUtils.isEmpty(productQuantityStr) && TextUtils.isEmpty(supplierName) &&
                TextUtils.isEmpty(supplierPhone)) {
            Toast.makeText(this, getString(R.string.editor_null_product_info_toast),
                    Toast.LENGTH_SHORT).show();
            return;
        }
        if (mCurrentProductUri == null &&
                (TextUtils.isEmpty(productName) || TextUtils.isEmpty(productPriceStr) ||
                        TextUtils.isEmpty(productQuantityStr) || TextUtils.isEmpty(supplierName) ||
                        TextUtils.isEmpty(supplierPhone))) {
            Toast.makeText(this, getString(R.string.editor_missing_product_info_toast),
                    Toast.LENGTH_SHORT).show();
            return;
        }

        int productPrice = Integer.parseInt(productPriceStr);
        int productQuantity = Integer.parseInt(productQuantityStr);

        if (productPrice <= 0) {
            Toast.makeText(this, getString(R.string.editor_negative_price_toast),
                    Toast.LENGTH_SHORT).show();
            return;
        }
        if (productQuantity < 0) {
            Toast.makeText(this, getString(R.string.editor_negative_quantity_toast),
                    Toast.LENGTH_SHORT).show();
            return;
        }

        ContentValues contentValues = new ContentValues();
        contentValues.put(ProductEntry.COLUMN_PRODUCT_NAME, productName);
        contentValues.put(ProductEntry.COLUMN_PRODUCT_PRICE, productPrice);
        contentValues.put(ProductEntry.COLUMN_PRODUCT_QUANTITY, productQuantity);
        contentValues.put(ProductEntry.COLUMN_SUPPLIER_NAME, supplierName);
        contentValues.put(ProductEntry.COLUMN_SUPPLIER_PHONE, supplierPhone);

        if (mCurrentProductUri == null) {
            Uri returnUri = getContentResolver().insert(ProductEntry.CONTENT_URI, contentValues);
            if (returnUri == null) {
                Toast.makeText(this, getString(R.string.editor_insert_error_toast),
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.editor_insert_success_toast),
                        Toast.LENGTH_SHORT).show();
            }
        } else {
            int rowsUpdated = getContentResolver().update(mCurrentProductUri, contentValues,
                    null, null);
            if (rowsUpdated <= 0) {
                Toast.makeText(this, getString(R.string.editor_update_error_toast),
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.editor_update_success_toast),
                        Toast.LENGTH_SHORT).show();
            }
        }

        finish();
    }

    private void showUnsavedChangesDialog(DialogInterface.OnClickListener discardButtonClickListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.editor_discard_dialog_message);
        builder.setPositiveButton(R.string.editor_discard_dialog_positive, discardButtonClickListener);
        builder.setNegativeButton(R.string.editor_discard_dialog_negative, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (dialog != null)
                    dialog.dismiss();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }
}
