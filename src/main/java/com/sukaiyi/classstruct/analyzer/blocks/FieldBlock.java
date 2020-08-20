package com.sukaiyi.classstruct.analyzer.blocks;

import com.sukaiyi.byteutils.analyzer.Block;
import com.sukaiyi.byteutils.utils.ByteUtils;
import com.sukaiyi.classstruct.analyzer.blocks.attrblock.AttributeBlock;
import com.sukaiyi.classstruct.analyzer.blocks.attrblock.AttributeCountBlock;

import java.util.List;
import java.util.Map;

/**
 * @author sukaiyi
 * @date 2020/08/12
 */
public class FieldBlock implements Block<FieldBlock.Data> {

    private final FieldBlock.Data data = new Data();

    @Override
    public long size(Map<Class<?>, List<Block<?>>> blockAlreadyDecode) {
        return 6;
    }

    @Override
    public void decode(Map<Class<?>, List<Block<?>>> blockAlreadyDecode, byte[] bytes, int start, int len, boolean finished) {
        this.data.accessFlag = (int) ByteUtils.byteToUnsignedLong(bytes, start, 2);
        this.data.nameIndex = (int) ByteUtils.byteToUnsignedLong(bytes, start + 2, 2);
        this.data.descriptorIndex = (int) ByteUtils.byteToUnsignedLong(bytes, start + 4, 2);
    }

    @Override
    public FieldBlock.Data getDecodedData() {
        return data;
    }

    @Override
    @SuppressWarnings("rawtypes")
    public Class<? extends Block> next(Map<Class<?>, List<Block<?>>> blockAlreadyDecode, List ret) {
        blockAlreadyDecode.remove(AttributeCountBlock.class);
        blockAlreadyDecode.remove(AttributeBlock.class);
        AttributeCountBlock.ATTRIBUTE_STATE_LOCAL.set(this);
        return AttributeCountBlock.class;
    }

    @lombok.Data
    public static final class Data {
        private int accessFlag;
        private int nameIndex;
        private int descriptorIndex;
        private List<AttributeBlock.Data> attributes;
    }
}
