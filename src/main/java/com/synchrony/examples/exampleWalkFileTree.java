package com.synchrony.examples;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitOption;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.EnumSet;

public class exampleWalkFileTree {
	
	public exampleWalkFileTree() throws Exception {
		
		FileSystem fs = FileSystems.getDefault();
		Path path1 = fs.getPath(System.getProperty("user.home"));
		
		//Files.walkFileTree(path1, new MySimpleFileVisitor());
		Files.walkFileTree(path1, EnumSet.of(FileVisitOption.FOLLOW_LINKS), 10, new MySimpleFileVisitor());	
	}
	

    
    class MySimpleFileVisitor implements FileVisitor<Path>{
    	
		public FileVisitResult postVisitDirectory(Path dir, IOException exc) {
            System.out.println("ByeBye directory: " + dir);
			if( exc != null ){
				exc.printStackTrace();
				return FileVisitResult.TERMINATE;
			}
			return FileVisitResult.CONTINUE;
		}

		public FileVisitResult preVisitDirectory(Path dir) {
            System.out.println("About to visit directory: " + dir);
			return FileVisitResult.CONTINUE;
		}

		public FileVisitResult preVisitDirectoryFailed(Path dir, IOException exc) {
			if( exc != null ){
				exc.printStackTrace();
			}
			return FileVisitResult.TERMINATE;
		}

		public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
            System.out.println("Visit file: " + file.getName());
			return FileVisitResult.CONTINUE;
		}

		public FileVisitResult visitFileFailed(Path file, IOException exc) {
			if( exc != null ){
				exc.printStackTrace();
			}
			return FileVisitResult.TERMINATE;
		}
    }
    

	
	public static void main(String[] args){
		try {
			new exampleWalkFileTree();
		} 
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}
