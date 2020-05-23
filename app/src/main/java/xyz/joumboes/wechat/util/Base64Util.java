package xyz.joumboes.wechat.util;

import android.util.Base64;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;



public class Base64Util {

    public  String fileToBase64(File file) {
        String base64 = null;
        InputStream in = null;
        try {
            in = new FileInputStream(file);
            byte[] bytes = new byte[in.available()];
            int length = in.read(bytes);
            base64 = Base64.encodeToString(bytes, 0, length, Base64.DEFAULT);
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return base64;
    }

    public void base64ToFile(String data, String path) {
        File file=new File(path);
        if (file.exists()){
            return;
        }
        try {
            byte[] buffer = Base64.decode(data, Base64.DEFAULT);
            FileOutputStream out = new FileOutputStream(path);
            out.write(buffer);
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

//        BufferedOutputStream bos = null;
//        FileOutputStream fos = null;
//        File file = null;
//
//        try {
//            byte[] bytes = decoder.decodeBuffer(data);
//            file = new File(path);
//            fos = new FileOutputStream(file);
//            bos = new BufferedOutputStream(fos);
//            bos.write(bytes);
//            bos.close();
//            fos.close();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
}
