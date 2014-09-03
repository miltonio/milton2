/*
 * Copyright 2013 McEvoy Software Ltd.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package io.milton.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a method as one which creates a child resource containing the given content,
 * or replaces an existing child's content with new data<br/><br/>
 * 
 * <p>Return type - Must return the created POJO object, or the updated child
 * 
 * <p>Parameters:
 * <ol>
 *  <li>first arg must be the object to update OR..</li>
 *  <li>the parent object, followed by the name of the child resource</li> 
 *  <li>items following that may include (in preferential order)
 *      <ol>
 *      <li>inputStream or byte[]</li>
 *      <li>contentType</li>
 *      <li>content length as Long</li>
 *     </ol>
 * </li>
 * </ol>
 * 
 * <p>
 * Example: Creating a new child resource
 * 
 * <pre>
 *    {@literal @}PutChild
 *    public MyDatabase.FileContentItem createFile(MyDatabase.FolderContentItem parent, String name, byte[] bytes) {
 *        FileContentItem file = parent.addFile(name);
 *        file.setBytes(bytes);
 *        return file;
 *    }
 * </pre>
 *
 *
 * <p>
 * Example: updating an existing resource
 * <pre>
 *    {@literal @}PutChild
 *    public Image uploadImage(Image image, byte[] bytes) throws IOException {
 *        File fRoot = getContentRoot();
 *        File content = new File(fRoot, image.getFileName());
 *        FileUtils.writeByteArrayToFile(content, bytes);
 *        return image;
 *    }
 * </pre>
 * 
 * <p>
 * Example: Creating a new child resource from an inputstream. This uses the contentLength and contentType
 * headers, but please note these are not always sent by client apps.
 * 
 * <pre>
 *    {@literal @}PutChild
 *    public MyDatabase.FileContentItem createFile(MyDatabase.FolderContentItem parent, String name, InputStream in, Long contentLength, String contentType) {
 *      ...
 *    }
 * </pre>
 *
 * @author brad
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface PutChild {
    
}
