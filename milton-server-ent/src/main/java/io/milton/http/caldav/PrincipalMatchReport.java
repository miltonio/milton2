/*
 * Copyright 2012 McEvoy Software Ltd.
 *
 */

package io.milton.http.caldav;

import io.milton.http.report.Report;
import io.milton.resource.Resource;
import org.jdom.Document;

/**
 *
 * @author alex
 */
public class PrincipalMatchReport implements Report
{

  public String getName()
  {
    return "principal-match";
  }

  public String process(String host, String path, Resource r, Document doc)
  {
    throw new UnsupportedOperationException("Not supported yet.");
  }
}
