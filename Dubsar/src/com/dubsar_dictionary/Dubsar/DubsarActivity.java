package com.dubsar_dictionary.Dubsar;

import android.app.Activity;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.graphics.Typeface;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;

public class DubsarActivity extends Activity {

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.search:
                onSearchRequested();
                return true;
            default:
                return false;
        }
    }
    
    /**
     * One of the obvious purposes of a Cursor is to grant efficient
     * database access to external, isolated components, without having
     * to extract the data wholesale from the database for transmission
     * to the other component. A Cursor effectively models an SQL
     * select result. However, in the current situation it would be 
     * inefficient and inconvenient in the web implementation to expose
     * the individual tables from the server and make the app perform
     * multiple requests to the server to render a single view. In
     * effect, the results of a query to the DubsarContentProvider are
     * like an outer table join, in which most rows have empty columns.
     * The view must extract subsets of cursor rows to populate
     * different portions of a view. For example, in the sense view, 
     * there are several scalars (name and part of speech, gloss, 
     * banner), while the synonyms, verb frames, samples and pointers 
     * are vectors. In order to render a ListView, the activity has to 
     * extract one of the vectors from the full cursor result and 
     * present that subset to the ListView. This seems inefficient, but
     * will eventually become moot with a local database. 
     * 
     * @param cursor input result Cursor
     * @param columnNames a subset of the column names in the input cursor
     * @param columnTypes types corresponding to the input columns
     * @param firstRow the first row to select from the input Cursor
     * @param lastRow the last row to select from the input Cursor
     * @return a new MatrixCursor object with the selected subset of rows
     * @throws IllegalArgumentException
     */
    public static Cursor extractSubCursor(Cursor cursor, String[] columnNames,
    		FieldType[] columnTypes, int firstRow, int numRows) 
    		throws IllegalArgumentException {
    	
    	if (cursor == null) {
    		throw new IllegalArgumentException("cursor is null");
    	}
    	if (columnNames == null) { 
    		throw new IllegalArgumentException("columnNames is null");
    	}
    	if (columnTypes == null) {
    		throw new IllegalArgumentException("columnTypes is null");
    	}
    	
    	if (columnNames.length != columnTypes.length) {
    		throw new IllegalArgumentException("mismatched column name and type sizes");
    	}
    	
    	if (numRows < 0 || firstRow < 0 || firstRow + numRows > cursor.getCount()) {
    		throw new IllegalArgumentException("invalid row selection");
    	}
    	
    	MatrixCursor newCursor = new MatrixCursor(columnNames, numRows);
    	
    	for (int j=firstRow; j<cursor.getCount() && j<firstRow + numRows; ++j) {
    		cursor.moveToPosition(j);
    		
    		MatrixCursor.RowBuilder builder = newCursor.newRow();
    		
    		for (int k=0; k<columnTypes.length; ++k) {
    			// throws IllegalArgumentException if the specified column name
    			// is not found
    			int columnIndex = cursor.getColumnIndexOrThrow(columnNames[k]);
    			
    			/*
    			 * It's not clear if getInt() or getString() throws an exception
    			 * if the field is not the specified type, or if it is coerced.
    			 * Starting with API level 11, you can query the Cursor for the
    			 * type of each column, obviating the columnTypes argument. 
    			 */
    			switch (columnTypes[k]) {
    			case Integer:
    				builder.add(new Integer(cursor.getInt(columnIndex)));
    				break;
    			case String:
    				builder.add(cursor.getString(columnIndex));
    				break;
    			default:
    				throw new IllegalArgumentException("invalid column type at index " + k);
    			}
    		}
    	}
    	
    	return newCursor;
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
    protected static void setBoldTypeface(TextView textView) {
    	textView.setTypeface(BOLD_TYPEFACE, Typeface.BOLD);
    }
    
    /**
     * Set the specified TextView to use the Dubsar normal font.
     * @param textView the TextView whose font to set
     */
    protected static void setNormalTypeface(TextView textView) {
    	textView.setTypeface(NORMAL_TYPEFACE, Typeface.NORMAL);
    }

    /**
     * Set the specified TextView to use the Dubsar bold font.
     * @param textView the TextView whose font to set
     */
    protected static void setItalicTypeface(TextView textView) {
    	textView.setTypeface(ITALIC_TYPEFACE, Typeface.ITALIC);
    }
    
    /**
     * Set the specified TextView to use the Dubsar normal font.
     * @param textView the TextView whose font to set
     */
    protected static void setBoldItalicTypeface(TextView textView) {
    	textView.setTypeface(BOLD_ITALIC_TYPEFACE, Typeface.BOLD_ITALIC);
    }
}
