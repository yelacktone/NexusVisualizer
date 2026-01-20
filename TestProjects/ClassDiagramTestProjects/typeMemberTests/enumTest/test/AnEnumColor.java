package test;

/**
 * フィールド、メソッド、コンストラクタが正しく出力されるかのテスト
 * 列挙型特有の要素のみ
 * 
 * @author Ishiguro
 * @version 1.0
 */
public enum AnEnumColor {
    /** 赤 */
    RED(255, 0, 0),
    /** 緑 */
    GREEN(0, 255, 0),
    /** 青 */
    BLUE(0, 0, 255),
    /** 黄 */
    YELLOW(255, 255, 0),
    /** マゼンタ */
    MAGENTA(255, 0, 255),
    /** シアン */
    CYAN(0, 255, 255);

    /**
     * フィールド
     * パブリック・スタティック・ファイナル・初期化あり
     * 数値の上限
     */
    public static final int MAX_COLOR_VALUE = 255;

    /**
     * フィールド
     * プライベート・ファイナル・初期化なし
     * 赤色の成分
     */
    private final int r;

    /**
     * フィールド
     * プライベート・ファイナル・初期化なし
     * 緑色の成分
     */
    private final int g;

    /**
     * フィールド
     * プライベート・ファイナル・初期化なし
     * 青色の成分
     */
    private final int b;

    /**
     * コンストラクタ
     * 暗黙プライベート・引数あり
     * 
     * @param r 赤色成分
     * @param g 緑色成分
     * @param b 青色成分
     */
    AnEnumColor(int r, int g, int b) {
        this.r = r;
        this.g = g;
        this.b = b;
    }

    /**
     * メソッド
     * パブリック・引数なし・通常型戻り値
     * 赤色の成分を取得する
     * 
     * @return int型
     */
    public int getR() {
        return this.r;
    }

    /**
     * メソッド
     * パブリック・引数なし・通常型戻り値
     * 緑色の成分を取得する
     * 
     * @return int型
     */
    public int getG() {
        return this.g;
    }

    /**
     * メソッド
     * パブリック・引数なし・通常型戻り値
     * 青色の成分を取得する
     * 
     * @return int型
     */
    public int getB() {
        return this.b;
    }

    /**
     * メソッド
     * スタティック
     * 赤色の列挙定数を取得する
     * 
     * @return AnEnumColor
     */
    static AnEnumColor getRED() {
        return RED;
    }
}
