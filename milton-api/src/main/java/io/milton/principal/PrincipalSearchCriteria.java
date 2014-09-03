package io.milton.principal;

import java.util.List;
import javax.xml.namespace.QName;

/**
 *
 * @author brad
 */
public class PrincipalSearchCriteria {

    public enum TestType {

        ANY,
        ALL
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
            return code;
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

        private List<QName> fields;
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
            if( fields != null ) {
                sb.append(" [");
                for( QName f : fields ) {
                    sb.append(f.toString()).append(",");
                }
                sb.append("]");
            }
            return sb.toString();
        }

        /**
         * @return the fields
         */
        public List<QName> getFields() {
            return fields;
        }

        /**
         * @param fields the fields to set
         */
        public void setFields(List<QName> fields) {
            this.fields = fields;
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
