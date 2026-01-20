package nexusviz.generator.converter;

import java.nio.file.Path;
import java.util.Set;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.EnumDeclaration;
import com.github.javaparser.ast.body.RecordDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;

import nexusviz.generator.model.structure.TypeInfo;
import nexusviz.generator.model.structure.TypeRelationInfo;
import nexusviz.generator.renderer.StructuralRenderer;

/**
 * 解析結果から PlantUML への変換の指示を出す。
 * 
 * @author Ishiguro
 * @version 1.0
 */
public class StructuralConverter extends Object {
	/**
	 * クラス図用のPlantUMLを書き込むためのクラス。
	 */
	private StructuralRenderer structuralRenderer;

	/**
	 * インタフェースであることを示す定数。
	 */
	private static final Boolean INTERFACE = true;

	/**
	 * インタフェースでないことを示す定数。
	 */
	private static final Boolean NOT_INTERFACE = false;

	/**
	 * インスタンスを生成して応答するコンストラクタ。
	 */
	public StructuralConverter() {
		this.structuralRenderer = new StructuralRenderer();
		return;
	}

	/**
	 * クラス図用のPlantUMLのコードを生成し、画像ファイルとして出力する。
	 * 
	 * @param projectPath   プロジェクトのパス
	 * @param typeInfos     型情報の集合
	 * @param typeRelations 関係情報の集合
	 * @return 生成した画像ファイルのパス文字列
	 */
	public String executeConversion(Path projectPath, Set<TypeInfo> typeInfos,
			Set<TypeRelationInfo> typeRelations) {
		try {
			// 型情報をPlantUMLに変換する
			typeInfos.forEach(typeInfo -> {
				TypeDeclaration<?> aType = typeInfo.typeDeclaration();
				if (aType != null) { // 型定義情報が存在する場合
					if (aType instanceof ClassOrInterfaceDeclaration aClassOrInterface) { // クラスもしくはインタフェース情報の場合
						if (aClassOrInterface.isInterface()) {
							convertInterface(aClassOrInterface, typeInfo.fullyQualifiedScope(), typeInfo.typeName(),
									typeInfo.isLocalType());
						} else {
							convertClass(aClassOrInterface, typeInfo.fullyQualifiedScope(), typeInfo.typeName(),
									typeInfo.isLocalType());
						}
					} else if (aType instanceof EnumDeclaration anEnum) { // 列挙型情報の場合
						convertEnum(anEnum, typeInfo.fullyQualifiedScope(), typeInfo.typeName(),
								typeInfo.isLocalType());
					} else if (aType instanceof RecordDeclaration aRecord) { // レコード情報の場合
						convertRecord(aRecord, typeInfo.fullyQualifiedScope(), typeInfo.typeName(),
								typeInfo.isLocalType());
					}
				} else { // 型定義情報が存在しない場合
					if (typeInfo.isInterface()) {
						// インタフェース情報のみ
						this.structuralRenderer.addEmptyInterface(typeInfo.fullyQualifiedScope(), typeInfo.typeName());
					} else {
						// クラス情報のみ
						this.structuralRenderer.addEmptyClass(typeInfo.fullyQualifiedScope(), typeInfo.typeName());
					}
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		}

		// 関係情報をPlantUMLに変換する
		typeRelations.forEach(aRelation -> {
			convertTypeRelation(aRelation);
		});

		// PlantUMLのコードをファイルに出力し、画像ファイルを生成する
		return this.structuralRenderer.render(projectPath);
	}

	/**
	 * クラス情報をPlantUMLに変換する。
	 * 
	 * @param aClass              クラス情報
	 * @param fullyQualifiedScope 完全修飾スコープ名
	 * @param className           クラス名
	 * @param isLocalType         ローカル型であるかどうか
	 */
	private void convertClass(ClassOrInterfaceDeclaration aClass, String fullyQualifiedScope, String className,
			Boolean isLocalType) {
		// クラス定義の記述を始める
		this.structuralRenderer.addClassDefinition(aClass, fullyQualifiedScope, className, isLocalType);

		// フィールド情報をPlantUMLに変換する
		aClass.getFields().forEach(field -> {
			this.structuralRenderer.addField(field, NOT_INTERFACE);
		});

		// コンストラクタ情報をPlantUMLに変換する
		aClass.getConstructors().forEach(constructor -> {
			this.structuralRenderer.addConstructor(constructor, aClass);
		});

		// メソッド情報をPlantUMLに変換する
		aClass.getMethods().forEach(method -> {
			this.structuralRenderer.addMethod(method, NOT_INTERFACE);
		});

		// クラス定義の記述を終える
		this.structuralRenderer.closeClassDefinition();
		return;
	}

	/**
	 * 列挙型情報をPlantUMLに変換する。
	 * 
	 * @param anEnum              列挙型情報
	 * @param fullyQualifiedScope 完全修飾スコープ名
	 * @param enumName            列挙型名
	 * @param isLocalType         ローカル型であるかどうか
	 */
	private void convertEnum(EnumDeclaration anEnum, String fullyQualifiedScope, String enumName, Boolean isLocalType) {
		// 列挙型定義の記述を始める
		this.structuralRenderer.addEnumDefinition(anEnum, fullyQualifiedScope, enumName, isLocalType);

		// 列挙型メンバをPlantUMLに変換する
		anEnum.getEntries().forEach(member -> {
			this.structuralRenderer.addEnumMember(anEnum.getNameAsString(), member);
		});

		// フィールド情報をPlantUMLに変換する
		anEnum.getFields().forEach(field -> {
			this.structuralRenderer.addField(field, NOT_INTERFACE);
		});

		// コンストラクタ情報をPlantUMLに変換する
		anEnum.getConstructors().forEach(constructor -> {
			this.structuralRenderer.addConstructor(constructor, anEnum);
		});

		// メソッド情報をPlantUMLに変換する
		anEnum.getMethods().forEach(method -> {
			this.structuralRenderer.addMethod(method, NOT_INTERFACE);
		});

		// 列挙型定義の記述を終える
		this.structuralRenderer.closeEnumDefinition();
		return;
	}

	/**
	 * インタフェース情報をPlantUMLに変換する。
	 * 
	 * @param anInterface         インタフェース情報
	 * @param fullyQualifiedScope 完全修飾スコープ名
	 * @param interfaceName       インタフェース名
	 * @param isLocalType         ローカル型であるかどうか
	 */
	private void convertInterface(ClassOrInterfaceDeclaration anInterface, String fullyQualifiedScope,
			String interfaceName, Boolean isLocalType) {
		// インタフェース定義の記述を始める
		this.structuralRenderer.addInterfaceDefinition(anInterface, fullyQualifiedScope, interfaceName, isLocalType);

		// フィールド情報をPlantUMLに変換する
		anInterface.getFields().forEach(field -> {
			this.structuralRenderer.addField(field, INTERFACE);
		});

		// メソッド情報をPlantUMLに変換する
		anInterface.getMethods().forEach(method -> {
			this.structuralRenderer.addMethod(method, INTERFACE);
		});

		// インタフェース定義の記述を終える
		this.structuralRenderer.closeInterfaceDefinition();
		return;
	}

	/**
	 * レコード情報をPlantUMLに変換する。
	 * 
	 * @param aRecord             レコード情報
	 * @param fullyQualifiedScope 完全修飾スコープ名
	 * @param recordName          レコード名
	 * @param isLocalType         ローカル型であるかどうか
	 */
	private void convertRecord(RecordDeclaration aRecord, String fullyQualifiedScope, String recordName,
			Boolean isLocalType) {
		// レコード定義の記述を始める
		this.structuralRenderer.addRecordDefinition(aRecord, fullyQualifiedScope, recordName, isLocalType);

		// レコードパラメータをPlantUMLに変換する
		aRecord.getParameters().forEach(param -> {
			this.structuralRenderer.addRecordParameter(param.getType().asString().replaceAll(",",
					", "), param.getNameAsString());
		});

		// フィールド情報をPlantUMLに変換する
		aRecord.getFields().forEach(field -> {
			this.structuralRenderer.addField(field, NOT_INTERFACE);
		});

		/**
		 * コンパクトコンストラクタ情報をPlantUMLに変換する
		 */
		aRecord.getCompactConstructors().forEach(compactConstructor -> {
			this.structuralRenderer.addCompactConstructor(compactConstructor, aRecord);
		});

		// コンストラクタ情報をPlantUMLに変換する
		aRecord.getConstructors().forEach(constructor -> {
			this.structuralRenderer.addConstructor(constructor, aRecord);
		});

		// メソッド情報をPlantUMLに変換する
		aRecord.getMethods().forEach(method -> {
			this.structuralRenderer.addMethod(method, NOT_INTERFACE);
		});

		// レコード定義の記述を終える
		this.structuralRenderer.closeRecordDefinition();
		return;
	}

	/**
	 * 関係情報をPlantUMLに変換する。
	 * 
	 * @param aRelation 関係情報
	 */
	private void convertTypeRelation(TypeRelationInfo aRelation) {
		switch (aRelation.relationType()) {
			case INHERITANCE -> { // 継承
				this.structuralRenderer.addInheritance(aRelation.fromType(), aRelation.fromTypeFullyQualifiedScope(),
						aRelation.toType(), aRelation.toTypeFullyQualifiedScope());
			}
			case IMPLEMENTATION -> { // 実装
				this.structuralRenderer.addImplementation(aRelation.fromType(), aRelation.fromTypeFullyQualifiedScope(),
						aRelation.toType(), aRelation.toTypeFullyQualifiedScope());
			}
			case UNIDIRECTIONAL_ASSOCIATION -> { // 片方向関連
				this.structuralRenderer.addUnidirectionalAssociation(aRelation.fromType(),
						aRelation.fromTypeFullyQualifiedScope(), aRelation.toType(),
						aRelation.toTypeFullyQualifiedScope());
			}
			case BIDIRECTIONAL_ASSOCIATION -> { // 双方向関連
				this.structuralRenderer.addBidirectionalAssociation(aRelation.fromType(),
						aRelation.fromTypeFullyQualifiedScope(), aRelation.toType(),
						aRelation.toTypeFullyQualifiedScope());
			}
			case MULTIPLICITY_UNIDIRECTIONAL_ASSOCIATION -> { // 多重片方向関連
				this.structuralRenderer.addMultiplicityUnidirectionalAssociation(aRelation.fromType(),
						aRelation.fromTypeFullyQualifiedScope(), aRelation.toType(),
						aRelation.toTypeFullyQualifiedScope());
			}
			case AGGREGATION -> { // 集約
				this.structuralRenderer.addAggregation(aRelation.fromType(), aRelation.fromTypeFullyQualifiedScope(),
						aRelation.toType(), aRelation.toTypeFullyQualifiedScope());
			}
			case COMPOSITION -> { // 合成
				this.structuralRenderer.addComposition(aRelation.fromType(), aRelation.fromTypeFullyQualifiedScope(),
						aRelation.toType(), aRelation.toTypeFullyQualifiedScope());
			}
			case CONTAINMENT -> { // 包含
				this.structuralRenderer.addContainment(aRelation.fromType(), aRelation.fromTypeFullyQualifiedScope(),
						aRelation.toType(), aRelation.toTypeFullyQualifiedScope());
			}
			case null -> { // 型情報のみ
				this.structuralRenderer.addEmptyClass(aRelation.toTypeFullyQualifiedScope(),
						aRelation.toType().asString());
			}
			default -> {
				// その他の関係は未対応
			}
		}
		return;
	}
}
