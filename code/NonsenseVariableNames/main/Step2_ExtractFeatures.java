package FindCasualNames.main;

import FindCasualNames.entity.Features;
import FindCasualNames.entity.Relation;
import FindCasualNames.entity.RelationKey;
import util.Config;
import util.Util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class Step2_ExtractFeatures {
    public static String inFileName = "C:\\PHDONE\\ASE19\\majorChange\\FilteredName992.csv";
    public static String outFileName = "C:\\PHDONExia\\RejectedASE\\Calibration\\relationKeyFeaturesHashMap.csv";
    private static double projectNum = 992;
    private static double existNumThreshold = 1;
    static double yan =1;
    private static double methodThreshold = yan;
    private static double parametersThreshold = yan;
    private static double wordsThreshold = yan;

    private static boolean isMethodToSplit = true;
    private static boolean isparametersToSplit = true;
    private static boolean iswordsToSplit = true;

    public static void main(String[] args) throws Exception {
        long begintime =System.currentTimeMillis();
        Util.deleteIfExist(new File(outFileName));
        ArrayList<Relation> relations = readRelations(inFileName);
        System.out.println(relations.size());
        HashMap<RelationKey, HashSet<Relation>> map = relationsToMap(relations);
        System.out.println(map.size());
        HashMap<RelationKey, Features> relationKeyFeaturesHashMap = getFeatures(map);
        printRelationKeyFeaturesHashMap(relationKeyFeaturesHashMap);
        long endtime = System.currentTimeMillis();
        long costtime=(endtime - begintime);
        System.out.println(costtime);
    }

    private static ArrayList<Relation> readRelations(String inFileName) throws Exception {
        ArrayList<Relation> relations = new ArrayList<>();
        BufferedReader in = new BufferedReader(new FileReader(inFileName));
        String line;
        while ((line = in.readLine()) != null) {
            relations.add(Relation.constructRelationFrom(line));
        }
        return relations;
    }

    private static void printRelationKeyFeaturesHashMap(HashMap<RelationKey, Features> relationKeyFeaturesHashMap) throws Exception {
        BufferedWriter out;
        out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outFileName, true)));

        for (RelationKey relationKey :
                relationKeyFeaturesHashMap.keySet()) {
            if (relationKeyFeaturesHashMap.get(relationKey).toString().trim().length() != 0) {
                out.write(relationKey + Config.fengefu + relationKeyFeaturesHashMap.get(relationKey) + "\n");
                out.flush();
                System.out.println(relationKey + Config.fengefu + relationKeyFeaturesHashMap.get(relationKey));
            }
        }
        out.close();
    }

    private static HashMap<RelationKey, Features> getFeatures(HashMap<RelationKey, HashSet<Relation>> map) {
        map = criterion1(map);
        HashMap<RelationKey, Features> hashMap = new HashMap<>();
        for (RelationKey relationKey :
                map.keySet()) {
            HashSet<String> methodKeySet = criterion2(map.get(relationKey));
            HashSet<String> parametersKeySet = criterion3(map.get(relationKey));
            HashSet<String> wordsKeySet = criterion4(map.get(relationKey));
            Features features = new Features(methodKeySet, parametersKeySet, wordsKeySet);
            hashMap.put(relationKey, features);
        }
        return hashMap;
    }

    private static HashMap<RelationKey, HashSet<Relation>> criterion1(HashMap<RelationKey, HashSet<Relation>> map) {
        HashMap<RelationKey, HashSet<Relation>> result = new HashMap<>();
        for (RelationKey relationKey :
                map.keySet()) {
            HashSet<String> projectNameSet = new HashSet<>();
            for (Relation relation :
                    map.get(relationKey)) {
                projectNameSet.add(relation.projectName);
            }
            if (projectNameSet.size()*1.0 / projectNum >= existNumThreshold) {
                result.put(relationKey, map.get(relationKey));
            }
        }
        return result;
    }

    private static HashSet<String> criterion2(HashSet<Relation> set) {
        HashSet<HashSet<String>> wordsList = new HashSet<>();
        for (Relation relation :
                set) {
            HashSet<String> temp = new HashSet<>();
            temp.add(relation.methodName);
            wordsList.add(temp);
        }
        return getKeyWords(wordsList, methodThreshold, isMethodToSplit);
    }

    private static HashSet<String> criterion3(HashSet<Relation> set) {
        HashSet<HashSet<String>> wordsList = new HashSet<>();
        for (Relation relation :
                set) {
            wordsList.add(relation.parameters);
        }
        return getKeyWords(wordsList, parametersThreshold, isparametersToSplit);
    }

    private static HashSet<String> criterion4(HashSet<Relation> set) {
        HashSet<HashSet<String>> wordsList = new HashSet<>();
        for (Relation relation :
                set) {
            wordsList.add(relation.words);
        }
        return getKeyWords(wordsList, wordsThreshold, iswordsToSplit);
    }

    public static HashSet<String> getKeyWords(HashSet<HashSet<String>> wordsList, double threshold, boolean isToSplit) {
        if (isToSplit) {
            wordsList = toSplit(wordsList);
        } else {
            wordsList = removeFirstLast(wordsList);
        }
        HashSet<String> allWords = getAllWords(wordsList);
        HashSet<String> keyWords = getKeyWords(allWords, wordsList, threshold);
        return keyWords;
    }

    private static HashSet<HashSet<String>> removeFirstLast(HashSet<HashSet<String>> wordsList) {
        HashSet<HashSet<String>> result = new HashSet<>();
        for (HashSet<String> words :
                wordsList) {
            HashSet<String> tempWords = new HashSet<>();
            for (String word :
                    words) {
                tempWords.add(RelationKey.removeFirstLastNumXiahuaxian(word));
            }
            result.add(tempWords);
        }
        return result;
    }

    private static HashSet<String> getKeyWords(HashSet<String> allWords, HashSet<HashSet<String>> wordsList, double threshold) {
        HashSet<String> result = new HashSet<>();

        int totalNum = wordsList.size();
        for (String word :
                allWords) {
            if (existNum(word, wordsList) * 1.0 / totalNum > threshold) {
                result.add(word);
            }
        }
        return result;
    }

    private static int existNum(String word, HashSet<HashSet<String>> wordsList) {
        int num = 0;
        for (HashSet<String> words :
                wordsList) {
            if (words.contains(word)) {
                num++;
            }
        }
        return num;
    }

    private static HashSet<HashSet<String>> toSplit(HashSet<HashSet<String>> wordsList) {
        HashSet<HashSet<String>> result = new HashSet<>();
        for (HashSet<String> words :
                wordsList) {
            HashSet<String> tempWords = new HashSet<>();
            for (String word :
                    words) {
                if (Util.split(word) != null) {
                    tempWords.addAll(Util.split(word));
                }
            }
            result.add(tempWords);
        }
        return result;
    }

    private static HashSet<String> getAllWords(HashSet<HashSet<String>> wordsList) {
        HashSet<String> result = new HashSet<>();
        for (HashSet<String> set :
                wordsList) {
            result.addAll(set);
        }
        return result;
    }

    private static HashMap<RelationKey, HashSet<Relation>> relationsToMap(ArrayList<Relation> relations) {
        HashMap<RelationKey, HashSet<Relation>> map = new HashMap<>();
        for (Relation relation :
                relations) {
            RelationKey relationKey = new RelationKey(relation.identifierType, relation.type, relation.name);
            if (map.containsKey(relationKey)) {
                map.get(relationKey).add(relation);
            } else {
                HashSet<Relation> relationHashSet = new HashSet<>();
                relationHashSet.add(relation);
                map.put(relationKey, relationHashSet);
            }
        }
        return map;
    }


    public static ArrayList<Relation> getRelations(String inFileName) throws Exception {
        ArrayList<Relation> relations = new ArrayList<>();
        BufferedReader in = new BufferedReader(new FileReader(inFileName));
        String line;
        while ((line = in.readLine()) != null) {
            Relation relation = Relation.constructRelationFrom(line);
            relations.add(relation);
        }
        in.close();
        return relations;
    }
}

