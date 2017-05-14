README

We had the problem of a ton loads of media files (JPEG, MOV, MPEG, MP3, 3GPP, etc) just dumped in a
hard disk of 2 TB containing media files dating back from late 90s named in various ways per the
recording source and in several folders and sub-folders and sub-sub folders. Not only that there were
duplicates since we were paranoid that we would miss images and videos and ended up transferring into
this disk again and again taking up space. Really a dump of all the history in an unorganized way.
We just wanted to go through this disk and place them all in one flat folder location, rename them
with their date and time of creation, and also leave out any duplicates. That prompted me to develop
this simple java tool that does exactly does. It computes checksum of each of these files and uses
that to check for any duplicates. Walks through the directory tree of the given source folder and
fishes out only files and does the above flattening. I tried to make a swing based UI front-end too.
But that was not performing as I wanted. So this is a jar file that can be executed as follows :-|

A jar made out of this code will be invoked like this:

java -jar sid_media_organizer.jar /Volumes/EXTERNAL_DISK1/MEDIA/ /Volumes/EXTERNAL_DISK2/CONSOLIDATED_MEDIA

The above will take all files under the /Volumes/EXTERNAL_DISK1/MEDIA/ directory tree and copy them
under /Volumes/EXTERNAL_DISK2/CONSOLIDATED_MEDIA after renaming each file after its date and time of
creation. In the process it identifies duplicates and do not copy them again. For example, a JPEG
file DSC003.JPEG will become 2004_02_12_14_42_46_8f944148e941018c6d3c7204e56be681.JPG because it
was created at 14_42_46 on 2004_02_12. The "8f944148e941018c6d3c7204e56be681" part is the checksum
of the file and is helpful just in case there are two different files create at exactly same time.

Such naming helps in sorting the media files by their names easily which will also be chronological
sorting in a way. Also such flattening helped me plug the USB hard disk into a smart TV or media
player to watch the thousands of content easily whenever I want. Might need some more tweaking
to optimize and error handling etc. Some of the issues I faced are the macbook going to sleep whilst
taking the source and destination hard disks with it in the midst of transferring tons of media.
Needed to raise the sleep time. Also simple copy did not transfer the create time properly to the
new file. I had to copy first into a temp file and retrieve creation time from that temp file,
then move that file by renaming with the right creation time. I didn't quite get why it was
happening. It could be a bug in the way BasicAttributes are obtained in Java. I will investigate
when time permits. However, the tool does the job as expected for me. One other thing, if any of
your folder arguments contain spaces you need to escape the spaces as follows:

java -jar media_organizer.jar /Volumes/EXTERNAL_DISK1/MEDIA/ /Volumes/My\ Passport\ for\ Mac/CONSOLIDATED_MEDIA/

Compilation Note: if you try to compile and run, it may not work due to not being able to find the Apache codecs
required. So first be sure to install the required packages, preferably through a GUI such as Eclipse (it provides
the easiest way to accomplish this task).

Note: There is also a Apache tika based file type identifier capability in the class. But I did not use it.
That of course can be used to be choosy about what type of file (JPEG, PNG, MPEG, etc) to be actually transferred.

*** END ***
