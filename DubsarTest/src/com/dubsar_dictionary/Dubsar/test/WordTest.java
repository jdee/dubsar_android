/*
 Dubsar Dictionary Project
 Copyright (C) 2010-11 Jimmy Dee
 
 This program is free software; you can redistribute it and/or
 modify it under the terms of the GNU General Public License
 as published by the Free Software Foundation; either version 2
 of the License, or (at your option) any later version.
 
 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.
 
 You should have received a copy of the GNU General Public License
 along with this program; if not, write to the Free Software
 Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package com.dubsar_dictionary.Dubsar.test;

import com.dubsar_dictionary.Dubsar.model.Model;
import com.dubsar_dictionary.Dubsar.model.Word;

import junit.framework.TestCase;

public class WordTest extends TestCase {

	public void testNameAndPos() {
		Word word = new Word(1, "food", Model.PartOfSpeech.Noun);
		
		assertEquals("food (n.)", word.getNameAndPos());
	}
	
	public void testSubtitle() {
		// DEBT: Need to be able to clone models
		Word word1 = new Word(1, "food", Model.PartOfSpeech.Noun);
		Word word2 = new Word(1, "food", Model.PartOfSpeech.Noun);
		Word word3 = new Word(1, "food", Model.PartOfSpeech.Noun);
		Word word4 = new Word(1, "food", Model.PartOfSpeech.Noun);
		
		word2.setFreqCnt(10);
		
		word3.setFreqCnt(10);
		word3.setInflections("foods");
		
		word4.setInflections("foods");
		
		assertEquals("", word1.getSubtitle());
		assertEquals("freq. cnt.: 10", word2.getSubtitle());
		assertEquals("freq. cnt.: 10; also foods", word3.getSubtitle());
		assertEquals("also foods", word4.getSubtitle());
	}
}
