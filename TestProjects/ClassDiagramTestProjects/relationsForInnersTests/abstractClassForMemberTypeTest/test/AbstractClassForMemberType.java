package test;

/**
 * 抽象クラスとメンバ型の関係が正しく出力されるかのテスト
 * ※通常クラスと重複
 * 
 * @author Ishiguro
 * @version 1.0
 */
public abstract class AbstractClassForMemberType {
    /**
     * 修飾子なしクラス
     */
    class ClassInAbstractClass {
    }

    /**
     * 抽象クラス
     */
    abstract class AbstractClassInAbstractClass {
    }

    /**
     * 暗黙スタティックインタフェース
     */
    interface ImplicitStaticInterfaceInAbstractClass {
    }

    /**
     * 暗黙スタティック列挙型
     */
    enum ImplicitStaticEnumInAbstractClass {
    }

    /**
     * 暗黙スタティックレコード
     */
    record ImplicitStaticRecordInAbstractClass() {
    }
}
