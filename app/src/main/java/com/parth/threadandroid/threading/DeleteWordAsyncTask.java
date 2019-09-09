package com.parth.threadandroid.threading;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.parth.threadandroid.models.Word;
import com.parth.threadandroid.persistence.AppDatabase;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

public class DeleteWordAsyncTask extends AsyncTask<Word ,Void, Integer>
{

    private static final String TAG = "DeleteWordAsyncTask";
    private AppDatabase mDb;

    public DeleteWordAsyncTask(Context context) {
        mDb = AppDatabase.getDatabase(context);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected void onPostExecute(Integer word) {
        super.onPostExecute(word);
    }

    @Override
    protected void onProgressUpdate(Void... values) {
        super.onProgressUpdate(values);
    }

    @Override
    protected Integer doInBackground(Word... words) {
        return deleteWordAsyncTask(words[0]);
    }

    private Integer deleteWordAsyncTask(Word word) {
        Log.d(TAG,"delete words. This is from thread:"+Thread.currentThread().getName());
        return mDb.wordDataDao().delete(word);
    }
}
