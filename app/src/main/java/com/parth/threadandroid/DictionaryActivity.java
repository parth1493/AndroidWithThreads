package com.parth.threadandroid;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;


import com.parth.threadandroid.adapters.WordsRecyclerAdapter;
import com.parth.threadandroid.models.Word;
import com.parth.threadandroid.threading.DeleteWordAsyncTask;
import com.parth.threadandroid.threading.RetrieveWordAsyncTask;
import com.parth.threadandroid.threading.TaskDelegate;
import com.parth.threadandroid.util.VerticalSpacingItemDecorator;

import java.util.ArrayList;

public class DictionaryActivity extends AppCompatActivity implements
        WordsRecyclerAdapter.OnWordListener,
        View.OnClickListener,
        SwipeRefreshLayout.OnRefreshListener,
        TaskDelegate
{

    private static final String TAG = "DictionaryActivity";

    //ui components
    private RecyclerView mRecyclerView;
    private SwipeRefreshLayout mSwipeRefresh;

    //vars
    private ArrayList<Word> mWords = new ArrayList<>();
    private WordsRecyclerAdapter mWordRecyclerAdapter;
    private FloatingActionButton mFab;
    private String mSearchQuery = "";
    private RetrieveWordAsyncTask retrieveWordAsyncTask;
    private DeleteWordAsyncTask deleteWordAsyncTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        restoreInstanceState(savedInstanceState);

        setContentView(R.layout.activity_dictionary);
        mRecyclerView = findViewById(R.id.recyclerView);
        mFab = findViewById(R.id.fab);
        mSwipeRefresh = findViewById(R.id.swipe_refresh);

        mFab.setOnClickListener(this);
        mSwipeRefresh.setOnRefreshListener(this);

        setupRecyclerView();
    }


    private void restoreInstanceState(Bundle savedInstanceState){
        if(savedInstanceState != null){
            mWords = savedInstanceState.getParcelableArrayList("words");
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putParcelableArrayList("words", mWords);
        super.onSaveInstanceState(outState);
    }


    @Override
    protected void onStart() {
        Log.d(TAG, "onStart: called.");
        super.onStart();

    }

    @Override
    protected void onStop() {
        Log.d(TAG, "onStop: called.");
        super.onStop();
        if(retrieveWordAsyncTask != null)
        {
            retrieveWordAsyncTask.cancel(true);
        }

        if(deleteWordAsyncTask != null)
        {
            deleteWordAsyncTask.cancel(true);
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        if(mSearchQuery.length() > 2){
            onRefresh();
        }
    }

    private void retrieveWords(String title) {
        Log.d(TAG, "retrieveWords: called.");
        if(retrieveWordAsyncTask != null)
        {
            retrieveWordAsyncTask.cancel(true);
        }
        retrieveWordAsyncTask = new RetrieveWordAsyncTask(this,this);
        retrieveWordAsyncTask.execute(title);
    }


    public void deleteWord(Word word) {
        Log.d(TAG, "deleteWord: called.");
        mWords.remove(word);
        mWordRecyclerAdapter.getFilteredWords().remove(word);
        mWordRecyclerAdapter.notifyDataSetChanged();
        if(deleteWordAsyncTask != null)
        {
            deleteWordAsyncTask.cancel(true);
        }
        deleteWordAsyncTask = new DeleteWordAsyncTask(this);
        deleteWordAsyncTask.execute(word);
    }



    private void setupRecyclerView(){
        Log.d(TAG, "setupRecyclerView: called.");
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(linearLayoutManager);
        VerticalSpacingItemDecorator itemDecorator = new VerticalSpacingItemDecorator(10);
        mRecyclerView.addItemDecoration(itemDecorator);
        new ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(mRecyclerView);
        mWordRecyclerAdapter = new WordsRecyclerAdapter(mWords, this);
        mRecyclerView.setAdapter(mWordRecyclerAdapter);
    }

    @Override
    public void onWordClick(int position) {
        Intent intent = new Intent(this, EditWordActivity.class);
        intent.putExtra("selected_word", mWords.get(position));
        startActivity(intent);
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()){

            case R.id.fab:{
                Intent intent = new Intent(this, EditWordActivity.class);
                startActivity(intent);
                break;
            }

        }
    }


    ItemTouchHelper.SimpleCallback itemTouchHelperCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
        @Override
        public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
            return false;
        }

        @Override
        public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
            deleteWord(mWords.get(mWords.indexOf(mWordRecyclerAdapter.getFilteredWords().get(viewHolder.getAdapterPosition()))));
        }
    };


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.dictionary_activity_actions, menu);

        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView =
                (SearchView) searchItem.getActionView();

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);

        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setMaxWidth(Integer.MAX_VALUE);

        // listening to search query text change
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // filter recycler view when query submitted
                if(query.length() > 2){
                    mSearchQuery = query;
                    retrieveWords(mSearchQuery);
                }
                else{
                    clearWords();
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String query) {
                // filter recycler view when text is changed
                if(query.length() > 2){
                    mSearchQuery = query;
                    retrieveWords(mSearchQuery);
                }
                else{
                    clearWords();
                }
                return false;
            }
        });

        return super.onCreateOptionsMenu(menu);
    }

    private void clearWords(){
        if(mWords != null){
            if(mWords.size() > 0){
                mWords.clear();
            }
        }
        mWordRecyclerAdapter.getFilter().filter(mSearchQuery);
    }

    @Override
    public void onRefresh() {
        retrieveWords(mSearchQuery);
        mSwipeRefresh.setRefreshing(false);
    }

    @Override
    public void onWordsRetrieve(ArrayList<Word> words) {
        clearWords();

        mWords.addAll(words);
        mWordRecyclerAdapter.notifyDataSetChanged();
    }
}


