package com.sukaiyi.classstruct.analyzer.blocks;

import com.sukaiyi.byteutils.analyzer.Block;

import java.util.List;
import java.util.Map;

/**
 * @author sukaiyi
 * @date 2020/08/12
 */
public class MagicBlock implements Block<byte[]> {

    private final byte[] data = new byte[4];
    private int pos = 0;

    @Override
    public long size(Map<Class<?>, List<Block<?>>> blockAlreadyDecode) {
        return 4;
    }

    @Override
    public void decode(Map<Class<?>, List<Block<?>>> blockAlreadyDecode, byte[] bytes, int start, int len, boolean finished) {
        for (int i = start; i < start + len; i++) {
            data[pos++] = bytes[i];
        }
    }

    @Override
    public byte[] getDecodedData() {
        return data;
    }

    @Override
    @SuppressWarnings("rawtypes")
    public Class<? extends Block> next(Map<Class<?>, List<Block<?>>> blockAlreadyDecode, List ret) {
        return MinorVersionBlock.class;
    }
}
