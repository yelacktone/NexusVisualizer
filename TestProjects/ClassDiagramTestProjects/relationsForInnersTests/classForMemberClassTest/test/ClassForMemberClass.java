package test;

/**
 * 通常クラスとメンバクラスの関係が正しく出力されるかのテスト
 * 
 * @author Ishiguro
 * @version 1.0
 */
@SuppressWarnings("unused")
public class ClassForMemberClass {
    /**
     * 修飾子なしクラス
     */
    class ClassInClass {
    }

    /**
     * パブリッククラス
     */
    public class PublicClassInClass {
    }

    /**
     * プロテクティッドスタティッククラス
     */
    protected static class ProtectedStaticClassInClass {
    }

    /**
     * プライベートファイナルクラス
     */
    private final class PrivateFinalClassInClass {
    }

    /**
     * スタティックファイナルクラス
     */
    static final class StaticFinalClassInClass {
    }

    /**
     * 抽象クラス
     */
    abstract class AbstractClassInClass {
    }
}
