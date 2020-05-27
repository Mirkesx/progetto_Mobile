package com.marco_cavalli.lost_and_found;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ProgressBar;

public class BaseActivity extends AppCompatActivity {

    public ProgressBar mProgressBar;

    public void setProgressBar(ProgressBar progressBar) {
        mProgressBar = progressBar;
    }

    public void showProgressBar() {
        if (mProgressBar != null) {
            mProgressBar.setVisibility(View.VISIBLE);
        }
    }

    public void hideProgressBar() {
        if (mProgressBar != null) {
            mProgressBar.setVisibility(View.INVISIBLE);
        }
    }

    public void hideKeyboard(View view) {
        final InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        hideProgressBar();
    }

}
