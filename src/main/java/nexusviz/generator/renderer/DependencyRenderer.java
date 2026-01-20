package nexusviz.generator.renderer;

import java.util.Map;

import nexusviz.generator.model.dependency.AccessType;
import nexusviz.generator.model.dependency.AccessedFieldInfo;
import nexusviz.generator.model.dependency.CalleeMethodInfo;
import nexusviz.generator.model.dependency.CallerMethodInfo;
import nexusviz.generator.util.PathGenerator;

/**
 * 依存解析結果をもとにPlantUML形式の記述に変換していく。
 * 主にメソッドの依存(インスタンス生成・メソッド呼び出し・フィールドアクセス)を扱う。
 * 
 * @author Ishiguro
 * @version 1.0
 */
public class DependencyRenderer extends AbstractRenderer {
    /**
     * インスタンスを生成して応答するコンストラクタ。
     */
    public DependencyRenderer() {
        super();
        this.puml.append("@startuml メソッド依存図").append(LINE_SEPARATOR);
        this.puml.append("left to right direction").append(LINE_SEPARATOR);
        return;
    }

    /**
     * フィールドアクセス関係を追加する。
     * 
     * @param callerDeclaringTypeName アクセス元メソッドが宣言されている型名
     * @param callerMethodInfo        アクセス元メソッドの情報
     * @param accessedFields          アクセスフィールドの情報とそのアクセス回数のマップ
     */
    public void addAccessRelations(String callerDeclaringTypeName, CallerMethodInfo callerMethodInfo,
            Map<AccessedFieldInfo, Integer> accessedFields) {
        accessedFields.keySet().forEach(accessedField -> {
            addMethodIdString(callerDeclaringTypeName, callerMethodInfo.methodName(), callerMethodInfo.parameters(),
                    callerMethodInfo.returnTypeName());
            addFrequencyArrow(accessedFields.get(accessedField));
            addFieldIdString(accessedField.declaringTypeName(), accessedField.fieldName());
            this.puml.append(" : ");
            this.puml.append(accessedFields.get(accessedField));
            if (accessedFields.get(accessedField) == 1) {
                if (accessedField.accessType() == AccessType.WRITE) { // 書き込みアクセス
                    this.puml.append(" write");
                } else if (accessedField.accessType() == AccessType.READ) { // 読み取りアクセス
                    this.puml.append(" read");
                } else if (accessedField.accessType() == AccessType.OTHER) { // それ以外のアクセス
                    this.puml.append(" access");
                }
            } else {
                if (accessedField.accessType() == AccessType.WRITE) { // 書き込みアクセス
                    this.puml.append(" writes");
                } else if (accessedField.accessType() == AccessType.READ) { // 読み取りアクセス
                    this.puml.append(" reads");
                } else if (accessedField.accessType() == AccessType.OTHER) { // それ以外のアクセス
                    this.puml.append(" accesses");
                }
            }
            this.puml.append(LINE_SEPARATOR);
        });
        return;
    }

    /**
     * アクセスフィールド群を追加する。
     * 
     * @param methodDeclaringTypeName メソッドを定義している型名
     * @param accessedFields          アクセスフィールドの情報とそのアクセス回数のマップ
     */
    public void addAllAccessedFields(String methodDeclaringTypeName, Map<AccessedFieldInfo, Integer> accessedFields) {
        accessedFields.entrySet().forEach(entry -> {
            AccessedFieldInfo accessedField = entry.getKey();
            addAccessedField(methodDeclaringTypeName, accessedField.declaringTypeName(), accessedField.fieldName(),
                    accessedField.accessType().toString());
        });
        return;
    }

    /**
     * 呼び出し先メソッド群を追加する。
     * 
     * @param calleeMethods 呼び出し先メソッドの情報とその呼び出し回数のマップ
     */
    public void addAllCalleeMethods(Map<CalleeMethodInfo, Integer> calleeMethods) {
        calleeMethods.entrySet().forEach(entry -> {
            CalleeMethodInfo calleeMethod = entry.getKey();
            addCalleeMethod(calleeMethod.declaringTypeName(), calleeMethod.methodName(),
                    calleeMethod.parameters(), calleeMethod.returnTypeName());
        });
        return;
    }

    /**
     * 呼び出し元メソッドを追加する。
     * 
     * @param declaringTypeName 定義している型名
     * @param methodInfo        メソッド情報
     */
    public void addCallerMethod(String declaringTypeName, CallerMethodInfo methodInfo) {
        this.puml.append("rectangle \"");
        this.puml.append(getSimpleName(declaringTypeName));
        this.puml.append("\" as ");
        this.puml.append(escapeForIdString(declaringTypeName));
        this.puml.append(" {").append(LINE_SEPARATOR);
        this.puml.append("\trectangle \"");
        addMethodSignature(methodInfo.methodName(), methodInfo.parameters(), methodInfo.returnTypeName());
        this.puml.append("\" as ");
        addMethodIdString(declaringTypeName, methodInfo.methodName(), methodInfo.parameters(),
                methodInfo.returnTypeName());
        this.puml.append(LINE_SEPARATOR);
        this.puml.append("}").append(LINE_SEPARATOR);
        return;
    }

    /**
     * メソッド呼び出し関係を追加する。
     * 
     * @param callerDeclaringTypeName 呼び出し元メソッドが宣言されている型名
     * @param callerMethodInfo        呼び出し元メソッドの情報
     * @param calleeMethods           呼び出し先メソッドの情報とその呼び出し回数のマップ
     */
    public void addCallRelations(String callerDeclaringTypeName, CallerMethodInfo callerMethodInfo,
            Map<CalleeMethodInfo, Integer> calleeMethods) {
        calleeMethods.keySet().forEach(calleeMethod -> {
            addMethodIdString(callerDeclaringTypeName, callerMethodInfo.methodName(), callerMethodInfo.parameters(),
                    callerMethodInfo.returnTypeName());
            addFrequencyArrow(calleeMethods.get(calleeMethod));
            addMethodIdString(calleeMethod.declaringTypeName(), calleeMethod.methodName(), calleeMethod.parameters(),
                    calleeMethod.returnTypeName());
            this.puml.append(" : ");
            this.puml.append(calleeMethods.get(calleeMethod));
            if (calleeMethods.get(calleeMethod) == 1) {
                this.puml.append(" call");
            } else {
                this.puml.append(" calls");
            }
            this.puml.append(LINE_SEPARATOR);
        });
        return;
    }

    /**
     * PlantUMLからメソッド依存図の画像(png)を生成する。
     * 
     * @param declaringTypeName 宣言されている型名
     * @param methodName        メソッド名
     * @param parameters        パラメータ名と型名のマップ
     * @return 生成した画像ファイルのパス文字列
     */
    public String render(String declaringTypeName, String methodName, Map<String, String> parameters) {
        String pumlFilePath = PathGenerator.outputDependencyDiagramFileString(declaringTypeName, methodName,
                parameters);
        String imageFilePath = PathGenerator.outputDependencyDiagramImageString(declaringTypeName, methodName,
                parameters);

        return exportToFile(pumlFilePath, imageFilePath);
    }

    /**
     * アクセスフィールドを追加する。
     * 
     * @param methodDeclaringTypeName メソッドを定義している型名
     * @param fieldDeclaringTypeName  フィールドを定義している型名
     * @param fieldName               フィールド名
     * @param accessType              アクセスの種類
     */
    private void addAccessedField(String methodDeclaringTypeName, String fieldDeclaringTypeName, String fieldName,
            String accessType) {
        if (fieldDeclaringTypeName.equals("this") || fieldDeclaringTypeName.equals("super")) { // thisまたはsuperの場合
            this.puml.append("rectangle \"");
            this.puml.append(getSimpleName(methodDeclaringTypeName));
            this.puml.append("\" as ");
            this.puml.append(escapeForIdString(methodDeclaringTypeName));
        } else { // 通常の型名の場合
            this.puml.append("rectangle \"");
            this.puml.append(getSimpleName(fieldDeclaringTypeName));
            this.puml.append("\" as ");
            this.puml.append(escapeForIdString(fieldDeclaringTypeName));
        }
        this.puml.append(" {").append(LINE_SEPARATOR);
        this.puml.append("\trectangle \"");
        this.puml.append(getSimpleName(fieldDeclaringTypeName));
        this.puml.append(".");
        this.puml.append(getSimpleName(fieldName));
        this.puml.append("\" as ");
        addFieldIdString(fieldDeclaringTypeName, fieldName);
        this.puml.append(LINE_SEPARATOR);
        this.puml.append("}").append(LINE_SEPARATOR);
        return;
    }

    /**
     * 呼び出し先メソッドを追加する。
     * 
     * @param declaringTypeName 定義している型名
     * @param methodName        メソッド名
     * @param parameters        パラメータ名と型名のマップ
     * @param returnTypeName    戻り値の型名
     */
    private void addCalleeMethod(String declaringTypeName, String methodName, Map<String, String> parameters,
            String returnTypeName) {
        this.puml.append("rectangle \"");
        this.puml.append(getSimpleName(declaringTypeName));
        this.puml.append("\" as ");
        this.puml.append(escapeForIdString(declaringTypeName));
        this.puml.append(" {").append(LINE_SEPARATOR);
        this.puml.append("\trectangle \"");
        addMethodSignature(methodName, parameters, returnTypeName);
        this.puml.append("\" as ");
        addMethodIdString(declaringTypeName, methodName, parameters, returnTypeName);
        this.puml.append(LINE_SEPARATOR);
        this.puml.append("}").append(LINE_SEPARATOR);
        return;
    }

    /**
     * フィールドのID文字列を作成してPlantUML形式の記述に追加する。
     * 
     * @param fieldDeclaringTypeName フィールドを定義している型名
     * @param fieldName              フィールド名
     */
    private void addFieldIdString(String fieldDeclaringTypeName, String fieldName) {
        this.puml.append(escapeForIdString(fieldDeclaringTypeName));
        this.puml.append("_");
        this.puml.append(escapeForIdString(fieldName));
        this.puml.append("_field");
        return;
    }

    /**
     * 呼び出しまたはアクセス回数に応じた矢印を追加する。
     * 
     * @param callOrAccessCount 呼び出しまたはアクセス回数
     */
    private void addFrequencyArrow(Integer callOrAccessCount) {
        switch (callOrAccessCount) {
            case 1 -> {
                this.puml.append(" .[#DEEPSKYBLUE].> ");
                break;
            }
            case 2 -> {
                this.puml.append(" .[#BLUE].> ");
                break;
            }
            case 3 -> {
                this.puml.append(" .[#GREEN].> ");
                break;
            }
            case 4, 5 -> {
                this.puml.append(" .[#LIME].> ");
                break;
            }
            case 6, 7 -> {
                this.puml.append(" .[#ORANGE].> ");
                break;
            }
            case 8, 9 -> {
                this.puml.append(" .[#RED].> ");
                break;
            }
            default -> {
                this.puml.append(" .[#DARKRED].> ");
                break;
            }
        }
        return;
    }

    /**
     * メソッドのID文字列を作成してPlantUML形式の記述に追加する。
     * 
     * @param declaringTypeName 宣言されている型名
     * @param methodName        メソッド名
     * @param parameters        パラメータ名と型名のマップ
     * @param returnTypeName    戻り値の型名
     */
    private void addMethodIdString(String declaringTypeName, String methodName, Map<String, String> parameters,
            String returnTypeName) {
        this.puml.append(escapeForIdString(declaringTypeName));
        this.puml.append("_");
        this.puml.append(escapeForIdString(methodName));
        parameters.entrySet().forEach(parameter -> {
            this.puml.append("_");
            this.puml.append(escapeForIdString(parameter.getKey()));
            this.puml.append("_");
            this.puml.append(escapeForIdString(getSimpleName(parameter.getValue())));
        });
        if (returnTypeName != null) {
            this.puml.append("_");
            this.puml.append(escapeForIdString(getSimpleName(returnTypeName)));
        }
        this.puml.append("_method");
        return;
    }

    /**
     * メソッドシグネチャを作成してPlantUML形式の記述に追加する。
     * 
     * @param methodName     メソッド名
     * @param parameters     パラメータ名と型名のマップ
     * @param returnTypeName 戻り値の型名
     */
    private void addMethodSignature(String methodName, Map<String, String> parameters, String returnTypeName) {
        StringBuilder signature = new StringBuilder();
        signature.append(getSimpleName(methodName));
        signature.append("(");
        String delimiter = "";
        for (Map.Entry<String, String> parameter : parameters.entrySet()) {
            signature.append(delimiter);
            signature.append(getSimpleName(parameter.getKey()));
            signature.append(" : ");
            signature.append(getSimpleName(parameter.getValue()));
            delimiter = ", ";
        }
        signature.append(")");
        if (returnTypeName != null) {
            signature.append(" : ");
            signature.append(getSimpleName(returnTypeName));
        }
        this.puml.append(signature.toString());
        return;
    }
}
