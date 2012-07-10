package jp.ramensroom.whereaboutsserver;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import org.eclipse.jetty.websocket.WebSocket;

// WebSocket.OnTextMessage を implements
public class MyWebSocket implements WebSocket.OnTextMessage {

	private int myID; // 接続ID
	private Connection connection;

	// 全てのWebSocket接続が収められた Set
	static Set<MyWebSocket> wsConnections = new CopyOnWriteArraySet<MyWebSocket>();

	public MyWebSocket(int id) {
		myID = id;
	}

	// WebSocket 接続開始時に呼ばれるメソッド
	@Override
	public void onOpen(Connection conn) {
		connection = conn;
		connection.setMaxIdleTime(Integer.MAX_VALUE);

		System.out.println("Connection Added:" + myID);
		System.out.println(" - MaxIdleTime:" + connection.getMaxIdleTime());
		System.out.println(" - MaxTextMessageSize:" + connection.getMaxTextMessageSize());

		synchronized (connection) {
			// Setに自身を登録
			wsConnections.add(this);
		}
	}

	// WebSocket 接続にてクライアントからのデータを受信した際に呼ばれるメソッド
	@Override
	public void onMessage(String msg) {
		// すべてのWebSocketなコネクションにメッセージを投げる
		System.out.println(myID + ": Message -> " + msg);
		for (MyWebSocket ws : wsConnections) {
			try {
				if (myID != ws.myID) {
					System.out.println("Get Message:" + msg);
					ws.connection.sendMessage(msg);
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	// WebSocket 接続終了時に呼ばれるメソッド
	@Override
	public void onClose(int parameter, String msg) {
		System.out.println(parameter + " disconnect. -> " + msg);
		synchronized (connection) {
			// Setから自身を削除
			wsConnections.remove(this);
		}
	}	
}