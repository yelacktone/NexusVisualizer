package test;

/**
 * 子インタフェース
 * 
 * @author Ishiguro
 * @version 1.0
 */
public interface ChildInterface extends ParentGenericsInterface<ChildInterface> {
    /**
     * フィールド
     */
    Integer anInteger = 0;
}
