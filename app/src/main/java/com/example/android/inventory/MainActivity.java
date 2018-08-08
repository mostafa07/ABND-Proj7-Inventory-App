package com.example.android.inventory;

import android.content.Intent;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

import com.example.android.inventory.data.ProductContract.ProductEntry;

import butterknife.BindView;
import butterknife.ButterKnife;


public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String LOG_TAG = MainActivity.class.getSimpleName();

    private static final int PRODUCT_LOADER = 0;

    private ProductCursorAdapter mProductCursorAdapter;

    @BindView(R.id.main_list)
    ListView mListView;
    @BindView(R.id.main_list_empty_view)
    View mEmptyView;
    @BindView(R.id.main_fab)
    FloatingActionButton mFab;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        mListView.setEmptyView(mEmptyView);

        mProductCursorAdapter = new ProductCursorAdapter(this, null);
        mListView.setAdapter(mProductCursorAdapter);

        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, EditorActivity.class));
            }
        });

        getSupportLoaderManager().initLoader(PRODUCT_LOADER, null, this);
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
                ProductEntry.COLUMN_PRODUCT_QUANTITY};

        return new CursorLoader(this, ProductEntry.CONTENT_URI, projection,
                null, null, null);
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {
        mProductCursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
        mProductCursorAdapter.swapCursor(null);
    }
}
