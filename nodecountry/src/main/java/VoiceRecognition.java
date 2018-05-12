/*
 * Copyright (C) 2014 migly.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import org.ros.concurrent.CancellableLoop;
import org.ros.message.MessageListener;
import org.ros.namespace.GraphName;
import org.ros.node.AbstractNodeMain;
import org.ros.node.ConnectedNode;
import org.ros.node.NodeMain;
import org.ros.node.topic.Publisher;
import org.ros.node.topic.Subscriber;

/**
 * A simple {@link Subscriber} {@link NodeMain}.
 */
public class VoiceRecognition extends AbstractNodeMain {

	static Socket cSocket = null;
	static PrintWriter writer = null;
	static BufferedReader reader = null;
	static String result = null;

	private static void connection() {

		while (true) {

			try {
				cSocket = new Socket("localhost", 10500);
				//クライアント側からサーバへの送信用
				writer = new PrintWriter(cSocket.getOutputStream(),true);
				//サーバ側からの受取用
				reader = new BufferedReader(new InputStreamReader(cSocket.getInputStream()));

				writer.println("PAUSE"); //停止

				break;

			} catch (IOException e) {
				System.out.print('.');
			}

		}

	}

	@Override
	public GraphName getDefaultNodeName() {
		return GraphName.of("Sound/Recognition");
	}

	@Override
	public void onStart(ConnectedNode connectedNode) {

		Subscriber<std_msgs.Bool> recognition_control = connectedNode.newSubscriber("main_recognition_control", std_msgs.Bool._TYPE);
		final Publisher<std_msgs.String> recognition_result_message = connectedNode.newPublisher("RecognitionResult", std_msgs.String._TYPE);

		connection(); //socket通信準備(つながるまで)

		//制御
		recognition_control.addMessageListener(new MessageListener<std_msgs.Bool>() {

			@Override
			public void onNewMessage(std_msgs.Bool message) {

				System.out.println(message.getData());

				if(message.getData() == true) writer.println("RESUME");

				if(message.getData() == false) writer.println("PAUSE");

			}
		});

		//認識結果
		connectedNode.executeCancellableLoop(new CancellableLoop() {

			std_msgs.String send = recognition_result_message.newMessage();

			@Override
			protected void setup() {

				Thread thread = new Thread(new Runnable() {

					String input = null;
					int start;
					int end;

					@Override
					public void run() {

						while (true) {

							try {

								if((input = reader.readLine()) != null) {

									input = input.trim();
									System.out.println(input);

									if(input.contains("WHYPO WORD")) {
										start = input.indexOf("WHYPO WORD") + 12;
										end = input.indexOf("CLASSID") - 2;
										result = input.substring(start, end);
										System.out.println("結果" + result);
									}

								}

							} catch (IOException e) {
								System.out.println("読み取れませんでした。");
							}
						}

					}
				});

				thread.start(); //スレッドを立てる

				while (true) {

					if(result != null) {
						send.setData(result);
						recognition_result_message.publish(send);
						result = null;
					}
					
					try {
						Thread.sleep(10);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}

				}			

			}

			@Override
			protected void loop() throws InterruptedException {

			}

		});
	}
}
