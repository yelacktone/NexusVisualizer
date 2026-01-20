package nexusviz.generator.analyzer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

import com.github.javaparser.ParseProblemException;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;

import nexusviz.generator.util.JavaParserConfigurator;

/**
 * StructuralAnalyzerとDependencyAnalyzerの抽象クラス。
 * 
 * @param <Result> 解析結果の型
 * 
 * @author Ishiguro
 * @version 1.0
 */
public abstract class AbstractAnalyzer<Result> extends Object {
    /**
     * 解析中にエラーが生じたかどうかを束縛する。
     */
    protected Boolean hasError;

    /**
     * デフォルトコンストラクタ。
     */
    public AbstractAnalyzer() {
    }

    /**
     * 引数で受け取ったファイルを解析し、結果を応答する。
     * 
     * @param sourceRootPath   ソースコードのルートパス
     * @param jarDirectoryPath ライブラリのディレクトリのパス
     * @return 解析結果
     */
    public Result analyze(Path sourceRootPath, Path jarDirectoryPath) {
        // JavaParserの初期設定
        JavaParserConfigurator.configureSolver(sourceRootPath, jarDirectoryPath);

        // エラーフラグをリセット
        resetError();

        // 結果を集めるための初期化
        initializeResultElements();

        // ファイルの探索と解析
        try (Stream<Path> paths = Files.walk(sourceRootPath)) {
            paths.filter(path -> Files.isRegularFile(path))
                    .filter(path -> path.toString().endsWith(".java"))
                    .forEach(path -> {
                        try {
                            CompilationUnit aCompilationUnit = StaticJavaParser.parse(path.toFile());
                            analyzeUnit(aCompilationUnit, path);
                        } catch (ParseProblemException e) {
                            System.err.println("構文エラー: " + path.toString());
                            handleError();
                        } catch (Exception e) {
                            System.err.println("解析失敗: " + path.toString());
                            handleError();
                        }
                    });
        } catch (IOException e) {
            System.err.println("ファイル読み込み失敗: " + e.getMessage());
            handleError();
        }

        return buildResult();
    }

    /**
     * エラー時の処理を行う。
     */
    protected void handleError() {
        this.hasError = true;
        return;
    }

    /**
     * ファイル単位の解析を行う。
     * 
     * @param aCompilationUnit コンパイルユニット
     * @param filePath         解析するファイルのパス
     */
    protected abstract void analyzeUnit(CompilationUnit aCompilationUnit, Path filePath);

    /**
     * 最終結果を生成して応答する。
     * 
     * @return 解析結果
     */
    protected abstract Result buildResult();

    /**
     * 解析結果の要素を初期化する。
     */
    protected abstract void initializeResultElements();

    /**
     * エラーフラグをリセットする。
     */
    private void resetError() {
        this.hasError = false;
        return;
    }
}
