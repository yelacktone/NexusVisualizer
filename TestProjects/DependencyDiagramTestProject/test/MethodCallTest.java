package test;

/**
 * メソッド呼び出し検証用クラス
 * 
 * @author Ishiguro
 * @version 1.0
 */
public class MethodCallTest {
    /**
     * 外部メソッド呼び出し用
     */
    private DependencyTestClass dependencyTestClass = new DependencyTestClass();

    /**
     * メソッド呼び出し検証用メソッド
     */
    public void methodCallTest() {
        // ローカルメソッド呼び出し
        localMethod(); // localMethod():1回目

        // thisによるローカルメソッド呼び出し
        this.localMethod(); // localMethod():2回目

        // 他のクラスのメソッド呼び出し
        dependencyTestClass.externalMethod(); // externalMethod():1回目

        // 引数および戻り値ありのメソッド呼び出し
        this.methodWithArgumentsAndReturnValue(10); // methodWithArgumentsAndReturnValue(Integer):1回目

        // メソッド呼び出し回数カウント
        countTestMethod(); // countTestMethod():1回目
        countTestMethod(); // countTestMethod():2回目
        countTestMethod(); // countTestMethod():3回目

        // thisによるメソッド呼び出し回数カウント
        this.countTestMethod(); // countTestMethod():4回目
        this.countTestMethod(); // countTestMethod():5回目
        this.countTestMethod(); // countTestMethod():6回目
        return;
    }

    /**
     * 再帰呼び出し検証用メソッド
     */
    public void recursiveCallMethod(Integer index) {
        if (index < 5) {
            System.out.println(index);
            index++;
            recursiveCallMethod(index);
        }
        return;
    }

    /**
     * 呼び出し回数カウントテスト用メソッド
     */
    private void countTestMethod() {
        return;
    }

    /**
     * ローカルメソッド
     */
    private void localMethod() {
        return;
    }

    /**
     * 引数および戻り値ありのメソッド呼び出し検証用メソッド
     * 
     * @param max ループ回数
     * @return 0からmaxまでの値を連結した文字列
     */
    private String methodWithArgumentsAndReturnValue(Integer max) {
        StringBuilder stringBuilder = new StringBuilder();
        for (Integer index = 0; index < max; index++) {
            if (index > 0) {
                stringBuilder.append(", ");
            }
            stringBuilder.append(index);
        }
        return stringBuilder.toString();
    }
}
