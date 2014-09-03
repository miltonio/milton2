package com.mycompany;

import io.milton.http.exceptions.BadRequestException;
import io.milton.http.exceptions.NotAuthorizedException;
import io.milton.httpclient.File;
import io.milton.httpclient.Folder;
import io.milton.httpclient.Host;
import io.milton.httpclient.HttpException;
import io.milton.httpclient.Resource;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;
import junit.framework.TestCase;

/**
 * Runnable class which shows how to use the client API
 *
 * @author brad
 */
public class ClientExample extends TestCase {

    Host h;

    @Override
    protected void setUp() throws Exception {
        // Create a host to the local server with a root path of /p/userA
        h = new Host("localhost", "/p/userA", 8080, "userA", "password", null, 60000, null, null);

        // uses digest by default, uncomment this to use basic
        //h.setUseDigestForPreemptiveAuth(false);
    }

    public void testSomeStuff() throws Exception {
        // Frist, lets list the current directory
        showDirectory(h);

        // Check if our test folder exists, and delete if it does
        Folder existingFolder = (Folder) h.child("newFolder");
        if (existingFolder != null) {
            System.out.println("newFolder does exist, so delete it...");
            existingFolder.delete();
            System.out.println("... done");
        } else {
            System.out.println("newFolder does not exist, lets create it...");
        }

        // Now create a folder
        System.out.println("create newFolder");
        Folder newFolder = h.createFolder("newFolder");

        // And upload something to it
        String helloWorld = "Hello world!!!";
        byte[] arr = helloWorld.getBytes("UTF-8");
        ByteArrayInputStream bin = new ByteArrayInputStream(arr);
        File newFile = newFolder.upload("hello-world.txt", bin, (long) arr.length, "text/plain", null, null);
        System.out.println("Done upload, new directory:");
        showDirectory(newFolder);

        // Update the new file
        helloWorld = "Hello world222!!!";
        arr = helloWorld.getBytes("UTF-8");
        bin = new ByteArrayInputStream(arr);
        System.out.println("Now update the file..");
        newFile.setContent(bin, (long) arr.length, null);
        System.out.println("..Done update");
        showDirectory(newFolder);

        System.out.println("Try to overwrite it without etag, should fail...");
        try {
            bin = new ByteArrayInputStream(arr);
            newFolder.upload("hello-world.txt", bin, (long) arr.length, "text/plain", null, null);
            throw new RuntimeException("Upload should have failed!!");
        } catch (Exception e) {
            System.out.println("..Overwrite failed as expected");
        }

        // Now copy it...
        newFile.copyTo(newFolder, "hello2.txt");
        File copiedFile = (File) newFolder.child("hello2.txt");
        System.out.println("Copied to: " + copiedFile.href());
        showDirectory(newFolder);

        // Delete the original uploaded file
        newFile.delete();

        // And list the new directory
        System.out.println("Done delete, new listing");
        showDirectory(newFolder);
    }

    private void showDirectory(Folder f) throws IOException, HttpException, NotAuthorizedException, BadRequestException {
        List<? extends Resource> children = f.children();
        System.out.println("Directory: " + f.href());
        for (Resource r : children) {
            if (r instanceof Folder) {
                Folder childFolder = (Folder) r;
                System.out.println(" - " + childFolder.name);
            }
        }
        for (Resource r : children) {
            if (r instanceof File) {
                File childFile = (File) r;
                System.out.println(" - " + childFile.name + " size: " + childFile.contentLength + " type: " + childFile.contentType);
            }
        }
    }
}
