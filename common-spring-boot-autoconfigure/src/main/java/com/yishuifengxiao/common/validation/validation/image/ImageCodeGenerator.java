/**
 * 
 */
package com.yishuifengxiao.common.validation.validation.image;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.Random;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.context.request.ServletWebRequest;

import com.yishuifengxiao.common.properties.CodeProperties;
import com.yishuifengxiao.common.validation.entity.ImageCode;
import com.yishuifengxiao.common.validation.generator.CodeGenerator;

/**
 * @author yishui
 * @date 2019年1月23日
 * @version 0.0.1
 */
public class ImageCodeGenerator implements CodeGenerator {
	private CodeProperties codeProperties;

	@Override
	public ImageCode generate(ServletWebRequest servletWebRequest) {
		int width = ServletRequestUtils.getIntParameter(servletWebRequest.getRequest(), "width",
				codeProperties.getImage().getWidth());
		int height = ServletRequestUtils.getIntParameter(servletWebRequest.getRequest(), "height",
				codeProperties.getImage().getHeight());
		// 生成一个图片对象
		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

		Graphics g = image.getGraphics();

		Random random = new Random();

		g.setColor(codeProperties.getImage().getFringe() ? getRandColor(200, 250) : Color.WHITE);
		g.fillRect(0, 0, width, height);

		g.setFont(new Font("Times New Roman", Font.ITALIC, 23));
		// 生成干扰条纹
		if (codeProperties.getImage().getFringe()) {
			g.setColor(getRandColor(160, 200));
			for (int i = 0; i < 155; i++) {
				int x = random.nextInt(width);
				int y = random.nextInt(height);
				int xl = random.nextInt(12);
				int yl = random.nextInt(12);
				g.drawLine(x, y, x + xl, y + yl);
			}
		}

		// 生成四位的随机数
		String sRand = "";
		for (int i = 0; i < codeProperties.getImage().getLength(); i++) {
			String rand = RandomStringUtils.random(1, codeProperties.getImage().isContainLetter(),
					codeProperties.getImage().isContainNumber());
			// 防止生成为null
			rand = StringUtils.isNotBlank(rand) ? rand : new Random().nextInt(10) + "";
			sRand += rand;
			// 字符的颜色随机
			g.setColor(new Color(20 + random.nextInt(110), 20 + random.nextInt(110), 20 + random.nextInt(110)));
			g.drawString(rand, 13 * i + 6, 22);
		}

		g.dispose();
		return new ImageCode(codeProperties.getImage().getExpireIn(), sRand, image);
	}

	/**
	 * 生成随机背景条纹
	 * 
	 * @param fc
	 * @param bc
	 * @return
	 */
	private Color getRandColor(int fc, int bc) {
		Random random = new Random();
		if (fc > 255) {
			fc = 255;
		}
		if (bc > 255) {
			bc = 255;
		}
		int r = fc + random.nextInt(bc - fc);
		int g = fc + random.nextInt(bc - fc);
		int b = fc + random.nextInt(bc - fc);
		return new Color(r, g, b);
	}

	public CodeProperties getCodeProperties() {
		return codeProperties;
	}

	public void setCodeProperties(CodeProperties codeProperties) {
		this.codeProperties = codeProperties;
	}

	public ImageCodeGenerator(CodeProperties codeProperties) {
		this.codeProperties = codeProperties;
	}

	public ImageCodeGenerator() {

	}

}