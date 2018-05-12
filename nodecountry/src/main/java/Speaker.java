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

import java.io.IOException;

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
public class Speaker extends AbstractNodeMain {

	@Override
	public GraphName getDefaultNodeName() {
		return GraphName.of("Sound/Speaker");
	}

	@Override
	public void onStart(ConnectedNode connectedNode) {

		Subscriber<std_msgs.String> speaker_message = connectedNode.newSubscriber("main_speaker_message", std_msgs.String._TYPE);
		final Publisher<std_msgs.Int32> signal = connectedNode.newPublisher("speaker_signal", std_msgs.Int32._TYPE);
		final std_msgs.Int32 send = signal.newMessage();

		speaker_message.addMessageListener(new MessageListener<std_msgs.String>() {

			String[] command = new String[3];
			Runtime runtime = Runtime.getRuntime();

			private int speaker(String inport) {

				command[0] = "/bin/sh";
				command[1] = "-c";
				command[2] = "echo \"" + inport + "\" | festival --tts"; //festival
				//command[2] = "espeak '{" + inport + "}'"; //espeak

				try {
					Process process = runtime.exec(command);
					process.waitFor();
					return 0;
				} catch (IOException ex) {
					ex.printStackTrace();
					return 1;
				} catch (InterruptedException e) {
					e.printStackTrace();
					return 1;
				}

			}

			@Override
			public void onNewMessage(std_msgs.String message) {

				System.out.println(message.getData());
				
				send.setData(speaker(message.getData()));
				signal.publish(send);
				
			}
		});
	}
}
