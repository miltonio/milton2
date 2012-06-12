
(copied from MimeUtil project)

This is being released under the Apache Licence V2.


MimeUtil Mime Type Detection

This document explains how mime types are detected and reported by this utility.

A mime or "Multipurpose Internet Mail Extensions" type is an internet standard that is important outside of just e-mail use. 
Mime is used extensively in other communications protocols such as HTTP for web communications. 
IANA "Internet Assigned Numbers Authority" is responsible for the standardisation and publication of mime types. Basically any 
resource on any computer that can be located via a URI can be assigned a mime type. So for instance, JPEG images have a mime type 
of image/jpg. Some resources can have multiple mime types associated with them such as files with an XML extension have the mime types 
text/xml and application/xml. The list of mime types is quite extensive but if you should require a new mime type you can always 
specify your own. Obviously if you transmit this information specifying your own mime types others around the world would not know 
what the information should represent, but you would be able to create clients and applications that handle resources with these 
specific mime type in-house. 

Anyway, mime types have been around on the internet nearly as long as the internet its self and because of the length of time many 
mime types have been standardised such that applications can be written to understand and handle information transmitted over the 
web in specific standardised ways. For instance HTML pages are transmitted with a mime type of text/html and browsers know what 
to do with this type of information.

Mostly mime types for files can be assumed based on their file extension such as files ending in .jar are assigned the mime type 
application/java-archive but some files do not have extensions and this association is then not possible. In these cases an 
alternative to mime type association by file extension is needed. Luckily within the Unix world file type detection using magic 
numbers has been around for quite some time, even before the internet. This technique peaks inside files to known offsets and 
compares values. It does this based on a set of rules contained in a file called magic located on the Unix system. 
The Unix file command uses this file, or a compiled version of it, to determine information about the file. It recursively passes 
the file through the rules defined in the magic file until a match is found. However, the magic file itself does not contain mime type 
information so Unix again comes to the rescue with an alternative file called magic.mime which has the exact same rule set as the 
magic file but can guess the mime types of the files based on the magic rule set. The downside of this detection method is that 
its much slower than association by file extension. Mime type detection is not guaranteed to be accurate. It is a best guess 
approach where speed should be of greater consideration than accuracy. So this mime utility uses the two techniques described 
above and in the order described. 

Firstly it will get a files extension (if it has one) and check this against a list of registered extensions within the MimeUtil class. 
If it finds a match for this extension it will return this value. Obviously the mime utility must have a list of mappings between 
file extension and mime types for this to work. Fortunately we compiled a relatively large list of mappings into a java properties file 
from information gleaned from many sites on the internet. This file resides in the eu.medsea.util package and has the name mime-types.properties. 
This is not a complete or exhaustive list as that would have proven too difficult to compile for this project. So instead we give 
you the opportunity to extend these existing mappings for yourself. You can create your own mime-types.properties file and 
place this on your classpath. The mime utility will then locate this file and use it to add new mappings or override existing mappings 
for file extensions. So for instance, if you are not happy with the mime types that we associated with the file extension "mp3" then 
you can change the existing list "audio/mpeg,audio/mpeg3,audio/x-mpeg-3,video/mpeg,video/x-mpeg" to your preference. Obviously this 
mechanism is easily fooled, for instance, if you change the extension of a file from .xml to .gif it will be associated with the .gif 
mime types. Once a match is found based on the file extension no further analysis of a file is done.

Secondly, if we are unable to determine the mime type from the file extension described above because for instance the file does not 
have a file extension OR the file extension it does have is not listed in the mime-types.properties provided internally to the utility 
and you have not created a mapping of your own in your customised mime-types.properties file located on your classpath, then the mime 
utility will fall back to the Unix magic.mime file processing. If you are not using a Unix system or your system does not have a 
magic.mime file, don’t worry, we have included a copy within the project and if we can't locate the file on your system we will use 
the internal version. This mechanism is much more processor intensive than the first step of file extension association so it is 
much better for you and your systems performance if you can define you previously unknown file extensions in your extended 
mime-types.properties file. However, this is not always possible. In one particular case we had a CMS (Content Management System) 
that stored all of its images within a directory structure but renames them all with the .img extension no matter what the original 
extension or format was. So it would not be possible to say that .img files were really .jpg files as even though this was correct in 
the majority of cases, things fell apart when it was not a JPEP image but rather a GIF image or even worse an SVG (xml format) image. 
In these cases and in the other case where a file has no extension at all we use the magic number mechanism.

Magic numbers are information contained in a file that identifies this file as being of a particular type. Again this is not always 
a precise science, just a best guess according to rules configured in the magic.mime file. However, Almost all file types have some 
kind of header or marker that identifies them as being of a specific type of content. These can be values at a well known offset 
within a file or a relative offset defining where to find other information within the file that can identify it. 
A full description of magic numbers is beyond the scope of this document so if you want to know more about them then take google for 
a ride. The mime utility takes the magic.mime file, compiles a recursive decent tree from the contents and then stores this internally 
for content comparison. Each file passed along to this part of the detection process is tested against each condition in turn until 
either a match is found or the end of the conditions is reached. If a condition matches and it has sub-conditions these are also then 
tested and only if all sub-conditions match is a test assumed to be passed. In the case of a match the mime type is returned. 
In the case of the magic.mime file only a single mime type seems to ever be returned, this does not have to be the case and we 
will see how to change this in a moment. If no matches were found then the mime utility returns a custom mime type i.e. one that 
we made up and is not registered with the IANA of "application/x-unknown-mime-type". The reason we did this instead of just returning 
the more common "application/octet-stream" was to be able to distinguish a "no match" from content that actually matches and returns 
the mime type "application/octet-stream" from the magic filter processing steps. The other mime type that we use extensively is 
"application/directory" again this is not a standard mime type but is apparently used and agreed by many people on the internet 
and may yet be standardised in the future, so we decided this was OK to use.

We have provided an extension capability to the magic.mime file as well as the mime-types.properties file. You just create your 
own magic.mime file using the format defined by the Unix magic.mime file and place this on your classpath along with your extention 
to the mime-types.properties file and the mime utility will locate and load it. A description of this format is beyond the scope of 
this document so check it out on the net by looking up the Unix file command. This means that you can define your own rules as well 
as fix any broken rules you may find and add any missing rules where you can determine a files type from well known information 
contained at various locations within the file. We have provided one extension to the magic.mime processing that is not supported 
by the Unix file command, and you can use this extension in your magic.mime file rules. DO NOT modify the internal copy of 
magic.mime or the original magic.mime file you find located on your Unix system.

This rule extension to the string type allows you to match strings in a file where you do not know the actual offset of the string 
but can only say something like “it will be ‘somewhere’ within the next 1024 characters” from this location. 
This is an important improvement to the string matching rules especially for text based documents such as HTML and XML formats. 
The reasoning for this was that the rules for matching SVG images defined in the original magic.mime file hardly ever worked, this is 
because of the fixed offset definitions within the magic rule format. As XML documents generally have an XML declaration that can 
contain various optional attributes the length of this header cannot be determined, therefore we cannot know that the DOCTYPE 
declaration for an SVG xml file starts at “this” location, all we can say is that, if this is an SVG xml file then it will have 
an SVG DOCTYPE somewhere near the beginning of the file and probably within the first 1024 characters. So we test for the xml 
declaration and then we test for the DOCTYPE within a specified number of characters and if found then we match this rule. 
This extension can be used to better identify ALL of the XML type mime mapping in the current magic.mime file.

An example of the PNG and SVG rules added to our classpath located magic.mime file. The PNG rule overrides the original PNG rule 
defined in the magic.mime file we took from the internet, and the SVG rule overrides the SVG detection also defined in the original
magic.mime file

#PNG Image Format
0		string		\211PNG\r\n\032\n		image/png

#SVG Image Format
#	We know its an XML file so it should start with an XML declaration.
0	string	\<?xml\ version=	text/xml
#	As the XML declaration in an XML file can be short or extended we cannot know
#	exactly where the declaration ends i.e. how long it is, 
#	also it could be terminated with by new line(s) or a space(s). 
#	So the next line states that somewhere after the 15th caracter position we should find the DOCTYPE declaration.
#	This DOCTYPE declaration should be within 1024 characters from the 15th character 
>15	string>1024<	\<!DOCTYPE\ svg\ PUBLIC\ "-//W3C//DTD\ SVG 	image/svg+xml

As you can see the extension is defined using the syntax string>bufsize<. It can only be used on a the string type and basically 
means match this within bufsize character from the position defined at the begining of the line. 


The extended mime-types.properties and magic.mime files we use can be located in the conf directory of this distribution.
The MimeUtil class
