package com.sukaiyi.classstruct.analyzer;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import javax.tools.*;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class JvmClassAnalyzerTest {


    @Test
    @Order(0)
    void test() {
    }

    @Test
    @Order(1)
    void compile() throws IOException {
        String outputPath = "src/test/resources/classes";
        List<String> paths = new ArrayList<String>();
        paths.add("src/test/resources/src/People.java");
        paths.add("src/test/resources/src/Chinese.java");

        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<JavaFileObject>();
        StandardJavaFileManager fileManager = compiler.getStandardFileManager(diagnostics, null, null);
        JavaFileManager.Location oLocation = StandardLocation.CLASS_OUTPUT;
        fileManager.setLocation(oLocation, Collections.singletonList(new File(outputPath)));
        Iterable<? extends JavaFileObject> compilationUnits = fileManager.getJavaFileObjectsFromStrings(paths);
        JavaCompiler.CompilationTask task = compiler.getTask(null, fileManager, diagnostics, null, null, compilationUnits);
        boolean result = task.call();
        fileManager.close();

        assertTrue(result, "compile failed");
    }


    @Test
    @Order(2)
    void analyze() {
        JvmClassAnalyzer analyzer = new JvmClassAnalyzer();
        List<JvmClassLinkedModel> models = Stream.of(
                "src/test/resources/classes/People.class",
                "src/test/resources/classes/Chinese.class",
                "target/classes/com/sukaiyi/classstruct/analyzer/JvmClassAnalyzer.class",
                "target/classes/com/sukaiyi/classstruct/analyzer/blocks/ConstantPoolInfoTagBlock.class",
                "target/classes/com/sukaiyi/classstruct/analyzer/blocks/SuperClassBlock.class"
        )
                .parallel()
                .map(analyzer::exec)
                .flatMap(Collection::stream)
                .map(JvmClassLinkedModel::of)
                .collect(Collectors.toList());
        assertNotNull(models);
    }

    @Test
    @Order(3)
    void printFailed() {
        JvmClassAnalyzer analyzer = new JvmClassAnalyzer();

        File root = new File("target/classes");
        Queue<File> queue = new LinkedList<>();
        queue.offer(root);
        while (!queue.isEmpty()) {
            File file = queue.poll();
            if (file.isDirectory()) {
                File[] children = file.listFiles();
                if (children != null && children.length > 0) {
                    for (File child : children) {
                        queue.offer(child);
                    }
                }
            } else {
                if (analyzer.exec(file).isEmpty()) {
                    System.out.println(file.getAbsolutePath());
                }
            }
        }
    }
}