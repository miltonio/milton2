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

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package io.milton.common;

/**
 * Represents a string object that contains an extra information of language. 
 * 
 * @author nabil.shams
 */
public class InternationalizedString{
        private String language;
        private String value;
        public InternationalizedString(String language, String value){
            this.language = language;
            this.value = value;
        }
        
        public String getLanguage(){ return language;}
        public String getValue() {return value;}
        
    }
