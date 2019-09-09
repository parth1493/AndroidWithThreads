package com.parth.threadandroid.threading;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.parth.threadandroid.models.Word;
import com.parth.threadandroid.persistence.AppDatabase;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

public class RetrieveWordAsyncTask extends AsyncTask<String,Void, ArrayList<Word>>
{

    private static final String TAG = "RetrieveWordAsyncTask";
    private AppDatabase mDb;
    private WeakReference<TaskDelegate> taskDelegateWeakReference;

    public RetrieveWordAsyncTask(Context context, TaskDelegate taskDelegate) {
        mDb = AppDatabase.getDatabase(context);
        taskDelegateWeakReference = new WeakReference<>(taskDelegate);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

    }

    @Override
    protected void onPostExecute(ArrayList<Word> words) {
        super.onPostExecute(words);
     taskDelegateWeakReference.get().onWordsRetrieve(words);
    }

    @Override
    protected void onProgressUpdate(Void... values) {
        super.onProgressUpdate(values);
    }

    @Override
    protected ArrayList<Word> doInBackground(String... strings) {
        return retrieveWordAsync(strings[0]);
    }

    private ArrayList<Word> retrieveWordAsync(String string) {
        Log.d(TAG,"retrieving words. This is from thread:"+Thread.currentThread().getName());
        return new ArrayList<>(mDb.wordDataDao().getWords(string));
    }
}
