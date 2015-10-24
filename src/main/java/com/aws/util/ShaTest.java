package com.aws.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public class ShaTest {
    public static void main(String[] args) throws Exception {
        System.out.println("Java version: " + System.getProperty("java.version"));
        URL url = new URL("https://www.amazonsha256.com/test");
        InputStream is = (InputStream) url.getContent();
        String actual = new String(toByteArray(is));
        is.close();
        System.out.println(actual);
        if (!"Success".equals(actual))
            throw new AssertionError("Unexpected content: " + actual);
        System.exit(0);
    }

    private static byte[] toByteArray(InputStream is) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try {
            byte[] b = new byte[16];
            int n = 0;
            while ((n = is.read(b)) != -1) {
                output.write(b, 0, n);
            }
            return output.toByteArray();
        } finally {
            output.close();
        }
    }
}
