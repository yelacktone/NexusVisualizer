package nexusviz.generator.util;

import java.util.Map;

/**
 * NexusVisualizer におけるファイル出力パスを生成するユーティリティクラス。
 * 
 * @author Ishiguro
 * @version 1.0
 */
public class PathGenerator extends Object {
    /**
     * インスタンス化を防止するためのコンストラクタ。
     */
    private PathGenerator() {
    }

    /**
     * 引数の文字列を、出力されるクラス図のPlantUML形式のテキストファイルの名前に組み込み、そのパスを応答する。
     * 
     * @param baseName テキストファイル名に組み込むディレクトリ名
     * @return テキストファイルのパス
     */
    public static String outputClassDiagramFileString(String baseName) {
        return "output_" + baseName + "_classdiagram.puml";
    }

    /**
     * 引数の文字列を、出力されるクラス図の画像の名前に組み込み、そのパスを応答する。
     * 
     * @param baseName 画像名に組み込むディレクトリ名
     * @return 画像のパス
     */
    public static String outputClassDiagramImageString(String baseName) {
        return "output_" + baseName + "_classdiagram.png";
    }

    /**
     * 引数の文字列を、出力される依存図のPlantUML形式のテキストファイルの名前に組み込み、そのパスを応答する。
     * 
     * @param declaringTypeName テキストファイル名に組み込む型名
     * @param methodName        テキストファイル名に組み込むメソッド名
     * @param parameters        パラメータ名と型名のマップ
     * @return テキストファイルのパス
     */
    public static String outputDependencyDiagramFileString(String declaringTypeName, String methodName,
            Map<String, String> parameters) {
        StringBuilder imagePath = new StringBuilder();
        imagePath.append("output_");
        imagePath.append(declaringTypeName);
        imagePath.append("_");
        imagePath.append(methodName);
        imagePath.append("_");
        parameters.keySet().forEach(paramName -> {
            imagePath.append(parameters.get(paramName));
            imagePath.append("_");
        });
        imagePath.append("dependencydiagram.puml");
        return imagePath.toString();
    }

    /**
     * 引数の文字列を、出力される依存図の画像の名前に組み込み、そのパスを応答する。
     * 
     * @param declaringTypeName 画像名に組み込む型名
     * @param methodName        画像名に組み込むメソッド名
     * @param parameters        パラメータ名と型名のマップ
     * @return 画像のパス
     */
    public static String outputDependencyDiagramImageString(String declaringTypeName, String methodName,
            Map<String, String> parameters) {
        StringBuilder imagePath = new StringBuilder();
        imagePath.append("output_");
        imagePath.append(declaringTypeName);
        imagePath.append("_");
        imagePath.append(methodName);
        imagePath.append("_");
        parameters.keySet().forEach(paramName -> {
            imagePath.append(parameters.get(paramName));
            imagePath.append("_");
        });
        imagePath.append("dependencydiagram.png");
        return imagePath.toString();
    }
}
