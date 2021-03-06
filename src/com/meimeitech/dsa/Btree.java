package com.meimeitech.dsa;

import java.util.Comparator;

public class Btree<K,V> {
    private int order;
    private int size;
    private BtreeNode<K,V> root;
    private BtreeNode<K,V> hot;

    public Btree(){
        this(3);
    }
    public Btree(int order) {
        //默认最低三阶
        if (order < 3) {
            this.order = 3;
        }
        this.order = order;
        this.root = new BtreeNode<>();
    }

    public int size() {
        return this.size;
    }

    public boolean put(K key, V value) {
        if (get(key) != null) {
            return false;
        }
        Entry<K, V> entry = new Entry<>(key, value);
        int rank = searchKey(hot, entry);
        hot.getEntrys().insert(rank + 1, entry);
        hot.getChilds().insert(rank + 2, null);
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
        Entry<K, V>[] keys = v.getEntrys().subVector(new Entry[v.getEntrys().size() - s - 1],s + 1, v.getEntrys().size());
        node.getEntrys().addAll(keys);
        BtreeNode<K, V>[] childs = v.getChilds().subVector(new BtreeNode[v.getChilds().size() - s - 1],s + 1, v.getChilds().size());
        //新建节点已经存在一个空孩子
        node.getChilds().remove(0);
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
            p.getChilds().putAt(0, v);
            v.setParent(p);
        }
        //连接右节点
        int rank = searchKey(p, key);
        p.getEntrys().insert(rank + 1, key);
        p.getChilds().insert(rank + 2, node);
        node.setParent(p);
        solveOverflow(p);
    }

    public V get(K key) {
        BtreeNode<K, V> v = root;
        hot = null;
        while (v != null) {
            int rank = searchKey(v, new Entry<>(key));
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
            int rank = searchKey(v, new Entry<>(key));
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
        int rank = searchKey(node, new Entry<>(key));
        V value = node.getEntrys().get(rank).getValue();
        //非叶子节点
        if (node.getChilds().get(0) != null) {
            //找到待删除关键码的后继
            BtreeNode<K,V> rc = node.getChilds().get(rank + 1);
            while (rc.getChilds().get(0) != null) {
                rc = rc.getChilds().get(0);
            }
            node.getEntrys().putAt(rank, rc.getEntrys().get(0));
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
            if (node.getEntrys().size() == 0 && node.getChilds().get(0) != null) {
                root = node.getChilds().get(0);
                root.setParent(null);
                //释放node
            }
            return;
        }
        int rank = 0;
        for (int i = 0; i < p.getChilds().size(); i++) {
            if (node == p.getChilds().get(i)) {
                rank = i;
                break;
            }
        }
        //左顾右盼-左顾
        if (0 < rank) {
            //左兄弟
            BtreeNode<K, V> ls = p.getChilds().get(rank - 1);
            if (ls.getChilds().size() > num) {
                //p借出关键码给node
                node.getEntrys().insert(0, p.getEntrys().remove(rank - 1));
                //左兄弟向父亲借出最大关键码
                p.getEntrys().insert(rank - 1, ls.getEntrys().remove(ls.getEntrys().size() - 1));
                //左兄弟的孩子过继给node
                BtreeNode<K,V> lsc = ls.getChilds().remove(ls.getChilds().size() - 1);
                node.getChilds().insert(0, lsc);
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
                node.getEntrys().insert(node.getEntrys().size(), p.getEntrys().remove(rank));
                p.getEntrys().insert(rank, rs.getEntrys().remove(0));
                BtreeNode<K,V> rsc = rs.getChilds().remove(0);
                node.getChilds().insert(node.getChilds().size(), rsc);
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
            ls.getEntrys().insert(ls.getEntrys().size(), p.getEntrys().remove(rank - 1));
            //node不再是p的孩子
            p.getChilds().remove(rank);
            //node最右侧的孩子过继给ls
            BtreeNode<K,V> nodeLc = node.getChilds().remove(0);
            ls.getChilds().insert(ls.getChilds().size(), nodeLc);
            if (nodeLc != null) {
                nodeLc.setParent(ls);
            }
            if (node.getEntrys().size() > 0) {
                ls.getEntrys().addAll(node.getEntrys().subVector(new Entry[node.getEntrys().size()],0, node.getEntrys().size()));
                BtreeNode<K, V>[] nodeChils = node.getChilds().subVector(new BtreeNode[node.getChilds().size()],0, node.getChilds().size());
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
            rs.getEntrys().insert(0, p.getEntrys().remove(rank));
            p.getChilds().remove(rank);
            BtreeNode<K,V> nodeRc = node.getChilds().remove(node.getChilds().size() - 1);
            rs.getChilds().insert(0, nodeRc);
            if (nodeRc != null) {
                nodeRc.setParent(rs);
            }
            for (int i = node.getEntrys().size() - 1; i >= 0; i--) {
                rs.getEntrys().insert(0, node.getEntrys().get(i));
                BtreeNode<K,V> nodeRchild = node.getChilds().get(i);
                rs.getChilds().insert(0, nodeRchild);
                if (nodeRchild != null) {
                    nodeRchild.setParent(rs);
                }
            }
            //释放node
        }
        solveUnderflow(p);
    }

    private int searchKey(BtreeNode<K, V> v, Entry<K, V> entry) {
        return v.getEntrys().search(entry, Comparator.comparingInt(e -> e.getKey().hashCode()));
    }
}
