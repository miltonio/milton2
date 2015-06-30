/*
 *
 * Copyright 2014 McEvoy Software Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.milton.principal;

import java.util.List;

/**
 *
 * @author brad
 */
public class PrincipalSearchCriteria {

    public enum TestType
    {
        ANY("anyof"),
        ALL("allof"),;

        private String code;

        private TestType(String code)
        {
            this.code = code;
        }

        public static TestType fromCode( String code )
        {
           TestType testType = ALL;
           if(code != null && code.equals(ANY.code))
              testType = ANY;
           return testType;
        }

        public String getCode()
        {
            return code;
        }
    }

    public enum MatchType {

        CONTAINS("contains"),
        EXACT("exact"),
        STARTSWITH("starts-with"),
        ENDSWITH("ends-with");

        String code;

        MatchType(String code) {
            this.code = code;
        }

        public String getCode() {
            return this.code;
        }

        public static MatchType fromCode( String code )
    		{
    			if ( code != null )
    			{
    				for ( MatchType enumm : MatchType.values() )
    				{
    					if ( enumm.name().equalsIgnoreCase( code ) || enumm.code.equalsIgnoreCase( code ) )
    					{
    						return enumm;
    					}
    				}
    			}
    			return null;
    		}
    }

    private TestType test;
    private String cuType; // eg INDIVIDUAL or GROUP
    private List<SearchItem> searchItems;

    /**
     * @return the test
     */
    public TestType getTest() {
        return test;
    }

    /**
     * @param test the test to set
     */
    public void setTest(TestType test) {
        this.test = test;
    }

    /**
     * @return the cuType
     */
    public String getCuType() {
        return cuType;
    }

    /**
     * @param cuType the cuType to set
     */
    public void setCuType(String cuType) {
        this.cuType = cuType;
    }

    /**
     * @return the searchItems
     */
    public List<SearchItem> getSearchItems() {
        return searchItems;
    }

    /**
     * @param searchItems the searchItems to set
     */
    public void setSearchItems(List<SearchItem> searchItems) {
        this.searchItems = searchItems;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("test=").append(test).append(", ");
        sb.append("cuType=").append(cuType).append(", ");
        sb.append("[");
        for (SearchItem item : searchItems) {
            sb.append(item.toString()).append(",");
        }
        sb.append("]");
        return sb.toString();
    }

    public static class SearchItem {

        private String field;
        private MatchType matchType;
        private String value;

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            if (matchType != null) {
                sb.append("matchType=").append(matchType);
            }
            if( value != null ) {
                sb.append("value=").append(value);
            }
            if ( field != null )
      			{
      				sb.append( field ).append( "," );
      			}
            return sb.toString();
        }

        /**
    		 * @return the fields
    		 */
    		public String getField()
    		{
    			return field;
    		}

    		/**
    		 * @param fields the fields to set
    		 */
    		public void setField( String fields )
    		{
    			this.field = fields;
    		}

        /**
         * @return the matchType
         */
        public MatchType getMatchType() {
            return matchType;
        }

        /**
         * @param matchType the matchType to set
         */
        public void setMatchType(MatchType matchType) {
            this.matchType = matchType;
        }

        /**
         * @return the value
         */
        public String getValue() {
            return value;
        }

        /**
         * @param value the value to set
         */
        public void setValue(String value) {
            this.value = value;
        }

    }
}
