package test;

/**
 * インタフェースを実装するクラス
 * 
 * @author Ishiguro
 * @version 1.0
 */
public class ImplementingClass implements ParentInterface, Cloneable {
    /**
     * Comparableのメソッドを実装。
     * ParentInterfaceがimplementsしているため。
     */
    @Override
    public int compareTo(Object object) {
        return 0;
    }

    /**
     * Cloneableのメソッドを実装。
     */
    @Override
    public Object clone() {
        return new Object();
    }
}
