/*
 * Copyright 2012 McEvoy Software Ltd.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package io.milton.http.values;

/**
 * This class exists to convey type information even when a value is null.
 *
 * This is important because we will often want to select parses and formatters
 * based on knowledge of the type of the value, even when that value is null.
 * 
 *
 * @author brad
 */
public class ValueAndType {

	private final Object value;
	private final Class type;

	public ValueAndType(Object value, Class type) {
		if (type == null) {
			throw new IllegalArgumentException("type may not be null");
		}
		if (value != null) {
			if (value.getClass() != type) {
				throw new RuntimeException("Inconsistent type information: " + value + " != " + type);
			}
		}
		this.value = value;
		this.type = type;
	}

	public Class getType() {
		return type;
	}

	public Object getValue() {
		return value;
	}
}
