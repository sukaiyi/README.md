package com.sukaiyi.classstruct.analyzer;

import com.sukaiyi.classstruct.analyzer.blocks.*;
import com.sukaiyi.classstruct.analyzer.blocks.attrblock.AttributeBlock;
import lombok.Data;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * @author sukaiyi
 * @date 2020/08/12
 */
@Data
public class JvmClassLinkedModel {
    private String magic;
    private int minorVersion;
    private int majorVersion;
    private List<ConstantPoolInfoBlock.Data> constantPoolInfos;
    private int accessFlags;
    private String thisClass;
    private String superClass;
    private List<String> interfaces;
    private List<FieldBlock.Data> fields;
    private List<MethodBlock.Data> methods;
    private List<AttributeBlock.Data> attributes;

    public static JvmClassLinkedModel of(JvmClassRawModel raw) {
        JvmClassLinkedModel model = new JvmClassLinkedModel();
        model.magic = raw.getMagic();
        model.minorVersion = raw.getMinorVersion();
        model.majorVersion = raw.getMajorVersion();
        model.constantPoolInfos = raw.getConstantPoolInfos();
        model.accessFlags = raw.getAccessFlags();
        model.thisClass = resolveClassInfo(raw.getConstantPoolInfos(), raw::getThisClass);
        model.superClass = resolveClassInfo(raw.getConstantPoolInfos(), raw::getSuperClass);
        model.interfaces = Optional.of(raw)
                .map(JvmClassRawModel::getInterfaces)
                .orElse(Collections.emptyList())
                .stream()
                .map(e -> resolveClassInfo(raw.getConstantPoolInfos(), () -> e))
                .collect(Collectors.toList());
        return model;
    }

    private static String resolveClassInfo(List<ConstantPoolInfoBlock.Data> constantPoolInfos, Supplier<Integer> indexSupplier) {
        return Optional.ofNullable(constantPoolInfos)
                .filter(e -> !e.isEmpty())
                .map(e -> e.get(indexSupplier.get()))
                .filter(e -> e instanceof ConstantPoolInfoBlock.ClassValue)
                .map(e -> (ConstantPoolInfoBlock.ClassValue) e)
                .map(ConstantPoolInfoBlock.ClassValue::getFullNameIndex)
                .map(e -> resolveUtf8Value(constantPoolInfos, () -> e))
                .orElse(null);
    }

    private static String resolveUtf8Value(List<ConstantPoolInfoBlock.Data> constantPoolInfos, Supplier<Integer> indexSupplier) {
        return Optional.ofNullable(constantPoolInfos)
                .filter(e -> !e.isEmpty())
                .map(e -> e.get(indexSupplier.get()))
                .filter(e -> e instanceof ConstantPoolInfoBlock.Utf8Value)
                .map(e -> (ConstantPoolInfoBlock.Utf8Value) e)
                .map(ConstantPoolInfoBlock.Utf8Value::getValue)
                .orElse(null);
    }
}
