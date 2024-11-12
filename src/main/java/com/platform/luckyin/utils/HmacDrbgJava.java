package com.platform.luckyin.utils;

import java.io.FileOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

class HmacDrbgJava {

    private final byte[] seed;
    private final AtomicInteger counter;

    public HmacDrbgJava(byte[] seed) {
        this.seed = seed;
        this.counter = new AtomicInteger(0);
    }

    public byte[] generateRandomBytes(int size) {
        byte[] randomBytes = new byte[size];
        int index = 0;
        while (index < size) {
            byte[] data = createData(counter.getAndIncrement());
            try {
                MessageDigest md = MessageDigest.getInstance("SHA-256");
                byte[] chunk = md.digest(data);
                System.arraycopy(chunk, 0, randomBytes, index, Math.min(32, size - index));
                index += 32;
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
        }
        return randomBytes;
    }

    private byte[] createData(int counterValue) {
        byte[] counterBytes = String.valueOf(counterValue).getBytes();
        return concatArrays(counterBytes, seed);
    }

    private byte[] concatArrays(byte[] arr1, byte[] arr2) {
        byte[] result = new byte[arr1.length + arr2.length];
        System.arraycopy(arr1, 0, result, 0, arr1.length);
        System.arraycopy(arr2, 0, result, arr1.length, arr2.length);
        return result;
    }
}


class RandomBitGeneratorJava {

    public static void main(String[] args) throws Exception {
        int totalBits = 100000000;
        int totalBytes = (totalBits + 7) / 8;

        String filePath = "/Users/zsw/secureRandomBits.bin";
        try (FileOutputStream fos = new FileOutputStream(filePath)) {
            byte[] seed = generateSecureSeed();
            System.out.println("Generated secure seed: " + bytesToHex(seed));

            HmacDrbgJava rng = new HmacDrbgJava(seed);

            int bytesWritten = 0;
            int chunkSize = 1024 * 1024;
            while (bytesWritten < totalBytes) {
                int bytesToWrite = Math.min(chunkSize, totalBytes - bytesWritten);
                byte[] randomData = rng.generateRandomBytes(bytesToWrite);
                fos.write(randomData);
                bytesWritten += bytesToWrite;
            }
        }
        System.out.println("Random bitstream generation complete.");
    }

    private static byte[] generateSecureSeed() {
        byte[] secureRandom = new byte[64];
        new Random().nextBytes(secureRandom);
        byte[] timeEntropy = String.valueOf(System.currentTimeMillis()).getBytes();
        return concatArrays(secureRandom, timeEntropy);
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte b : bytes) {
            result.append(String.format("%02x", b));
        }
        return result.toString();
    }

    private static byte[] concatArrays(byte[] arr1, byte[] arr2) {
        byte[] result = new byte[arr1.length + arr2.length];
        System.arraycopy(arr1, 0, result, 0, arr1.length);
        System.arraycopy(arr2, 0, result, arr1.length, arr2.length);
        return result;
    }

}
