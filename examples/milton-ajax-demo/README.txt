Milton Ajax Demo

This runnable project demonstrates doing a PROPFIND and a PUT using AJAX.

Over time it should be expanded to show PROPPATCH, DELETE and other methods.

Note that this project used milton-filesystem for its resources, so by default
you will be viewing and potentially modifying files in your home directory.

This project uses the dojo toolkit for its ajax support, but the milton ajax
gateway doesnt depend on this explicitly. It will work with any ajax library.

To use it:
1. run with this maven cmd:
  mvn jetty:run

2. open your web browser at this address:
  http://127.0.0.1:8095/webdav/cms.html

3. Press the 'show' button. This will execute an AJAX request to list the contents
of the root of the webdav share and will display the results on the page. You
can enter a different URL in the text box to list some other directory

4. Select a file to upload with the browse button. Use the 'submit' button to upload
the file using a regular form post, but with the form action changed to PUT. The
'upload' button shows the alternative approach using dojo, but which doesnt quite
work yet