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

package io.milton.hashsplit;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import org.junit.*;
import static org.junit.Assert.*;

/**
 *
 * @author brad
 */
public class HashSplitServiceTest {

    HashSplitService service;
    
    //@Test
    public void testClientUploads() throws IOException {
        // Get the test data
        InputStream inOrig = this.getClass().getResourceAsStream("/hashsplit-original.txt");
        assertNotNull(inOrig);
        InputStream inMod = this.getClass().getResourceAsStream("/hashsplit-modified.txt");
        assertNotNull(inMod);
        
        // Parse the original
        List<HashNode> rootNodes = service.parse(inOrig);
        LocalHashNodeProvider hashNodeProvider = new LocalHashNodeProvider(rootNodes);
        HashSplitDeltaGenerator deltaGenerator = new HashSplitDeltaGenerator(hashNodeProvider);
        File dest = File.createTempFile("hashsplit-test", null);
        deltaGenerator.generateDeltas(inMod, dest);
        
        // so we should now have changed blocks in the "dest" file
        
    }

    @Before
    public void setUp() {
        service = new HashSplitService();
    }
    
    @After
    public void tearDown() {
    }

}
