package com.meimeitech.dsa;

/**
 * B树节点
 */
public class BtreeNode<K,V> {
    private BtreeNode parent;
    private Vector<Entry<K,V>> entrys = new Vector<>();
    private Vector<BtreeNode<K,V>> childs = new Vector<>();

    public BtreeNode() {
        childs.add(null);
    }

    public BtreeNode<K,V> getParent() {
        return parent;
    }

    public void setParent(BtreeNode<K,V> parent) {
        this.parent = parent;
    }

    public Vector<Entry<K, V>> getEntrys() {
        return entrys;
    }

    public void setEntrys(Vector<Entry<K, V>> entrys) {
        this.entrys = entrys;
    }

    public Vector<BtreeNode<K, V>> getChilds() {
        return childs;
    }

    public void setChilds(Vector<BtreeNode<K, V>> childs) {
        this.childs = childs;
    }
}
