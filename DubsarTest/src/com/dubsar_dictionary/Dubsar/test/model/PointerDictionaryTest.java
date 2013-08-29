/*
 Dubsar Dictionary Project
 Copyright (C) 2010-13 Jimmy Dee
 
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

package com.dubsar_dictionary.Dubsar.test.model;

import com.dubsar_dictionary.Dubsar.model.PointerDictionary;

import junit.framework.TestCase;

public class PointerDictionaryTest extends TestCase {

	public void testLabels() {
		assertEquals("Antonyms", PointerDictionary.labelFromPtype("antonym"));
		assertEquals("Hypernyms", PointerDictionary.labelFromPtype("hypernym"));
		assertEquals("Instance Hypernyms", PointerDictionary.labelFromPtype("instance hypernym"));
		assertEquals("Hyponyms", PointerDictionary.labelFromPtype("hyponym"));
		assertEquals("Instance Hyponyms", PointerDictionary.labelFromPtype("instance hyponym"));
		assertEquals("Member Holonyms", PointerDictionary.labelFromPtype("member holonym"));
		assertEquals("Substance Holonyms", PointerDictionary.labelFromPtype("substance holonym"));
		assertEquals("Part Holonyms", PointerDictionary.labelFromPtype("part holonym"));
		assertEquals("Member Meronyms", PointerDictionary.labelFromPtype("member meronym"));
		assertEquals("Substance Meronyms", PointerDictionary.labelFromPtype("substance meronym"));
		assertEquals("Part Meronyms", PointerDictionary.labelFromPtype("part meronym"));
		assertEquals("Attributes", PointerDictionary.labelFromPtype("attribute"));
		assertEquals("Derivationally Related Forms", PointerDictionary.labelFromPtype("derivationally related form"));
		assertEquals("Domain of Synset (Topic)", PointerDictionary.labelFromPtype("domain of synset (topic)"));
		assertEquals("Members of this Domain (Topic)", PointerDictionary.labelFromPtype("member of this domain (topic)"));
		assertEquals("Domain of Synset (Region)", PointerDictionary.labelFromPtype("domain of synset (region)"));
		assertEquals("Members of this Domain (Region)", PointerDictionary.labelFromPtype("member of this domain (region)"));
		assertEquals("Domain of Synset (Usage)", PointerDictionary.labelFromPtype("domain of synset (usage)"));
		assertEquals("Members of this Domain (Usage)", PointerDictionary.labelFromPtype("member of this domain (usage)"));
		assertEquals("Entailments", PointerDictionary.labelFromPtype("entailment"));
		assertEquals("Causes", PointerDictionary.labelFromPtype("cause"));
		assertEquals("Also See", PointerDictionary.labelFromPtype("also see"));
		assertEquals("Verb Group", PointerDictionary.labelFromPtype("verb group"));
		assertEquals("Similar to", PointerDictionary.labelFromPtype("similar to"));
		assertEquals("Participle of Verb", PointerDictionary.labelFromPtype("participle of verb"));
		assertEquals("Derived from/Pertains to", PointerDictionary.labelFromPtype("derived from/pertains to"));
	}

	public void testHelp() {
		assertEquals("words opposite in meaning", PointerDictionary.helpFromPtype("antonym"));
		assertEquals("more generic terms", PointerDictionary.helpFromPtype("hypernym"));
		assertEquals("classes of which this is an instance", PointerDictionary.helpFromPtype("instance hypernym"));
		assertEquals("more specific terms", PointerDictionary.helpFromPtype("hyponym"));
		assertEquals("instances of this class", PointerDictionary.helpFromPtype("instance hyponym"));
		assertEquals("wholes of which this is a member", PointerDictionary.helpFromPtype("member holonym"));
		assertEquals("wholes of which this is an ingredient", PointerDictionary.helpFromPtype("substance holonym"));
		assertEquals("wholes of which this is a part", PointerDictionary.helpFromPtype("part holonym"));
		assertEquals("constituent members", PointerDictionary.helpFromPtype("member meronym"));
		assertEquals("constituent substances", PointerDictionary.helpFromPtype("substance meronym"));
		assertEquals("constituent parts", PointerDictionary.helpFromPtype("part meronym"));
		assertEquals("general quality", PointerDictionary.helpFromPtype("attribute"));
		assertEquals("cognates, etc.", PointerDictionary.helpFromPtype("derivationally related form"));
		assertEquals("related topics", PointerDictionary.helpFromPtype("domain of synset (topic)"));
		assertEquals("entries under this topic", PointerDictionary.helpFromPtype("member of this domain (topic)"));
		assertEquals("relevant region", PointerDictionary.helpFromPtype("domain of synset (region)"));
		assertEquals("things relevant to this region", PointerDictionary.helpFromPtype("member of this domain (region)"));
		assertEquals("pertinent to usage", PointerDictionary.helpFromPtype("domain of synset (usage)"));
		assertEquals("relevant by usage", PointerDictionary.helpFromPtype("member of this domain (usage)"));
		assertEquals("consequences", PointerDictionary.helpFromPtype("entailment"));
		assertEquals("origins or reasons", PointerDictionary.helpFromPtype("cause"));
		assertEquals("related entries", PointerDictionary.helpFromPtype("also see"));
		assertEquals("related verbs", PointerDictionary.helpFromPtype("verb group"));
		assertEquals("near in meaning, but not exact", PointerDictionary.helpFromPtype("similar to"));
		assertEquals("root verb", PointerDictionary.helpFromPtype("participle of verb"));
		assertEquals("adj: pertinent noun; adv: source noun", PointerDictionary.helpFromPtype("derived from/pertains to"));
	}
}
