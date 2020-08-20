package com.sukaiyi.classstruct.analyzer.blocks;

import com.sukaiyi.byteutils.analyzer.Block;
import com.sukaiyi.byteutils.utils.ByteUtils;

import java.util.List;
import java.util.Map;

/**
 * @author sukaiyi
 * @date 2020/08/12
 */
public class ConstantPoolCountBlock implements Block<Integer> {

    private int data = 0;

    @Override
    public long size(Map<Class<?>, List<Block<?>>> blockAlreadyDecode) {
        return 2;
    }

    @Override
    public void decode(Map<Class<?>, List<Block<?>>> blockAlreadyDecode, byte[] bytes, int start, int len, boolean finished) {
        this.data = (int) ByteUtils.byteToUnsignedLong(bytes, start, len) - 1;
    }

    @Override
    public Integer getDecodedData() {
        return data;
    }

    @Override
    @SuppressWarnings("rawtypes")
    public Class<? extends Block> next(Map<Class<?>, List<Block<?>>> blockAlreadyDecode, List ret) {
        return ConstantPoolInfoTagBlock.class;
    }
}
