package test;

/**
 * トップレベルレコードとメンバ型の関係が正しく出力されるかのテスト
 * 
 * @author Ishiguro
 * @version 1.0
 */
public record RecordForMemberType() {
    /**
     * 修飾子なしクラス
     */
    class ClassInRecord {
    }

    /**
     * 抽象クラス
     */
    abstract class AbstractClassInRecord {
    }

    /**
     * 暗黙スタティックインタフェース
     */
    interface ImplicitStaticInterfaceInRecord {
    }

    /**
     * 暗黙スタティック列挙型
     */
    enum ImplicitStaticEnumInRecord {
    }

    /**
     * 暗黙スタティックレコード
     */
    record ImplicitStaticRecordInRecord() {
    }
}
