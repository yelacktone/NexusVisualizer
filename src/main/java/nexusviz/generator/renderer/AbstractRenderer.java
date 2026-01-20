package nexusviz.generator.renderer;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import net.sourceforge.plantuml.SourceStringReader;

/**
 * StructuralRendererとDependencyRendererの抽象クラス。
 * 
 * @author Ishiguro
 * @version 1.0
 */
public abstract class AbstractRenderer extends Object {
    /**
     * PlantUML形式で図の内容を束縛する。
     */
    protected StringBuilder puml;

    /**
     * 改行コードを束縛する。
     */
    protected static final String LINE_SEPARATOR = System.lineSeparator();

    /**
     * PlantUMLのサイズ制限の最大値を束縛する。
     */
    private static final String MAX_PLANTUML_SIZE = "16384";

    /**
     * フィールドを初期化するコンストラクタ。
     */
    public AbstractRenderer() {
        this.puml = new StringBuilder();
        return;
    }

    /**
     * IDに使用できない文字をエスケープして返す。
     * 
     * @param stringForId エスケープ対象の文字列
     * @return エスケープ後の文字列
     */
    protected String escapeForIdString(String stringForId) {
        return stringForId.replaceAll("[^A-Za-z0-9_]", "_");
    }

    /**
     * シグネチャ文字列に使用できない文字をエスケープして応答する。
     * 
     * @param stringForSignature エスケープ対象の文字列
     * @return エスケープ後の文字列
     */
    protected String escapeForSignatureString(String stringForSignature) {
        return stringForSignature.replaceAll("[\\n\\r\\t]+", "").replace("\"", "'");
    }

    /**
     * ジェネリクスの記述をエスケープして返す。
     * "❮"、"❯"に置換する。
     * 別案として、"⟨"、"⟩"に置換する方法もあるが、細くて見づらいため、こちらを採用する。
     * 
     * @param typeName 型名
     * @return エスケープ後の型名
     */
    protected String escapeGenerics(String typeName) {
        return typeName.replaceAll("<", "❮").replaceAll(">", "❯");
    }

    /**
     * PlantUMLテキストのファイルへの書き出し、画像の生成を行い、生成された画像ファイルのパスを応答する。
     * 
     * @param pumlFilePath  PlantUMLテキストファイルのパス
     * @param imageFilePath 画像ファイルのパス
     * @return 画像ファイルのパスの文字列
     */
    protected String exportToFile(String pumlFilePath, String imageFilePath) {
        this.puml.append("@enduml");

        // PlantUMLファイルの出力
        try (PrintWriter aWriter = new PrintWriter(new BufferedWriter(new FileWriter(pumlFilePath)))) {
            aWriter.println(this.puml.toString());
            System.out.println("PlantUMLファイルを出力しました: " + pumlFilePath);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // 画像ファイルの出力
        System.setProperty("PLANTUML_LIMIT_SIZE", MAX_PLANTUML_SIZE);
        try (OutputStream outputStream = new FileOutputStream(imageFilePath)) {
            SourceStringReader aReader = new SourceStringReader(this.puml.toString());
            aReader.outputImage(outputStream);
            System.out.println("画像ファイルを出力しました: " + imageFilePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return imageFilePath;
    }

    /**
     * 完全修飾名からクラス名・メソッド名などのシンプルネームを取得して返す。
     * 
     * @param fullyQualifiedName 完全修飾名
     * @return シンプルネーム
     */
    protected String getSimpleName(String fullyQualifiedName) {
        if (fullyQualifiedName == null || fullyQualifiedName.isEmpty()) {
            return "";
        }

        // 再帰的に<>内も処理
        if (fullyQualifiedName.contains("<")) {
            Integer startIndex = fullyQualifiedName.indexOf('<');
            Integer endIndex = fullyQualifiedName.lastIndexOf('>');
            String base = fullyQualifiedName.substring(0, startIndex).trim();
            String genericPart = fullyQualifiedName.substring(startIndex + 1, endIndex).trim();
            Integer genericsDepth = 0;

            // カンマ区切りのジェネリック型を分割して、それぞれ再帰的に処理
            List<String> genericTypes = new ArrayList<>();
            StringBuilder currentType = new StringBuilder();
            for (Character currentChar : genericPart.toCharArray()) {
                if (currentChar == '<') {
                    genericsDepth++;
                } else if (currentChar == '>') {
                    genericsDepth--;
                }

                // ネストレベルが0、かつカンマが来たときだけ分割する
                if (currentChar == ',' && genericsDepth == 0) {
                    genericTypes.add(currentType.toString().trim());
                    currentType.setLength(0);
                } else {
                    currentType.append(currentChar);
                }
            }
            genericTypes.add(currentType.toString().trim());

            StringBuilder simpleName = new StringBuilder(getSimpleName(base));
            simpleName.append("<");
            String delimiter = "";
            for (String type : genericTypes) {
                simpleName.append(delimiter);
                simpleName.append(getSimpleName(type));
                delimiter = ", ";
            }
            simpleName.append(">");

            // ジェネリクスの終わり(">")以降に文字列(配列の"[]")があれば追加する
            if (fullyQualifiedName.length() > endIndex + 1) {
                simpleName.append(fullyQualifiedName.substring(endIndex + 1));
            }

            return escapeForSignatureString(simpleName.toString());
        }

        // ジェネリクス外なら、最後のドット以降を取る
        Integer lastDotIndex = fullyQualifiedName.lastIndexOf('.');
        if (lastDotIndex == -1) {
            return escapeForSignatureString(fullyQualifiedName.trim());
        }
        return escapeForSignatureString(fullyQualifiedName.substring(lastDotIndex + 1).trim());
    }
}
