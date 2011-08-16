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

public class PointerDictionary {

	public static final String labelFromPtype(String ptype) {
		/*
		 * DEBT: Make these all constants or something. But as
		 * long as it's encapsulated in this class, who cares?
		 */
		
		if (ptype.equals("antonym")) {
			return "Antonyms";
		}
		else if (ptype.equals("hypernym")) {
			return "Hypernyms";
		}
		else if (ptype.equals("instance hypernym")) {
			return "Instance Hypernyms";
		}
		else if (ptype.equals("hyponym")) {
			return "Hyponyms";
		}
		else if (ptype.equals("instance hyponym")) {
			return "Instance Hyponyms";
		}
		else if (ptype.equals("member holonym")) {
			return "Member Holonyms";
		}
		else if (ptype.equals("substance holonym")) {
			return "Substance Holonyms";
		}
		else if (ptype.equals("part holonym")) {
			return "Part Holonyms";
		}
		else if (ptype.equals("member meronym")) {
			return "Member Meronyms";
		}
		else if (ptype.equals("substance meronym")) {
			return "Substance Meronyms";
		}
		else if (ptype.equals("part meronym")) {
			return "Part Meronyms";
		}
		else if (ptype.equals("attribute")) {
			return "Attributes";
		}
		else if (ptype.equals("derivationally related form")) {
			return "Derivationally Related Forms";
		}
		else if (ptype.equals("domain of synset (topic)")) {
			return "Domain of Synset (Topic)";
		}
		else if (ptype.equals("member of this domain (topic)")) {
			return "Members of this Domain (Topic)";
		}
		else if (ptype.equals("domain of synset (region)")) {
			return "Domain of Synset (Region)";
		}
		else if (ptype.equals("member of this domain (region)")) {
			return "Members of this Domain (Region)";
		}
		else if (ptype.equals("domain of synset (usage)")) {
			return "Domain of Synset (Usage)";
		}
		else if (ptype.equals("member of this domain (usage)")) {
			return "Members of this Domain (Usage)";
		}
		else if (ptype.equals("entailment")) {
			return "Entailments";
		}
		else if (ptype.equals("cause")) {
			return "Causes";
		}
		else if (ptype.equals("also see")) {
			return "Also See";
		}
		else if (ptype.equals("verb group")) {
			return "Verb Group";
		}
		else if (ptype.equals("similar to")) {
			return "Similar to";
		}
		else if (ptype.equals("participle of verb")) {
			return "Participle of Verb";
		}
		else if (ptype.equals("derived from/pertains to")) {
			return "Derived from/Pertains to";
		}
		
		return "";
	}
}
