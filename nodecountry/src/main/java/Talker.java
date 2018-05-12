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
import java.util.Scanner;

import org.ros.concurrent.CancellableLoop;
import org.ros.namespace.GraphName;
import org.ros.node.AbstractNodeMain;
import org.ros.node.ConnectedNode;
import org.ros.node.NodeMain;
import org.ros.node.topic.Publisher;

/**
 * A simple {@link Publisher} {@link NodeMain}.
 */
public class Talker extends AbstractNodeMain {

	@Override
	public GraphName getDefaultNodeName() {
		return GraphName.of("rosjava/Talker");
	}

	@Override
	public void onStart(final ConnectedNode connectedNode) {

		final Publisher<std_msgs.String> beep = connectedNode.newPublisher("beep", std_msgs.String._TYPE);
		final Publisher<std_msgs.String> speaker = connectedNode.newPublisher("speaker", std_msgs.String._TYPE);
		final Publisher<std_msgs.String> recognition = connectedNode.newPublisher("recognition", std_msgs.String._TYPE);
		// This CancellableLoop will be canceled automatically when the node shuts
		// down.
		connectedNode.executeCancellableLoop(new CancellableLoop() {

			final Scanner scan = new Scanner(System.in);
			private String input = null;

			@Override
			protected void setup() {

			}

			@Override
			protected void loop() throws InterruptedException {

				switch (scan.nextLine()) {
				case "beep":
					std_msgs.String beep_send = beep.newMessage();
					input = scan.nextLine();
					beep_send.setData(input);
					beep.publish(beep_send);
					break;

				case "speak":
					std_msgs.String speak_send = speaker.newMessage();
					input = scan.nextLine();
					speak_send.setData(input);
					speaker.publish(speak_send);
					break;

				default:
					std_msgs.String recognition_send = recognition.newMessage();
					recognition_send.setData("start");
					recognition.publish(recognition_send);

					Thread.sleep(10000);

					recognition_send.setData("stop");
					recognition.publish(recognition_send);
					break;
				}

			}
		});
	}
}
