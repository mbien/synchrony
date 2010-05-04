package com.synchrony.examples;

import java.net.URI;
import java.nio.channels.FileChannel;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.nio.file.spi.FileSystemProvider;
import java.util.Iterator;
import java.util.Map;

public class examplePath {
	
	public examplePath() throws Exception {
		
		FileSystem fs = FileSystems.getDefault();
		Path image = fs.getPath( System.getProperty("user.home")+"/testdatei" );
		
		FileSystemProvider fsp = FileSystemProvider.installedProviders().get( 0 );
		Path path2 = fsp.getPath( new URI( "file:///"+System.getProperty("user.home")+"/testdatei" ));
		

		//einfacher
		Path path3 = Paths.get(new URI( "file:///"+System.getProperty("user.home")+"/testdatei"));
		Path path4 = Paths.get( System.getProperty("user.home")+"/testdatei" );
		
		
		System.out.println( "exists:"+image.exists() );		
		System.out.println( "isIdentical:"+image.isSameFile(path2));	
		System.out.println( "isIdentical:"+image.isSameFile(path3));	
		System.out.println( "isIdentical:"+image.isSameFile(path4));
		
		Map<String, ?> map = image.readAttributes( "*" );
		Iterator it = map.entrySet().iterator();
		System.out.println( "BasicFileAttributes:");
		while( it.hasNext() ){
			System.out.println( "\t"+it.next() );
		}
		System.out.println( );
		
		System.out.println( "absolute Path: "+image);
		Path baseDir = Paths.get(System.getProperty("user.home"));
		System.out.println( "base Directory:"+baseDir);
		Path relative = baseDir.relativize( image );
		System.out.println( "relativize   : "+ relative );
		System.out.println( "resolve      : "+ baseDir.resolve( relative ));
		

		baseDir = Paths.get( System.getProperty("user.home"));
		System.out.println( "base Directory:"+baseDir);
		relative = baseDir.relativize( image );
		System.out.println( "relativize   : "+ relative );
		
		//cast of file channel
		SeekableByteChannel sbc = image.newByteChannel( StandardOpenOption.READ, StandardOpenOption.WRITE );
		System.out.println( "is FileChannel:" + (sbc instanceof FileChannel));
		FileChannel fc = (FileChannel) sbc;
		fc.lock();
		fc.close();
	}

	
	public static void main(String[] args){
		try {
			new examplePath();
		} 
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}
