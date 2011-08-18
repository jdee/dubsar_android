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

package com.dubsar_dictionary.Dubsar.model;

import java.util.HashMap;

public class PointerDictionary {
	
	private static HashMap<String, String> sTitles = null;
	private static HashMap<String, String> sHelp = null;

	public static final String labelFromPtype(String ptype) {
		String value = getTitles().get(ptype);
		
		return value != null ? value : "";
	}
	
	public static final String helpfromPtype(String ptype) {
		String value = getHelp().get(ptype);
		
		return value != null ? value : "";
	}
	
	protected static final HashMap<String, String> getTitles() {
		if (sTitles == null) {
			sTitles = new HashMap<String, String>();
			
			/*
			 * DEBT: Make these all constants or something. But as
			 * long as it's encapsulated in this class, who cares?
			 */
			sTitles.put("antonym", "Antonyms");
			sTitles.put("hypernym", "Hypernyms");
			sTitles.put("instance hypernym", "Instance Hypernyms");
			sTitles.put("hyponym", "Hyponyms");
			sTitles.put("instance hyponyms", "Instance Hyponyms");
			sTitles.put("member holonym", "Member Holonyms");
			sTitles.put("substance holonym", "Substance Holonyms");
			sTitles.put("part holonym", "Part Holonyms");
			sTitles.put("member meronym", "Member Meronyms");
			sTitles.put("substance meronym", "Substance Meronyms");
			sTitles.put("part meronym", "Part Meronyms");
			sTitles.put("attribute", "Attribute");
			sTitles.put("derivationally related form", "Derivationally Related Forms");
			sTitles.put("domain of synset (topic)", "Domain of Synset (Topic)");
			sTitles.put("member of this domain (topic)", "Members of this Domain (Topic)");
			sTitles.put("domain of synset (region)", "Domain of Synset (Region)");
			sTitles.put("member of this domain (region)", "Members of this Domain (Region)");
			sTitles.put("domain of synset (usage)", "Domain of Synset (Usage)");
			sTitles.put("member of this domain (usage)", "Members of this Domain (Usage)");
			sTitles.put("entailment", "Entailments");
			sTitles.put("cause", "Causes");
			sTitles.put("also see", "Also See");
			sTitles.put("verb group", "Verb Group");
			sTitles.put("similar to", "Similar to");
			sTitles.put("participle of verb", "Participle of Verb");
			sTitles.put("derived from/pertains to", "Derived from/Pertains to");
			
		}
		
		return sTitles;
	}
	
	protected static final HashMap<String, String> getHelp() {
		if (sHelp == null) {
			sHelp = new HashMap<String, String>();
			
			/*
			 * DEBT: Make these all constants or something. But as
			 * long as it's encapsulated in this class, who cares?
			 */
			sHelp.put("antonym", "words opposite in meaning");
			sHelp.put("hypernym", "more generic terms");
			sHelp.put("instance hypernym", "classes of which this is an instance");
			sHelp.put("hyponym", "more specific terms");
			sHelp.put("instance hyponyms", "instances of this class");
			sHelp.put("member holonym", "wholes of which this is a member");
			sHelp.put("substance holonym", "wholes of which this is an ingredient");
			sHelp.put("part holonym", "wholes of which this is a part");
			sHelp.put("member meronym", "constituent members");
			sHelp.put("substance meronym", "constituent substances");
			sHelp.put("part meronym", "constituent parts");
			sHelp.put("attribute", "general quality");
			sHelp.put("derivationally related form", "cognates, etc.");
			sHelp.put("domain of synset (topic)", "related topics");
			sHelp.put("member of this domain (topic)", "entries under this topic");
			sHelp.put("domain of synset (region)", "relevant region");
			sHelp.put("member of this domain (region)", "things relevant to this region");
			sHelp.put("domain of synset (usage)", "pertinent to usage");
			sHelp.put("member of this domain (usage)", "relevant by usage");
			sHelp.put("entailment", "consequences");
			sHelp.put("cause", "origins or reasons");
			sHelp.put("also see", "related entries");
			sHelp.put("verb group", "related verbs");
			sHelp.put("similar to", "near in meaning, but not exact");
			sHelp.put("participle of verb", "root verb");
			sHelp.put("derived from/pertains to", "adj: pertinent noun; adv: source noun");
		}
		
		return sHelp;
	}
}
