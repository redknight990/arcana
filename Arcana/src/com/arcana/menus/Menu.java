package com.arcana.menus;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.swing.JFrame;

import com.arcana.anim.Animation;
import com.arcana.input.Mouse;
import com.arcana.loaders.ImageLoader;
import com.arcana.settings.Settings;
import com.arcana.src.Arcana;
import com.utils.src.Logger;

public class Menu {
	
	public int currentItem = 0;
	private int transitionItem = -1;
	private String[] items = {"Multiplayer", "Options", "About", "Exit"};
	private Rectangle[] itemPositions;
	private Rectangle2D[] actualPositions;
	private JFrame frame;
	private Font font, titleFont;
	private boolean isInTransition = false;
	public static enum MenuState {
			MainMenu,
			OptionsMenu,
			PlayMenu
	}
	private MenuState menuState = MenuState.MainMenu;
	private int spaceBetweenItems;
	private int timer;
	private Animation mainAnim;
	public OptionsMenu options;
	public PlayMenu play;
	
	Settings s;
	
	/**
	 * Initializes the variables needed for the ticking and rendering of the menu.
	 * @param frame The JFrame in which the menu is to be rendered.
	 */
	public Menu(JFrame frame, Settings s){
		this.frame = frame;
		this.font = new Font("Yoster Island", Font.PLAIN, frame.getContentPane().getHeight() * 8 / 108);
		this.titleFont =  new Font("Yoster Island", Font.PLAIN, frame.getContentPane().getHeight() * 14 / 108);
		this.spaceBetweenItems = frame.getContentPane().getHeight() * 2 / 108;
		itemPositions = new Rectangle[items.length];
		FontMetrics fm = frame.getGraphics().getFontMetrics(font);
		int startY = (frame.getContentPane().getHeight() / 2) + (((frame.getContentPane().getHeight() / 2) - (((((int)fm.getStringBounds("A", frame.getGraphics()).getHeight()) + spaceBetweenItems) * items.length))) / 2);
		for(int i = 0; i < items.length; i++){
			double strWidth = fm.getStringBounds(items[i], frame.getGraphics()).getWidth();
			double strHeight = fm.getStringBounds(items[i], frame.getGraphics()).getHeight();
			int x = (frame.getContentPane().getWidth() / 4) - (int)(strWidth / 2);
			int y = startY + (int)(strHeight * (i + 1) + (spaceBetweenItems * i)) - (int)strHeight;
			itemPositions[i] = new Rectangle(x, y, (int)strWidth, (int)strHeight);
		}
		this.s = s;
		this.mainAnim = new Animation(loadAnimFrames(), 0.1);
		this.options = new OptionsMenu(frame, this, s);
		this.play = new PlayMenu(frame, this, s);
	}
	
	private BufferedImage[] loadAnimFrames() {
		String jarPath = Arcana.getJarPath();
		String animPath = "/res/menu/anim/";
		int frameNumber = 8;
		BufferedImage[] frames = new BufferedImage[frameNumber];
		for(int i = 0; i < frameNumber; i++){
			try {
				frames[i] = ImageLoader.loadImage(jarPath + animPath + "frame_" + Integer.toString(i) + ".png");
			} catch (IOException e) {
				Arcana.LOGGER.println(e.toString(), Logger.ERROR);
				for(StackTraceElement elem: e.getStackTrace())
					Arcana.LOGGER.println(elem.toString(), Logger.ERROR);
			}
		}
		return frames;
	}

	/**
	 * Adds mouse hover and animations to the menu.
	 */
	public void tick(){
		if(menuState == MenuState.MainMenu){
			if(isInTransition && timer < Arcana.ticksPerSec / 10){
				timer++;
			}else if(isInTransition && timer >= Arcana.ticksPerSec / 10){
				timer = 0;
				isInTransition = false;
				currentItem = transitionItem;
			}
			if(!isInTransition && actualPositions != null){
				for(int i = 0; i < items.length; i++){
					if(actualPositions[i] != null && Mouse.isMouseIn(actualPositions[i])){
						selectItem(i);
					}
				}
			}
		}else if(menuState == MenuState.OptionsMenu){
			options.tick();
		}else if(menuState == MenuState.PlayMenu){
			play.tick();
		}
	}
	
	public void selectItem(int index){
		if(currentItem != index){
			isInTransition = true;
			transitionItem = index;
		}
	}
	
	public void moveCursorUp(){
		for(int i = 0; i < items.length; i++){
			if(Mouse.isMouseIn(actualPositions[i]))
				return;
		}
		int moveTo = currentItem - 1;
		if(moveTo < 0)
			moveTo = items.length - 1;
		selectItem(moveTo);
	}
	
	public void moveCursorDown(){
		for(int i = 0; i < items.length; i++){
			if(Mouse.isMouseIn(actualPositions[i]))
				return;
		}
		int moveTo = currentItem + 1;
		if(moveTo > items.length - 1)
			moveTo = 0;
		selectItem(moveTo);
	}
	
	/**
	 * Renders the menu.
	 */
	public void render(Graphics g){
		Graphics2D g2d = (Graphics2D) g;
		mainAnim.render(g, new Rectangle(0, 0, Arcana.frame.getContentPane().getWidth(), Arcana.frame.getContentPane().getHeight()));
		if(menuState == MenuState.MainMenu){
			g.setColor(new Color(0, 50, 255));
			g.setFont(titleFont);
			int strWidth = (int) g.getFontMetrics(titleFont).getStringBounds(Arcana.TITLE, g).getWidth();
			g.drawString(Arcana.TITLE, (frame.getContentPane().getWidth() / 2) - (int)(strWidth / 2), 30 + (frame.getContentPane().getHeight() * 14 / 108));
			g.setFont(font);
			if(this.actualPositions == null){
				actualPositions = new Rectangle2D[items.length];
				for(int j = 0; j < items.length; j++){
					actualPositions[j] = getStringBounds(items[j], g2d, (int)itemPositions[j].getMinX(), (int)itemPositions[j].getMaxY());
				}
			}
			
			for(int i = 0; i < items.length; i++){
				if(Mouse.isMouseIn(actualPositions[i])){
					g.setColor(Color.CYAN);
				}else{
					g.setColor(Color.white);
				}
				g.drawString(items[i], (int)itemPositions[i].getMinX(), (int)itemPositions[i].getMaxY());
			}
			g2d.setStroke(new BasicStroke(frame.getContentPane().getHeight() / 216));
			if(isInTransition){
				g2d.setColor(Color.white);
				int y = (int)actualPositions[currentItem].getCenterY() + (int)((actualPositions[transitionItem].getCenterY() - actualPositions[currentItem].getCenterY()) * timer / (Arcana.ticksPerSec / 10));
				int x = (int)actualPositions[currentItem].getMinX() + (int)((actualPositions[transitionItem].getMinX() - actualPositions[currentItem].getMinX()) * timer / (Arcana.ticksPerSec / 10)) - (frame.getContentPane().getWidth() * 9 / 192);
				int x2 = (int)actualPositions[currentItem].getMaxX() + (int)((actualPositions[transitionItem].getMaxX() - actualPositions[currentItem].getMaxX()) * timer / (Arcana.ticksPerSec / 10)) + (frame.getContentPane().getWidth() * 9 / 192);
				g2d.drawLine(0, y, x, y);
				g2d.drawLine(x, y, x + frame.getContentPane().getWidth() * 2 / 192, y + (int)((actualPositions[transitionItem].getMaxY() - actualPositions[transitionItem].getCenterY())));
				g2d.drawLine(x, y, x + frame.getContentPane().getWidth() * 2 / 192, y - (int)((actualPositions[transitionItem].getMaxY() - actualPositions[transitionItem].getCenterY())));
				g2d.drawLine(frame.getContentPane().getWidth(), y, x2, y);
				g2d.drawLine(x2, y, x2 - frame.getContentPane().getWidth() * 2 / 192, y + (int)((actualPositions[transitionItem].getMaxY() - actualPositions[transitionItem].getCenterY())));
				g2d.drawLine(x2, y, x2 - frame.getContentPane().getWidth() * 2 / 192, y - (int)((actualPositions[transitionItem].getMaxY() - actualPositions[transitionItem].getCenterY())));
			}else{
				g2d.setColor(Color.white);
				int x = (int)actualPositions[currentItem].getMinX() - (frame.getContentPane().getWidth() * 9 / 192);
				int x2 = (int)actualPositions[currentItem].getMaxX() + (frame.getContentPane().getWidth() * 9 / 192);
				int y = (int)actualPositions[currentItem].getCenterY();
				g2d.drawLine(0, y, x, y);
				g2d.drawLine(x, y, x + frame.getContentPane().getWidth() * 2 / 192, y + (int)((actualPositions[currentItem].getMaxY() - actualPositions[currentItem].getCenterY())));
				g2d.drawLine(x, y, x + frame.getContentPane().getWidth() * 2 / 192, y - (int)((actualPositions[currentItem].getMaxY() - actualPositions[currentItem].getCenterY())));
				g2d.drawLine(frame.getContentPane().getWidth(), y, x2, y);
				g2d.drawLine(x2, y, x2 - frame.getContentPane().getWidth() * 2 / 192, y + (int)((actualPositions[currentItem].getMaxY() - actualPositions[currentItem].getCenterY())));
				g2d.drawLine(x2, y, x2 - frame.getContentPane().getWidth() * 2 / 192, y - (int)((actualPositions[currentItem].getMaxY() - actualPositions[currentItem].getCenterY())));
			}
		}else if(menuState == MenuState.OptionsMenu){
			options.render(g);
		}else if(menuState == MenuState.PlayMenu){
			play.render(g);
		}
	}
	
	/**
	 * Called when the mouse is released.
	 * @param e The MouseEvent containing the coordinates of the mouse click.
	 */
	public void mouseReleased(MouseEvent e){
		if(this.menuState == MenuState.MainMenu){
			int y = e.getY();
			int x = e.getX();
			for(int i = 0; i < items.length; i++){
				Rectangle2D r = actualPositions[i];
				if(x <= r.getMaxX() && x >= r.getMinX() 
					&& y <= r.getMaxY() && y >= r.getMinY()){
					buttonPressed(i);
					return;
				}
			}
		}else if(this.menuState == MenuState.OptionsMenu){
			options.mouseReleased(e);
		}else if(this.menuState == MenuState.PlayMenu){
			play.mouseReleased(e);
		}
	}
	
	public void mousePressed(MouseEvent e){
		if(this.menuState == MenuState.MainMenu){
			
		}else if(this.menuState == MenuState.OptionsMenu){
			options.mousePressed(e);
		}else if(this.menuState == MenuState.PlayMenu){
			play.mousePressed(e);
		}
	}
	
	private Rectangle2D getStringBounds(String str, Graphics2D g2d, int x, int y){
		FontRenderContext frc = g2d.getFontRenderContext();
		GlyphVector gv = g2d.getFont().createGlyphVector(frc, str);
		return gv.getPixelBounds(null, x, y);
	}
	
	/**
	 * Called if menu button is pressed.
	 * @param index The index of the button in the array.
	 */
	public void buttonPressed(int index){
		if(index == 0){
			play.currentItem = 0;
			menuState = MenuState.PlayMenu;
		}else if(index == 1){
			options.currentItem = 1;
			menuState = MenuState.OptionsMenu;
		}else if(index == 2){
			
		}else if(index == 3){
			Arcana.exitGame(0);
		}
	}
	
	public MenuState getState(){
		return menuState;
	}
	
	public int getCurrentItem(){
		return currentItem;
	}
	
	public void setState(MenuState state){
		this.menuState = state;
	}
}
