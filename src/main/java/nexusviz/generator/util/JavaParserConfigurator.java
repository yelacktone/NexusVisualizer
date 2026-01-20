package nexusviz.generator.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JarTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;

/**
 * JavaParserの設定を行うユーティリティクラス。
 * 
 * @author Ishiguro
 * @version 1.0
 */
public class JavaParserConfigurator extends Object {
    /**
     * インスタンス化を防止するためのコンストラクタ。
     */
    private JavaParserConfigurator() {
    }

    /**
     * JavaParserのシンボル解決とJava 21対応の設定を行う。
     * 
     * @param sourceRootDirectory ソースコードのルートディレクトリ
     * @param jarDirectory        JARファイルが格納されているディレクトリ
     */
    public static void configureSolver(Path sourceRootDirectory, Path jarDirectory) {
        // 型解決のためのCombinedTypeSolverを構築
        CombinedTypeSolver typeSolver = new CombinedTypeSolver();

        // JDKクラスなど標準のTypeSolverを追加
        typeSolver.add(new ReflectionTypeSolver());

        // ソースコードのTypeSolverを追加
        typeSolver.add(new JavaParserTypeSolver(sourceRootDirectory));
        System.out.println("ソースコードのTypeSolverを追加: " + sourceRootDirectory.toString());

        // JARディレクトリを再帰的に探索し、JarTypeSolverにJARファイルを追加
        if (jarDirectory != null && Files.isDirectory(jarDirectory) && Files.exists(jarDirectory)) {
            try (Stream<Path> paths = Files.walk(jarDirectory)) {
                paths.filter(path -> Files.isRegularFile(path))
                        .filter(path -> path.toString().endsWith(".jar"))
                        .forEach(path -> {
                            try {
                                typeSolver.add(new JarTypeSolver(path.toFile()));
                                System.out.println("JARファイルを追加: " + path.getFileName());
                            } catch (Exception e) {
                                System.err.println("JARファイルの追加失敗: " + e.getMessage());
                            }
                        });
            } catch (IOException e) {
                System.err.println("JARファイルの読み込み失敗: " + e.getMessage());
            }
        }

        // Java 21 に対応する設定
        ParserConfiguration config = new ParserConfiguration()
                .setLanguageLevel(ParserConfiguration.LanguageLevel.JAVA_21).setSymbolResolver(
                        new JavaSymbolSolver(typeSolver));

        // StaticJavaParserのデフォルト設定にも反映
        StaticJavaParser.setConfiguration(config);
        return;
    }
}
