package com.sukaiyi.classstruct;

import com.sukaiyi.classstruct.analyzer.JvmClassAnalyzer;
import com.sukaiyi.classstruct.analyzer.JvmClassRawModel;

import java.util.List;

/**
 * @author sukaiyi
 * @date 2020/08/12
 */
public class Main {

    public static void main(String[] args) {
        JvmClassAnalyzer jvmClassAnalyzer = new JvmClassAnalyzer();
//        List<JvmClassModel> classModels = jvmClassAnalyzer.exec("C:\\Users\\HT-Dev\\Documents\\Projects\\byte-utils\\target\\classes\\com\\sukaiyi\\byteutils\\analyzer\\BaseByteAnalyzer.class");
        List<JvmClassRawModel> classModels = jvmClassAnalyzer.exec("C:\\Users\\HT-Dev\\Documents\\Projects\\class-structure\\target\\classes\\com\\sukaiyi\\classstruct\\analyzer\\blocks\\AccessFlagBlock.class");
    }
}
