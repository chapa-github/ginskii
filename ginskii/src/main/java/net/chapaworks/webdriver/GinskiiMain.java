package net.chapaworks.webdriver;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxProfile;

/**
 * テキストのURLをクロールし、画面ショットを取得する
 * @author chapa
 *
 */
public class GinskiiMain {

	/** URLチェック用 */
	private static final String[] URL_PREFIX = { "http://", "https://" };

	/** クロール用ディレクトリ */
	private static final String INPUT_DIR = "input/";

	/** クロール後のスクリーンショット保存用ディレクトリ(PC用) */
	private static final String OUTPUT_PC_DIR = "output/pc/";

	/** クロール後のスクリーンショット保存用ディレクトリ(SP用) */
	private static final String OUTPUT_SP_DIR = "output/sp/";

	/** twitterURL for PC */
	private static final String TWITTER_PC = "https://twitter.com";

	/** twitterURL for SP */
	private static final String TWITTER_SP = "https://mobile.twitter.com";

	/** userAgent(iPhone) */
	private static final String USER_AGENT = "Mozilla/5.0 (iPhone; CPU iPhone OS 8_0_2 like Mac OS X) AppleWebKit/600.1.4 (KHTML, like Gecko) Version/8.0 Mobile/12A405 Safari/600.1.4";

	/**
	 * mainメソッド
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		GinskiiMain ginskii = new GinskiiMain();

		// 事前処理
		ginskii.before(new File(OUTPUT_PC_DIR));
		ginskii.before(new File(OUTPUT_SP_DIR));

		// クロール処理
		ginskii.execute();
	}

	/**
	 * 事前処理(前回のファイル削除)
	 * @param dir
	 */
	private void before(File dir) {
		File[] files = dir.listFiles();
		if (files != null) {
			for (File f : files) {
				if (!f.exists()) {
					continue;
				} else if (f.isFile()) {
					f.delete();
				}
			}
		}
	}

	/**
	 * クロール処理
	 * @throws IOException
	 */
	private void execute() throws IOException {
		BufferedReader br = null;
		List<String> urls = new ArrayList<String>();
		// file read
		File file = new File(INPUT_DIR + "input_url.txt");

		try {
			br = new BufferedReader(new FileReader(file));
			for (String line; (line = br.readLine()) != null;) {
				urls.add(line);
			}
		} finally {
			if (br != null) {
				br.close();
			}
		}
		// PC,SPでそれぞれクロール
		this.crowler(urls, OUTPUT_PC_DIR + "pc_");
		this.crowler(urls, OUTPUT_SP_DIR + "sp_");
	}

	/**
	 * WebDriverでのクロール処理
	 * @param wd WebDrivers
	 * @param urls URLのリスト
	 * @param outputDir ファイル保存先ディレクトリ
	 */
	private void crowler(List<String> urls, String outputDir) {
		long l = 1;
		int  sleep = 250;
		WebDriver wd = null;
		if (StringUtils.startsWith(outputDir, OUTPUT_PC_DIR)) {
			// PC Driver set up
			wd = new FirefoxDriver();
		} else {
			// SP Driver set up
			FirefoxProfile profile = new FirefoxProfile();
			profile.setPreference("general.useragent.override", USER_AGENT);
			wd = new FirefoxDriver(profile);
		}

		for (String url : urls) {
			if (StringUtils.startsWith(url, TWITTER_PC) && StringUtils.startsWith(outputDir, OUTPUT_SP_DIR)) {
				// UAがSPでTwitterの場合、url変更とSLEEP時間を延ばす
				url = StringUtils.replace(url, TWITTER_PC, TWITTER_SP);
				sleep += 1000;
			}
			if (StringUtils.startsWithAny(url, URL_PREFIX)) {
				wd.get(url);
				sleep(sleep);
				File file = ((TakesScreenshot) wd).getScreenshotAs(OutputType.FILE);
				this.saveScreenShot(file, outputDir + StringUtils.leftPad(String.valueOf(l), 6, "0"));
			}
			l++;
		}
		wd.quit();
	}

	/**
	 * スクリーンショット保存処理
	 * @param file Fileオブジェクト
	 * @param fileName ディレクトリ/ファイル名
	 */
	private void saveScreenShot(File file, String fileName) {
		try {
			FileUtils.copyFile(file, new File(fileName + ".png"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * スリープ処理
	 * @param microtime ミリ秒
	 */
	private static void sleep(int microtime) {
		try {
			Thread.sleep(microtime);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}