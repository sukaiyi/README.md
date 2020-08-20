package com.sukaiyi.classstruct.analyzer.blocks.attrblock;

import com.sukaiyi.byteutils.analyzer.Block;
import com.sukaiyi.classstruct.analyzer.blocks.*;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * @author sukaiyi
 * @date 2020/08/13
 */
public class NextBlockAfterAttribute {

    private NextBlockAfterAttribute() {

    }

    @SuppressWarnings("rawtypes")
    public static Class<? extends Block> choose(Map<Class<?>, List<Block<?>>> blockAlreadyDecode) {
        // 本次要读取的属性总数
        Integer attributeCnt = Optional.of(AttributeCountBlock.class)
                .map(blockAlreadyDecode::get)
                .map(e -> e.get(0))
                .map(Block::getDecodedData)
                .map(e -> (Integer) e)
                .orElse(0);
        // 已经读取的属性数
        Integer attributeCntRead = Optional.of(AttributeBlock.class)
                .map(blockAlreadyDecode::get)
                .map(List::size)
                .orElse(0);
        if (attributeCnt > attributeCntRead) {
            return AttributeBlock.class;
        }

        // 字段总数
        Integer fieldCnt = Optional.of(FieldsCountBlock.class)
                .map(blockAlreadyDecode::get)
                .map(e -> e.get(0))
                .map(Block::getDecodedData)
                .map(e -> (Integer) e)
                .orElse(0);
        // 已经读取的字段数
        Integer fieldCntRead = Optional.of(FieldBlock.class)
                .map(blockAlreadyDecode::get)
                .map(List::size)
                .orElse(0);
        // 方法总数
        Integer methodCnt = Optional.of(MethodsCountBlock.class)
                .map(blockAlreadyDecode::get)
                .map(e -> e.get(0))
                .map(Block::getDecodedData)
                .map(e -> (Integer) e)
                .orElse(0);
        // 已经读取的方法数
        Integer methodCntRead = Optional.of(MethodBlock.class)
                .map(blockAlreadyDecode::get)
                .map(List::size)
                .orElse(0);
        if (fieldCnt > fieldCntRead) {
            return FieldBlock.class;
        } else if (Objects.equals(fieldCnt, fieldCntRead) && blockAlreadyDecode.get(MethodsCountBlock.class) == null) {
            return MethodsCountBlock.class;
        } else if (methodCnt > methodCntRead) {
            return MethodBlock.class;
        }
        if (AttributeCountBlock.ATTRIBUTE_STATE_LOCAL.get() != null) {
            // 读取 class 的 attribute
            blockAlreadyDecode.remove(AttributeCountBlock.class);
            blockAlreadyDecode.remove(AttributeBlock.class);
            AttributeCountBlock.ATTRIBUTE_STATE_LOCAL.remove();
            return AttributeCountBlock.class;
        }
        // 全部读取完毕，从魔数开始下一个字节码文件的解析
        return MagicBlock.class;
    }
}
