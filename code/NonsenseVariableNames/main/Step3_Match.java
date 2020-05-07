package FindCasualNames.main;

import FindCasualNames.entity.Features;
import FindCasualNames.entity.Relation;
import FindCasualNames.entity.RelationKey;
import FindCasualNames.relation.ForVariable;
import util.Config;
import util.Util;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;


public class Step3_Match {
    static double jiang =0.15;
    private static double methodThreshold = jiang;
    private static double parameterThreshold =jiang;
    private static double wordsThreshold =jiang;
    private static String featureFile = "C:\\PHDONExia\\RejectedASE\\Calibration\\relationKeyFeaturesHashMap.csv";
    private static String toCheckFilteredNamesFile = "C:\\PHDONExia\\RejectedASE\\RQ4\\goldenset.csv";
    private static String tempOutFileName = "C:\\PHDONExia\\RejectedASE\\RQ4\\goldensetloop.csv";

    public static void main(String[] args) throws Exception {
        Util.deleteIfExist(new File(tempOutFileName));
        BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(tempOutFileName, true)));
        int precisionFenmu = 0, precisionFenzi = 0;
        int recallFenmu = 0, recallFenzi = 0;
        int accuracyFenzi = 0;

        HashMap<RelationKey, Features> relationKeyFeaturesHashMap = readRelationKeyFeaturesHashMap();
        ArrayList<Relation> toCheckRelations = Step2_ExtractFeatures.getRelations(toCheckFilteredNamesFile);
        ArrayList<String> predictions = new ArrayList<>();
        ArrayList<String> trueValues = new ArrayList<>();
        for (Relation toCheckRelation :
                toCheckRelations) {
            String prediction = getPredictionFor(toCheckRelation, relationKeyFeaturesHashMap);
            trueValues.add(toCheckRelation.trueLabel);
            out.write(prediction + Config.fengefu + toCheckRelation.trueLabel + Config.fengefu + toCheckRelation + "\n");
            out.flush();
            predictions.add(prediction);
        }

        for (int i = 0; i < predictions.size(); i++) {
            if (predictions.get(i).equals(trueValues.get(i))) {
                accuracyFenzi++;
            }
            if (trueValues.get(i).equals("1")) {
                recallFenmu++;
            }
            if (predictions.get(i).equals("1")) {
                precisionFenmu++;
            }
            if (predictions.get(i).equals("1") && trueValues.get(i).equals("1")) {
                precisionFenzi++;
                recallFenzi++;
            }
        }
        out.close();
        double P = precisionFenzi*1.0/precisionFenmu;
        double R = recallFenzi*1.0/recallFenmu;
        System.out.println("Precision:\t" + (precisionFenzi*1.0/precisionFenmu));
        System.out.println("Recall:\t" + (recallFenzi*1.0/recallFenmu));
        System.out.println("Accuracy:\t" + (accuracyFenzi*1.0/toCheckRelations.size()));
        System.out.println("F1\t" + ((2*P*R*1.0)/(P+R)));
    }


    // 1 means need rename; 0 not
    private static String getPredictionFor(Relation relation, HashMap<RelationKey, Features> relationKeyFeaturesHashMap) {
        RelationKey relationKey = new RelationKey(relation.identifierType, relation.type, relation.name);
        if (!relationKeyFeaturesHashMap.containsKey(relationKey)) {
            return "1";
        } else {
            if (relationKey.identifierType.equals(ForVariable.NAME)) {
                return "0";
            }
            boolean methodCheck;
            if (relation.methodName.trim().length() == 0) {
                methodCheck = false;
            } else {
                methodCheck = checkMethods(relation.methodName, relationKeyFeaturesHashMap.get(relationKey).methods);
            }
            boolean parameterCheck;
            if (relation.methodName.trim().length() == 0) {
                parameterCheck = false;
            } else {
                parameterCheck = checkParameters(relation.parameters, relationKeyFeaturesHashMap.get(relationKey).parameters);
            }
            boolean wordCheck;
            if (relation.words.size() == 0) {
                wordCheck = false;
            } else {
                wordCheck = checkWords(relation.words, relationKeyFeaturesHashMap.get(relationKey).words);
            }
            if (methodCheck || parameterCheck || wordCheck) {
                return "0";
            } else {
                return "1";
            }
        }
    }

    private static boolean checkTokens(HashSet<String> toCheckWords, HashSet<String> features, double threshold) {
        int total = features.size();
        if (total == 0) {
            return false;
        }
        int num = 0;
        for (String feature :
                features) {
            if (toCheckWords.contains(feature)) {
                num++;
            }
        }
        if ((num * 1.0 / total) >= threshold) {
            return true;
        } else {
            return false;
        }
    }

    private static boolean checkParameters(HashSet<String> toCheckParameters, HashSet<String> features) {
        return checkTokens(toCheckParameters, features, parameterThreshold);
    }

    private static boolean checkWords(HashSet<String> toCheckWords, HashSet<String> features) {
        HashSet<String> words = new HashSet<>();
        for (String word :
                toCheckWords) {
            if (Util.split(word) != null) {
                words.addAll(Util.split(word));
            }
        }
        return checkTokens(words, features, wordsThreshold);
    }

    private static boolean checkMethods(String methodName, HashSet<String> features) {
        ArrayList<String> parts = Util.split(methodName);
        HashSet<String> toCheckWords = new HashSet<>();
        for (String part :
                parts) {
            toCheckWords.add(part);
        }
        return checkTokens(toCheckWords, features, methodThreshold);
    }

    private static HashMap<RelationKey, Features> readRelationKeyFeaturesHashMap() throws Exception {
        HashMap<RelationKey, Features> relationKeyFeaturesHashMap = new HashMap<>();
        BufferedReader in = new BufferedReader(new FileReader(featureFile));
        String line;
        while ((line = in.readLine()) != null) {
            String[] temp = line.split(Config.fengefu, -1);
            RelationKey relationKey = new RelationKey(temp[0], temp[1], temp[2]);
            Features features = Features.constructFrom(temp[3], temp[4], temp[5]);
            relationKeyFeaturesHashMap.put(relationKey, features);
        }
        return relationKeyFeaturesHashMap;
    }
}
