package test;

/**
 * ジェネリクスを継承した子クラス
 * 
 * @author Ishiguro
 * @version 1.0
 */
public class ChildConcreteClass extends ParentAbstractClass<ChildConcreteClass> {
    /**
     * フィールド
     */
    String aString = "ChildConcreteClass";

    /**
     * ParentAbstractClassの抽象メソッドを実装。
     */
    void parentAbstractClass() {
        return;
    }
}
