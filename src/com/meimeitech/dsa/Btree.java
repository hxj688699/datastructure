package com.meimeitech.dsa;

import java.util.Comparator;

public class Btree<K,V> {
    private int order;
    private int size;
    private BtreeNode<K,V> root;
    private BtreeNode<K,V> hot;

    public Btree(int order) {
        this.order = order;
    }

    public boolean put(K key, V value) {
        if (get(key) != null) {
            return false;
        }
        Entry<K, V> entry = new Entry<>(key, value);
        int rank = searchKey(hot, entry);
        hot.getEntrys().add(rank + 1, entry);
        hot.getChilds().add(rank + 2, null);
        size++;
        solveOverflow(hot);
        return true;
    }

    private void solveOverflow(BtreeNode<K, V> v) {
        if (order >= v.getChilds().size()) {
            return;
        }
        int s = order / 2;
        //上溢关键码
        Entry<K, V> key = v.getEntrys().get(s);
        //分裂右节点
        BtreeNode<K, V> node = new BtreeNode<>();
        Entry<K, V>[] keys = v.getEntrys().subVector(s + 1, v.getEntrys().size());
        node.getEntrys().addAll(keys);
        BtreeNode<K, V>[] childs = v.getChilds().subVector(s + 1, v.getChilds().size());
        node.getChilds().addAll(childs);
        v.getEntrys().batchRemove(s, v.getEntrys().size());
        v.getChilds().batchRemove(s + 1, v.getChilds().size());
        if (v.getChilds().get(0) != null) {
            for (BtreeNode<K, V> child : childs) {
                child.setParent(node);
            }
        }
        BtreeNode<K, V> p = v.getParent();
        if (p == null) {
            root = p = new BtreeNode<>();
            //注意已经存在一个空孩子
            p.getChilds().add(0, v);
            v.setParent(p);
        }
        //连接右节点
        int rank = searchKey(p, key);
        p.getEntrys().add(rank + 1, key);
        p.getChilds().add(rank + 2, node);
        node.setParent(p);
        solveOverflow(p);
    }

    public V get(K key) {
        BtreeNode<K, V> v = root;
        hot = null;
        while (v != null) {
            int rank = searchKey(v, new Entry<K, V>(key));
            Vector<Entry<K,V>> keys = v.getEntrys();
            if (rank >= 0 && keys.get(rank).getKey() == key) {
                return keys.get(rank).getValue();
            }
            hot = v;
            Vector<BtreeNode<K, V>> childs = v.getChilds();
            v = childs.get(rank + 1);
        }
        return null;
    }

    public BtreeNode<K, V> search(K key) {
        BtreeNode<K, V> v = root;
        hot = null;
        while (v != null) {
            int rank = searchKey(v, new Entry<K, V>(key));
            Vector<Entry<K,V>> keys = v.getEntrys();
            if (rank >= 0 && keys.get(rank).getKey() == key) {
                return v;
            }
            hot = v;
            Vector<BtreeNode<K, V>> childs = v.getChilds();
            v = childs.get(rank + 1);
        }
        return null;
    }

    public V remove(K key) {
        BtreeNode<K,V> node = search(key);
        if (node == null) {
            return null;
        }
        int rank = searchKey(node, new Entry<K, V>(key));
        V value = node.getEntrys().get(rank).getValue();
        //非叶子节点
        if (node.getChilds().get(0) != null) {
            //找到待删除关键码的后继
            BtreeNode<K,V> rc = node.getChilds().get(rank + 1);
            while (rc != null) {
                Vector<BtreeNode<K,V>> childs = rc.getChilds();
                rc = childs.get(0);
            }
            node.getEntrys().add(rank, rc.getEntrys().get(0));
            node = rc;
            rank = 0;
        }

        node.getEntrys().remove(rank);
        node.getChilds().remove(rank + 1);
        size--;

        solveUnderflow(node);
        return value;
    }

    private void solveUnderflow(BtreeNode<K,V> node) {
        //阶数除2的上整
        int num = (order + 1) / 2;
        if (num <= node.getChilds().size()) {
            return;
        }
        BtreeNode<K,V> p = node.getParent();
        if (p == null) {
            return;
        }
        int rank = 0;
        for (BtreeNode<K, V> child : p.getChilds()) {
            if (node == child) {
                break;
            }
            rank++;
        }
        //左顾右盼-左顾
        if (0 < rank) {
            //左兄弟
            BtreeNode<K, V> ls = p.getChilds().get(rank - 1);
            if (ls.getChilds().size() > num) {
                //p借出关键码给node
                node.getEntrys().add(0, p.getEntrys().get(rank - 1));
                //左兄弟向父亲借出最大关键码
                p.getEntrys().add(rank - 1, ls.getEntrys().remove(ls.getEntrys().size() - 1));
                //左兄弟的孩子过继给node
                BtreeNode<K,V> lsc = ls.getChilds().remove(ls.getChilds().size() - 1);
                node.getChilds().add(0, lsc);
                if (lsc != null) {
                    lsc.setParent(node);
                }
                return;
            }
        }
        //右盼
        if (rank < p.getChilds().size() - 1) {
            //右兄弟
            BtreeNode<K, V> rs = p.getChilds().get(rank + 1);
            if (rs.getChilds().size() > num) {
                node.getEntrys().add(node.getEntrys().size(), p.getEntrys().get(rank));
                p.getEntrys().add(rank, rs.getEntrys().remove(0));
                BtreeNode<K,V> rsc = rs.getChilds().remove(0);
                node.getChilds().add(node.getChilds().size(), rsc);
                if (rsc != null) {
                    rsc.setParent(node);
                }
                return;
            }
        }
        //父节点下溢作为粘合剂合并
        if (rank > 0) {
            //p下溢与左孩子合并
            BtreeNode<K, V> ls = p.getChilds().get(rank - 1);
            //父亲下溢借出一个关键码
            ls.getEntrys().add(ls.getEntrys().size(), p.getEntrys().remove(rank - 1));
            //node不再是p的孩子
            p.getChilds().remove(rank);
            //node最右侧的孩子过继给ls
            BtreeNode<K,V> nodeLc = node.getChilds().remove(0);
            ls.getChilds().add(ls.getChilds().size(), nodeLc);
            if (nodeLc != null) {
                nodeLc.setParent(ls);
            }
            if (node.getEntrys().size() > 0) {
                ls.getEntrys().addAll(node.getEntrys().subVector(0, node.getEntrys().size()));
                BtreeNode<K, V>[] nodeChils = node.getChilds().subVector(0, node.getChilds().size());
                ls.getChilds().addAll(nodeChils);
                for (BtreeNode<K, V> nodeChil : nodeChils) {
                    if (nodeChil != null) {
                        nodeChil.setParent(ls);
                    }
                }
                //释放node
            }
        } else {
            //p下溢与右孩子合并
            BtreeNode<K, V> rs = p.getChilds().get(rank + 1);
            rs.getEntrys().add(0, p.getEntrys().remove(rank));
            p.getChilds().remove(rank);
            BtreeNode<K,V> nodeRc = node.getChilds().remove(node.getChilds().size() - 1);
            rs.getChilds().add(0, nodeRc);
            if (nodeRc != null) {
                nodeRc.setParent(rs);
            }
            for (int i = node.getEntrys().size() - 1; i >= 0; i--) {
                rs.getEntrys().add(0, node.getEntrys().get(i));
                BtreeNode<K,V> nodeRchild = node.getChilds().get(i);
                rs.getChilds().add(0, nodeRchild);
                if (nodeRchild != null) {
                    nodeRchild.setParent(rs);
                }
            }
            //释放node
        }
    }

    private int searchKey(BtreeNode<K, V> v, Entry<K, V> entry) {
        return v.getEntrys().search(entry, Comparator.comparingInt(e -> e.getKey().hashCode()));
    }
}
