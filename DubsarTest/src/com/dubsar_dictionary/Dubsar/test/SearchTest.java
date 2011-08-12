package com.dubsar_dictionary.Dubsar.test;

import java.util.List;

import junit.framework.TestCase;

import org.json.JSONException;
import org.json.JSONTokener;

import com.dubsar_dictionary.Dubsar.model.Model;
import com.dubsar_dictionary.Dubsar.model.Search;
import com.dubsar_dictionary.Dubsar.model.Word;

public class SearchTest extends TestCase {

	
	public void testParsing() {
		String stringData = "[\"already\",[[21774,\"already\",\"adv\",107,\"\"]],1]";
		
		Search search = new Search("li");
		search.setData(stringData);
		
		try {
			JSONTokener tokener = new JSONTokener(stringData);
			search.parseData(tokener.nextValue());
		}
		catch (JSONException e) {
			fail("JSON parsing failed with error " + e.getMessage());
			return;
		}
		
		List<Word> results = search.getResults();
		Word word = results.get(0);
		
		assertEquals(1, search.getTotalPages());
		
		assertEquals(1, results.size());
		
		assertEquals(21774, word.getId());
		assertEquals("already", word.getName());
		assertEquals(Model.PartOfSpeech.Adverb, word.getPartOfSpeech());
		assertEquals(107, word.getFreqCnt());
		assertEquals("", word.getInflections());
		
	}

}
