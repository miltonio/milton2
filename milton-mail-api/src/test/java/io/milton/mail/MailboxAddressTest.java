package io.milton.mail;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 *
 * @author brad
 */
public class MailboxAddressTest {
    

    @Test
    public void testParse_NoPersonal() {
        MailboxAddress ma = MailboxAddress.parse("abc@def.com");
        assertEquals("abc", ma.user);
        assertEquals("def.com", ma.domain);
        assertNull(ma.personal);
    }

    // todo
//    public void testParse_WithPersonal() {
//        MailboxAddress ma = MailboxAddress.parse("\"joker\" <abc@def.com>");
//        assertEquals("abc", ma.user);
//        assertEquals("def.com", ma.domain);
//        assertEquals("joker", ma.personal);
//        assertNull(ma.personal);
//    }

    @Test
    public void testToString() {
        MailboxAddress ma;
        ma = new MailboxAddress("abc", "def.com");
        assertEquals("abc@def.com", ma.toString());

        ma = new MailboxAddress("abc", "def.com", "joker");
        assertEquals("\"joker\" <abc@def.com>", ma.toString());

    }

    @Test
    public void testParse_WithPersonal() {
        MailboxAddress ma = MailboxAddress.parse("\"John Wayne\" <everyone@www.aucklandcanoepolo.co.nz>");
        System.out.println("user: " + ma.user);
        System.out.println("domain: " + ma.domain);
        System.out.println("personal: " + ma.personal);
        assertEquals("everyone", ma.user);
        assertEquals("www.aucklandcanoepolo.co.nz", ma.domain);
        assertEquals("John Wayne", ma.personal);
        // round trip
        assertEquals("\"John Wayne\" <everyone@www.aucklandcanoepolo.co.nz>", ma.toString());
    }

    @Test
    public void testRemoveSurroundingDelimiters() {
        assertEquals("abc", MailboxAddress.removeSurroundingDelimiters("\"abc\"", "\"", "\""));
        assertEquals("abc", MailboxAddress.removeSurroundingDelimiters(" \"abc\"", "\"", "\""));
        assertEquals("abc", MailboxAddress.removeSurroundingDelimiters("\"abc\" ", "\"", "\""));
        assertEquals("abc", MailboxAddress.removeSurroundingDelimiters(" \"abc\" ", "\"", "\""));
        assertEquals("abc", MailboxAddress.removeSurroundingDelimiters("\"abc", "\"", "\""));
    }
}
