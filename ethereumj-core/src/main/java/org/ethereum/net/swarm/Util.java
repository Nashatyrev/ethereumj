package org.ethereum.net.swarm;

import java.util.Collection;

/**
 * Created by Admin on 17.06.2015.
 */
public class Util {

    public static String getCommonPrefix(String s1, String s2) {
        int pos = 0;
        while(pos < s1.length() && pos < s2.length() && s1.charAt(pos) == s2.charAt(pos)) pos++;
        return s1.substring(0, pos);
    }

    public static String repeat(String s, int n) {
        StringBuilder ret = new StringBuilder();
        for (int i = 0; i < n; i++) ret.append(s);
        return ret.toString();
    }


    public static <P extends StringTrie.TrieNode<P>> String dumpTree(P n) {
        return dumpTree(n, 0);
    }

    private static <P extends StringTrie.TrieNode<P>> String dumpTree(P n, int indent) {
        String ret = Util.repeat("  ", indent) + "[" + n.path + "] " + n + "\n";
        for (P c: n.getChildren()) {
            ret += dumpTree(c, indent + 1);
        }
        return ret;
    }

}
