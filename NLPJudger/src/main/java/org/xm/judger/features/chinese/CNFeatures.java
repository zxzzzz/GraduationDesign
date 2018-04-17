package org.xm.judger.features.chinese;

import org.xm.judger.domain.CNEssayInstance;

import java.util.HashMap;

/**
 * @author xuming
 */
public interface CNFeatures {
    /**
     * 计算特征分数
     *
     * @param instance
     * @return
     */
    HashMap<String, Double> getFeatureScores(CNEssayInstance instance);

}
