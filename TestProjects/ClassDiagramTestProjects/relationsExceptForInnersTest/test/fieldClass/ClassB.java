package test.fieldClass;

import java.util.Set;

/**
 * フィールド用クラスB
 * 
 * @author Ishiguro
 * @version 1.0
 */
public class ClassB {
    /**
     * 片方向関連
     */
    ClassC classC;

    /**
     * 多重片方向関連
     */
    Set<ClassC> classCs;
}
