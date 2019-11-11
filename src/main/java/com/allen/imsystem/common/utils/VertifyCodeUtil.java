package com.allen.imsystem.common.utils;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

/**
 * 生成图片验证码的工具类
 */
public class VertifyCodeUtil {

	private static final int LENGTH = 4;
	private static final int HEIGHT = 30;
	private static final int WIDTH = 120;
	private static final String CHARSET = "0123456789ABCDEFGHJKLMNOPQRSTUVWXYZabcdefghjklmnopqrstuvwxyz";
	private static final String[] FONTS = GraphicsEnvironment.getLocalGraphicsEnvironment()
			.getAvailableFontFamilyNames();

	public static Map<String, Object> getCodeImage() {
		BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
		Graphics g = image.getGraphics();
		String code = createRandomCode();
		
		setImageBackGround(g);
		setImageCode(g, code);

		Map<String, Object> result = new HashMap<>();
		result.put("code", code);
		result.put("image", image);
		return result;
	}

	private static String createRandomCode() {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < LENGTH; i++) {
			char ch = CHARSET.charAt((int) (Math.random() * CHARSET.length()));
			sb.append(ch);
		}
		return sb.toString();
	}

	private static void setImageBackGround(Graphics g) {
		g.setColor(Color.LIGHT_GRAY);
		g.fillRect(0, 0, WIDTH, HEIGHT);

		g.setColor(Color.GREEN);
		g.drawRect(1, 1, WIDTH - 2, HEIGHT - 2);

		g.setColor(Color.BLUE);

		for (int i = 0; i < 5; i++) {
			int fromX = (int) (Math.random() * WIDTH);
			int fromY = (int) (Math.random() * HEIGHT);
			int toX = (int) (Math.random() * WIDTH);
			int toY = (int) (Math.random() * HEIGHT);
			g.drawLine(fromX, fromY, toX, toY);
		}
	}
	
	private static void setImageCode(Graphics g, String code) {
		g.setColor(Color.RED);
		Font font = new Font("Georgia", Font.ITALIC, 22);
		g.setFont(font);
		for (int i = 0 , x = 10; i < code.length(); i++) {
			String ch = ""+code.charAt(i);
			g.drawString(ch, x, 20);
			x += 30;
		}
	}
	public static void main(String[] args) {
		Map<String,Object> map = getCodeImage();
		System.out.println(map.get("code"));
		BufferedImage image = (BufferedImage) map.get("image");
		System.out.println(image);
//		for (String string : FONTS) {
//			System.out.println(string);
//		}
	}
}
