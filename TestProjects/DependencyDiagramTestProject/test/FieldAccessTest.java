package test;

/**
 * フィールドアクセス検証用クラス
 * 
 * @author Ishiguro
 * @version 1.0
 */
@SuppressWarnings("unused")
public class FieldAccessTest {
    /**
     * 検証用フィールド
     */
    private int intField = 0;
    private static int staticIntField = 0;
    private FieldAccessTest self;

    /**
     * インスタンス生成のスコープ検証用メンバクラス
     */
    private class Inner {
        void innerMethod() {
            return;
        }
    }

    /**
     * 括弧で囲まれたフィールドアクセス検証用メソッド
     */
    public void enclosedFieldAccessTest() {
        // 書き込み用ローカル変数
        int temp;

        // 括弧で囲まれた代入式の左辺の書き込みフィールドアクセス
        (this.intField) = 10; // this.intField:1回目(書き込み)

        // 二重の括弧で囲まれた代入式の左辺の書き込みフィールドアクセス
        ((this.intField)) = 10; // this.intField:2回目(書き込み)

        // 括弧で囲まれた代入式の右辺の読み取りフィールドアクセス
        temp = (this.intField); // this.intField:1回目(読み取り)

        // 二重の括弧で囲まれた代入式の右辺の読み取りフィールドアクセス
        temp = ((this.intField)); // this.intField:2回目(読み取り)

        // 括弧で囲まれたインクリメント演算子を用いた単項演算式の書き込みフィールドアクセス
        (((intField)))++; // intField:1回目(書き込み)

        // 括弧で囲まれたメソッド呼び出しのスコープとしてのフィールドアクセス(レシーバ)
        (self).dummyMethod(0); // self:1回目(その他)

        // 括弧で囲まれたインスタンス生成のスコープとしてのフィールドアクセス(レシーバ)
        Inner inner = (this.self).new Inner(); // this.self:1回目(その他)
        return;
    }

    /**
     * フィールドアクセス検出検証用メソッド
     */
    public void fieldAccessDetectionTest() {
        // 明示的なスコープ指定(this)があるアクセス
        this.intField = 1; // this.intField:1回目(書き込み)

        // 暗黙的なフィールドアクセス
        intField = 2; // FieldAccessTest.intField:1回目(書き込み)

        // staticなフィールドへのアクセス
        staticIntField = 3; // FieldAccesstest.staticIntField:1回目(書き込み)
        FieldAccessTest.staticIntField = 4; // FieldAccessTest.staticIntField:2回目(書き込み)

        // フィールドアクセス回数カウント
        intField = 5; // FieldAccessTest.intField:2回目(書き込み)
        intField = 6; // FieldAccessTest.intField:3回目(書き込み)
        return;
    }

    /**
     * その他のフィールドアクセス検証用メソッド
     */
    public void otherAccessTest() {
        // メソッド呼び出しのスコープとしてのフィールドアクセス(レシーバ)
        this.self.dummyMethod(0); // this.self:1回目(その他)

        // インスタンス生成のスコープとしてのフィールドアクセス(レシーバ)
        Inner inner = this.self.new Inner(); // this.self:2回目(その他)

        // ローカル変数の場合フィールドアクセスではない
        FieldAccessTest anotherSelf = new FieldAccessTest();
        anotherSelf.dummyMethod(0);
        return;
    }

    /**
     * 読み取りフィールドアクセス検証用メソッド
     */
    public void readAccessTest() {
        // 書き込み用ローカル変数
        int temp;

        // 代入式の右辺の読み取りフィールドアクセス
        temp = intField; // intField:1回目(読み取り)

        // 二項演算式の読み取りフィールドアクセス
        temp = intField + temp; // intField:2回目(読み取り)

        // 条件式の単項演算式の読み取りフィールドアクセス
        if (intField > 0) { // intField:3回目(読み取り)
        }

        // 三項演算式の読み取りフィールドアクセス
        temp = (intField > 0) ? 1 : 0; // intField:4回目(読み取り)

        // 引数内の読み取りフィールドアクセス
        dummyMethod(intField); // intField:5回目(読み取り)
        return;
    }

    /**
     * 書き込みフィールドアクセス検証用メソッド
     */
    public void writeAccessTest() {
        // 代入式の左辺の書き込みフィールドアクセス
        intField = 10; // intField:1回目(書き込み)

        // インクリメント演算子を用いた単項演算式の書き込みフィールドアクセス
        intField++; // intField:2回目(書き込み)
        ++intField; // intField:3回目(書き込み)

        // デクリメント演算子を用いた単項演算式の書き込みフィールドアクセス
        intField--; // intField:4回目(書き込み)
        --intField; // intField:5回目(書き込み)
        return;
    }

    /**
     * ダミーメソッド
     * 
     * @param intArgument
     */
    private void dummyMethod(int intArgument) {
        return;
    }
}
