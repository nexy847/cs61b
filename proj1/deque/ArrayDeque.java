package deque;

import java.util.Iterator;

public class ArrayDeque<T> implements Deque<T> , Iterable<T>{
    T[] item;
    int sentinel;
    int last;
    int size;
    int N=16;

    public ArrayDeque(){
        item=(T[])new Object[N];
        sentinel=N/2;
        last= sentinel;
    }

    public void addFirst(T item){
        if((last+1)%N==sentinel){
            this.resizeExpanded();
        }
        if(sentinel==0){
            sentinel=N-1;
        }else {
            if(this.item[sentinel]!=null){
                sentinel -= 1;
            }
        }
        this.item[sentinel]=item;
        size+=1;
    }

    public void addLast(T item){
        if((last+1)%N==sentinel){
            this.resizeExpanded();
        }
        if(this.item[last]!=null){
            last=(last+1)%N;
        }
        this.item[last]=item;
        size+=1;
    }

    public void resizeExpanded(){
        if(sentinel>last){
            T[] temp=(T[])new Object[N];
            System.arraycopy(item,0,temp,0,N);
            N=N*4;
            this.item=(T[])new Object[N];
            System.arraycopy(temp,sentinel,this.item,N/2,temp.length-sentinel);
            System.arraycopy(temp,0,this.item,N/2+temp.length-sentinel,last+1);
            sentinel=N/2;
            last=sentinel+temp.length-1;
        }else{
            T[] temp=(T[])new Object[N];
            System.arraycopy(item,0,temp,0,N);
            N=N*4;
            this.item=(T[])new Object[N];
            System.arraycopy(temp,0,this.item,N/2,temp.length);
            sentinel=N/2;
            last=sentinel+temp.length-1;
        }
    }

    public T removeFirst(){
        if(size==0){
            return null;
        }
        T item=this.item[sentinel];
        sentinel=(sentinel+1)%N;
        size-=1;
        if(size<N/4){
            this.resizeShrink();
        }
        return item;
    }

    public T removeLast(){
        if(size==0){
            return null;
        }
        T item=this.item[last];
        this.item[last]=null;
        if(last==0){
            last=N-1;
        }else{
            last-=1;
        }
        size-=1;
        if(size<N/4){
            this.resizeShrink();
        }
        return item;
    }

    public void resizeShrink(){
        T[] temp=(T[])new Object[N];
        System.arraycopy(item,0,temp,0,N);
        N=size*4;
        this.item=(T[])new Object[N];
        System.arraycopy(temp,sentinel,item,N/2,size);
        sentinel=N/2;
        last=sentinel+size-1;
    }

    public int size(){
        return size;
    }

    public T get(int index){
        T point=this.item[sentinel+index];
        if(point==null) return null;
        return point;
    }

    public void printDeque(){
        for(int i=sentinel;i<=last;i++){
            System.out.println(item[i]);
        }
    }

    private class ArrayDequeIterator implements Iterator<T>{
        private int wizPos;

        public ArrayDequeIterator(){
            wizPos=0;
        }

        @Override
        public boolean hasNext(){
            return wizPos < size;
        }

        @Override
        public T next(){
            T returnItem=get(wizPos);
            wizPos+=1;
            return returnItem;
        }
    }

    @Override
    public Iterator<T> iterator() {
        return new ArrayDequeIterator();
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
        return returnSB.toString();
    }

    public boolean contains(T other){
        for(int i=0;i<size;i++){
            if(get(i)==other) return true;
        }
        return false;
    }

    @Override
    public boolean equals(Object other){
        if(other==this) return true;
        if(other==null) return false;
        if(other.getClass()!=this.getClass()) return false;
        ArrayDeque<T> o=(ArrayDeque<T>) other;
        if(o.size!=this.size) return false;
        for(T item:this){
            if(!o.contains(item)) return false;
        }
        return true;
    }

    //对重写方法的调试
    public static void main(String [] args){
        ArrayDeque<Integer> x=new ArrayDeque<>();
        x.addLast(1);
        x.addLast(2);
        x.addLast(3);

        for(int item:x){
            System.out.println(item);
        }

        System.out.println(x);

        ArrayDeque<Integer> other=new ArrayDeque<>();
        other.addLast(1);
        other.addLast(2);
        other.addLast(3);

        System.out.println(x.equals(other));
    }
}
