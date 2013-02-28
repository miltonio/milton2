package io.milton.common;

import java.util.*;

import junit.framework.TestCase;

/**
 *
 * @author brad
 */
public class DefaultContentTypeServiceTest extends TestCase {

    DefaultContentTypeService contentTypeService;
    Map<String, List<String>> mapOfContentTypes;
    
    @Override
    protected void setUp() throws Exception {
        mapOfContentTypes = new HashMap<String, List<String>>();
        mapOfContentTypes.put("html", ContentTypeUtils.toList("text/html"));
        mapOfContentTypes.put("aiff", ContentTypeUtils.toList("audio/x-aiff,audio/aiff"));
        
        contentTypeService = new DefaultContentTypeService(mapOfContentTypes);
    }
        

    public void testFindContentTypes_Single() {
        List<String> list = contentTypeService.findContentTypes("x.html");
        assertNotNull(list);
        assertEquals(1, list.size());
        assertEquals("text/html", list.get(0));
    }

    public void testFindContentTypes_None() {
        List<String> list = contentTypeService.findContentTypes("x.XXX");
        assertNull(list);
    }
    
    public void testFindContentTypes_Multi() {
        List<String> list = contentTypeService.findContentTypes("x.aiff");
        assertNotNull(list);
        assertEquals(2, list.size());
        assertEquals("audio/x-aiff", list.get(0));
        assertEquals("audio/aiff", list.get(1));
    }    
    
    
    /**
     * Test of getPreferedMimeType method, of class DefaultContentTypeService.
     */
    public void testGetPreferedMimeType_NoAccept() {
        List<String> list = ContentTypeUtils.toList("x,y");
        String ct = contentTypeService.getPreferedMimeType((String)null, list);
        assertEquals("x", ct);
    }
    
    public void testGetPreferedMimeType_NoCanProvide() {
        List<String> list = ContentTypeUtils.toList("x,y");
        String ct = contentTypeService.getPreferedMimeType("x", null);
        assertNull(ct);
    }
    
    public void testGetPreferedMimeType_MatchingSingle() {
        List<String> list = ContentTypeUtils.toList("x");
        String ct = contentTypeService.getPreferedMimeType("x", list);
        assertEquals("x", ct);
    }    

    public void testGetPreferedMimeType_MatchingFirst() {
        List<String> list = ContentTypeUtils.toList("x,y");
        String ct = contentTypeService.getPreferedMimeType("x", list);
        assertEquals("x", ct);
    }    

    public void testGetPreferedMimeType_MatchingLast() {
        List<String> list = ContentTypeUtils.toList("x,y");
        String ct = contentTypeService.getPreferedMimeType("y", list);
        assertEquals("y", ct);
    }    

    public void testGetPreferedMimeType_Ignores_QoP() {
        List<String> list = ContentTypeUtils.toList("x; q=0.4,y; q=0.5");
        String ct = contentTypeService.getPreferedMimeType("y", list);
        assertEquals("y", ct);
    }    
    
    public void testGetPreferedMimeType_MultiAcceptsFirst() {
        List<String> list = ContentTypeUtils.toList("x; q=0.4,y; q=0.5");
        List<String> accepts = ContentTypeUtils.toList("x, y");
        String ct = contentTypeService.getPreferedMimeType(accepts, list);
        assertEquals("x", ct);
    }        

    public void testGetPreferedMimeType_MultiAcceptsSecond() {
        List<String> list = ContentTypeUtils.toList("x; q=0.4,y; q=0.5");
        List<String> accepts = ContentTypeUtils.toList("a, y");
        String ct = contentTypeService.getPreferedMimeType(accepts, list);
        assertEquals("y", ct);
    }        
    
}
