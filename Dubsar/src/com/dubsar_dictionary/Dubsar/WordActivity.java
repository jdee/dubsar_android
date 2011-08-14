package com.dubsar_dictionary.Dubsar;

import java.lang.ref.WeakReference;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

public class WordActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        setContentView(R.layout.word);

        Intent intent = getIntent();
        Uri uri = intent.getData();
        Bundle extras = intent.getExtras();
        
        String nameAndPos = extras.getString(DubsarContentProvider.WORD_NAME_AND_POS);
        
        TextView textView = (TextView)findViewById(R.id.word_banner);
        textView.setText(nameAndPos);
        
        new WordLoader(textView, (ListView)findViewById(R.id.word_sense_list)).execute(uri);
	}

	class WordLoader extends AsyncTask<Uri, Void, Cursor> {
    	
    	private final WeakReference<TextView> mTextViewReference;
    	private final WeakReference<ListView> mListViewReference;
    	
    	public WordLoader(TextView textView, ListView listView) {
    		mTextViewReference = new WeakReference<TextView>(textView);
    		mListViewReference = new WeakReference<ListView>(listView);
    	}

		@Override
		protected Cursor doInBackground(Uri... params) {
			return managedQuery(params[0], null, null, null, null);
		}

		@Override
		protected void onPostExecute(Cursor result) {

			super.onPostExecute(result);
			
			if (isCancelled()) return;
			
			TextView textView = mTextViewReference != null ? mTextViewReference.get() : null;
			ListView listView = mListViewReference != null ? mListViewReference.get() : null;
			
			if (textView == null || listView == null) return;

	        if (result == null) {
	        	// DEBT: externalize
	            textView.setText("ERROR!");
	        } else {
	            // Specify the columns we want to display in the result
	            String[] from = new String[] { DubsarContentProvider.SENSE_SUBTITLE, 
	            		DubsarContentProvider.SENSE_GLOSS,
	            		DubsarContentProvider.SENSE_SYNONYMS_AS_STRING };

	            // Specify the corresponding layout elements where we want the columns to go
	            int[] to = new int[] { R.id.sense_banner, R.id.sense_gloss, R.id.sense_synonyms };

	            // Create a simple cursor adapter for the definitions and apply them to the ListView
	            SimpleCursorAdapter words = 
	            		new SimpleCursorAdapter(listView.getContext(),
	                                          R.layout.sense, result, from, to);
	                                
	            listView.setAdapter(words);

	        }
		}
		
	}
}
