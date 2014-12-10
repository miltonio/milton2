package io.milton.http.report;

import javax.xml.namespace.QName;

/**
 * Report with a qualified name so that reports can be implemented from
 *  namespace other than DAV:
 */
public interface QualifiedReport extends Report
{
    QName getQualifiedName();
}
