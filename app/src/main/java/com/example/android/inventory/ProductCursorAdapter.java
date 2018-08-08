package com.example.android.inventory;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.inventory.data.ProductContract.ProductEntry;

import butterknife.BindView;
import butterknife.ButterKnife;


public class ProductCursorAdapter extends CursorAdapter {

    private static final String LOG_TAG = ProductCursorAdapter.class.getSimpleName();

    @BindView(R.id.item_product_name)
    TextView productNameTV;
    @BindView(R.id.item_product_quantity)
    TextView productQuantityTV;
    @BindView(R.id.item_product_price)
    TextView productPriceTV;
    @BindView(R.id.item_sale_button)
    Button saleButton;

    /**
     * Constructor
     */
    public ProductCursorAdapter(Context context, Cursor cursor) {
        super(context, cursor, 0);
    }

    /**
     * Overridden methods
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    @Override
    public void bindView(View view, final Context context, Cursor cursor) {

        ButterKnife.bind(this, view);

        int productIdColumnIndex = cursor.getColumnIndex(ProductEntry._ID);
        int productNameColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_NAME);
        int productQuantityColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_QUANTITY);
        int productPriceColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_PRICE);

        final int productId = cursor.getInt(productIdColumnIndex);
        final String productName = cursor.getString(productNameColumnIndex);
        final int productQuantity = cursor.getInt(productQuantityColumnIndex);
        final int productPrice = cursor.getInt(productPriceColumnIndex);

        String quantityStr = context.getString(R.string.main_in_stock) + " " + productQuantity;
        String priceStr = context.getString(R.string.dollar_sign) + productPrice;

        productNameTV.setText(productName);
        productQuantityTV.setText(quantityStr);
        productPriceTV.setText(priceStr);

        saleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (productQuantity <= 0) {
                    Toast.makeText(context, context.getString(R.string.detail_no_items_toast), Toast.LENGTH_SHORT).show();
                } else {
                    decreaseQuantityBy1(context, productId, productQuantity);
                }
            }
        });
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent detailIntent = new Intent(context, DetailActivity.class);
                Uri currentProductUri = ContentUris.withAppendedId(ProductEntry.CONTENT_URI, productId);
                detailIntent.setData(currentProductUri);
                context.startActivity(detailIntent);
            }
        });
        view.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Intent editIntent = new Intent(context, EditorActivity.class);
                Uri currentProductUri = ContentUris.withAppendedId(ProductEntry.CONTENT_URI, productId);
                editIntent.setData(currentProductUri);
                context.startActivity(editIntent);
                return true;
            }
        });
    }

    /**
     * Other helper methods
     */
    private void decreaseQuantityBy1(Context context, int productId, int currentProductQuantity) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(ProductEntry.COLUMN_PRODUCT_QUANTITY, currentProductQuantity - 1);
        Uri productUri = ContentUris.withAppendedId(ProductEntry.CONTENT_URI, productId);

        int returnResult = context.getContentResolver().update(productUri, contentValues, null, null);
        if (returnResult <= 0) {
            Toast.makeText(context, context.getString(R.string.sale_error_toast) + returnResult,
                    Toast.LENGTH_SHORT).show();
        } else if (returnResult == 1) {
            Toast.makeText(context, context.getString(R.string.sale_success_toast),
                    Toast.LENGTH_SHORT).show();
        }
    }
}
