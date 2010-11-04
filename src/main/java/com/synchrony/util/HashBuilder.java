package com.synchrony.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import java.util.Set;
import static java.nio.file.StandardOpenOption.*;

public class HashBuilder {

    public final String HASH_ALGORITHM;
    final FileAttribute<Set<PosixFilePermission>> attr;
    private final int DIGEST_LENGHT;

    public HashBuilder() throws NoSuchAlgorithmException {
        this("SHA");
    }

    public HashBuilder(String algorithm) throws NoSuchAlgorithmException {
//        if(Security.getProvider(algorithm) == null){
//            throw new NoSuchAlgorithmException("Requested algorithm not available. Requested was: "+algorithm);
//        }
        HASH_ALGORITHM = algorithm;

        DIGEST_LENGHT = MessageDigest.getInstance(HASH_ALGORITHM).getDigestLength();
        
        attr = PosixFilePermissions.asFileAttribute(
                PosixFilePermissions.fromString("rwxr-x---")
            );


//        Provider[] providers = Security.getProviders();
//        for (Provider provider : providers) {
//            System.out.println(provider);
//            Set<Service> services = provider.getServices();
//            for (Service service : services) {
//                System.out.println(" - "+service);
//            }
//        }
    }

    public boolean fileEqualsHash(Path file, Path chkFile) throws IOException {

        byte[] chk1 = buildChecksum(file);
        byte[] chk2 = new byte[chk1.length];

        InputStream is = chkFile.newInputStream(READ);
        is.read(chk2);
        is.close();

        return Arrays.equals(chk1, chk2);
    }

    public byte[] buildChecksum(Path file) throws IOException {

        InputStream is = file.newInputStream(READ);

        MessageDigest msgDigest;
        try {
            msgDigest = MessageDigest.getInstance(HASH_ALGORITHM);
        } catch (NoSuchAlgorithmException ex) {
            throw new RuntimeException("no suitable checksum algorithm available", ex);
        }

        int numRead;
        byte[] buffer = new byte[1048576];

        try {
            while((numRead = is.read(buffer)) != -1) {
                msgDigest.update(buffer, 0, numRead);
            }
        }catch (IOException ex) {
            is.close();
            throw ex;
        }

        is.close();

        return msgDigest.digest();
    }

    public byte[] storeHashFile(Path file, Path checkSumFile) throws IOException {

        byte[] hash = buildChecksum(file);

        Path destFile = Paths.get(checkSumFile + "." + HASH_ALGORITHM);
        System.out.println("new checksum: "+destFile);

        Files.createDirectories(destFile.getParent(), attr);

        OutputStream os = destFile.newOutputStream(CREATE);
        try{
            os.write(hash);
        }catch(IOException ex) {
            os.close();
            throw ex;
        }
        os.close();
        return hash;
    }

    public byte[] readHashFile(Path hashFile) throws IOException {
        byte[] hash = new byte[DIGEST_LENGHT];
        InputStream is = hashFile.newInputStream(READ);
        try{
            is.read(hash);
        }finally{
            is.close();
        }
        return hash;
    }
    
}
