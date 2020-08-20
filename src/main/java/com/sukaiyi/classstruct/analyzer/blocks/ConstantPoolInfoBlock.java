package com.sukaiyi.classstruct.analyzer.blocks;

import com.sukaiyi.byteutils.analyzer.Block;
import com.sukaiyi.byteutils.utils.ByteUtils;
import lombok.EqualsAndHashCode;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * @author sukaiyi
 * @date 2020/08/12
 */
public class ConstantPoolInfoBlock implements Block<ConstantPoolInfoBlock.Data> {

    public static final int CONSTANT_TAG_UTF8 = 1;
    public static final int CONSTANT_TAG_INTEGER = 3;
    public static final int CONSTANT_TAG_FLOAT = 4;
    public static final int CONSTANT_TAG_LONG = 5;
    public static final int CONSTANT_TAG_DOUBLE = 6;
    public static final int CONSTANT_TAG_CLASS = 7;
    public static final int CONSTANT_TAG_STRING = 8;
    public static final int CONSTANT_TAG_FIELD_REF = 9;
    public static final int CONSTANT_TAG_METHOD_REF = 10;
    public static final int CONSTANT_TAG_INTERFACE_METHOD_REF = 11;
    public static final int CONSTANT_TAG_NAME_AND_TYPE = 12;
    public static final int CONSTANT_TAG_METHOD_HANDLE = 15;
    public static final int CONSTANT_TAG_METHOD_TYPE = 16;
    public static final int CONSTANT_TAG_INVOKE_DYNAMIC = 18;

    private static final Map<Integer, Integer> SIZE_MAP = new HashMap<Integer, Integer>() {{
        put(CONSTANT_TAG_UTF8, 2);
        put(CONSTANT_TAG_INTEGER, 4);
        put(CONSTANT_TAG_FLOAT, 4);
        put(CONSTANT_TAG_LONG, 8);
        put(CONSTANT_TAG_DOUBLE, 8);
        put(CONSTANT_TAG_CLASS, 2);
        put(CONSTANT_TAG_STRING, 2);
        put(CONSTANT_TAG_FIELD_REF, 4);
        put(CONSTANT_TAG_METHOD_REF, 4);
        put(CONSTANT_TAG_INTERFACE_METHOD_REF, 4);
        put(CONSTANT_TAG_NAME_AND_TYPE, 4);
        put(CONSTANT_TAG_METHOD_HANDLE, 3);
        put(CONSTANT_TAG_METHOD_TYPE, 2);
        put(CONSTANT_TAG_INVOKE_DYNAMIC, 4);
    }};

    Data data;
    private Integer pos = 0;

    @Override
    public long size(Map<Class<?>, List<Block<?>>> blockAlreadyDecode) {
        List<Block<?>> tagBlocks = blockAlreadyDecode.get(ConstantPoolInfoTagBlock.class);
        Block<?> lastTagBlock = tagBlocks.get(tagBlocks.size() - 1);
        Integer tag = (Integer) lastTagBlock.getDecodedData();
        Integer size = SIZE_MAP.getOrDefault(tag, 0);
        if (data == null) {
            data = new Data();
            data.setTag(tag);
            data.setBytes(new byte[size]);
        }
        // Long 和 Double 占用两个常量池条目，所以在 Long/Double 的常量后面添加一个 null 占位
        // 参考 https://docs.oracle.com/javase/specs/jvms/se8/html/jvms-4.html#jvms-4.4.5
        if (tag == ConstantPoolInfoBlock.CONSTANT_TAG_LONG || tag == ConstantPoolInfoBlock.CONSTANT_TAG_DOUBLE) {
            blockAlreadyDecode.computeIfAbsent(ConstantPoolInfoBlock.class, key -> new ArrayList<>()).add(null);
        }
        return size;
    }

    @Override
    public void decode(Map<Class<?>, List<Block<?>>> blockAlreadyDecode, byte[] bytes, int start, int len, boolean finished) {
        for (int i = start; i < start + len; i++) {
            data.bytes[pos++] = bytes[i];
        }
    }

    @Override
    public ConstantPoolInfoBlock.Data getDecodedData() {
        return data;
    }

    @Override
    @SuppressWarnings("rawtypes")
    public Class<? extends Block> next(Map<Class<?>, List<Block<?>>> blockAlreadyDecode, List ret) {
        Integer constantCnt = (Integer) blockAlreadyDecode.get(ConstantPoolCountBlock.class).get(0).getDecodedData();
        List<Block<?>> constantBlocksRead = blockAlreadyDecode.get(ConstantPoolInfoBlock.class);
        Integer constantCntRead = constantBlocksRead.size();
        if (constantCnt >= constantCntRead) {
            List<Block<?>> tagBlocks = blockAlreadyDecode.get(ConstantPoolInfoTagBlock.class);
            Block<?> lastTagBlock = tagBlocks.get(tagBlocks.size() - 1);
            Integer tag = (Integer) lastTagBlock.getDecodedData();
            if (tag == CONSTANT_TAG_UTF8) {
                return ConstantPoolInfoUtf8Block.class;
            }
            return constantCnt > constantCntRead ? ConstantPoolInfoTagBlock.class : AccessFlagBlock.class;
        }
        for (Block<?> block : constantBlocksRead) {
            if (block != null) {
                ConstantPoolInfoBlock poolInfoBlock = (ConstantPoolInfoBlock) block;
                poolInfoBlock.data = poolInfoBlock.data.toActualType();
            }
        }
        return AccessFlagBlock.class;
    }

    public static final class ConstantPoolInfoUtf8Block implements Block<byte[]> {

        private byte[] data;
        private int pos = 0;

        @Override
        public long size(Map<Class<?>, List<Block<?>>> blockAlreadyDecode) {
            List<Block<?>> poolInfoBlocks = blockAlreadyDecode.get(ConstantPoolInfoBlock.class);
            Block<?> lastBlock = poolInfoBlocks.get(poolInfoBlocks.size() - 1);
            ConstantPoolInfoBlock.Data data = (ConstantPoolInfoBlock.Data) lastBlock.getDecodedData();
            byte[] lengthBytes = data.getBytes();
            int length = (int) ByteUtils.byteToUnsignedLong(lengthBytes, 0, lengthBytes.length);
            if (this.data == null) {
                this.data = new byte[length];
            }
            return length;
        }

        @Override
        public void decode(Map<Class<?>, List<Block<?>>> blockAlreadyDecode, byte[] bytes, int start, int len, boolean finished) {
            for (int i = start; i < start + len; i++) {
                data[pos++] = bytes[i];
            }
            if (finished) {
                List<Block<?>> poolInfoBlocks = blockAlreadyDecode.get(ConstantPoolInfoBlock.class);
                Block<?> lastBlock = poolInfoBlocks.get(poolInfoBlocks.size() - 1);
                ConstantPoolInfoBlock.Data data = (ConstantPoolInfoBlock.Data) lastBlock.getDecodedData();
                data.setBytes(this.data);
            }
        }

        @Override
        public byte[] getDecodedData() {
            return data;
        }

        @Override
        @SuppressWarnings("rawtypes")
        public Class<? extends Block> next(Map<Class<?>, List<Block<?>>> blockAlreadyDecode, List ret) {
            Integer constantCnt = (Integer) blockAlreadyDecode.get(ConstantPoolCountBlock.class).get(0).getDecodedData();
            List<Block<?>> constantBlocksRead = blockAlreadyDecode.get(ConstantPoolInfoBlock.class);
            Integer constantCntRead = constantBlocksRead.size();
            if (constantCnt > constantCntRead) {
                return ConstantPoolInfoTagBlock.class;
            }
            for (Block<?> block : constantBlocksRead) {
                if (block != null) {
                    ConstantPoolInfoBlock poolInfoBlock = (ConstantPoolInfoBlock) block;
                    poolInfoBlock.data = poolInfoBlock.data.toActualType();
                }
            }
            return AccessFlagBlock.class;
        }
    }


    @lombok.Data
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class Data {

        public static final Map<Integer, Function<Data, Data>> FUNC_MAP = new HashMap<Integer, Function<Data, Data>>() {{
            put(CONSTANT_TAG_UTF8, d -> new Utf8Value(d.tag, d.bytes));
            put(CONSTANT_TAG_INTEGER, d -> new IntegerValue(d.tag, d.bytes));
            put(CONSTANT_TAG_FLOAT, d -> new FloatValue(d.tag, d.bytes));
            put(CONSTANT_TAG_LONG, d -> new LongValue(d.tag, d.bytes));
            put(CONSTANT_TAG_DOUBLE, d -> new DoubleValue(d.tag, d.bytes));
            put(CONSTANT_TAG_CLASS, d -> new ClassValue(d.tag, d.bytes));
            put(CONSTANT_TAG_STRING, d -> new StringValue(d.tag, d.bytes));
            put(CONSTANT_TAG_FIELD_REF, d -> new FieldRefValue(d.tag, d.bytes));
            put(CONSTANT_TAG_METHOD_REF, d -> new MethodRefValue(d.tag, d.bytes));
            put(CONSTANT_TAG_INTERFACE_METHOD_REF, d -> new InterfaceMethodRefValue(d.tag, d.bytes));
            put(CONSTANT_TAG_NAME_AND_TYPE, d -> new NameAndTypeValue(d.tag, d.bytes));
            put(CONSTANT_TAG_METHOD_HANDLE, d -> new MethodHandleValue(d.tag, d.bytes));
            put(CONSTANT_TAG_METHOD_TYPE, d -> new MethodTypeValue(d.tag, d.bytes));
            put(CONSTANT_TAG_INVOKE_DYNAMIC, d -> new InvokeDynamicValue(d.tag, d.bytes));
        }};

        private int tag;
        private byte[] bytes;

        public Data toActualType() {
            return FUNC_MAP.get(tag).apply(this);
        }
    }

    @EqualsAndHashCode(callSuper = true)
    @lombok.Data
    @lombok.AllArgsConstructor
    public static final class Utf8Value extends Data {
        private String value;

        public Utf8Value(Integer tag, byte[] bytes) {
            super(tag, bytes);
            this.value = new String(bytes, StandardCharsets.UTF_8);
        }
    }

    @EqualsAndHashCode(callSuper = true)
    @lombok.Data
    @lombok.AllArgsConstructor
    public static final class IntegerValue extends Data {
        private Integer value;

        public IntegerValue(Integer tag, byte[] bytes) {
            super(tag, bytes);
            this.value = Math.toIntExact(ByteUtils.byteToUnsignedLong(bytes, 0, bytes.length));
        }
    }

    @EqualsAndHashCode(callSuper = true)
    @lombok.Data
    @lombok.AllArgsConstructor
    public static final class FloatValue extends Data {
        private Float value;

        public FloatValue(Integer tag, byte[] bytes) {
            super(tag, bytes);
            this.value = null;
        }
    }

    @EqualsAndHashCode(callSuper = true)
    @lombok.Data
    @lombok.AllArgsConstructor
    public static final class LongValue extends Data {
        private Long value;

        public LongValue(Integer tag, byte[] bytes) {
            super(tag, bytes);
            this.value = ByteUtils.byteToUnsignedLong(bytes, 0, bytes.length);
        }
    }

    @EqualsAndHashCode(callSuper = true)
    @lombok.Data
    @lombok.AllArgsConstructor
    public static final class DoubleValue extends Data {
        private Double value;

        public DoubleValue(Integer tag, byte[] bytes) {
            super(tag, bytes);
            this.value = null;
        }
    }

    @EqualsAndHashCode(callSuper = true)
    @lombok.Data
    @lombok.AllArgsConstructor
    public static final class ClassValue extends Data {
        private Integer fullNameIndex;

        public ClassValue(Integer tag, byte[] bytes) {
            super(tag, bytes);
            this.fullNameIndex = Math.toIntExact(ByteUtils.byteToUnsignedLong(bytes, 0, bytes.length));
        }
    }

    @EqualsAndHashCode(callSuper = true)
    @lombok.Data
    @lombok.AllArgsConstructor
    public static final class StringValue extends Data {
        private Integer index;

        public StringValue(Integer tag, byte[] bytes) {
            super(tag, bytes);
            this.index = Math.toIntExact(ByteUtils.byteToUnsignedLong(bytes, 0, bytes.length));
        }
    }

    @EqualsAndHashCode(callSuper = true)
    @lombok.Data
    @lombok.AllArgsConstructor
    public static final class FieldRefValue extends Data {
        private Integer classInfoIndex;
        private Integer nameAndTypeIndex;

        public FieldRefValue(Integer tag, byte[] bytes) {
            super(tag, bytes);
            this.classInfoIndex = Math.toIntExact(ByteUtils.byteToUnsignedLong(bytes, 0, 2));
            this.nameAndTypeIndex = Math.toIntExact(ByteUtils.byteToUnsignedLong(bytes, 2, 2));
        }
    }

    @EqualsAndHashCode(callSuper = true)
    @lombok.Data
    @lombok.AllArgsConstructor
    public static final class MethodRefValue extends Data {
        private Integer classInfoIndex;
        private Integer nameAndTypeIndex;

        public MethodRefValue(Integer tag, byte[] bytes) {
            super(tag, bytes);
            this.classInfoIndex = Math.toIntExact(ByteUtils.byteToUnsignedLong(bytes, 0, 2));
            this.nameAndTypeIndex = Math.toIntExact(ByteUtils.byteToUnsignedLong(bytes, 2, 2));
        }
    }

    @EqualsAndHashCode(callSuper = true)
    @lombok.Data
    @lombok.AllArgsConstructor
    public static final class InterfaceMethodRefValue extends Data {
        private Integer classInfoIndex;
        private Integer nameAndTypeIndex;

        public InterfaceMethodRefValue(Integer tag, byte[] bytes) {
            super(tag, bytes);
            this.classInfoIndex = Math.toIntExact(ByteUtils.byteToUnsignedLong(bytes, 0, 2));
            this.nameAndTypeIndex = Math.toIntExact(ByteUtils.byteToUnsignedLong(bytes, 2, 2));
        }
    }

    @EqualsAndHashCode(callSuper = true)
    @lombok.Data
    @lombok.AllArgsConstructor
    public static final class NameAndTypeValue extends Data {
        private Integer nameIndex;
        private Integer typeIndex;

        public NameAndTypeValue(Integer tag, byte[] bytes) {
            super(tag, bytes);
            this.nameIndex = Math.toIntExact(ByteUtils.byteToUnsignedLong(bytes, 0, 2));
            this.typeIndex = Math.toIntExact(ByteUtils.byteToUnsignedLong(bytes, 2, 2));
        }
    }

    @EqualsAndHashCode(callSuper = true)
    @lombok.Data
    @lombok.AllArgsConstructor
    public static final class MethodHandleValue extends Data {
        private Integer referenceKind;
        private Integer referenceIndex;

        public MethodHandleValue(Integer tag, byte[] bytes) {
            super(tag, bytes);
            this.referenceKind = Math.toIntExact(ByteUtils.byteToUnsignedLong(bytes, 0, 1));
            this.referenceIndex = Math.toIntExact(ByteUtils.byteToUnsignedLong(bytes, 1, 2));
        }
    }

    @EqualsAndHashCode(callSuper = true)
    @lombok.Data
    @lombok.AllArgsConstructor
    public static final class MethodTypeValue extends Data {
        private Integer descriptorIndex;

        public MethodTypeValue(Integer tag, byte[] bytes) {
            super(tag, bytes);
            this.descriptorIndex = Math.toIntExact(ByteUtils.byteToUnsignedLong(bytes, 0, 2));
        }
    }

    @EqualsAndHashCode(callSuper = true)
    @lombok.Data
    @lombok.AllArgsConstructor
    public static final class InvokeDynamicValue extends Data {
        private Integer bootstrapMethodAttrIndex;
        private Integer namAndTypeIndex;

        public InvokeDynamicValue(Integer tag, byte[] bytes) {
            super(tag, bytes);
            this.bootstrapMethodAttrIndex = Math.toIntExact(ByteUtils.byteToUnsignedLong(bytes, 0, 2));
            this.namAndTypeIndex = Math.toIntExact(ByteUtils.byteToUnsignedLong(bytes, 2, 2));
        }
    }

}
