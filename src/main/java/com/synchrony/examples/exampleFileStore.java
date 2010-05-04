package com.synchrony.examples;

import java.nio.file.FileStore;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.attribute.AclFileAttributeView;
import java.nio.file.attribute.FileStoreSpaceAttributeView;
import java.nio.file.attribute.FileStoreSpaceAttributes;

public class exampleFileStore {
	
	public exampleFileStore() throws Exception {
		
		FileSystem fs = FileSystems.getDefault();
	
		System.out.println( "supported views from fileSystem:" );
		for( String views : fs.supportedFileAttributeViews() ){
			System.out.println( "\t"+ views );
		}


		System.out.format("%-20s %-10s %12s %12s %12s %12s%n", "Device",  "Type" , "Total", "Used", "Available", "ACL");
		for (FileStore store : fs.getFileStores()) {
			//FileStoreSpaceAttributes attrs = Attributes.readFileStoreSpaceAttributes(store);
			
			FileStoreSpaceAttributeView view = store.getFileStoreAttributeView( FileStoreSpaceAttributeView.class );
			FileStoreSpaceAttributes attrs = view.readAttributes();
			
			boolean acl = store.supportsFileAttributeView( AclFileAttributeView.class );
			
			long total = attrs.totalSpace() / 1024;
			long used = (attrs.totalSpace() - attrs.unallocatedSpace()) / 1024;
			long avail = attrs.usableSpace() / 1024;
			System.out.format("%-20s %-10s %12d %12d %12d %12b%n", store,  store.type() ,total, used, avail, acl);
		}
	}

	
	public static void main(String[] args){
		try {
			new exampleFileStore();
		} 
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}
