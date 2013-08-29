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

package com.dubsar_dictionary.Dubsar.model;

import java.util.ArrayList;

import android.content.Intent;

/**
 * Basic stack for forward navigation
 */
public class ForwardStack extends ArrayList<Intent> {

	/**
	 * Generated by Eclipse
	 */
	private static final long serialVersionUID = -6397448552694882061L;

	/**
	 * Examine the top of the stack without modifying it
	 * @return the Intent at the top of the stack
	 */
	public Intent peek() {
		if (size() == 0) return null;
		
		return get(size()-1);
	}
	
	/**
	 * Pop the top Intent off the stack
	 * @return the Intent at the top of the stack
	 * @throws IndexOutOfBoundsException if the stack is empty
	 */
	public Intent pop() {		
		return remove(size()-1);
	}
	
	/**
	 * Push a new Intent on the top of the stack
	 * @param intent the new Intent
	 */
	public void push(Intent intent) {
		add(intent);
	}
}
