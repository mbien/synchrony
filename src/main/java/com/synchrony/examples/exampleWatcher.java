package com.synchrony.examples;

import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKind;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;

public class exampleWatcher implements Runnable{
	
	WatchService watcher;
	WatchKey key;
	Object ojb;
	
	public exampleWatcher()throws Exception{
	    	
    	FileSystem fs = FileSystems.getDefault();
    	Path path = fs.getPath( System.getProperty("user.home") );
    	
    	
    	watcher = fs.newWatchService();	
    	key = path.register( watcher, 
    			StandardWatchEventKind.ENTRY_CREATE, 
    			StandardWatchEventKind.ENTRY_DELETE, 
    			StandardWatchEventKind.ENTRY_MODIFY );
    	
    	System.out.println( key );
    	
    	new Thread( this ).start();
    	
    	Thread.sleep( 60000 );
    	System.out.println( "cancel" );
    	key.cancel();
    	watcher.close();
		
	}
	
	public void run(){
		while( true ){
			try {
		    	System.out.println( "take" );
				WatchKey wk = watcher.take();
				System.out.println( wk );
				
				for (WatchEvent<?> event: wk.pollEvents()) {
					Path path = (Path) event.context();
					System.out.println( "**************" );
					System.out.println( path + " -> " + path.exists() );
					System.out.println( event.count() );
					System.out.println( event.kind() );
		         }

				
				boolean valid = wk.reset();
				if( !valid){
					break;
				}
			} 
			catch (InterruptedException e) {
			}
		}
		System.out.println( "ende" );
	}
	
	public static void main(String[] args){
		try {
			new exampleWatcher();
		} 
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}
