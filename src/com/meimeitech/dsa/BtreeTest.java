package com.meimeitech.dsa;

import java.util.Random;

/**
 * @author HXJ
 * @date 2020/5/13
 */
public class BtreeTest {
    public static void main(String[] args) {
        Btree<Integer, Integer> btree = new Btree<>(4);
        Random r = new Random();
        for (int i = 0; i < 5; i++) {
            btree.put(i, r.nextInt(100));
        }
        Integer v = btree.get(50);
        System.out.println(v);
        Integer remove2 = btree.remove(2);
        Integer remove = btree.remove(0);
        Integer remove1 = btree.remove(1);
        System.out.println(remove);
    }
}
