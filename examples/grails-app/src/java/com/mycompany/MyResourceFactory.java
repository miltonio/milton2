package com.mycompany;


import io.milton.http.ResourceFactory;
import io.milton.http.exceptions.BadRequestException;
import io.milton.http.exceptions.NotAuthorizedException;
import io.milton.http.fs.FileSystemResourceFactory;
import io.milton.resource.Resource;

/**
 *
 * @author brad
 */
public class MyResourceFactory  implements ResourceFactory{

    private FileSystemResourceFactory fileSystemResourceFactory = new FileSystemResourceFactory();

    
    
    @Override
    public Resource getResource(String host, String path) throws NotAuthorizedException, BadRequestException {
        return fileSystemResourceFactory.getResource(host, path);
    }

    public FileSystemResourceFactory getFileSystemResourceFactory() {
        return fileSystemResourceFactory;
    }

    public void setFileSystemResourceFactory(FileSystemResourceFactory fileSystemResourceFactory) {
        this.fileSystemResourceFactory = fileSystemResourceFactory;
    }

    
}
