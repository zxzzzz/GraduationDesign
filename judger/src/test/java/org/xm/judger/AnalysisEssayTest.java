package org.xm.judger;

import org.junit.Test;
import org.xm.judger.analyzer.FeatureHandler;
import org.xm.judger.domain.CNEssayInstance;
import org.xm.judger.parser.CNEssayInstanceParser;

import java.io.IOException;
import java.util.ArrayList;

/**
 * @author xuming
 */
public class AnalysisEssayTest {
    @Test
    public void testCN() throws IOException {
        //文章路径
        String trainSetPath = "data/jinyong";
        //保存路径
        String saveTrainFeaturesPath = "data/jinyong_training_result.arff";
        //中文解析器
        CNEssayInstanceParser parser = new CNEssayInstanceParser();
        //加载中文文章 设置内容/标题
        ArrayList<CNEssayInstance> instances = parser.load(trainSetPath);
        //设置文章
        Judger.setCNInstances(instances);
        //计算特征分数
        ArrayList<CNEssayInstance> instancesFeatures = FeatureHandler.getFeatures(instances);
        // Now we have all the instances and features
        // use any Machine Learning Tools (such as Weka)
        FeatureHandler.saveFeatures(instancesFeatures, saveTrainFeaturesPath);
    }

    @Test
    public void testCN_bajin() throws IOException {
        String trainSetPath = "data/bajin/A";
        String saveTrainFeaturesPath = "data/bajin_novels_A_features.arff";

        CNEssayInstanceParser parser = new CNEssayInstanceParser();
        // Parse the input training file
        ArrayList<CNEssayInstance> instances = parser.load(trainSetPath);
        Judger.setCNInstances(instances);

        ArrayList<CNEssayInstance> instancesFeatures = FeatureHandler.getFeatures(instances);
        FeatureHandler.saveFeatures(instancesFeatures, saveTrainFeaturesPath);
    }
}
