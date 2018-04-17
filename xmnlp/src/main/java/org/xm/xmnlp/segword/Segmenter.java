package org.xm.xmnlp.segword;

import org.xm.xmnlp.dic.*;
import org.xm.xmnlp.math.FinalSegmenter;
import org.xm.xmnlp.util.CharacterUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 生成所有切词可能的有向无环图 =DAG=
 * 基于 trie 树结构实现高效词图扫描
 *
 * Created by xuming on 2016/7/6.
 */
public class Segmenter {
    private static WordDict wordDict = WordDict.getInstance();
    private static FinalSegmenter finalSeg = FinalSegmenter.getInstance();

    /**
     * 生成DAG
     * @param sentence
     * @return
     */
    public Map<Integer, List<Integer>> createDAG(String sentence) {
        Map<Integer, List<Integer>> dag = new HashMap<Integer, List<Integer>>();
        DictSegment trie = wordDict.getTrie();
        char[] chars = sentence.toCharArray();
        int N = chars.length;
        int i = 0, j = 0;
        while (i < N) {
            Hit hit = trie.match(chars, i, j - i + 1);
            if (hit.isPrefix() || hit.isMatch()) {
                if (hit.isMatch()) {
                    if (!dag.containsKey(i)) {
                        List<Integer> value = new ArrayList<Integer>();
                        dag.put(i, value);
                        value.add(j);
                    } else
                        dag.get(i).add(j);
//                        System.out.print(sentence.charAt(j));
                }
                j += 1;
                if (j >= N) {
                    i += 1;
                    j = i;
                }
            } else {
                i += 1;
                j = i;
            }
        }
        for (i = 0; i < N; ++i) {
            if (!dag.containsKey(i)) {
                List<Integer> value = new ArrayList<Integer>();
                value.add(i);
                dag.put(i, value);

            }

        }

        return dag;
    }

    /**
     * 动态规划--基于词频最大切分组合
     * @param sentence
     * @param dag
     * @return
     */
    private Map<Integer, DictPair<Integer>> calc(String sentence, Map<Integer, List<Integer>> dag) {
        int N = sentence.length();
        HashMap<Integer, DictPair<Integer>> route = new HashMap<Integer, DictPair<Integer>>();
        route.put(N, new DictPair<Integer>(0, 0.0, ""));
        for (int i = N - 1; i > -1; i--) {
            DictPair<Integer> candidate = null;
            for (Integer x : dag.get(i)) {
                double freq = wordDict.getFreq(sentence.substring(i, x + 1)) + route.get(x + 1).freq;
                String nature = wordDict.getNature(sentence.substring(i, x + 1)) + route.get(x + 1).nature;
                if (null == candidate) {
                    candidate = new DictPair<Integer>(x, freq, nature);
                } else if (candidate.freq < freq) {
                    candidate.freq = freq;
                    candidate.key = x;
                    candidate.nature = nature;
                }
            }
            route.put(i, candidate);
        }
        return route;
    }

    /**
     * @param sentence
     * @return List<String>
     * @Title: word segmentation
     */
    private List<String> sentenceProcess(String sentence) {
        List<String> tokens = new ArrayList<String>();
        int N = sentence.length();
        Map<Integer, List<Integer>> dag = createDAG(sentence);
        Map<Integer, DictPair<Integer>> route = calc(sentence, dag);

        int x = 0;
        int y = 0;
        String buf;
        StringBuilder sb = new StringBuilder();
        while (x < N) {
            y = route.get(x).key + 1;
            String lword = sentence.substring(x, y);
            if (y - x == 1)
                sb.append(lword);
            else {
                if (sb.length() > 0) {
                    buf = sb.toString();
                    sb = new StringBuilder();
                    if (buf.length() == 1) {
                        tokens.add(buf);
                    } else {
                        if (wordDict.containsWord(buf))
                            tokens.add(buf);
                        else
                            finalSeg.cut(buf, tokens);
                    }
                }
                tokens.add(lword);
            }
            x = y;
        }
        buf = sb.toString();
        if (buf.length() > 0) {
            if (buf.length() == 1)
                tokens.add(buf);
            else {
                if (wordDict.containsWord(buf))
                    tokens.add(buf);
                else
                    finalSeg.cut(buf, tokens);
            }
        }
        return tokens;
    }

    /**
     * 中文分词
     * @param paragraph
     * @param mode
     * @return
     */
    public List<Item> process(String paragraph, SegMode mode) {
        List<Item> tokens = new ArrayList<Item>();
        StringBuilder sb = new StringBuilder();
        int offset = 0;
        for (int i = 0; i < paragraph.length(); ++i) {
            char ch = CharacterUtil.regularize(paragraph.charAt(i));// 字串先规范化处理
            if (CharacterUtil.ccFind(ch))
                sb.append(ch);
            else {
                if (sb.length() > 0) {
                    //process
                    if (mode == SegMode.SEARCH) {
                        tokens.addAll(searchMode(sb.toString(), offset));
                    } else {
                        tokens.addAll(indexMode(sb.toString(), offset));
                    }
                    sb = new StringBuilder();
                    offset = i;
                }
                tokens.add(new Item(paragraph.substring(i, i + 1), offset, ++offset, wordDict.getNature(paragraph.substring(i, i + 1))));
            }
        }
        if (sb.length() > 0) {
            if (mode == SegMode.SEARCH) {
                tokens.addAll(searchMode(sb.toString(), offset));
            } else {
                tokens.addAll(indexMode(sb.toString(), offset));
            }
        }

        return tokens;
    }

    /**
     * search模式
     * @param str
     * @param offset
     * @return
     */
    public List<Item> searchMode(String str, int offset) {
        List<Item> tokens = new ArrayList<Item>();
        for (String word : sentenceProcess(str)) {
            tokens.add(new Item(word, offset, offset += word.length(), wordDict.getNature(word)));
        }
        return tokens;
    }

    /**
     * index模式
     * @param str
     * @param offset
     * @return
     */
    public List<Item> indexMode(String str, int offset) {
        List<Item> tokens = new ArrayList<Item>();
        for (String token : sentenceProcess(str)) {
            if (token.length() > 2) {
                String gram2;
                int j = 0;
                for (; j < token.length() - 1; ++j) {
                    gram2 = token.substring(j, j + 2);
                    if (wordDict.containsWord(gram2))
                        tokens.add(new Item(gram2, offset + j, offset + j + 2, wordDict.getNature(gram2)));
                }
            }
            if (token.length() > 3) {
                String gram3;
                int j = 0;
                for (; j < token.length() - 2; ++j) {
                    gram3 = token.substring(j, j + 3);
                    if (wordDict.containsWord(gram3))
                        tokens.add(new Item(gram3, offset + j, offset + j + 3, wordDict.getNature(gram3)));

                }
            }
            tokens.add(new Item(token, offset, offset += token.length(), wordDict.getNature(token)));
        }
        return tokens;
    }


}
