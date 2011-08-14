package com.dubsar_dictionary.Dubsar.test;

import android.test.ActivityInstrumentationTestCase2;
import android.widget.TextView;

import com.dubsar_dictionary.Dubsar.MainActivity;
import com.dubsar_dictionary.Dubsar.R;
import com.dubsar_dictionary.Dubsar.model.Model;

public class MainActivityTest extends ActivityInstrumentationTestCase2<MainActivity> {

	public MainActivityTest() {
		super("com.dubsar_dictionary.Dubsar", MainActivity.class);
	}
	
	protected void setUp() {
		Model.addMock("/wotd", "[25441,\"resourcefully\",\"adv\",0,\"\"]");
	}
	
	public void testWotd() {
		TextView wotdWord = (TextView)getActivity().findViewById(R.id.wotd_word);
		TextView wotdSubtitle = (TextView)getActivity().findViewById(R.id.wotd_subtitle);
		
		try {
			// delay long enough to let the mock data populate the view
			Thread.sleep(1000);
		}
		catch (InterruptedException e) {
			fail("sleep interrupted");
		}
		
		assertEquals("resourcefully (adv.)", wotdWord.getText());
		assertEquals("", wotdSubtitle.getText());
	}
}
