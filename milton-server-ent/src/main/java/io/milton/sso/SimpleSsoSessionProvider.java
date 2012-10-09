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
package io.milton.sso;

/**
 * Just for testing and debugging, this uses a constant string as the "session
 * id"
 *
 * @author brad
 */
public class SimpleSsoSessionProvider implements SsoSessionProvider {

    private String prefix;

    @Override
    public Object getUserTag(String firstComp) {
        if (firstComp != null && firstComp.equals(prefix)) {
            return "aUser";
        } else {
            return null;
        }
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }
}
