package test;

/**
 * コンストラクタ呼び出し検証用クラス
 * 
 * @author Ishiguro
 * @version 1.0
 */
@SuppressWarnings("unused")
public class ConstructorCallTest extends ParentClass {
    /**
     * superによる明示的なコンストラクタ呼び出し検証用コンストラクタ
     */
    public ConstructorCallTest() {
        super();
        return;
    }

    /**
     * thisによる明示的なコンストラクタ呼び出し検証用コンストラクタ
     */
    public ConstructorCallTest(int index) {
        this();
        return;
    }

    /**
     * オブジェクト生成式検証用メソッド
     */
    public void objectCreationTest() {
        // オブジェクト生成式におけるコンストラクタ呼び出し
        DependencyTestClass dependencyTestClass1 = new DependencyTestClass(); // DependencyTestClass():1回目

        // コンストラクタ呼び出し回数カウント
        DependencyTestClass dependencyTestClass2 = new DependencyTestClass(); // DependencyTestClass():2回目
        DependencyTestClass dependencyTestClass3 = new DependencyTestClass(); // DependencyTestClass():3回目
        return;
    }
}
