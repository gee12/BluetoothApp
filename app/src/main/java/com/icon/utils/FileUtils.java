package com.icon.utils;

import org.apache.commons.io.input.ReversedLinesFileReader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

/**
 * Created by Ivan on 07.10.2015.
 */
public class FileUtils {

    /*
    *
    */
    public static int cutFileFromEnd(File file, int maxLines) throws IOException {
//        BufferedReader in = new BufferedReader(new InputStreamReader(new ReverseLineInputStream(file)));
////        ReversedLinesFileReader reader;
        StringBuffer sb = new StringBuffer();
//
        int lineNum = 0;
        String line;
//        while ((line = in.readLine()) != null && lineNum++ < maxLines) {
//            sb.insert(0, line);
//        }
//        in.close();
//

        ReversedLinesFileReader fr = new ReversedLinesFileReader(file, 64, Charset.defaultCharset());
        while((line = fr.readLine()) != null && lineNum < maxLines) {
            sb.append(line);
            lineNum++;
        }
        fr.close();

//        long allLinesNum = 0;
//        FileInputStream fis = new FileInputStream(file);
//        BufferedReader in = new BufferedReader(new InputStreamReader(fis));
////        in.mark(0);
//
//        while(in.readLine() != null){
//            allLinesNum++;
//        }
////        in.reset();
//        fis.getChannel().position(0);
//        in = new BufferedReader(new InputStreamReader(fis));
//
//        final long startLine = allLinesNum - maxLines;
//
//        while((line = in.readLine()) != null && lineNum < allLinesNum){
//            if (lineNum >= startLine) {
//                sb.append(line);
//            }
//            lineNum++;
//        }
//        in.close();

        String s = sb.toString();

        FileOutputStream fos = new FileOutputStream(file, false);
//        BufferedWriter bw = new BufferedWriter(new FileWriter(file, false));
        fos.write(s.getBytes());
        fos.flush();
        fos.close();

        return lineNum;
    }

    public static String getLastNLogLines(File file, int maxLines) {
        StringBuilder s = new StringBuilder();
        try {
            Process p = Runtime.getRuntime().exec("tail -" + maxLines + " " + file);
            java.io.BufferedReader input = new java.io.BufferedReader(new java.io.InputStreamReader(p.getInputStream()));
            String line;
            while((line = input.readLine()) != null){
                s.append(line).append('\n');
            }
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
        return s.toString();
    }
}
