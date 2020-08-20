package com.sukaiyi.classstruct.analyzer.blocks.attrblock;

import com.sukaiyi.byteutils.analyzer.Block;
import com.sukaiyi.byteutils.utils.ByteUtils;
import com.sukaiyi.byteutils.utils.ReflectUtils;
import com.sukaiyi.classstruct.analyzer.JvmClassRawModel;
import com.sukaiyi.classstruct.analyzer.blocks.MagicBlock;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author sukaiyi
 * @date 2020/08/12
 */
public class AttributeBlock implements Block<AttributeBlock.Data> {

    public static final int ATTRIBUTE_HEAD_LENGTH = 6;

    private final AttributeBlock.Data data = new Data();

    @Override
    public long size(Map<Class<?>, List<Block<?>>> blockAlreadyDecode) {
        return ATTRIBUTE_HEAD_LENGTH;
    }

    @Override
    public void decode(Map<Class<?>, List<Block<?>>> blockAlreadyDecode, byte[] bytes, int start, int len, boolean finished) {
        this.data.attributeNameIndex = (int) ByteUtils.byteToUnsignedLong(bytes, start, 2);
        this.data.attributeLength = (int) ByteUtils.byteToUnsignedLong(bytes, start + 2, 4);
    }

    @Override
    public AttributeBlock.Data getDecodedData() {
        return data;
    }

    @Override
    @SuppressWarnings("all")
    public Class<? extends Block> next(Map<Class<?>, List<Block<?>>> blockAlreadyDecode, List ret) {
        if (data.attributeLength > 0) {
            return AttributeContentBlock.class;
        }
        Class<? extends Block> next = NextBlockAfterAttribute.choose(blockAlreadyDecode);
        if (next == MagicBlock.class) {
            ret.add(JvmClassRawModel.of(blockAlreadyDecode));
        }
        return next;
    }

    @lombok.Data
    public static final class Data {
        private int attributeNameIndex;
        private long attributeLength;
        private byte[] attributeData;
    }

    public static final class AttributeContentBlock implements Block<byte[]> {

        private byte[] data;
        private int pos = 0;

        @Override
        public long size(Map<Class<?>, List<Block<?>>> blockAlreadyDecode) {
            List<Block<?>> blocks = blockAlreadyDecode.get(AttributeBlock.class);
            Block<?> lastBlock = blocks.get(blocks.size() - 1);
            Data data = (Data) lastBlock.getDecodedData();
            long length = data.getAttributeLength();
            if (this.data == null) {
                this.data = new byte[(int) Math.min(length, Integer.MAX_VALUE)];
            }
            return length;
        }

        @Override
        @SuppressWarnings("all")
        public void decode(Map<Class<?>, List<Block<?>>> blockAlreadyDecode, byte[] bytes, int start, int len, boolean finished) {
            for (int i = start; i < start + len; i++) {
                data[pos++] = bytes[i];
            }
            if (finished) {
                List<Block<?>> blocks = blockAlreadyDecode.get(AttributeBlock.class);
                Block<?> lastBlock = blocks.get(blocks.size() - 1);
                Data data = (Data) lastBlock.getDecodedData();
                data.setAttributeData(this.data);
                blockAlreadyDecode.remove(AttributeContentBlock.class);

                Block<?> block = AttributeCountBlock.ATTRIBUTE_STATE_LOCAL.get();
                if (block != null) {
                    Object decodedData = block.getDecodedData();
                    List attributes = (List) ReflectUtils.getFieldValue(decodedData, "attributes");
                    if (attributes == null) {
                        attributes = new ArrayList();
                        ReflectUtils.setFieldValue(decodedData, "attributes", attributes);
                    }
                    attributes.add(data);
                }
            }
        }

        @Override
        public byte[] getDecodedData() {
            return data;
        }

        @Override
        @SuppressWarnings("all")
        public Class<? extends Block> next(Map<Class<?>, List<Block<?>>> blockAlreadyDecode, List ret) {
            Class<? extends Block> next = NextBlockAfterAttribute.choose(blockAlreadyDecode);
            if (next == MagicBlock.class) {
                ret.add(JvmClassRawModel.of(blockAlreadyDecode));
            }
            return next;
        }
    }
}
