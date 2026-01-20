package test;

/**
 * 通常クラスとメンバレコードの関係が正しく出力されるかのテスト
 * 
 * @author Ishiguro
 * @version 1.0
 */
@SuppressWarnings("unused")
public class ClassForMemberRecord {
    /**
     * 修飾子なしレコード
     */
    record RecordInClass() {
    }

    /**
     * パブリックレコード
     */
    public record PublicRecordInClass() {
    }

    /**
     * プロテクティッドスタティックレコード
     */
    protected static record ProtectedStaticRecordInClass() {
    }

    /**
     * プライベートファイナルレコード
     */
    private final record PrivateFinalRecordInClass() {
    }

    /**
     * スタティックファイナルレコード
     */
    static final record StaticFinalRecordInClass() {
    }
}
