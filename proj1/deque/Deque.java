package deque;

public interface Deque<T> {
    void addFirst(T item);
    void addLast(T item);

    T removeFirst();
    T removeLast();

    int size();

    T get(int index);

    default boolean isEmpty(){
        if(size()==0) return true;
        else return false;
    }

    void printDeque();
}
