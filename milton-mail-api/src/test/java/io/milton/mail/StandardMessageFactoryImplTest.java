package io.milton.mail;

import io.milton.common.ReadingException;
import io.milton.common.StreamUtils;
import io.milton.common.WritingException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;
import junit.framework.TestCase;

/**
 *
 * @author brad
 */
public class StandardMessageFactoryImplTest extends TestCase {

    Session session;
    StandardMessageFactoryImpl factory;

    public StandardMessageFactoryImplTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        factory = new StandardMessageFactoryImpl();
        session = Session.getDefaultInstance(new Properties());
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testSimpleText() throws MessagingException, IOException {
        InputStream in = this.getClass().getResourceAsStream("simple-text.smtp");
        assertNotNull(in);
        MimeMessage mm = new MimeMessage(null, in);
        StandardMessage sm = new StandardMessageImpl();
        factory.toStandardMessage(mm,sm);
        assertEquals("simple message", sm.getSubject());
        assertEquals("text content", sm.getText());

        mm = factory.toMimeMessage(sm, session);
        assertEquals("simple message", mm.getSubject());
        assertNotNull(mm.getAllRecipients());
        assertEquals(1, mm.getAllRecipients().length);
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        mm.writeTo(bout);
        System.out.println("reconstructed simple message size: " + bout.size());
        System.out.println(bout.toString());
    }

    public void atestSimpleHtml() throws Exception {
        InputStream in = this.getClass().getResourceAsStream("simple-html.smtp");
        assertNotNull(in);
        MimeMessage mm = new MimeMessage(null, in);
        StandardMessage sm = new StandardMessageImpl();
        factory.toStandardMessage(mm,sm);
        assertEquals("html message", sm.getSubject());
        assertEquals("html content", sm.getText());
        assertTrue(sm.getHtml().contains("<STRONG>content</STRONG>"));

        mm = factory.toMimeMessage(sm, session);
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        mm.writeTo(bout);
        System.out.println("bout: " + bout.size());
        System.out.println(bout.toString());
    }

    public void atestForwardedWithAttach() throws Exception {
        InputStream in = this.getClass().getResourceAsStream("forward-with-attach.smtp");
        assertNotNull(in);
        MimeMessage mm = new MimeMessage(null, in);
        StandardMessage sm = new StandardMessageImpl();
        factory.toStandardMessage(mm,sm);
        assertEquals("Fw: test4", sm.getSubject());
        //assertEquals(1, sm.getAttachedMessages().size());
//        System.out.println("sub messages: " + sm.getAttachedMessages().size());
//        for( StandardMessage smChild : sm.getAttachedMessages() ) {
//            System.out.println("::html: " + smChild.getHtml());
//            System.out.println("::text: " + smChild.getText());
//            System.out.println("..");
//        }
//        System.out.println("-----------");
        assertEquals(1, sm.getAttachments().size());
        for( Attachment att : sm.getAttachments() ) {
            System.out.println( att.getName() + " - " + att.size() );
            att.useData(new InputStreamConsumer() {

                public void execute(InputStream in) {
                    ByteArrayOutputStream bout = new ByteArrayOutputStream();
                    try {
                        StreamUtils.readTo(in, bout);
                    } catch (ReadingException ex) {
                        Logger.getLogger(StandardMessageFactoryImplTest.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (WritingException ex) {
                        Logger.getLogger(StandardMessageFactoryImplTest.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    //System.out.println(bout.toString());
                }
            });
        }

        mm = factory.toMimeMessage(sm, session);
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        mm.writeTo(bout);
        System.out.println("bout: " + bout.size());

    }

    public void atestHtmlWithImage() throws Exception {
        System.out.println("---------------- testHtmlWithImage --------------");
        InputStream in = this.getClass().getResourceAsStream("html-image.smtp");
        assertNotNull(in);
        MimeMessage mm = new MimeMessage(null, in);
        StandardMessage sm = new StandardMessageImpl();
        factory.toStandardMessage(mm,sm);
        assertEquals("html with images", sm.getSubject());
        assertNotNull(sm.getHtml());
        assertTrue(sm.getHtml().length() > 0);
        assertNotNull(sm.getText());
        assertTrue(sm.getText().length() > 0);
        assertNotNull(sm.getAttachments());
        assertEquals(1, sm.getAttachments().size());
        Attachment att = sm.getAttachments().get(0);
        assertNotNull(att.getContentId());
        assertEquals("<0FB995E5A5A642018247136B06623E43@bradsalien>", att.getContentId());
        assertTrue(att.getContentType().contains("image/jpeg"));

        mm = factory.toMimeMessage(sm, session);
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        mm.writeTo(bout);
        //System.out.println(bout.toString());
    }
}
