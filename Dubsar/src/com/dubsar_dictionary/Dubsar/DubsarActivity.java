package com.dubsar_dictionary.Dubsar;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;

public class DubsarActivity extends Activity {
	public static final String EXPANDED = "expanded";
	public static final String POINTER_IDS = "pointer_ids";
	public static final String POINTER_TYPES = "pointer_types";
	public static final String POINTER_TARGET_IDS = "pointer_target_ids";
	public static final String POINTER_TARGET_TYPES = "pointer_target_types";
	public static final String POINTER_TARGET_TEXTS = "pointer_target_texts";
	public static final String POINTER_TARGET_GLOSSES =  "pointer_target_glosses";

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
}
