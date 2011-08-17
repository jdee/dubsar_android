package com.dubsar_dictionary.Dubsar;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.dubsar_dictionary.Dubsar.model.ForwardStack;

public class DubsarActivity extends Activity {

	public static final String EXPANDED = "expanded";
	public static final String POINTER_IDS = "pointer_ids";
	public static final String POINTER_TYPES = "pointer_types";
	public static final String POINTER_TARGET_IDS = "pointer_target_ids";
	public static final String POINTER_TARGET_TYPES = "pointer_target_types";
	public static final String POINTER_TARGET_TEXTS = "pointer_target_texts";
	public static final String POINTER_TARGET_GLOSSES =  "pointer_target_glosses";
	
	private Button mLeftArrow=null;
	private Button mRightArrow=null;
	
	protected static volatile ForwardStack sForwardStack=new ForwardStack();

	protected void onCreate(Bundle savedInstanceState, int layout) {
		super.onCreate(savedInstanceState);
		setContentView(layout);
		adjustForwardStack();
		setupNavigation();
	}

	/**
	 * If the forward button was not enabled the last time we visited
	 * the page we just went back to, it does not get redrawn, and the
	 * button remains disabled. This method fixes that.
	 */
	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		
		setButtonState(mRightArrow, !sForwardStack.isEmpty());		
	}
	
	@Override
	public void onBackPressed() {
		sForwardStack.push(getIntent());
		mRightArrow.setEnabled(true);
		super.onBackPressed();
	}

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        	case R.id.home:
        		startMainActivity();
        		return true;
            case R.id.search:
                onSearchRequested();
                return true;
            default:
                return false;
        }
    }
    
    /**
     * The SDK doesn't provide support for querying column types until
     * SDK 11.
     */
    public enum FieldType {
    	Unknown,
    	Integer,
    	String
    }
    
    public static final String TYPEFACE_FAMILY = "Tahoma";
    public static final Typeface BOLD_TYPEFACE = Typeface.create(TYPEFACE_FAMILY, Typeface.BOLD);
    public static final Typeface NORMAL_TYPEFACE = Typeface.create(TYPEFACE_FAMILY, Typeface.NORMAL);
    public static final Typeface ITALIC_TYPEFACE = Typeface.create(TYPEFACE_FAMILY, Typeface.ITALIC);
    public static final Typeface BOLD_ITALIC_TYPEFACE = Typeface.create(TYPEFACE_FAMILY, Typeface.BOLD_ITALIC);

    /**
     * Set the specified TextView to use the Dubsar bold font.
     * @param textView the TextView whose font to set
     */
    public static void setBoldTypeface(TextView textView) {
    	textView.setTypeface(BOLD_TYPEFACE, Typeface.BOLD);
    }
    
    /**
     * Set the specified TextView to use the Dubsar normal font.
     * @param textView the TextView whose font to set
     */
    public static void setNormalTypeface(TextView textView) {
    	textView.setTypeface(NORMAL_TYPEFACE, Typeface.NORMAL);
    }

    /**
     * Set the specified TextView to use the Dubsar bold font.
     * @param textView the TextView whose font to set
     */
    public static void setItalicTypeface(TextView textView) {
    	textView.setTypeface(ITALIC_TYPEFACE, Typeface.ITALIC);
    }
    
    /**
     * Set the specified TextView to use the Dubsar normal font.
     * @param textView the TextView whose font to set
     */
    public static void setBoldItalicTypeface(TextView textView) {
    	textView.setTypeface(BOLD_ITALIC_TYPEFACE, Typeface.BOLD_ITALIC);
    }
    
    protected void startMainActivity() {
    	Intent intent = new Intent(getApplicationContext(), MainActivity.class);
    	startActivity(intent);
    }
    
    protected static void setButtonState(Button button, boolean state) {
    	button.setEnabled(state);
    	button.setFocusable(state);
    }
	
    protected void adjustForwardStack() {
    	if (sForwardStack.isEmpty()) return;
    	
    	Log.d(getString(R.string.app_name), 
    			"starting new intent with URI " + getIntent().getData());
		if (!equalIntents(getIntent(), sForwardStack.peek())) {
			// user is visiting something other than the previous forward
			// link, clear the stack
			sForwardStack.clear();
		}
		else {
			// user went forward. pop this entry from the stack
			sForwardStack.pop();
		}

    }

    protected void setupNavigation() {
		mLeftArrow = (Button)findViewById(R.id.left_arrow);
		mRightArrow = (Button)findViewById(R.id.right_arrow);

		setButtonState(mRightArrow, !sForwardStack.isEmpty());		
		
		mLeftArrow.setOnClickListener(new OnClickListener(){
			public void onClick(View v) {
				onBackPressed();
			}
		});
		
		mRightArrow.setOnClickListener(new OnClickListener(){
			public void onClick(View v) {
				// get the top of the stack
				Intent intent = sForwardStack.peek();
				startActivity(intent);
			}
		});
	}
    
    protected static boolean equalIntents(Intent i1, Intent i2) {
    	if (i1 == null || i2 == null) 
    		throw new NullPointerException("not expecting null Intents");
    	
    	Uri uri1 = i1.getData();
    	Uri uri2 = i2.getData();
    	
    	String query1 = i1.getStringExtra(SearchManager.QUERY);
    	String query2 = i2.getStringExtra(SearchManager.QUERY);
    	
    	if (uri1 == null && uri2 == null) {
    		if (query1 == null || query2 == null)
    			throw new NullPointerException("null parameters");
    		
    		return query1.equals(query2);
    	}
    	
    	if (uri1 == null || uri2 == null) {
    		if ((uri1 == null && query1 == null) ||
    			(uri2 == null && query2 == null))
    			throw new NullPointerException("invalid null parameters in intent");
    		return false;
    	}
    	boolean equal = uri1.equals(uri2);
    	
    	return equal;
    }
}
