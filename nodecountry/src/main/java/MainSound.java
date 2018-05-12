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
public class MainSound extends AbstractNodeMain {

	int lockcount = 0;
	boolean bufflag = false; 
	boolean action_flag = false;

	private void string_send(String message, Publisher<std_msgs.String> publish_message) { //

		if(!action_flag) {
			std_msgs.String send_message = publish_message.newMessage();
			send_message.setData(message);
			publish_message.publish(send_message);
			action_flag = true;
		}

	}

	private void recognition_control_send(Boolean flag, Publisher<std_msgs.Bool> publish_message) { //

		std_msgs.Bool send_message = publish_message.newMessage();
		send_message.setData(flag);
		publish_message.publish(send_message);
		action_flag = flag;
		System.out.println(flag);

	}

	private void waiting() {

		while (action_flag) {
			try {
				Thread.sleep(10); 
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

	}

	@Override
	public GraphName getDefaultNodeName() {
		return GraphName.of("Sound/MainNode");
	}

	@Override
	public void onStart(ConnectedNode connectedNode) {
		
		/*Runtime runtime = Runtime.getRuntime();
		try {
			Process p = runtime.exec("pwd");
			BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
			System.out.println("////////////////////////////////\n" + br.readLine() + "\n////////////////////////////////\n");
		} catch (IOException e) {
			e.printStackTrace();
		}//*/

		Subscriber<std_msgs.String> recognition_control = connectedNode.newSubscriber("recognition", std_msgs.String._TYPE); //音声認識開始/終了
		Subscriber<std_msgs.String> speaker_message = connectedNode.newSubscriber("speaker", std_msgs.String._TYPE); //発音
		Subscriber<std_msgs.String> beep_message = connectedNode.newSubscriber("beep", std_msgs.String._TYPE); //ビープ音
		Subscriber<std_msgs.Int32> speaker_signal = connectedNode.newSubscriber("speaker_signal", std_msgs.Int32._TYPE); //発音終了信号
		Subscriber<std_msgs.Int32> beep_signal = connectedNode.newSubscriber("beep_signal", std_msgs.Int32._TYPE); //ビープ音終了信号

		final Publisher<std_msgs.Bool> main_recognition_control = connectedNode.newPublisher("main_recognition_control", std_msgs.Bool._TYPE); //音声認識開始/終了受け渡し
		final Publisher<std_msgs.String> main_speaker_message = connectedNode.newPublisher("main_speaker_message", std_msgs.String._TYPE); //発音メッセージ受け渡し
		final Publisher<std_msgs.String> main_beep_message = connectedNode.newPublisher("main_beep_message", std_msgs.String._TYPE); //ビープ音メッセージ受け渡し

		recognition_control.addMessageListener(new MessageListener<std_msgs.String>() { //音声認識開始・終了

			@Override
			public void onNewMessage(std_msgs.String message) {

				System.out.println("音声認識");

				switch (message.getData()) {
				case "start":
					System.out.println("start");
					string_send("RecognitionStart", main_beep_message);
					waiting(); //待機
					recognition_control_send(true, main_recognition_control);

					break;

				case "stop":
					System.out.println("stop");
					recognition_control_send(false, main_recognition_control);
					string_send("RecognitionStop", main_beep_message);

					break;

				default:
					recognition_control_send(false, main_recognition_control);
					string_send("RecognitionErrer", main_beep_message);
					break;
				}

			}
		});

		speaker_message.addMessageListener(new MessageListener<std_msgs.String>() { //発音メッセージ受け渡し

			@Override
			public void onNewMessage(std_msgs.String message) {

				string_send(message.getData(), main_speaker_message);

			}
		});

		beep_message.addMessageListener(new MessageListener<std_msgs.String>() { //ビープ音メッセージ受け渡し

			@Override
			public void onNewMessage(std_msgs.String message) {

				string_send(message.getData(), main_beep_message);

			}
		});

		speaker_signal.addMessageListener(new MessageListener<std_msgs.Int32>() { //発音シグナル受信

			@Override
			public void onNewMessage(std_msgs.Int32 message) {
				System.out.println("発音シグナル" + message.getData());
				action_flag = false;

			}
		});

		beep_signal.addMessageListener(new MessageListener<std_msgs.Int32>() { //ビープ音シグナル受信

			@Override
			public void onNewMessage(std_msgs.Int32 message) {
				System.out.println("ビープシグナル" + message.getData());
				action_flag = false;

			}
		});

	}
}
