package test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import test.fieldClass.ClassA;
import test.fieldClass.ClassB;
import test.fieldClass.ClassC;
import test.fieldClass.ClassD;
import test.fieldClass.ClassE;
import test.fieldClass.ClassF;
import test.fieldClass.ClassG;

/**
 * フィールドで他クラスを参照するクラスA
 * 片方向関連、双方向関連、ジェネリクスによる多重度関連、配列による多重度関連
 * 
 * @author Ishiguro
 * @version 1.0
 */
public class FieldReferencingClassA {
    /**
     * 片方向関連
     */
    public ClassA unidirectionalAssociationField;

    /**
     * 双方向関連
     */
    public FieldReferencingClassB bidirectionalAssociationField;

    /**
     * ジェネリクスによる多重度関連
     */
    public List<ClassB> genericsMultiplicityListField;

    /**
     * 深いジェネリクスによる多重度関連
     */
    public Map<ClassC, Map<ClassD, ClassE>> deepGenericsMultiplicityMapField;

    /**
     * 多次元配列による多重度関連
     */
    public ClassF[][] multiArrayMultiplicityField;

    /**
     * ジェネリクスの多次元配列による多重度関連
     */
    public Set<ClassG>[][] genericsMultiArrayMultiplicityField;
}
