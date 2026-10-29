package com.example.ui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;

public final class GameIcon {

    private GameIcon() {
    }

    public static Image create() {
        BufferedImage image = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        try {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);

            g.setColor(new Color(8, 55, 83));
            g.fillRoundRect(1, 1, 62, 62, 17, 17);

            g.setColor(new Color(37, 92, 74));
            g.fill(new Ellipse2D.Double(10, 43, 44, 16));
            g.setColor(new Color(88, 176, 102));
            g.fill(new Ellipse2D.Double(8, 38, 48, 18));
            g.setColor(new Color(187, 244, 178));
            g.fill(new Ellipse2D.Double(22, 44, 20, 6));

            g.setColor(new Color(77, 194, 98));
            g.fill(new Ellipse2D.Double(20, 18, 28, 24));
            g.setColor(new Color(35, 125, 59));
            g.setStroke(new BasicStroke(2f));
            g.draw(new Ellipse2D.Double(20, 18, 28, 24));

            g.setColor(new Color(243, 255, 238));
            g.fill(new Ellipse2D.Double(23, 11, 10, 10));
            g.fill(new Ellipse2D.Double(36, 11, 10, 10));
            g.setColor(new Color(12, 44, 29));
            g.fill(new Ellipse2D.Double(27, 14, 4, 5));
            g.fill(new Ellipse2D.Double(40, 14, 4, 5));

            g.setColor(new Color(17, 90, 45));
            g.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.drawArc(30, 25, 11, 8, 205, 130);
        } finally {
            g.dispose();
        }
        return image;
    }
}
