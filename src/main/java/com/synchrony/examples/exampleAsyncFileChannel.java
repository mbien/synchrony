package com.synchrony.examples;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.FileLock;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class exampleAsyncFileChannel{
	
	public exampleAsyncFileChannel()throws Exception{
		
		AsynchronousFileChannel asyncFc = AsynchronousFileChannel.open(
				Paths.get( System.getProperty("user.home")+"/testdatei"),
				EnumSet.of(StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE ),
				new ThreadPoolExecutor( 10, 20, 10, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>() ) );
		
		
		Future<FileLock> flock = asyncFc.lock();
		FileLock lock = flock.get();	
		System.out.println( "get file lock:"+lock );
				
		ByteBuffer buffer = ByteBuffer.allocate( 64*1024 );
		int loops = 10 * 1024 /64; //schreib 10MB
		
		long position = 0L;	
		ArrayList<Future<Integer>> writeFutures = new ArrayList<Future<Integer>>( loops*2 );
		
		for (int i = 0; i < loops; i++ ) {
			ByteBuffer header = ByteBuffer.allocate( 4 );
			header.putInt( i );
			header.flip();
			
			Future<Integer> h = asyncFc.write( header, position );
			writeFutures.add( h );
			position += 4;
			
			ByteBuffer body = buffer.slice();
			Future<Integer> b = asyncFc.write( body, position );
			writeFutures.add( b );
			position += buffer.remaining();	
		}
		
		//check Futures if writing is ready;
		int writeOps = writeFutures.size();
		for (int i = 0; i < writeOps; i++) {
			Integer wb = writeFutures.get( i ).get();
			System.out.println( "written bytes:"+wb);
		}
		
		asyncFc.close();
		System.out.println( "file channel closed" );
	}
	
	public static void main(String[] args){
		try {
			new exampleAsyncFileChannel();
		} 
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}
