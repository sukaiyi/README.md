package com.sukaiyi.classstruct.analyzer.blocks.attrblock;

import com.sukaiyi.byteutils.analyzer.Block;
import com.sukaiyi.byteutils.utils.ByteUtils;
import com.sukaiyi.classstruct.analyzer.JvmClassRawModel;
import com.sukaiyi.classstruct.analyzer.blocks.MagicBlock;

import java.util.List;
import java.util.Map;

/**
 * @author sukaiyi
 * @date 2020/08/13
 */
public final class AttributeCountBlock implements Block<Integer> {

    /**
     * 标志了当前读取的 Attribute 所属于哪一个数据块？(field, method, class),
     * 所属于 class 时，值为null
     * 所属于 field 或者 method 时，值为对应的 block 对象
     */
    public static final ThreadLocal<Block<?>> ATTRIBUTE_STATE_LOCAL = new ThreadLocal<>();

    private int data = 0;

    @Override
    public long size(Map<Class<?>, List<Block<?>>> blockAlreadyDecode) {
        return 2;
    }

    @Override
    public void decode(Map<Class<?>, List<Block<?>>> blockAlreadyDecode, byte[] bytes, int start, int len, boolean finished) {
        this.data = (int) ByteUtils.byteToUnsignedLong(bytes, start, len);
    }

    @Override
    public Integer getDecodedData() {
        return data;
    }

    @Override
    @SuppressWarnings("all")
    public Class<? extends Block> next(Map<Class<?>, List<Block<?>>> blockAlreadyDecode, List ret) {
        if (data > 0) {
            return AttributeBlock.class;
        }
        Class<? extends Block> next = NextBlockAfterAttribute.choose(blockAlreadyDecode);
        if (next == MagicBlock.class) {
            ret.add(JvmClassRawModel.of(blockAlreadyDecode));
        }
        return next;
    }
}
