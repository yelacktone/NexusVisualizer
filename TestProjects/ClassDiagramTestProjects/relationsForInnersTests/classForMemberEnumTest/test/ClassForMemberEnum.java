package test;

/**
 * 通常クラスとメンバ列挙型の関係が正しく出力されるかのテスト
 * 
 * @author Ishiguro
 * @version 1.0
 */
@SuppressWarnings("unused")
public class ClassForMemberEnum {
    /**
     * 修飾子なし列挙型
     */
    enum EnumInClass {
    }

    /**
     * パブリック列挙型
     */
    public enum PublicEnumInClass {
    }

    /**
     * プロテクティッドスタティック列挙型
     */
    protected static enum ProtectedStaticEnumInClass {
    }

    /**
     * プライベート列挙型
     */
    private enum PrivateEnumInClass {
    }
}
