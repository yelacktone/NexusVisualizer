package test;

/**
 * 通常クラスとメンバインタフェースの関係が正しく出力されるかのテスト
 * 
 * @author Ishiguro
 * @version 1.0
 */
@SuppressWarnings("unused")
public class ClassForMemberInterface {
    /**
     * 修飾子なしインタフェース
     */
    interface InterfaceInClass {
    }

    /**
     * パブリックインタフェース
     */
    public interface PublicInterfaceInClass {
    }

    /**
     * プロテクティッドスタティックインタフェース
     */
    protected static interface ProtectedStaticInterfaceInClass {
    }

    /**
     * プライベート抽象インタフェース
     */
    private abstract interface PrivateInterfaceInClass {
    }

    /**
     * 抽象スタティックインタフェース
     */
    abstract static interface AbstractStaticInterfaceInClass {
    }
}
