package io.milton.http.annotated;

/**
 *
 * @author brad
 */
public class SpecificityUtils {
	public static int sourceSpecifityIndex(Class methodType, Class actualType) {
		Class c = actualType;
		int i = 0;
		while( !methodType.equals(c)) {
			c = c.getSuperclass();
			i++;
		}
		return 100 - i;
	}
}
