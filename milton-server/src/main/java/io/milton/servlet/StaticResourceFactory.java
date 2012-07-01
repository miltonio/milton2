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

package io.milton.servlet;

import io.milton.common.ContentTypeUtils;
import io.milton.http.HttpManager;
import io.milton.http.ResourceFactory;
import io.milton.resource.Resource;
import java.io.File;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This can either be initialised with the old servlet/ApplicationConfig/Initable
 * approach, or with directly setting constructor args for the web context
 * and file root.
 *
 * If a URL resolves to a file (not a directory) this ResourceFactory will return
 * a new StaticResource which will server the file content
 *
 * @author brad
 */
public class StaticResourceFactory implements ResourceFactory, Initable {

    private static final Logger log = LoggerFactory.getLogger( StaticResourceFactory.class );
    /**
     * either this or root will be set
     */
    private Config config;
    /**
     * either this or config will be set
     */
    private File root;
    private String contextPath;

    public StaticResourceFactory() {
    }

    public StaticResourceFactory( Config config ) {
        this.config = config;
    }

    public StaticResourceFactory( String context, File root ) {
        this.root = root;
        this.contextPath = context;
        log.debug( "root: " + root.getAbsolutePath() + " - context:" + context );
    }

	@Override
    public void init( Config config, HttpManager manager ) {
        this.config = config;
    }

	@Override
    public Resource getResource( String host, String url ) {
        File file;
        if( root != null ) {
            // strip the context
            log.debug( "url: " + url );
            String s = stripContext( url );
            log.debug( "url: " + s );
            file = new File( root, s );
        } else {
            if( config == null )
                throw new RuntimeException( "ResourceFactory was not configured. ApplicationConfig is null" );
            if( config.getServletContext() == null )
                throw new NullPointerException( "config.servletContext is null" );
            String path = "WEB-INF/static" + url;
            path = config.getServletContext().getRealPath( path );
            file = new File( path );
        }

        if( file.exists() && !file.isDirectory() ) {
            String contentType;
            if( config != null ) {
                contentType = MiltonUtils.getContentType( config.getServletContext(), file.getName() );
            } else {
                contentType = ContentTypeUtils.findContentTypes( file );
            }
            return new StaticResource( file, url, contentType );
        } else {
            return null;
        }

    }

    private String stripContext( String url ) {
        if( this.contextPath != null && contextPath.length() > 0 ) {
            url = url.replaceFirst( '/' + contextPath, "" );
            log.debug( "stripped context: " + url );
            return url;
        } else {
            return url;
        }
    }

	@Override
    public void destroy( HttpManager manager ) {
    }

    public String getSupportedLevels() {
        return "1,2";
    }
}
