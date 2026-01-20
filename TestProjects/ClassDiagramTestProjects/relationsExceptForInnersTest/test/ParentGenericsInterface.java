package test;

/**
 * ジェネリクスの親インタフェース
 * 
 * @author Ishiguro
 * @version 1.0
 */
public interface ParentGenericsInterface<T extends ParentGenericsInterface<T>> extends Cloneable, ParentInterface {
    /**
     * フィールド
     */
    Double aDouble = 0.0;
}
