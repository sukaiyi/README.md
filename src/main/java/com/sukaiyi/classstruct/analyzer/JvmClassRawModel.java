package com.sukaiyi.classstruct.analyzer;

import com.sukaiyi.byteutils.analyzer.Block;
import com.sukaiyi.byteutils.utils.ByteUtils;
import com.sukaiyi.classstruct.analyzer.blocks.*;
import com.sukaiyi.classstruct.analyzer.blocks.attrblock.AttributeBlock;
import lombok.Data;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author sukaiyi
 * @date 2020/08/12
 */
@Data
public class JvmClassRawModel {
    private String magic;
    private int minorVersion;
    private int majorVersion;
    private List<ConstantPoolInfoBlock.Data> constantPoolInfos;
    private int accessFlags;
    private int thisClass;
    private int superClass;
    private List<Integer> interfaces;
    private List<FieldBlock.Data> fields;
    private List<MethodBlock.Data> methods;
    private List<AttributeBlock.Data> attributes;

    public static JvmClassRawModel of(Map<Class<?>, List<Block<?>>> blockAlreadyDecode) {
        JvmClassRawModel model = new JvmClassRawModel();

        model.magic = ByteUtils.byteToHexString((byte[]) blockAlreadyDecode.get(MagicBlock.class).get(0).getDecodedData(), 0, 4);
        model.minorVersion = Optional.of(MinorVersionBlock.class).map(blockAlreadyDecode::get).filter(e -> !e.isEmpty()).map(e -> e.get(0)).map(e -> (MinorVersionBlock) e).map(MinorVersionBlock::getDecodedData).orElse(0);
        model.majorVersion = Optional.of(MajorVersionBlock.class).map(blockAlreadyDecode::get).filter(e -> !e.isEmpty()).map(e -> e.get(0)).map(e -> (MajorVersionBlock) e).map(MajorVersionBlock::getDecodedData).orElse(0);
        model.constantPoolInfos = Optional.of(ConstantPoolInfoBlock.class)
                .map(blockAlreadyDecode::get)
                .orElse(Collections.emptyList())
                .stream()
                .filter(e -> e instanceof ConstantPoolInfoBlock || e == null)
                .map(e -> (ConstantPoolInfoBlock) e)
                .map(e -> e == null ? null : e.getDecodedData())
                .collect(Collectors.toList());
        model.accessFlags = Optional.of(AccessFlagBlock.class).map(blockAlreadyDecode::get).filter(e -> !e.isEmpty()).map(e -> e.get(0)).map(e -> (AccessFlagBlock) e).map(AccessFlagBlock::getDecodedData).orElse(0);
        model.thisClass = Optional.of(ThisClassBlock.class).map(blockAlreadyDecode::get).filter(e -> !e.isEmpty()).map(e -> e.get(0)).map(e -> (ThisClassBlock) e).map(ThisClassBlock::getDecodedData).orElse(0);
        model.superClass = Optional.of(SuperClassBlock.class).map(blockAlreadyDecode::get).filter(e -> !e.isEmpty()).map(e -> e.get(0)).map(e -> (SuperClassBlock) e).map(SuperClassBlock::getDecodedData).orElse(0);

        model.interfaces = Optional.of(InterfaceBlock.class)
                .map(blockAlreadyDecode::get)
                .orElse(Collections.emptyList())
                .stream()
                .filter(e -> e instanceof InterfaceBlock)
                .map(e -> (InterfaceBlock) e)
                .map(Block::getDecodedData)
                .collect(Collectors.toList());
        model.fields = Optional.of(FieldBlock.class)
                .map(blockAlreadyDecode::get)
                .orElse(Collections.emptyList())
                .stream()
                .filter(e -> e instanceof FieldBlock)
                .map(e -> (FieldBlock) e)
                .map(Block::getDecodedData)
                .collect(Collectors.toList());
        model.methods = Optional.of(MethodBlock.class)
                .map(blockAlreadyDecode::get)
                .orElse(Collections.emptyList())
                .stream()
                .filter(e -> e instanceof MethodBlock)
                .map(e -> (MethodBlock) e)
                .map(Block::getDecodedData)
                .collect(Collectors.toList());
        model.attributes = Optional.of(AttributeBlock.class)
                .map(blockAlreadyDecode::get)
                .orElse(Collections.emptyList())
                .stream()
                .filter(e -> e instanceof AttributeBlock)
                .map(e -> (AttributeBlock) e)
                .map(Block::getDecodedData)
                .collect(Collectors.toList());
        return model;
    }
}
