package application;
	
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;


public class Main extends Application {
	
	Socket socket;
	
	TextArea txtDisplay;
	TextField txtInput;
	Button btnConn, btnSend;
	
	@Override
	public void start(Stage primaryStage) {
		try {
			BorderPane root = new BorderPane();
			Scene scene = new Scene(root,400,400);
			
			txtDisplay = new TextArea();
			txtDisplay.setEditable(false);
			BorderPane.setMargin(txtDisplay, new Insets(0,0,2,0));
			root.setCenter(txtDisplay);
			
			BorderPane bottom = new BorderPane();
			 txtInput = new TextField();
			 txtInput.setPrefSize(60, 30);
			 BorderPane.setMargin(txtInput, new Insets(0,1,1,1));
			 
			 btnConn = new Button();
			 btnConn.setPrefSize(60, 30);
			 btnConn.setText("start");
			 btnConn.setOnAction((e)->{
				 if(btnConn.getText().equals("start")) {
					 startClient();
				 }else {
					 stopClient();
				 }
			 });
			 
			 btnSend = new Button();
			 btnSend.setPrefSize(60, 30);
			 btnSend.setDisable(true);
			 btnSend.setText("send");
			 btnSend.setOnAction(e->{
				 send(txtInput.getText());
			 });
			 
			 bottom.setCenter(txtInput);
			 bottom.setLeft(btnConn);
			 bottom.setRight(btnSend);
			root.setBottom(bottom);
			 
			
			scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
			primaryStage.setScene(scene);
			primaryStage.show();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		launch(args);
	}
	public void displayText(String message) {
		txtDisplay.appendText(message+"\n");
	}
	
	public void startClient() {
		Thread thread = new Thread() {
			@Override
			public void run() {
				try {
					socket = new Socket();
					socket.connect(new InetSocketAddress("localhost",5001));
					Platform.runLater(()->{
						displayText("[연결완료: "+socket.getRemoteSocketAddress()+": "+Thread.currentThread().getName()+"]");
					});
					btnConn.setText("stop");
					btnSend.setDisable(false);
				} catch (Exception e) {
					// TODO: handle exception
					Platform.runLater(()->{
						displayText("[서버 통신 안됨]");
						if(!socket.isClosed())stopClient();
						return;
					});
				}
				receive();
			}
		};
		thread.start();
	}
	
	public void stopClient() {
		try {
			Platform.runLater(()->{
				displayText("[연결 끊음]");
				btnConn.setText("start");
				btnSend.setDisable(true);
			});
			if(socket != null && !socket.isClosed()) {
				socket.close();
			}
		} catch (Exception e) {
		}
	}
	
	public void send(String message) {
		Thread thread = new Thread() {
			@Override
			public void run() {
				try {
					byte[] dataArr = message.getBytes();
					OutputStream os = socket.getOutputStream();
					os.write(dataArr);
					os.flush();
					Platform.runLater(()->{
						displayText("[보내기완료]");
					});
				} catch (Exception e) {
					// TODO: handle exception
					Platform.runLater(()->{
						displayText("[서버 통신 안됨]");
					});
					stopClient();
				}
			}
		};
		thread.start();
	}
	
	public void receive() {
		while(true) {
			try {
				byte[] dataArr = new byte[100];
				InputStream is = socket.getInputStream();
				int readByCount = is.read(dataArr);
				
				String message = new String(dataArr,0,readByCount,"UTF-8");
				
				Platform.runLater(()->{
					displayText("[받기완료: "+message+"]");
				});
			} catch (Exception e) {
				// TODO: handle exception
				Platform.runLater(()->{
					displayText("[서버 통신 안됨]");
				});
				stopClient();
				break;
			}
		}
	}
}
