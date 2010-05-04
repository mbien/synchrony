package com.synchrony.examples;

import java.nio.file.DirectoryStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;

public class exampleDirectoryStream {
	
	public exampleDirectoryStream() throws Exception {
		
		Path dir = Paths.get(System.getProperty("user.home"));
		
		DirectoryStream<Path> stream = dir.newDirectoryStream( "*");


		
		Iterator<Path> it = stream.iterator();
		while( it.hasNext() ){
                        String currentFileName = it.next().toString();
                        if(!currentFileName.contains("/.") ) {
			System.out.println( currentFileName );
                        }
		}
		stream.close();
		
	}

	
	public static void main(String[] args){
		try {
			new exampleDirectoryStream();
		} 
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}
