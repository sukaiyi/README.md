package com.sukaiyi.classstruct.analyzer;

import com.sukaiyi.byteutils.analyzer.AbstractStateBasedFileAnalyzer;
import com.sukaiyi.byteutils.analyzer.Block;
import com.sukaiyi.classstruct.analyzer.blocks.MagicBlock;

/**
 * @author sukaiyi
 * @date 2020/08/12
 */
public class JvmClassAnalyzer extends AbstractStateBasedFileAnalyzer<JvmClassRawModel> {
    @SuppressWarnings("rawtypes")
    @Override
    protected Class<? extends Block> initState() {
        return MagicBlock.class;
    }
}
