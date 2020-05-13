package com.meimeitech.dsa;

import java.util.Random;

public class BtreeTest {
    public static void main(String[] args) {
        Btree<Integer, Integer> btree = new Btree<>();
        Random r = new Random();
        for (int i = 0; i < 100; i++) {
            btree.put(i, r.nextInt(100));
        }
        System.out.println("xxx");
    }
}
