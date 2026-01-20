package nexusviz;

import java.awt.Dimension;
import java.awt.Toolkit;

import javax.swing.JFrame;
import javax.swing.JScrollPane;

import nexusviz.mvc.VisualizeController;
import nexusviz.mvc.VisualizeModel;
import nexusviz.mvc.VisualizeView;

/**
 * NexusVisualizerアプリケーションのエントリポイントを表すクラス。
 * 
 * @author Ishiguro
 * @version 1.0
 */
public class Main extends Object {
	/**
	 * インスタンス化を防止するためのコンストラクタ。
	 */
	private Main() {
	}

	/**
	 * NexusVisualizerアプリケーションを起動する。
	 * 
	 * @param arguments 引数の文字列の配列
	 */
	public static void main(String[] arguments) {
		// ウィンドウのサイズを決め、モデルを作る。
		Dimension aDimension = new Dimension(1200, 900);
		VisualizeModel aModel = new VisualizeModel();
		VisualizeController aController = new VisualizeController();

		// 上記のモデルのビューとコントローラのペアを作り、ウィンドウに乗せる。
		VisualizeView aView = new VisualizeView(aModel, aController);
		JFrame aWindow = new JFrame("Nexus Visualizer");
		JScrollPane scrollPane = new JScrollPane(aView);
		scrollPane.getVerticalScrollBar().setUnitIncrement(10);
		scrollPane.getHorizontalScrollBar().setUnitIncrement(10);
		aWindow.getContentPane().add(scrollPane);

		// 高さはタイトルバーの高さを考慮してウィンドウの大きさを決定する。
		aWindow.addNotify();
		Integer titleBarHeight = aWindow.getInsets().top;
		Integer width = aDimension.width;
		Integer height = aDimension.height + titleBarHeight;
		aWindow.setSize(width, height);

		// 最初のウィンドウの出現位置を計算する。
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		Integer x = (screenSize.width / 2) - (width / 2);
		Integer y = (screenSize.height / 2) - (height / 2);

		// ウィンドウに各種設定を行って出現させる。
		aWindow.setMinimumSize(new Dimension(400, 300 + titleBarHeight));
		aWindow.setResizable(true);
		aWindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		aWindow.setLocation(x, y);
		aWindow.setVisible(true);
		aWindow.toFront();
		return;
	}
}
