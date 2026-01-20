package test;

/**
 * トップレベルインタフェースとメンバ型の関係が正しく出力されるかのテスト
 * 
 * @author Ishiguro
 * @version 1.0
 */
public interface InterfaceForMemberType {
    /**
     * 暗黙パブリックスタティッククラス
     */
    class ImplicitPublicStaticClassInInterface {
    }

    /**
     * 暗黙パブリックスタティック抽象クラス
     */
    abstract class ImplicitStaticAbstractClassInInterface {
    }

    /**
     * 暗黙パブリックスタティックインタフェース
     */
    interface ImplicitPublicStaticInterfaceInInterface {
    }

    /**
     * 暗黙パブリックスタティック列挙型
     */
    enum ImplicitStaticEnumInInterface {
    }

    /**
     * 暗黙パブリックスタティックレコード
     */
    record ImplicitPublicStaticRecordInInterface() {
    }
}
