/*
 * Copyright <2017> <Sidharth Rajaram>
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without 
 * limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of 
 * the Software, and to permit persons to whom the Software is furnished to do so, subject to the following 
 * conditions:

 * The above copyright notice and this permission notice shall be included in all copies or substantial portions
 * of the Software.

 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED
 * TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL
 * THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF 
 * ONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS
 * IN THE SOFTWARE.
 */
package media_organizer;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import static java.nio.file.StandardCopyOption.*;

import java.util.ArrayList;
import java.util.Iterator;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.tika.Tika;


public class MediaInspector {

	private Tika tika;
	private Path topStartDirectory;
	private Path destDirectory;
	private Path tempDirectory;
	private File tmp_directory;
	private ArrayList<String> globalChecksumList;

	public MediaInspector(String starting_dir, String destination_dir) {
		this.tika = new Tika();
		topStartDirectory = Paths.get(starting_dir).normalize().toAbsolutePath();
		destDirectory = Paths.get(destination_dir).normalize().toAbsolutePath();
		String temp_dir_path = destination_dir + "/tmp_consolidator_dir/";
		tmp_directory = new File(temp_dir_path);
	    if (!tmp_directory.exists()){
	    	tmp_directory.mkdir();
	    }

		tempDirectory = Paths.get(temp_dir_path).normalize().toAbsolutePath();

		setGlobalChecksumList(new ArrayList<String>());
	}

	private static boolean symbolicLink(File file) throws IOException {
        if (file == null) {
            throw new NullPointerException("NULL file object!");
        }
        File canonicalFile;
        if (file.getParent() == null) {
            canonicalFile = file;
        } else {
            File canonicalDir = file.getParentFile().getCanonicalFile();
            canonicalFile = new File(canonicalDir, file.getName());
        }
        return !canonicalFile.getCanonicalFile().equals(canonicalFile.getAbsoluteFile());
    }

	private static boolean pureDirectory(File file) throws IOException {
        return file.isDirectory() && !symbolicLink(file);
    }

    @SuppressWarnings("unchecked")
	public void unique_file_generator() {
            Iterator<File> iter = FileUtils.iterateFilesAndDirs(topStartDirectory.toFile(), TrueFileFilter.INSTANCE,
                                                                new IOFileFilter() {
                                                                    @Override
                                                                    public boolean accept(File file) {
                                                                        try {
                                                                            return pureDirectory(file);
                                                                        } catch (IOException ex) {
                                                                            return false;
                                                                        }
                                                                    }

                                                                    @Override
                                                                    public boolean accept(File dir, String name) {
                                                                        try {
                                                                            return pureDirectory(dir);
                                                                        } catch (IOException ex) {
                                                                            return false;
                                                                        }
                                                                    }
            });

            File n;
            try {
                while (iter.hasNext()) {
                    n = iter.next();
                	
                    if (!pureDirectory(n)) {
                    	if (n.getAbsolutePath().contains("DS_Store")){
                    		continue;
                    	}
                        String cksm = getChecksum(n, false);
                        if (!globalChecksumList.contains(cksm)) {
                        	globalChecksumList.add(cksm);
                        	// Rename or copy file into the new name and store the file path in list
                        	String create_time_str = get_file_creation_time(n);
                        	String temp_name = create_time_str + "_" + cksm + "." + FilenameUtils.getExtension(n.getAbsolutePath());
                        	File tempFile = new File(tempDirectory.toString() + "/" + temp_name);
                        	System.out.println("Copying " + n.getAbsolutePath() + " to temp location as " + tempFile.getAbsolutePath());
                        	Files.copy(n.toPath(), tempFile.toPath(), COPY_ATTRIBUTES);
                        	
                        	String new_create_time_str = get_file_creation_time(tempFile);
                        	String new_name = new_create_time_str + "_" + cksm + "." + FilenameUtils.getExtension(n.getAbsolutePath());
                        	File newFile = new File(destDirectory.toString() + "/" + new_name);
                        	System.out.println("Moving " + tempFile.getAbsolutePath() + " as " + newFile.getAbsolutePath());
                        	Files.move(tempFile.toPath(), newFile.toPath(), ATOMIC_MOVE);
                        } else {
                        	System.out.println("\nSkipping Duplicate file: " + n.getName() + "\n");
                        }
                    }
                }
                System.out.println("\n");
            } catch (IOException ex) {
            	System.out.format(ex.getMessage());
            } finally {
            	if (tmp_directory.exists()){
        	    	tmp_directory.delete();
        	    }
            }
        }
 
    public String getChecksum(File f, Boolean useSha) {
    	FileInputStream is = null;
    	String checksumValue = null;
    	try {
			 is = new FileInputStream(f);
			 if (useSha) {
				 checksumValue = DigestUtils.sha1Hex(is);
			 } else {
				 checksumValue = DigestUtils.md5Hex(is);
			 }
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				is.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return checksumValue;
    }

    public String get_file_type(File file) {
		// Find type of file		
		String mimeType = null;
		try {
			mimeType = tika.detect(file);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return mimeType;
	}
    
	public String get_file_type(String file_path_name) {
		// Find type of file		
		File file = new File(file_path_name);
		String mimeType = null;
		try {
			mimeType = tika.detect(file);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return mimeType;
	}

	public String get_file_creation_time(File file) throws IOException {
		// Find file creation time , etc
		Path f = file.toPath();
		BasicFileAttributes attr;
		String fileCreationTime = null;
		try {
			attr = Files.readAttributes(f, BasicFileAttributes.class);
			fileCreationTime = attr.creationTime().toString();
			//System.out.println("create time: " + attr.creationTime());
			return fileCreationTime.replaceAll("[-:T.]", "_").replaceAll("[Z]", "");

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw e;
		}
	}
	
	@SuppressWarnings("unchecked")
	public ArrayList<String> getGlobalChecksumList() {
		return globalChecksumList;
	}

	public void setGlobalChecksumList(ArrayList<String> globalChecksumList) {
		this.globalChecksumList = globalChecksumList;
	}


    public static void main(String args[])
    {
    	if (args.length < 2) {
    		System.out.println("USAGE: java -jar media_organizer.jar source_folder_name destination_folder_name");
    		return;
    	}
    	
    	MediaInspector mi = new MediaInspector(args[0], args[1]);
    	mi.unique_file_generator();
    }
}
