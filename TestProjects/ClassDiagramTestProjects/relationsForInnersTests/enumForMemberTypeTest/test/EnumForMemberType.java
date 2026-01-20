package test;

/**
 * トップレベル列挙型とメンバ型の関係が正しく出力されるかのテスト
 * 
 * @author Ishiguro
 * @version 1.0
 */
public enum EnumForMemberType {
    /**
     * 列挙定数
     */
    EnumConstant;

    /**
     * 修飾子なしクラス
     */
    class ClassInEnum {
    }

    /**
     * 抽象クラス
     */
    abstract class AbstractClassInEnum {
    }

    /**
     * 暗黙スタティックインタフェース
     */
    interface ImplicitStaticInterfaceInEnum {
    }

    /**
     * 暗黙スタティック列挙型
     */
    enum ImplicitStaticEnumInEnum {
    }

    /**
     * 暗黙スタティックレコード
     */
    record ImplicitStaticRecordInEnum() {
    }
}
