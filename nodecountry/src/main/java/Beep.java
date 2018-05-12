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
public class Beep extends AbstractNodeMain {
	
	String FILE = "../../../../src/main/java/sound/";

	@Override
	public GraphName getDefaultNodeName() {
		return GraphName.of("Sound/Beep");
	}

	@Override
	public void onStart(ConnectedNode connectedNode) {

		Subscriber<std_msgs.String> beep_message = connectedNode.newSubscriber("main_beep_message", std_msgs.String._TYPE);
		final Publisher<std_msgs.Int32> signal = connectedNode.newPublisher("beep_signal", std_msgs.Int32._TYPE);
		final std_msgs.Int32 send = signal.newMessage();
		final SoundPlayer sound = new SoundPlayer();

		beep_message.addMessageListener(new MessageListener<std_msgs.String>() {

			private int beep(std_msgs.String string) {

				switch (string.getData()) {
				case "RecognitionStart": //認識開始
					System.out.println("認識開始");
					sound.play(FILE + "RecognitionStart.wav");
					return 0;

				case "RecognitionStop": //認識成功
					System.out.println("認識終了");
					sound.play(FILE + "RecognitionStop.wav");
					return 0;

				case "RecognitionErrer": //認識失敗
					System.out.println("認識失敗");
					sound.play(FILE + "RecognitionErrer.wav");
					return 0;
					
				case "SystemStart": //システム起動
					System.out.println("システム起動");
					sound.play(FILE + "SystemStart.wav");
					return 0;
					
				case "SystemStop": //システム終了
					System.out.println("システム終了");
					sound.play(FILE + "SystemStop.wav");
					return 0;

				}
				
				return 1;

			}

			@Override
			public void onNewMessage(std_msgs.String message) {

				send.setData(beep(message)); //ビープ音
				signal.publish(send);

			}
		});
	}
}
