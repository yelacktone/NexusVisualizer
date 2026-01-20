package nexusviz.mvc;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.nio.file.Path;

import javax.swing.JPopupMenu;

import mvc.Model;
import nexusviz.generator.model.dependency.CallerMethodInfo;
import nexusviz.generator.result.DependencyAnalysisResult;

/**
 * MVCモデルのモデルに相当するクラス。
 * 
 * @author Ishiguro
 * @version 1.0
 */
public class VisualizeModel extends Model {
    /**
     * 依存解析結果を束縛する。
     */
    private DependencyAnalysisResult dependencyAnalysisResult;

    /**
     * ウィンドウ上で右クリックした際に表示されるポップアップメニューを束縛する。
     */
    private JPopupMenu popupMenu;

    /**
     * クラス図のサイズを束縛する。
     */
    private Dimension preferredSize;

    /**
     * クラス図に変換するプロジェクトのソースディレクトリを束縛する。
     */
    private Path selectedDirectory;

    /**
     * クラス図に変換するプロジェクトで使用されているライブラリのディレクトリを束縛する。
     */
    private Path selectedLibraryDirectory;

    /**
     * 選択されたメソッドが宣言されている型名を束縛する。
     */
    private String selectedMethodDeclaringTypeName;

    /**
     * 選択されたメソッド情報を束縛する。
     */
    private CallerMethodInfo selectedMethodInfo;

    /**
     * インスタンスを生成して応答するコンストラクタ。
     */
    public VisualizeModel() {
        super();
        this.preferredSize = new Dimension(1200, 900);
        this.selectedDirectory = null;
        this.selectedLibraryDirectory = null;
        this.selectedMethodDeclaringTypeName = null;
        this.selectedMethodInfo = null;
        this.popupMenu = null;
        return;
    }

    /**
     * モデルの内部状態が変化していたので、自分の依存物へupdateのメッセージを送信する。
     * 
     * @param anImage 変更後の画像
     */
    public void changed(BufferedImage anImage) {
        this.preferredSize = new Dimension(anImage.getWidth(), anImage.getHeight());
        this.dependents.forEach(dependent -> {
            ((VisualizeView) dependent).update();
        });
        return;
    }

    /**
     * 引数の解析結果をフィールドにセットする。
     * 
     * @param dependencyAnalysisResult 依存解析結果
     */
    public void dependencyAnalysisResult(DependencyAnalysisResult dependencyAnalysisResult) {
        this.dependencyAnalysisResult = dependencyAnalysisResult;
        return;
    }

    /**
     * フィールド変数 dependencyAnalysisResult を返す。
     * 
     * @return 依存解析結果
     */
    public DependencyAnalysisResult dependencyAnalysisResult() {
        return this.dependencyAnalysisResult;
    }

    /**
     * 引数のポップアップメニューをフィールドにセットする。
     * 
     * @param popupMenu ポップアップメニュー
     */
    public void popupMenu(JPopupMenu popupMenu) {
        this.popupMenu = popupMenu;
        return;
    }

    /**
     * フィールド変数 popupMenu を返す。
     * 
     * @return ポップアップメニュー
     */
    public JPopupMenu popupMenu() {
        return this.popupMenu;
    }

    /**
     * 引数のサイズをフィールドにセットする。
     * 
     * @param preferredSize クラス図のサイズ
     */
    public void preferredSize(Dimension preferredSize) {
        this.preferredSize = preferredSize;
        return;
    }

    /**
     * フィールド変数 preferredSize を返す。
     * 
     * @return クラス図のサイズ
     */
    public Dimension preferredSize() {
        return this.preferredSize;
    }

    /**
     * 引数のファイルをフィールドにセットする。
     * 
     * @param sourceRootDirectory クラス図に変換するディレクトリ
     */
    public void selectedDirectory(Path sourceRootDirectory) {
        this.selectedDirectory = sourceRootDirectory;
        return;
    }

    /**
     * フィールド変数 selectedDirectory を返す。
     * 
     * @return クラス図に変換するプロジェクト
     */
    public Path selectedDirectory() {
        return this.selectedDirectory;
    }

    /**
     * 引数のファイルをフィールドにセットする。
     * 
     * @param libraryDirectory クラス図に変換するプロジェクトで使用されているライブラリのディレクトリ
     */
    public void selectedLibraryDirectory(Path libraryDirectory) {
        this.selectedLibraryDirectory = libraryDirectory;
        return;
    }

    /**
     * フィールド変数 selectedLibraryDirectory を返す。
     * 
     * @return クラス図に変換するプロジェクトで使用されているライブラリのディレクトリ
     */
    public Path selectedLibraryDirectory() {
        return this.selectedLibraryDirectory;
    }

    /**
     * 引数の型名をフィールドにセットする。
     * 
     * @param declaringTypeName 型名
     */
    public void selectedMethodDeclaringTypeName(String declaringTypeName) {
        this.selectedMethodDeclaringTypeName = declaringTypeName;
        return;
    }

    /**
     * フィールド変数 selectedMethodDeclaringTypeName を返す。
     * 
     * @return 型名
     */
    public String selectedMethodDeclaringTypeName() {
        return this.selectedMethodDeclaringTypeName;
    }

    /**
     * 引数のメソッド情報をフィールドにセットする。
     * 
     * @param methodInfo メソッド情報
     */
    public void selectedMethodInfo(CallerMethodInfo methodInfo) {
        this.selectedMethodInfo = methodInfo;
        return;
    }

    /**
     * フィールド変数 selectedMethod を返す。
     * キー：型名，バリュー：メソッド情報
     * 
     * @return メソッド情報(キー：型名，バリュー：メソッド情報)
     */
    public CallerMethodInfo selectedMethodInfo() {
        return this.selectedMethodInfo;
    }
}
