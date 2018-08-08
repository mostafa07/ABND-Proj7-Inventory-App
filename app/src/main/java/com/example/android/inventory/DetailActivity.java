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
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.inventory.data.ProductContract.ProductEntry;

import butterknife.BindView;
import butterknife.ButterKnife;


public class DetailActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String LOG_TAG = DetailActivity.class.getSimpleName();
    private static final int EXISTING_PRODUCT_LOADER = 0;
    private static final int PLUS_ONE = 0;
    private static final int NEG_ONE = 1;

    private Uri mCurrentProductUri;
    private int mProductQuantity;
    private String mSupplierPhone;

    @BindView(R.id.detail_product_name)
    TextView mProductNameTV;
    @BindView(R.id.detail_product_price)
    TextView mProductPriceTV;
    @BindView(R.id.detail_product_quantity)
    TextView mProductQuantityTV;
    @BindView(R.id.detail_supplier_name)
    TextView mSupplierNameTV;
    @BindView(R.id.detail_supplier_phone)
    TextView mSupplierPhoneTV;
    @BindView(R.id.detail_quantity_plus_one_button)
    ImageButton mPlusOneButton;
    @BindView(R.id.detail_quantity_neg_one_button)
    ImageButton mNegOneButton;
    @BindView(R.id.detail_fab)
    FloatingActionButton mFab;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        ButterKnife.bind(this);

        Intent openingIntent = getIntent();
        mCurrentProductUri = openingIntent.getData();

        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialSupplier();
            }
        });
        mSupplierPhoneTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialSupplier();
            }
        });
        mPlusOneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                modifyQuantity(PLUS_ONE);
            }
        });
        mNegOneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                modifyQuantity(NEG_ONE);
            }
        });

        getSupportLoaderManager().initLoader(EXISTING_PRODUCT_LOADER, null, this);
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
            mProductQuantity = cursor.getInt(productQuantityColumnIndex);
            String supplierName = cursor.getString(supplierNameColumnIndex);
            mSupplierPhone = cursor.getString(supplierPhoneColumnIndex);

            mProductNameTV.setText(productName);
            mProductPriceTV.setText(String.valueOf(productPrice));
            String productQuantityStr = String.valueOf(mProductQuantity) + " " + getString(R.string.items_in_stock);
            mProductQuantityTV.setText(productQuantityStr);
            mSupplierNameTV.setText(supplierName);
            mSupplierPhoneTV.setText(mSupplierPhone);
        }
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
        // TODO: What?!
        mProductNameTV.setText("");
        mProductPriceTV.setText("");
        mProductQuantityTV.setText("");
        mSupplierNameTV.setText("");
        mSupplierPhoneTV.setText("");
    }

    /**
     * Other overridden methods
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.detail_action_delete: {
                showDeleteConfirmationDialog();
                return true;
            }
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Other helper methods
     */
    private void dialSupplier() {
        if (mSupplierPhone != null && !mSupplierPhone.equals("")) {
            Intent callIntent = new Intent(Intent.ACTION_DIAL);
            callIntent.setData(Uri.parse("tel:" + mSupplierPhone));
            startActivity(callIntent);
        } else {
            Toast.makeText(getApplicationContext(), getString(R.string.detail_no_phone_num_toast),
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void modifyQuantity(final int plusOrNeg) {
        if (mProductQuantity <= 0 && plusOrNeg == NEG_ONE) {
            Toast.makeText(this, getString(R.string.detail_no_items_toast),
                    Toast.LENGTH_SHORT).show();
            return;
        }

        ContentValues contentValues = new ContentValues();

        switch (plusOrNeg) {
            case PLUS_ONE: {
                contentValues.put(ProductEntry.COLUMN_PRODUCT_QUANTITY, mProductQuantity + 1);
                break;
            }
            case NEG_ONE: {
                contentValues.put(ProductEntry.COLUMN_PRODUCT_QUANTITY, mProductQuantity - 1);
                break;
            }
        }

        int rowsUpdated = getContentResolver().update(mCurrentProductUri, contentValues,
                null, null);
        if (rowsUpdated == 0) {
            Toast.makeText(this, getString(R.string.detail_update_error_toast),
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void deleteProduct() {
        if (mCurrentProductUri != null) {
            int rowsDeleted = getContentResolver().delete(mCurrentProductUri, null, null);

            if (rowsDeleted == 0) {
                Toast.makeText(this, getString(R.string.detail_delete_error_toast),
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.detail_delete_success_toast),
                        Toast.LENGTH_SHORT).show();
            }
        }

        finish();
    }

    private void showDeleteConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.detail_delete_dialog_message);
        builder.setPositiveButton(R.string.detail_delete_dialog_positive, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                deleteProduct();
            }
        });
        builder.setNegativeButton(R.string.detail_delete_dialog_negative, new DialogInterface.OnClickListener() {
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
