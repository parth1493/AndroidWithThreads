package com.parth.threadandroid.threading;

import com.parth.threadandroid.models.Word;

import java.util.ArrayList;

public interface TaskDelegate {

    void onWordsRetrieve(ArrayList<Word> words);

    void onRowsRetrieved(int numRows);

}
