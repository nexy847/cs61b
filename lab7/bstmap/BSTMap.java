package bstmap;

import edu.princeton.cs.algs4.BST;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class BSTMap<K extends Comparable<K>,V> implements Map61B<K,V>{
    BSTNode root;
    int size;

    private class BSTNode{
        K k;
        V v;
        BSTNode left;
        BSTNode right;

        BSTNode(K k,V v){
            this.k=k;
            this.v=v;
            this.left=null;
            this.right=null;
        }

        void setLeft(BSTNode left){
            this.left=left;
        }

        void setRight(BSTNode right){
            this.right=right;
        }
    }

    public BSTMap(){
    }

    @Override
    public void clear() {
        root=null;
        size=0;
    }

    public boolean contains(K key) {
        if(key==null){
            throw new IllegalArgumentException("calls should not be empty");
        }
        return get(key)!=null;
    }

    @Override
    public boolean containsKey(K key){
        if(key==null) throw new IllegalArgumentException("calls containsKey() not be empty");
        return getKey(root,key)!=null;
    }

    public K getKey(BSTNode node,K key){
        if(node==null) return null;
        int cmp= key.compareTo(node.k);
        if(cmp>0) return getKey(node.right,key);
        else if(cmp<0) return getKey(node.left,key);
        else return node.k;
    }

    @Override
    public V get(K key) {
        return get(root,key);
    }

    public V get(BSTNode node,K key){
        if(key==null) throw new IllegalArgumentException("calls get() with a null key");
        if(node==null) return null;
        int cmp=key.compareTo(node.k);
        if(cmp>0) {
            return get(node.right, key);
        }
        else if(cmp<0) {
            return get(node.left, key);
        }
        else return node.v;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public void put(K key, V value) {
        if(key==null) throw new IllegalArgumentException("calls put() with null");
        root=put(root,key,value);
    }

    public BSTNode put(BSTNode node,K key,V value){
        if(node==null) {
            size+=1;
            return new BSTNode(key, value);
        }
        int cmp= key.compareTo(node.k);
        if(cmp>0) node.right=put(node.right,key,value);
        else if(cmp<0) node.left=put(node.left,key,value);
        else {
            node.v = value;
        };
        return node;
    }

    public void printInorder(){
        printInorder(root);
    }

    public void printInorder(BSTNode node){
        if(node==null) return;
        printInorder(node.left);
        System.out.println(node.v);
        printInorder(node.right);
    }

    @Override
    public Set<K> keySet() {
        Set<K> set=new HashSet<>();
        set=keySet(root,set);
        return set;
    }

    public Set<K> keySet(BSTNode node,Set<K> set){
        if(node==null) return set;
        keySet(node.left,set);
        set.add(node.k);
        keySet(node.right,set);
        set.add(node.k);
        return set;
    }

    @Override
    public V remove(K key) {
        V value=get(key);
        remove(key,value);
        return null;
    }

    @Override
    public V remove(K key, V value) {
        root=remove(root,key);
        return value;
    }

    public BSTNode remove(BSTNode root,K key){
        int cmp= key.compareTo(root.k);
        if(root==null) return root;
        else if(cmp<0) root.left=remove(root,key);
        else if(cmp>0) root.right=remove(root,key);
        else{
            if(root.left==null&&root.right==null){
                root=null;
                return root;
            }else if(root.left==null){
                BSTNode temp=root;
                root=root.right;
                return root;
            }else if(root.right==null){
                BSTNode temp=root;
                root=root.left;
                return root;
            }else{
                BSTNode temp=find_min(root.right);
                root.k=temp.k;
                root.v=temp.v;
                root.right=remove(root.right,temp.k);
            }
        }
        return root;
    }

    public BSTNode find_min(BSTNode node){
        if(node==null) return node;
        else if(node.left==null) return node;
        return find_min(node.left);
    }

    public BSTNode find_max(BSTNode node){
        if(node==null) return node;
        else if(node.right==null) return node;
        return find_max(node.right);
    }

    @Override
    public Iterator<K> iterator() {
        return new IterateBST();
    }

    private class IterateBST implements Iterator{
        BSTNode node;
        queue queue;

        private class queue{
            BSTNode temp;
            queue next;
            queue front;
            queue rear=front=null;

            queue(BSTNode temp,queue next){
                this.temp=temp;
                this.next=next;
            }

            void enqueue(BSTNode sentinel){
                queue temp1=new queue(sentinel,null);
                if(front==null && rear==null){
                    front=rear=temp1;
                    return;
                }
                rear.next=temp1;
                rear=temp1;
            }

            void dequeue(){
                queue temp1=front;
                if(front==null) return;
                if(front==rear){
                    front=rear=null;
                }else{
                    front=front.next;
                }
            }
        }

        public IterateBST(){
            queue=new queue(root,null);
            queue.enqueue(root);
        }

        @Override
        public boolean hasNext(){
            return queue.front!=null&&queue.rear!=null;
        }

        @Override
        public Object next() {
            node=queue.front.temp;
            //System.out.println(node.v);
            queue.dequeue();
            if(node.left!=null) queue.enqueue(node.left);
            if(node.right!=null) queue.enqueue(node.right);
            return node.v;
        }
    }

    public static void main(String[] args){
        BSTMap<Integer,Integer> x=new BSTMap<>();
        x.put(1,1);
        x.put(2,2);
        x.put(3,3);

        //iteration test
        for(int i:x){
            System.out.println(i);
        }

        x.printInorder();

        x.remove(1);

        x.printInorder();

        System.out.println(x.keySet());
    }

}
