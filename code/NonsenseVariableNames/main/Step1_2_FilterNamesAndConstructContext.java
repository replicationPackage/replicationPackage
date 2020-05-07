package FindCasualNames.main;

import FindCasualNames.entity.Relation;
import FindCasualNames.relation.IdentifierWithType;
import util.Dic;
import util.Heu;
import util.Util;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

public class Step1_2_FilterNamesAndConstructContext {
    public static int contextLine = 2;
    public static String filteredName = "C:\\PHDONE\\ASE19\\testProject0\\pmd-5.2.0\\reported.csv";

    public static int totalNum = 0;
    public static int criterion1Num = 0;
    public static int criterion2Num = 0;
    public static int heuNum = 0;
    public static int dicNum = 0;

    public static void parseOneFile(ArrayList<IdentifierWithType> identifierWithTypes) throws Exception{
        printRecords(identifierWithTypes, filteredName);
    }

    private static void printRecords(ArrayList<IdentifierWithType> identifierWithTypes, String filteredName) throws Exception {
        for (IdentifierWithType identifierWithType :
                identifierWithTypes) {
            totalNum++;
            if (criterion1(identifierWithType.getContent())) {
                criterion1Num++;
                if (criterion2(identifierWithType.getContent())) {
                    criterion2Num++;
                    if (canNotHeu(identifierWithType.getContent(), identifierWithType.type, identifierWithType.comment)) {
                        heuNum++;
                        if (canNotDic(identifierWithType.getContent())) {
                            dicNum++;
                            print(Relation.constructRelationFrom(identifierWithType) + "\n", filteredName);
                        }
                    }
                }
            }
        }
    }

    private static boolean canNotHeu(String name, String type, String comment) {
        if (type.length() > 0) {
            if (!canNotHeuType(name, type)) {
                return false;
            }
        }
        if (comment.length() > 0) {
            if (!canNotHeuComment(name, comment)) {
                return false;
            }
        }
       return true;
    }

    private static boolean canNotHeuComment(String name, String comment) {
        ArrayList<String> parts = Util.split(name);
        for (String part :
                parts) {
            if (Heu.canHeuComment(part, comment, "H1") ||
                    Heu.canHeuComment(part, comment, "H2")) {
                return false;
            }
        }
        return true;
    }

    private static boolean canNotHeuType(String name, String type) {
        ArrayList<String> parts = Util.split(name);
        for (String part :
                parts) {
            if (Heu.canHeuType(part, type, "H1") ||
                    Heu.canHeuType(part, type, "H2")) {
                return false;
            }
        }
        return true;
    }

    private static boolean canNotDic(String name) {
        ArrayList<String> parts = Util.split(name);
        for (String part :
                parts) {
            if (Dic.abbrDicHashMap.containsKey(part) ||
                    Dic.computerAbbrDicHashMap.containsKey(part)) {
                return false;
            }
        }
        return true;
    }


    private static boolean criterion2(String name) {
        ArrayList<String> tokens = Util.split(name);
        for (String token :
                tokens) {
            if (token.length() >= 2 && Dic.isInDictToken(token)) {
                return false;
            }
        }
        return true;
    }

    private static boolean criterion3(String name) {
        ArrayList<String> tokens = Util.split(name);
        for (String token :
                tokens) {
            if (token.length() == 1) {
                return true;
            }
            if (token.length() >= 2) {
                if (!Dic.isInDict(token)) {
                    return true;
                }
            }
        }
        return false;

    }

    private static boolean criterion1(String name) {
        return name.length() <= 6;
         // return true;
    }

    private static void print(String name, String fileName) throws Exception {
        BufferedWriter out;
        out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileName, true)));
        out.write(name);
        out.flush();
        out.close();
    }
}
