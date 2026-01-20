package test;

/**
 * ジェネリクスの親クラス
 * 
 * @author Ishiguro
 * @version 1.0
 */
public abstract class ParentAbstractClass<T extends ParentAbstractClass<T>> extends Object {
    /**
     * 抽象メソッド
     */
    abstract void parentAbstractClass();
}
