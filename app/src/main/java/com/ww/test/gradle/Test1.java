package com.ww.test.gradle;

import android.content.Context;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

/**
 * @Author weiwei
 * @Date 2022/7/15 18:41
 */

public class test1 {

    // 测试json对象写入到文件
    public static void testJsonWriteFile(Context context) throws IOException {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("一", 1);
            jsonObject.put("二", 2);
            jsonObject.put("三", 3);
            Writer output = null;

            File file = new File("storage/sdcard/MyIdea/MyCompositions/" + "jsonObject.json");

            output = new BufferedWriter(new FileWriter(file));

            output.write(jsonObject.toString());

            output.close();


        } catch (Exception e) {


        }
    }

    public static void main(String[] args) throws IOException {

        testJsonWriteFile();
    }
}


