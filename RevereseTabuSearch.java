import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.Random;
import java.util.Scanner;
import java.util.StringTokenizer;

public class RevereseTabuSearch {
	static List<Integer> elements; //行列の要素格納List
	static List<Integer> reE; //reverse操作の際使用するList
	static List<Integer> reE2; //reverse操作の際使用するList
	static List<Integer> TabuList; //タブーリスト
	static String inFileName; //読み込むファイル名
	static int vertexNum; //読み込んだグラフの頂点数
	static double den; //読み込んだグラフの辺密度
	static int id; //読み込んだグラフのテキストナンバー
	static int MaxValue; //近傍で一番成績のよい値
	static int finalMaxValue; //現在の暫定解
	static int preMaxValue; //前の評価値
	static int prePreMaxValue; //前の評価値
	static List<Integer> vertex; //頂点のリスト
	static Queue<Integer> Tabu = new ArrayDeque<>(); //タブーリスト
	static int bestY; //挿入する頂点
	static int bestX; //挿入される頂点

	//	タブーサーチメインクラス
	public static void main(String[] args) {
		Scanner sc = new Scanner(System.in);
		System.out.print("頂点数:");
		int i = sc.nextInt();
		System.out.print("辺密度:");
		double i2 = sc.nextDouble();
		System.out.print("テキストナンバー:");
		int i3 = sc.nextInt();

		vertexNum = i;
		den = i2;
		id = i3;
		//inFileName = "C:\\Users\\Ryo Tanaka\\Desktop\\digraphs\\test.txt";		//テスト用ファイル
		//inFileName = String.format("C:\\Users\\Ryo Tanaka\\Desktop\\digraphs\\n%d_d%02d_%d.txt", i, (int) (10 * i2), i3);		//digraphGeneraterファイル
		//inFileName = "C:\\Users\\Ryo Tanaka\\Documents\\BenchMark\\SGB\\N-sgb75.01"; //SGB75ベンチマーク問題ファイル
		inFileName = "C:\\Users\\Ryo Tanaka\\Documents\\BenchMark\\IO\\N-be75np"; //IOベンチマーク問題ファイル

		run();

	}

	public static void run() { //実行メソッド
		fileRead(inFileName);
		neighborhood();
	}

	public static void fileRead(String filename) { //ファイル読み込みメソッド
		elements = new ArrayList<>();
		vertex = new ArrayList<>();

		try {
			//fileのパスを指定
			File file = new File(filename);

			if (!file.exists()) {
				System.out.print("ファイルが存在しません");
				return;
			}

			FileReader fileReader = new FileReader(file);
			BufferedReader bufferedReader = new BufferedReader(fileReader);
			String data;
			StringTokenizer token;
			while ((data = bufferedReader.readLine()) != null) {
				token = new StringTokenizer(data, " ");

				while (token.hasMoreTokens()) {
					elements.add(Integer.parseInt(token.nextToken()));
				}
			}


			fileReader.close();

			MaxValue = valueCalculation(elements);
			preMaxValue = MaxValue;

			for (int i = 1; i < vertexNum + 1; i++) //頂点集合
				vertex.add(i);

		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	//評価値計算メソッド
	public static int valueCalculation(List<Integer> element) {
		int value = 0;
		for (int j = 0; j < vertexNum-1; j++) { //正方行列対角成分上の合計値=評価値
			for (int i = (j * vertexNum) + j + 2; i < ((j + 1) * vertexNum) + 1; i++) {
				if (element.get(i) != 0) { //0でない成分の抽出
					value += element.get(i);
				}
			}
		}

		return value;
	}



	//挿入に際して更新される評価値を求めるメソッド(i = 挿入場所, j = 挿入するもの)
	public static int newValueCalculation(int i, int j, int value, List<Integer> element) {
		int newValue = value; //更新後の評価値の初期値は更新前の評価値

		//挿入場所<挿入するものの場所
		if (i < j) {
			for (int x = i; x < j; x++)
				newValue += element.get(((j - 1) * vertexNum) + x) - element.get(((x - 1) * vertexNum) + j); //評価関数値更新式
		}

		//挿入場所>挿入するものの場所
		if (i > j) {
			for (int x = j + 1; x < i + 1; x++)
				newValue += element.get((x - 1) * vertexNum + j) - element.get((j - 1) * vertexNum + x); //評価関数値更新式
		}

		return newValue;
	}



	//挿入操作メソッド(x=挿入場所,y=挿入するもの,element = insertする前の初期順序)
	public static void insertMove(int x, int y, List<Integer> element) {
		int[] b = new int[vertexNum];
		int[] c = new int[vertexNum];

		for (int i = 0; i < vertexNum; i++) { //b,cの初期化
			b[i] = 0;
			c[i] = 0;
		}

		//x<yの時の処理
		if (x < y) {

			vertex.add(x - 1, vertex.get(y - 1));
			vertex.remove(y);

			for (int j = 0; j < vertexNum; j++) { //挿入する列を行列に格納
				b[j] = element.get(j * vertexNum + y);
			}

			for (int u = y; u > x; u--) {
				for (int k = 0; k < vertexNum; k++) {
					element.set(k * vertexNum + u, element.get(k * vertexNum + (u - 1)));
				}
			}

			for (int l = 0; l < vertexNum; l++) {
				element.set(l * vertexNum + x, b[l]);
			}

			for (int j = 0; j < vertexNum; j++) { //挿入する行を行列に格納
				c[j] = element.get((y - 1) * vertexNum + j + 1);
			}

			for (int u = y; u > x; u--) {
				for (int k = 0; k < vertexNum; k++) {
					element.set((u - 1) * vertexNum + (k + 1), element.get((u - 2) * vertexNum + (k + 1)));
				}
			}

			for (int n = 0; n < vertexNum; n++) {
				element.set((x - 1) * vertexNum + n + 1, c[n]);
			}
		}

		//x>yの時の処理
		if (x > y) {

			vertex.add(x, vertex.get(y - 1));
			vertex.remove(y - 1);

			for (int j = 0; j < vertexNum; j++) { //挿入する列を行列に格納
				b[j] = element.get(j * vertexNum + y);
			}

			for (int u = y; u < x; u++) {
				for (int k = 0; k < vertexNum; k++) {
					element.set(k * vertexNum + u, element.get(k * vertexNum + (u + 1)));
				}
			}

			for (int l = 0; l < vertexNum; l++) {
				element.set(l * vertexNum + x, b[l]);
			}

			for (int j = 0; j < vertexNum; j++) { //挿入する行を行列に格納
				c[j] = element.get((y - 1) * vertexNum + j + 1);
			}

			for (int u = y; u < x; u++) {
				for (int k = 0; k < vertexNum; k++) {
					element.set((u - 1) * vertexNum + (k + 1), element.get(u * vertexNum + (k + 1)));
				}
			}

			for (int n = 0; n < vertexNum; n++) {
				element.set((x - 1) * vertexNum + n + 1, c[n]);
			}
		}


		valueCalculation(element);
	}

	//局所探索を行うメソッド
	public static void selectVertex() {
		MaxValue = -1;
		int nV;

		//すべての頂点に対して評価値を評価
		for (int y = 1; y < vertexNum + 1; y++) {
			if (Tabu.contains(vertex.get(y - 1))) {
				continue;
			}
			for (int x = 1; x < vertexNum + 1; x++) {
				nV = newValueCalculation(x, y, preMaxValue, elements);
				if (x != y && MaxValue < nV) {
					MaxValue = nV;
					bestY = y;
					bestX = x; //全ての評価値を計算して最大の評価値とそのときの挿入位置、挿入場所を記憶
				}
			}

		}
	}

	//Reverse操作
	public static void ReverseVertex() {

		int[] reV = new int[vertexNum];
		int[] reE = new int[vertexNum*vertexNum];

		//vertexコレクションのreverse
		for (int i = 0; i < vertexNum; i++) {
			reV[i] = vertex.get(i);
		}

		for (int j = 0; j < vertexNum; j++) {
			vertex.set(j, reV[vertexNum - j-1]);
		}


		//elementsコレクションのReverse
		for(int m =0;m<vertexNum*vertexNum;m++){
			reE[m] = elements.get(m+1);
		}

		//elementsコレクションのReverese
		for(int n=0;n<vertexNum*vertexNum;n++){
			elements.set(n+1, reE[vertexNum*vertexNum - n -1]);
		}



	}

	//近傍操作メソッド
	public static void neighborhood() {
		int TL = 4; //タブーリストの長さ
		int loop = 1000000; //タブーサーチを回す回数
		Random r = new Random();
		int RevereseFre = 4;
		boolean isReverse;

		for (int z = 0; z < loop; z++) {

			isReverse = false ;

			int randomNumber = r.nextInt(100);




			if (randomNumber < RevereseFre) {
				//System.out.println("反転");
				ReverseVertex();
				isReverse = true;
			}



			selectVertex();

			if(isReverse) {
				MaxValue = valueCalculation(elements);
			}

			if (finalMaxValue < MaxValue) {
				finalMaxValue = MaxValue;
			}


			//過去一回分の暫定解を保持
			preMaxValue = MaxValue;


			//タブー長TL分だけタブーリスト追加
			Tabu.offer(vertex.get(bestY - 1));
			if (Tabu.size() == TL + 1)
				Tabu.poll();

			/*
			System.out.println("bestX:"+bestX);
			System.out.println("bestY:"+bestY);
			System.out.println("MaxValue:"+MaxValue);
			System.out.println(elements.get(0));
			for (int i = 0; i < vertexNum; i++) {
				for (int j = 0; j < vertexNum; j++) {
					if (j != vertexNum - 1)
						System.out.print(elements.get(i * vertexNum + j + 1) + " ");
					else
						System.out.println(elements.get(i * vertexNum + j + 1));
				}
			}

			System.out.println("insert実行");

            */
			insertMove(bestX, bestY, elements);


			/*
			for (int i = 0; i < vertexNum; i++) {
				for (int j = 0; j < vertexNum; j++) {
					if (j != vertexNum - 1)
						System.out.print(elements.get(i * vertexNum + j + 1) + " ");
					else
						System.out.println(elements.get(i * vertexNum + j + 1));
				}
			}

			System.out.println("---------------------------------------------------");
            */

			}




		//最終結果
		for (int i = 0; i < vertexNum; i++) {
			System.out.println("頂点集合:" + vertex.get(i));
		}

		System.out.println("タブーリスト:" + Tabu);
		System.out.println("解:" + finalMaxValue);
		System.out.println("------------------------------------------------");

	}
}