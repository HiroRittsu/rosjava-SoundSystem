import java.io.File;
import java.io.IOException;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineListener;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

public class SoundPlayer implements LineListener{
	
	private static boolean flag = true;
	
	public void play(String filename) {
		
		try {
			
			flag = true;
			
			AudioInputStream ais = AudioSystem.getAudioInputStream(new File(filename));
			
			//ファイルの形式取得
			AudioFormat af = ais.getFormat();
			
			//単一のオーディオ形式を含む指定した情報からデータラインの情報オブジェクトを構築
			DataLine.Info dataLine = new DataLine.Info(Clip.class,af);

			//指定された Line.Info オブジェクトの記述に一致するラインを取得
			Clip c = (Clip)AudioSystem.getLine(dataLine);
			
			//再生準備完了
			c.open(ais);
			
			//監視
			c.addLineListener(this);
			
			//開始
			c.start();
			
			//待機
			while(flag) Thread.sleep(1);
			
		} catch (UnsupportedAudioFileException | IOException e) {
			e.printStackTrace();
		} catch (LineUnavailableException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void update(LineEvent event) {
		
		if (event.getType() == LineEvent.Type.STOP) {
			Clip clip = (Clip) event.getSource();
			clip.stop();
			clip.setFramePosition(0); // 再生位置を最初に戻す
			flag = false;
		}
	}

}