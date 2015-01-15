/*
 Dubsar Dictionary Project
 Copyright (C) 2010-15 Jimmy Dee
 
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

import junit.framework.TestCase;
import android.content.Intent;

import com.dubsar_dictionary.Dubsar.model.ForwardStack;

public class ForwardStackTest extends TestCase {

	Intent mIntent1;
	Intent mIntent2;
	Intent mIntent3;
	
	ForwardStack mForwardStack = new ForwardStack();
	
	protected void setUp() {
		mIntent1 = new Intent();
		mIntent2 = new Intent();
		mIntent3 = new Intent();
		
		mForwardStack.push(mIntent1);
		mForwardStack.push(mIntent2);
	}
	
	public void testPeek() {
		assertEquals(2, mForwardStack.size());
		assertEquals(mIntent2, mForwardStack.peek());
		assertEquals(2, mForwardStack.size());
	}
	
	public void testPop() {
		assertEquals(2, mForwardStack.size());
		assertEquals(mIntent2, mForwardStack.pop());
		assertEquals(mIntent1, mForwardStack.peek());
		assertEquals(1, mForwardStack.size());
	}
	
	public void testPush() {
		mForwardStack.push(mIntent3);
		
		assertEquals(mIntent3, mForwardStack.peek());
	}
	
	public void testEmptyPeek() {
		mForwardStack.clear();
		
		assertNull(mForwardStack.peek());
	}
	
	public void testEmptyPop() {
		mForwardStack.clear();
		
		try {
			mForwardStack.pop();
		}
		catch (IndexOutOfBoundsException e) {
			// success
			return;
		}
		
		fail("pop() should throw an IndexOutOfBoundsException when isEmpty()");
	}
}
