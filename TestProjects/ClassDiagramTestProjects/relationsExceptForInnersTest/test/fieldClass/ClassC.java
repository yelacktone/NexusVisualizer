package test.fieldClass;

import java.util.Set;

/**
 * フィールド用クラスC
 * 
 * @author Ishiguro
 * @version 1.0
 */
public class ClassC {
    /**
     * 多重片方向関連
     */
    Set<ClassD> classDs;

    /**
     * 片方向関連
     */
    ClassD classD;
}
