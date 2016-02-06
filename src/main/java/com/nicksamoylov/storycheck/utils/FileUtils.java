package com.nicksamoylov.storycheck.utils;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.channels.FileChannel;
import java.security.SecureRandom;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.regex.Pattern;
import java.util.zip.ZipFile;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileSystemUtils;
import org.springframework.web.multipart.MultipartFile;

public class FileUtils {
	private static final Logging log = Logging.getLog(FileUtils.class);
	public static final String SUCCESS = "Success";
	public static final int BUFFER_SIZE = 8192;
    
    public static final FileFilter zipFileFilter =
        new FileFilter (){
            public boolean accept (File pathname){
                return pathname.getName ().toUpperCase ().endsWith (".ZIP");
            }
        };
	public static List<File> getFiles(File targetDir){
		ArrayList<File> fileList = new ArrayList<File>();
		getFiles(fileList, targetDir);
		return fileList == null? new ArrayList<File>(): fileList;
	}
	
	private static void getFiles(List<File> fileList, File targetDir) {
		if(targetDir.isDirectory() && targetDir.exists()){
			File[] childrenFiles = targetDir.listFiles();
			for(File cf : childrenFiles){
				if(cf.isDirectory()){
					getFiles(fileList, cf);
				} else{
					fileList.add(cf);
				}
			}			
		}
	}

	public static boolean fileExistsIgnoreCase(File f) {
		return findFileIgnoreCase(f.getParentFile(), f.getName()) != null;
	}

	public static File findFileIgnoreCase(File dir, String name) {
		File[] files = dir.listFiles();
		if (files != null) {
			for (int i = 0; i < files.length; i++) {
				if (name.equalsIgnoreCase(files[i].getName())) {
					return files[i];
				}
			}
		}
		return null;
	}

	public static void closeNoExceptions(InputStream in) {
		if (null == in) {
			return;
		}
		try {
			in.close();
		} catch (IOException e) {
			log.error(e.getMessage(), e);
		}
	}

	public static void closeNoExceptions(Reader reader) {
		if (null == reader) {
			return;
		}
		try {
			reader.close();
		} catch (IOException e) {
			log.error(e.getMessage(), e);
		}
	}

	public static void closeNoExceptions(Writer out) {
		if (null == out) {
			return;
		}
		try {
			out.close();
		} catch (IOException e) {
			log.error(e.getMessage(), e);
		}
	}

	public static void sync(FileOutputStream out) {
		if (out == null) {
			return;
		}
		try {
			FileDescriptor fd = out.getFD();
			fd.sync();
		} catch (IOException e) {
			log.error(e.getMessage(), e);
		}
	}

	public static void sync(File file) {
		FileOutputStream fos = null;
		try {
			if (file == null) {
				return;
			}
			if (!file.exists()) {
				return;
			}
			fos = new FileOutputStream(file);
			sync(fos);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		} finally {
			closeNoExceptions(fos);
		}
	}

	public static void closeNoExceptions(OutputStream out) {
		if (null == out) {
			return;
		}
		try {
			out.close();
		} catch (IOException e) {
			log.error(e.getMessage(), e);
		}
	}

	public static final boolean deleteFileTree(File rootFile) {
		boolean success = false;
		if (rootFile != null && rootFile.exists()) {
			if (rootFile.isDirectory()) {
				success = deleteDirEntries(rootFile);
				if(!success){
					log.error("deleteFileTree(): Failed to delete '" + rootFile + "'.");
					return success;
				}
			}
			success = rootFile.delete();
			if (!success) {
				log.error("deleteFileTree(): Failed to delete '" + rootFile + "'.");
				return success;
			}
		}
		return success;
	}

	private static final boolean deleteDirEntries(File rootFile) {
		boolean success = false;
		File[] files = rootFile.listFiles();
		if (files != null) {
			for (File file: files) {
				success = file.delete();
				if (file.exists()) {
					if(file.isDirectory()){
						success = deleteDirEntries(file);
						log.error("deleteDirEntries(): Failed to delete '" + file + "'.");
						return success;
					}
					success = file.delete();
					if (!success) {
						log.error("deleteDirEntries(): Failed to delete '" + file + "'.");
						return success;
					}
				}
			}
		}
		return success;
	}

	public static String getContentFromFile(File f) {
		String content = "";
		try {
			content = getContent(f);
		} catch (IOException e) {
			log.warn("Unable to read content from " + f.getAbsolutePath());
		}
		return content;
	}

	public static String getUniqueContentFromFile(File f, String separator) {
		String content = "";
		try {
			content = getUniqueContent(f, separator);
		} catch (IOException e) {
			log.warn("Unable to read unique content from "
					+ f.getAbsolutePath());
		}
		return content;

	}

	public static String getContent(File f) throws IOException {
		String content = "";
		BufferedReader in = null;
		try {
			in = new BufferedReader(new FileReader(f));
			String str;

			while ((str = in.readLine()) != null) {
				content += (str + "\n");
			}
		} catch (FileNotFoundException e) {
			log.warn("System cannot find the file: " + f.getAbsolutePath());

		} finally {
			closeNoExceptions(in);
		}
		return content;
	}

	public static void append(String fname, String line) {
		File f = new File(fname);
		append(f, line);
	}

	public static void append(File f, String line) {
		BufferedWriter bw = null;
		try {
			FileOutputStream fos = new FileOutputStream(f, true);
			OutputStreamWriter osw = new OutputStreamWriter(fos);
			bw = new BufferedWriter(osw);
			bw.write(line);
			bw.newLine();
		} catch (Exception e) {
			log.warn("Couldn't append line to file " + f.getAbsolutePath()
					+ " " + line);
		} finally {
			closeNoExceptions(bw);
		}
	}

	public static List<String> getContentAsList(String str) throws IOException {
		File f = new File(str);
		return getContentAsList(f);
	}

	public static List<String> getContentAsList(File f) throws IOException {
		ArrayList<String> al = new ArrayList<String>();
		BufferedReader in = null;
		try {
			in = new BufferedReader(new FileReader(f));
			String str;

			while ((str = in.readLine()) != null) {
				al.add(str);
			}
		} catch (FileNotFoundException e) {
			log.warn("System cannot find the file: " + f.getAbsolutePath());

		} finally {
			closeNoExceptions(in);
		}
		return al;
	}

	public static String getUniqueContent(File f, String separator) throws IOException {
		String content = "";
		BufferedReader in = null;
		try {
			in = new BufferedReader(new FileReader(f));
			String str;
			ArrayList<String> al = new ArrayList<String>();
			while ((str = in.readLine()) != null) {
				String prefix = str;
				if (separator != null) {
					StringTokenizer st = new StringTokenizer(str, separator);
					prefix = st.nextToken();
				}
				if (al.contains(prefix)) {
					log.warn("Found duplicate string=" + str);
					continue;
				}
				al.add(prefix);
				content += (str + "\n");
			}
		} catch (FileNotFoundException e) {
			log.warn("Cannot find the file: " + f.getAbsolutePath());

		} finally {
			closeNoExceptions(in);
		}
		return content;
	}
	public static File getTemporaryDirectory() {
		File parent = null;
		SecureRandom sr = new SecureRandom();
		byte[] barr = new byte[8];
		sr.nextBytes(barr);
		try {
			File f = File.createTempFile("bogus", "tmp");
			parent = f.getParentFile();
			File tempDir = new File(parent, getRandomString());
			f.delete();
			if (!tempDir.mkdir()) {
				log.error("Couldn't create temporary directory");
				return null;
			}
			log.debug("Creating temporary directory "
					+ tempDir.getAbsolutePath());
			return tempDir;
		} catch (Exception e) {
			log.error("Can't get temp file directory");
			File tempDir = null;
			try{
				File tmpFile = File.createTempFile("tmp", "txt");
				tempDir = new File(tmpFile.getParentFile(), "tmp");
				tmpFile.delete();
				return tempDir;
			}
			catch(IOException ex){
				log.error("Could not create temporary directory");
				return null;
			}
		}
	}

	private static String getRandomString() {
		SecureRandom sr = new SecureRandom();
		// Using BASE 64, so if length == 9, clength == 12.
		// If you modify length, make sure that clength is set appropriately.
		int length = 9;
		int clength = 12;
		byte[] barr = new byte[length];
		sr.nextBytes(barr);
		String str = new String(Base64.encodeBase64(barr));
		char[] carr = new char[clength];
		str.getChars(0, clength, carr, 0);
		// Just want alphanumeric characters.
		// Nothing special about 6, 8, and A.
		// Not necessary to use random alphanumeric replacement.
		for (int i = 0; i < clength; i++) {
			if (carr[i] == '/') {
				carr[i] = '6';
			} else if (carr[i] == '+') {
				carr[i] = '8';
			} else if (carr[i] == '=') {
				carr[i] = 'A';
			}
		}
		str = new String(carr);
		return str;
	}

	public static String formPath(String... pathParts) {
		if (pathParts == null) {
			return null;
		} else if (pathParts.length == 0) {
			return null;
		} else if (pathParts.length == 1) {
			return pathParts[0];
		} else {
			String formedPath = pathParts[0];
			for (int i = 1; i < pathParts.length; i++) {
				File tempFile = new File(formedPath, pathParts[i]);
				formedPath = tempFile.getPath();
			}
			return formedPath;
		}
	}

	public static File formPathFile(String... pathParts) {
		String path = formPath(pathParts);
		if (path != null) {
			return new File(path);
		} else {
			return null;
		}
	}

	public static boolean createAllParentDirsIfNeeded(File file) {
		if (file == null) {
			return false;
		}
		//log.debug("createAllParentDirsIfNeeded name: " + file.getName());
		File fullPath = file.getAbsoluteFile();
		File parentDir = null;
		log.debug("createAllParentDirsIfNeeded():" + fullPath.getAbsolutePath());
		parentDir = fullPath.getParentFile();
		// In the event the file has no parent dir information associated with
		// it (/, C:\ that
		// I know of).
		if (parentDir == null) {
			return true;
		}
		//log.debug("createAllParentDirsIfNeeded parent dir: " + parentDir.getAbsolutePath());
		if (!parentDir.exists()) {
			return parentDir.mkdirs();
		} 
		else {
			return true;
		}
	}

	public static void mkdirs(File dir) throws IOException {
		if (!dir.exists()) {
			log.debug(dir.getName()+" did not exist. Creating...");
			if (!dir.mkdirs()) {
				String errMsg = "Unable to create directory "
						+ dir.getAbsolutePath();
				log.error(errMsg);
				throw new IOException(errMsg);
			}
		}
	}

	public static void copy(File inputFile, File outFile) throws IOException {
		copy(inputFile, new FileOutputStream(outFile)); 
	}

	public static String copy(File inputFile, File outFile, boolean makeTargetDirs){
		String result = SUCCESS;
		if (makeTargetDirs && !createAllParentDirsIfNeeded(outFile)) {
			result = "Failed to mkdirs=" + outFile.getAbsolutePath();
			log.error(result);
		}
		if(SUCCESS.equals(result)){
			try{
				result = copy(inputFile, new FileOutputStream(outFile));
			} catch(Exception ex){
				result = "Exception while creating fileoutpustream from "+outFile+", see log";
				log.error(result, ex);
			}
		}
		return result;
	}

	private static String copy(File inputFile, OutputStream out){
		String result = SUCCESS;
		FileInputStream in = null;
		try {
			in = new FileInputStream(inputFile);
			byte[] buf = new byte[BUFFER_SIZE];

			for (int count = in.read(buf); count > 0; count = in.read(buf)) {
				out.write(buf, 0, count);
			}
		} catch(Exception ex){
			result = "Exception while copying from "+inputFile+", see log";
			log.error(result, ex);
		} finally {
			closeNoExceptions(in);
			closeNoExceptions(out);
		}
		return result;
	}

	public static boolean move(File src, File dest) {
		// First, try to rename. This is most efficient.
		boolean flag = src.renameTo(dest);
		if (flag) {
			return true;
		}

		log.warn("src.renameTo(dest) operation has failed for \n"
				+ src.getAbsolutePath() + "\nto\n" + dest.getAbsolutePath());

		// File.renameTo() has failed.
		// First copy the directory, then delete it.
		flag = copyFileTree(src, dest);

		// Unable to copy directory, nothing more to try.
		if (!flag) {
			return false;
		}

		// Directory successfully copied, delete old location.
		deleteFileTree(src);

		return true;
	}

	public static boolean copyFileTree(File inputDir, File outputDir) {
		try {
			if (!inputDir.exists()) {
				log.error("Attempt to copy a non-existent directory "
						+ inputDir.getAbsolutePath());
				return false;
			}
			if (!inputDir.isDirectory()) {
				copy(inputDir, outputDir);
				return true;
			}
			boolean flag = recursiveCopy(inputDir, outputDir);
			if (!flag) {
				deleteFileTree(outputDir);
			}
			return flag;
		} catch (Exception e) {
			log.error(e.toString(), e);
			if (outputDir.exists()) {
				deleteFileTree(outputDir);
			}
			return false;
		}
	}

	private static boolean recursiveCopy(File src, File dest) {
		try {
			if (src.isDirectory()) {
				if (!dest.mkdir()) {
					log.error("Could not create directory " + dest.getAbsolutePath());
					return false;
				}
				for (File f: src.listFiles()) {
					File fDest = new File(dest, f.getName());
					if (!recursiveCopy(f, fDest)) {
						return false;
					}
				}
			} 
			else {
				copy(src, dest);
			}
			return true;
		} catch (Exception e) {
			log.error(e.toString(), e);
			return false;
		}
	}

	public static String normalizeSize(double size) {
		if (size < 0.0) {
			throw new IllegalArgumentException("size cannot be negative: "
					+ size);
		}
		String units = "KB";
		if (size >= 1024) {
			size /= 1024;
			units = "MB";
			if (size >= 1024) {
				size /= 1024;
				units = "GB";
			}
		}
		String used = String.format("%1.1f" + " " + units, size);
		return used;
	}

	public static boolean isFileNameValid(String fileName) {
		boolean ret = false;
		if (Pattern.matches("[a-zA-Z0-9\\-_ ]+", fileName) == true) {
			ret = true;
		}
		return ret;
	}

	public static void closeNoExceptions(ZipFile zipFile) {
		try {
			if (zipFile != null) {
				zipFile.close();
			}
		} catch (Throwable t) {
			// do nothing
		}
	}

	public static String getErrorMessage(File f, String str) {
		try {
			for (String line : getContentAsList(f)) {
				if (line.startsWith(str)) {
					return line.substring(line.indexOf(":") + 1);
				}
			}
		} catch (IOException ex) {
			log.error("writeFile()", ex);
		}
		return null;
	}

	public static void writeFile(File f, List<String> list) throws IOException {
		BufferedWriter bw = null;
		log.debug("write list to " + f.getAbsolutePath());
		try {
			FileOutputStream fos = new FileOutputStream(f);
			OutputStreamWriter osw = new OutputStreamWriter(fos);
			bw = new BufferedWriter(osw);
			for (String str : list) {
				log.debug("write line " + str);
				bw.write(str);
				bw.newLine();
			}

			sync(fos);
		} catch (IOException ex) {
			log.error("writeFile():", ex);
			throw ex;
		} finally {
			if (bw != null) {
				closeNoExceptions(bw);
			}

			List<String> lines = getContentAsList(f);
			log.debug("After writeFile, number of lines is " + lines.size());
		}
	}

	public static void writeFile(File f, String str) {
		BufferedWriter bw = null;
		log.debug("write str to " + f.getAbsolutePath());
		try {
			FileOutputStream fos = new FileOutputStream(f);
			OutputStreamWriter osw = new OutputStreamWriter(fos);
			bw = new BufferedWriter(osw);
			bw.write(str);
			sync(fos);
		} catch (IOException ex) {
			log.error("writeFile() failed:", ex);
		} finally {
			if (bw != null) {
				closeNoExceptions(bw);
			}
		}
	}

	public static void createDirIfDoesNotExist(File dir) throws IOException {
		if (!dir.exists() && !dir.getParent().equalsIgnoreCase("null")) {
			log.debug("Creating directory: " + dir.getName() + " as "
					+ dir.getAbsolutePath());
			if (!dir.mkdirs()) {
				String errMsg = "Unable to create directory "
						+ dir.getAbsolutePath();
				throw new IOException(errMsg);
			}
		}
	}

	public static String getAvailableSpaceMB(String volNameStr) {
		long freeSpace = 0L;
		try {
			log.debug(String.format("checking free space on >%s<", volNameStr));
			// freeSpaceKb cannot handle relative paths, attempt to resolve into
			// a full path
			File volName = new File(volNameStr);
			// If dir does not exist, give up
			if (volName.exists()) {
				String volNameFullPath = volName.getCanonicalPath();
				log.debug(String.format("resolved dir name to >%s<",
						volNameFullPath));
				// If the dir passed into this method does not exist, this
				// throws NPE
				// In this case, we default to 0 for now.
				freeSpace = FileSystemUtils.freeSpaceKb(volNameFullPath) / 1024;
				log.debug("free space: " + freeSpace + " MB");
			}
		} catch (Exception e) {
			log.error("Exception getting freeSpace", e);
		}

		return String.valueOf(freeSpace);
	}

	public static boolean xCopy(String sourceFilePath, String destinationDirPath,
			long timeout) {
		boolean retVal = false;
		log.debug("Executing xCopy()....");
		try {
			File srcFile = new File(sourceFilePath);
			File destinationFile = new File(destinationDirPath);			
			deleteFileTree(destinationFile);
			createDirIfDoesNotExist(destinationFile);						
			if (srcFile.isFile()) {
				String tempDestinationPath = (!destinationDirPath.endsWith("\\")) ? MessageFormat
						.format("{0}\\", destinationDirPath)
						: destinationDirPath;
				destinationFile = new File(MessageFormat.format("{0}\\{1}",
						tempDestinationPath, srcFile.getName()));
				if(destinationFile.isFile() && destinationFile.exists()){
					destinationFile.delete();
				}
			}					
			log.debug(MessageFormat.format("Copying {0} dir to {1}", srcFile
					.getAbsolutePath(), destinationFile.getAbsolutePath()));
			copyDirectory(srcFile, destinationFile);
			retVal = true;
		} catch (Exception e) {
			log.error(MessageFormat.format("Unable to copy {0} to {1} .", sourceFilePath, destinationDirPath), e);
			retVal = false;
		}
		return retVal;
	}
	
	public static void copyDirectory(File srcPath, File dstPath) throws Exception {
		if (srcPath.isDirectory()) {
			if (!dstPath.exists()) {
				dstPath.mkdir();
			}
			String files[] = srcPath.list();
			for (int i = 0; i < files.length; i++) {
				copyDirectory(new File(srcPath, files[i]), new File(dstPath, files[i]));
			}
		} else {
			if (srcPath.exists()) {
				InputStream in = null;
				OutputStream out = null;
				try {
					in = new FileInputStream(srcPath);
					out = new FileOutputStream(dstPath);
					// Transfer bytes from in to out
					byte[] buf = new byte[1024];
					int len;
					while ((len = in.read(buf)) > 0) {
						out.write(buf, 0, len);
					}
				} catch (Exception exp) {
					log.error("Failed to copy dir.", exp);
					throw exp;
				} finally {
					if (in != null) {
						in.close();
					}
					if (out != null) {
						out.close();
					}
				}
			}
		}
	}

	public static void copy(File source, File dest, int maxBufferSize)
			throws IOException {
		int DEFAULT_BUFFER_SIZE = 1024 * 1024 * 1; // 1 MB
		FileInputStream inputStream = new FileInputStream(source);
		FileChannel inChannel = inputStream.getChannel();

		// If the input Max Buffer Size is zero or negative, set it to 1MB
		if (maxBufferSize <= 0)
			maxBufferSize = DEFAULT_BUFFER_SIZE;

		FileOutputStream destStream = new FileOutputStream(dest);
		FileChannel destChannel = destStream.getChannel();

		try {
			log.debug("Copying file using FileChannel.transferTo method (Buffer Size = "
					+ maxBufferSize/1024 + " KB) for file " + dest.getAbsolutePath());
			// Mass data transfer directly between FileChannels without
			// allocating byte[] buffer
			// therefore, the JVM memory usage is small and independent of file
			// size
			long fileSize = inChannel.size();
			long offs = 0, doneCnt = 0, copyCnt = Math.min(maxBufferSize, fileSize);
			do {
				doneCnt = inChannel.transferTo(offs, copyCnt, destChannel);
				offs += doneCnt;
				fileSize -= doneCnt;
			} while (fileSize > 0);
			log.debug("Sucessfully created file " + dest.getAbsolutePath());
		} finally {
			if (inChannel != null)
				inChannel.close();
			if (destChannel != null)
				destChannel.close();
			FileUtils.closeNoExceptions(inputStream);
			FileUtils.closeNoExceptions(destStream);
		}
	}
	
	public static void saveFile(final MultipartFile file, String targetDirName, String targetFileName){
    	BufferedOutputStream bos = null;
    	try{  
    		File dir = new File(targetDirName);
    		if(!dir.exists()){
        		dir.mkdirs();
    		}
    		File textFile = new File(dir, targetFileName);
    		if(textFile.exists()){
        		textFile.delete();
    		}
        	log.debug("saveFile(file="+textFile.getAbsolutePath());
        	bos = new BufferedOutputStream(new FileOutputStream(textFile));
        	byte[] bytes = file.getBytes();
        	bos.write(bytes);
    	}catch(Throwable ex){
    		log.error(ex.getMessage(), ex);
    	}
    	finally{
    		FileUtils.closeNoExceptions(bos);
    	}
	}
    	
}
