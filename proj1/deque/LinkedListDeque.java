package deque;


import java.util.Iterator;

public class LinkedListDeque<T> implements Deque<T>,Iterable<T> {
    private int size;
    private IntNode sentinel;
    private IntNode last;
    private IntNode temp;

    public class IntNode{
        public T item;
        public IntNode next;
        public IntNode prev;

        public IntNode(T i,IntNode n,IntNode p){
            item=i;
            next=n;
            prev=p;
        }
    }

    public LinkedListDeque(){
        sentinel=new IntNode(null,null,null);
        last=sentinel;
        size=0;
    }

    public void addFirst(T item){
        if(sentinel.next==null){
            sentinel.next=new IntNode(item,sentinel.next,sentinel);
            last=sentinel.next;
            last.next=sentinel;
        }else{
            IntNode p=sentinel.next;
            sentinel.next=new IntNode(item,sentinel.next,sentinel);
            p.prev=sentinel.next;
        }
        size+=1;
    }

    public void addLast(T item){
        last.next=new IntNode(item,sentinel,last);
        last=last.next;
        size+=1;
    }

    public int size(){
        return size;
    }

    public void printDeque(){
        IntNode temp=sentinel.next;
        while(temp!=sentinel){
            System.out.println(temp.item+" ");
            temp=temp.next;
        }
        System.out.println();
    }

    public T removeFirst(){
        if(sentinel.next==null){
            last=sentinel;
            return null;
        }else{
            IntNode p=sentinel.next;
            sentinel.next=sentinel.next.next;
            if(size!=0) size-=1;
            return p.item;
        }
    }

    public T removeLast(){
        if(last==sentinel){
            return null;
        }else{
            IntNode temp=last;
            last=last.prev;
            last.next=sentinel;
            if(size!=0) size-=1;
            return temp.item;
        }
    }

    public T get(int index){
        IntNode temp=sentinel.next;
        for(int i=0;i<=index;i++){
            if(i==index){
                return temp.item;
            }
            temp=temp.next;
        }
        return null;
    }


    //以下为get方法的递归实现,但是想不出来
//    public void initializeTemp(int index){
//        temp=last;
//        for(int i=1;i<=index;i++){
//            temp=temp.prev;
//        }
//    }

//    public T getRecursive(int index){
//        if(index==0){
//            return temp.item;
//        }
//        initializeTemp(index);
//        getRecursive(index--);
//        return null;
//    }

    private class LinkedListDequeIterator implements Iterator<T>{
        private int wizPos;

        public LinkedListDequeIterator(){
            wizPos=0;
        }

        public boolean hasNext(){
            return wizPos<size;
        }

        public T next(){
            T returnItem=get(wizPos);
            wizPos+=1;
            return returnItem;
        }
    }

    @Override
    public Iterator<T> iterator() {
        return new LinkedListDequeIterator();
    }

    @Override
    public String toString(){
        StringBuilder returnSB=new StringBuilder("{");
        for(int i=0;i<size-1;i++){
            returnSB.append(get(i));
            returnSB.append(",");
        }
        returnSB.append(get(size-1));
        returnSB.append("}");
        return  returnSB.toString();
    }

    @Override
    public boolean equals(Object other){
        if(this==other) return true;
        if(other==null) return false;
        if(other.getClass()!=this.getClass()) return false;
        LinkedListDeque<T> o=(LinkedListDeque<T>) other;
        if(o.size!=this.size) return false;
        for(T item:this){
            if(!o.contains(item)) return false;
        }
        return true;
    }

    public boolean contains(T other){
        for(int i=0;i<size;i++){
            if(get(i)==other) return true;
        }
        return false;
    }

    public static void main(String[] args){
        LinkedListDeque<Integer> x=new LinkedListDeque();
        x.addLast(1);
        x.addLast(2);
        x.addLast(3);

        System.out.println(x);

        for(int i : x){
            System.out.println(i);
        }

        LinkedListDeque<Integer> other=new LinkedListDeque<>();
        other.addLast(2);
        other.addLast(2);
        other.addLast(3);

        System.out.println(x.equals(other));
    }
}
