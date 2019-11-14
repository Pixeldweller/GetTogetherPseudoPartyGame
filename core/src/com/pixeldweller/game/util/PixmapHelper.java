package com.pixeldweller.game.util;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Blending;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.glutils.PixmapTextureData;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Disposable;

public class PixmapHelper implements Disposable {

	private class PixmapChange {

		int x, y;
		int width;

		void set(int x, int y, int width) {
			this.x = x;
			this.y = y;
			this.width = width;
		}
	}
	
	public FileHandle orgPixmap;

	public Pixmap pixmap;
	public Sprite sprite;
	public Texture texture;

	public final Color color = new Color();
	public float radiusFactor = 1f;	

	// only allow 10 modifications
	private PixmapChange[] modifications = new PixmapChange[15];
	private int lastModification = 0;

	private Pixmap renderPixmap32;
	private Pixmap renderPixmap64;
	private Pixmap renderPixmap128;
	private Pixmap renderPixmap256;
	private Pixmap renderPixmap512;
	
	public PixmapHelper(Pixmap pixmap, Sprite sprite, Texture texture) {
		this.pixmap = pixmap;
		this.sprite = sprite;
		this.texture = texture;	
		
		for (int i = 0; i < modifications.length; i++)
			modifications[i] = new PixmapChange();

		this.renderPixmap32 = new Pixmap(32, 32, Format.RGBA8888);
		this.renderPixmap64 = new Pixmap(64, 64, Format.RGBA8888);
		this.renderPixmap128 = new Pixmap(128, 128, Format.RGBA8888);
		this.renderPixmap256 = new Pixmap(256, 256, Format.RGBA8888);
		this.renderPixmap512 = new Pixmap(512, 512, Format.RGBA8888);
	}
	
	public void setFileHandle(FileHandle file)
	{
		orgPixmap = file;
	}

	public PixmapHelper(Pixmap pixmap) {
		this.pixmap = pixmap;
		this.texture = new Texture(new PixmapTextureData(pixmap, pixmap.getFormat(), false, false));
		this.texture.setFilter(TextureFilter.Linear, TextureFilter.Linear);		
		this.sprite = new Sprite(texture);

		for (int i = 0; i < modifications.length; i++)
			modifications[i] = new PixmapChange();

		this.renderPixmap32 = new Pixmap(32, 32, Format.RGBA8888);
		this.renderPixmap64 = new Pixmap(64, 64, Format.RGBA8888);
		this.renderPixmap128 = new Pixmap(128, 128, Format.RGBA8888);
		this.renderPixmap256 = new Pixmap(256, 256, Format.RGBA8888);
		this.renderPixmap512 = new Pixmap(512, 512, Format.RGBA8888);
	}

	/**
	 * Projects the coordinates (x, y) to the Pixmap coordinates system and store the result in the specified Vector2.
	 * 
	 * @param position
	 *            The Vector2 to store the transformed coordinates.
	 * @param x
	 *            The x coordinate to be projected.
	 * @param y
	 *            The y coordinate to be prjected
	 */
	public void project(Vector2 position, float x, float y) {
		position.set(x, y);

		float centerX = sprite.getX() + sprite.getOriginX();
		float centerY = sprite.getY() + sprite.getOriginY();

		position.add(-centerX, -centerY);

		position.rotate(-sprite.getRotation());

		float scaleX = pixmap.getWidth() / sprite.getWidth();
		float scaleY = pixmap.getHeight() / sprite.getHeight();

		position.x *= scaleX;
		position.y *= scaleY;

		position.add( //
				pixmap.getWidth() * 0.5f, //
				-pixmap.getHeight() * 0.5f //
		);

		position.y *= -1f;
	}

	public int getPixel(Vector2 position) {
		return getPixel(position.x, position.y);
	}

	public int getPixel(float x, float y) {
		return pixmap.getPixel((int) x, (int) y);
	}

	public void setPixel(float x, float y, int value) {
		Color.rgba8888ToColor(color, value);
		pixmap.setColor(color);
	}

	public void eraseCircle(float x, float y, float radius, float factor) {
		if (lastModification == modifications.length)
			return;

		float scaleX = pixmap.getWidth() / sprite.getWidth();

		int newRadius = Math.round(radius * scaleX * radiusFactor);

		if (x + newRadius < 0 || y + newRadius < 0)
			return;

		if (x - newRadius > pixmap.getWidth() || y - newRadius > pixmap.getHeight())
			return;

		Blending blending = pixmap.getBlending();
		pixmap.setColor(0f, 0f, 0f, 0f);
		pixmap.setBlending(Blending.None);

		int newX = Math.round(x);
		int newY = Math.round(y);
	   
	    /*
	    pixmap.drawPixel(x0, y0 + radius);
	    pixmap.drawPixel(x0, y0 - radius);
	    pixmap.drawPixel(x0 + radius, y0);
	    pixmap.drawPixel(x0 - radius, y0);

	    while(x < y)
	    {
	      if(f >= 0)
	      {
	        y--;
	        ddF_y += 2;
	        f += ddF_y;
	      }
	      x++;
	      ddF_x += 2;
	      f += ddF_x + 1;
	      	      
	      pixmap.drawLine(x0 + x, y0 + y,x0 - x, y0 + y);	    
	      
	      
	      pixmap.drawLine(x0 + x, y0 - y,x0 - x, y0 - y);	     
	      pixmap.drawLine(x0 + y, y0 + x,x0 - y, y0 + x);	     
	      pixmap.drawLine(x0 + y, y0 - x,x0 - y, y0 - x);
	     
	    }*/		
	    
	    for(float y0=-radius; y0<=radius; y0++)
	        for(float x0=-radius; x0<=radius; x0++)
	            if(x0*x0+y0*y0 <= radius*radius)
	            {
	            	Color c = new Color(pixmap.getPixel((int)(newX+x0), (int)(newY+y0)));
	          	    
	            	if(c.a > 0.15f)
	            	{
		         	    c.r-=factor*.5f;
		         	    c.b-=factor*.5f;
		         	    c.g-=factor*.5;	         	    
		         	    c.a-=factor;
	            	
		         	    if(c.r < 0f) c.r = 0f;
		         	    if(c.b < 0f) c.b = 0f;
		         	    if(c.g < 0f) c.g = 0f;
		         	    if(c.a < 0.15f) c.a = 0.15f;
	            	}
	         	    
	         	    
	         	    pixmap.setColor(c);
	            	
	            	pixmap.drawPixel((int)(newX+x0), (int)(newY+y0));
	            }
		
		//pixmap.fillCircle(newX, newY, newRadius);		
	    pixmap.setBlending(blending);
		
		modifications[lastModification++].set(newX, newY, newRadius * 2);
	}
	
	public void fadeCircle(float x, float y, float radius, float factor) {
		if (lastModification == modifications.length)
			return;

		float scaleX = pixmap.getWidth() / sprite.getWidth();

		int newRadius = Math.round(radius * scaleX * radiusFactor);

		if (x + newRadius < 0 || y + newRadius < 0)
			return;

		if (x - newRadius > pixmap.getWidth() || y - newRadius > pixmap.getHeight())
			return;

		Blending blending = pixmap.getBlending();
		pixmap.setColor(0f, 0f, 0f, 0f);
		pixmap.setBlending(Blending.None);

		int newX = Math.round(x);
		int newY = Math.round(y);
	   
	    for(float y0=-radius; y0<=radius; y0++)
	        for(float x0=-radius; x0<=radius; x0++)
	            if(x0*x0+y0*y0 <= radius*radius)
	            {
	            	Color c = new Color(pixmap.getPixel((int)(newX+x0), (int)(newY+y0)));
	          	    
	            	float d = (float) Math.sqrt(Math.pow((y0), 2) + Math.pow((x0), 2));
	            	
	            	if(c.a > 0f)
	            	{		         	        	    
		         	    float alpha = factor + d/radius;
	            		
		         	    if(c.a > alpha)
		         	    {
		         	    	c.a=alpha;		
		         	    	
		         	    	if(c.a < 0.15f) c.a = 0.15f;
		         	    	
		         	    	//c.g = 1f-d/(radius*0.9f);
		         	    	
		         	    	if(c.g < 0) c.g = 0;
		         	    }
		         	    
		         	   
		         	   
		         	  
		         	    if(c.a > 1f) c.a = 1f;
		         	    if(c.a < 0f) c.a = 0f;
	            	}	         	    
	         	    
	         	    pixmap.setColor(c);	     
	            	pixmap.drawPixel((int)(newX+x0), (int)(newY+y0));
	            }
		
		/*pixmap.setColor(new Color(0f,0f,0f, 0.85f));
		pixmap.fillCircle(newX, newY, (int) radius);
		pixmap.setColor(new Color(0f,0f,0f, 0.6f));
		pixmap.fillCircle(newX, newY, (int) (radius*0.85f));
		pixmap.setColor(new Color(0f,0f,0f, 0.25f));
		pixmap.fillCircle(newX, newY, (int) (radius*0.6f));
		pixmap.setColor(new Color(0f,0f,0f, 0.05f));
		pixmap.fillCircle(newX, newY, (int) (radius*0.3f));
		*/
		
		
		//pixmap.drawPixmap(light, newX-(int)(light.getWidth()/2*radius/100f), newY-(int)(light.getHeight()/2*radius/100f),light.getWidth()/2,light.getWidth()/2, (int)(light.getHeight()*radius/100), (int)(light.getHeight()*radius/100));
		
		
		//pixmap.drawPixmap(light, newX-(int)(light.getWidth()/2), newY-(int)(light.getHeight()/2));
		
		//pixmap.drawPixmap(light, newX-(int)(light.getWidth()/2)+25, newY-(int)(light.getHeight()/2));
		
		//batch.draw(tex, e.getPosX()-tex.getWidth()/4,e.getPosY()-tex.getHeight()/4, tex.getWidth()/4, 0, tex.getWidth()/2,tex.getHeight()/2,1f, 1f, rotation, 0, 0, tex.getWidth(),tex.getHeight(), false, false);		
		
		//pixmap.fillCircle(newX, newY, newRadius);		
	    pixmap.setBlending(blending);
		
		modifications[lastModification++].set(newX, newY, newRadius * 2);
	}
	
	public void light(float x, float y, float radius, Color color) {
		if (lastModification == modifications.length)
			return;

		float scaleX = pixmap.getWidth() / sprite.getWidth();

		int newRadius = Math.round(radius * scaleX * radiusFactor);

		if (x + newRadius < 0 || y + newRadius < 0)
			return;

		if (x - newRadius > pixmap.getWidth() || y - newRadius > pixmap.getHeight())
			return;

		Blending blending = pixmap.getBlending();
		pixmap.setColor(0f, 0f, 0f, 0f);
		pixmap.setBlending(Blending.None);

		int newX = Math.round(x);
		int newY = Math.round(y);
	   
	    for(float y0=-radius; y0<=radius; y0++)
	        for(float x0=-radius; x0<=radius; x0++)
	            if(x0*x0+y0*y0 <= radius*radius)
	            {
	            	Color c = new Color(pixmap.getPixel((int)(newX+x0), (int)(newY+y0)));
	          	    
	            	float d = (float) Math.sqrt(Math.pow((y0), 2) + Math.pow((x0), 2));
	            	
	            	if(c.a > 0f)
	            	{		         	        	    
		         	    float alpha = d/radius;
	            		
		         	    if(c.a > alpha)
		         	    {
		         	    	c.a=alpha;		
		         	    	
		         	    	if(c.a < 0.15f) c.a = 0.15f;
		         	    	
		         	    	float r = color.r-d/(radius*0.9f), b = color.b-d/(radius*0.9f), g = color.g-d/(radius*0.9f);
		         	    	
		         	    	if(r > 1f) r = 1f;
		         	    	if(b > 1f) b = 1f;
		         	    	if(g > 1f) g = 1f;
		         	    	
		         	    	if(r < 0f) r = 0f;
		         	    	if(b < 0f) b = 0f;
		         	    	if(g < 0f) g = 0f;
		         	    	
		         	    	c.r = r;
		         	    	c.b = b;
		         	    	c.g = g;
		         	    	
		         	    	
		         	    	//c.g = 1f-d/(radius*0.9f);
		         	    }
		         	  
		         	    if(c.a > 1f) c.a = 1f;
		         	    if(c.a < 0f) c.a = 0f;
	            	}	         	    
	         	    
	         	    pixmap.setColor(c);	     
	            	pixmap.drawPixel((int)(newX+x0), (int)(newY+y0));
	            }
		
		//pixmap.fillCircle(newX, newY, newRadius);		
	    pixmap.setBlending(blending);
		
		modifications[lastModification++].set(newX, newY, newRadius * 2);
	}
	
	public void drawCircle(float x, float y, float radius, float factor) {
		if (lastModification == modifications.length)
			return;

		float scaleX = pixmap.getWidth() / sprite.getWidth();

		int newRadius = Math.round(radius * scaleX * radiusFactor);

		if (x + newRadius < 0 || y + newRadius < 0)
			return;

		if (x - newRadius > pixmap.getWidth() || y - newRadius > pixmap.getHeight())
			return;

		Blending blending = pixmap.getBlending();	
		pixmap.setBlending(Blending.None);

		int newX = Math.round(x);
		int newY = Math.round(y);
	    
	    for(float y0=-radius; y0<=radius; y0++)
	        for(float x0=-radius; x0<=radius; x0++)
	            if(x0*x0+y0*y0 <= radius*radius)
	            {
	            	Color c = new Color(pixmap.getPixel((int)(newX+x0), (int)(newY+y0)));
	          	   	         	    
	         	    if( c.a > 0f && c.a < 1f)
	         	    {	
	         	    	c.r*=factor;
	         	    	c.b*=factor;
	         	    	c.g*=factor;	
	         	    	if(c.a <= 0.15f) c.a = 0.155f;
	         	    	c.a*=factor;	         	   	         	    
	         	    	
		         	    if(c.r > 0.65f) c.r = 0.65f;
		         	    if(c.b > 0.65f) c.b = 0.65f;
		         	    if(c.g > 0.65f) c.g = 0.65f;
		         	    if(c.a > 1f) c.a = 1f;
	         	    }
	         	    
	         	    pixmap.setColor(c);
	            	
	            	pixmap.drawPixel((int)(newX+x0), (int)(newY+y0));
	            }
		
		
	    pixmap.setBlending(blending);
		
		modifications[lastModification++].set(newX, newY, newRadius * 2);
	}
	
	public void drawRect(float x, float y, float w, float h, float a) {
		if (lastModification == modifications.length)
			return;

		float scaleX = pixmap.getWidth() / sprite.getWidth();		
		
		Blending blending = pixmap.getBlending();	
		pixmap.setBlending(Blending.None);

		int newX = Math.round(x);
		int newY = Math.round(y);
	    
		Color c = new Color(Color.BLACK);
	          	   	         	    
 	    c.a = a;
 	    	
    	if(c.a <= 0.05f) c.a = 0.005f;
 	    if(c.a > 1f) c.a = 1f;
 	       	    
 	    pixmap.setColor(c);
    	
    	pixmap.fillRectangle((int)(x-w/2), (int)(y-h/2), (int)w,(int) h);	            
		
		
    	pixmap.setBlending(blending);
		
		modifications[lastModification++].set(newX, newY, (int)(w * h));
	}	
	
	
	public void drawX(int x, int y, Pixmap toPrint) {
		if (lastModification == modifications.length)
			return;
		
		Blending blending = pixmap.getBlending();	
		pixmap.setBlending(Blending.SourceOver);
		
		int newX = x;//new Random().nextInt(300)+10;
		int newY = y;//new Random().nextInt(300)+10;		
		
		
		for(int y0= 0; y0<=toPrint.getHeight(); y0++)
        for(int x0=0; x0<=toPrint.getWidth(); x0++)
        {   
        	Color org = new Color(pixmap.getPixel(x0, y0));    		
        	
        	org.a = 0.4f;
        	org.r*=org.a;
        	org.b*=org.a;
        	org.g*=org.a;	
        	org.a = 1f;
        	
        	Color c = new Color(toPrint.getPixel(x0, y0));
        	if (c.a == 1f && c != org)
        	{
        		c.a*=0.75;
        		pixmap.setColor(c);
        		pixmap.drawPixel(newX+x0, newY+y0);
        	}
        	
        }
		 
		
    	//pixmap.drawPixmap(toPrint, newX, newY, 0, 0, 128, 128);   
    	//pixmap.drawPixmap(toPrint, 0, 0, toPrint.getWidth(), toPrint.getHeight(), newX, newY, new Random().nextInt(40)+10, new Random().nextInt(40)+10);
		pixmap.setBlending(blending);
    	
    	
    	modifications[lastModification++].set(newX, newY, toPrint.getWidth()*2);
    	    
    	
    	update();
		//modifications[lastModification++].set(newX, newY, toPrint.getWidth()*2);
	}
	
	public void tresh() {
			 
	    for(float y0= 0; y0<=texture.getHeight(); y0++)
	        for(float x0=-0; x0<=texture.getWidth(); x0++)
	        {   
            	Color c = new Color(pixmap.getPixel((int)(x0), (int)(y0)));
	          	   	         	    
            	if(c.a < 0.6f && c.a > 0.4f)
         	    {
         	    	c.a = 0.25f;
         	    	c.r*=c.a;
         	    	c.b*=c.a;
         	    	c.g*=c.a;	
         	    	c.a = 1f;
         	    }
         	    else if (c.a < 0.4f)
         	    	c.a = 0f;
	            	  
	         	    pixmap.setColor(c);
	            	
	            	pixmap.drawPixel((int)(x0), (int)(y0));
	        }	  
	    
	    for(float i = 0; i<texture.getWidth()/10f; i+=texture.getWidth()/10f)
    	for(float j = 0; j<texture.getHeight()/10f; j+=texture.getHeight()/10f)
    	{
    		  modifications[lastModification++].set((int)i, (int)j, (int)Math.round(pixmap.getHeight()/10f) * (int)Math.round(pixmap.getWidth()/10f));
    	}	    
	   
	    System.out.println("Wird ausgeführt");
		update();
	}
	
	public void transition(int h) {
		 
		
	}

	private Pixmap getPixmapForRadius(int width) {
		if (width <= 32)
			return renderPixmap32;
		if (width <= 64)
			return renderPixmap64;
		if (width <= 128)
			return renderPixmap128;
		if (width <= 256)
			return renderPixmap256;
		return renderPixmap512;
	}

	/**
	 * Updates the opengl texture with all the pixmap modifications.
	 */
	public void update() {

		if (lastModification == 0)
			return;

		
		Gdx.gl.glBindTexture(GL20.GL_TEXTURE_2D, texture.getTextureObjectHandle());

		int width = pixmap.getWidth();
		int height = pixmap.getHeight();

		for (int i = 0; i < lastModification; i++) {

			PixmapChange pixmapChange = modifications[i];

			Pixmap renderPixmap = getPixmapForRadius(pixmapChange.width);

			int dstWidth = renderPixmap.getWidth();
			int dstHeight = renderPixmap.getHeight();

			pixmap.setBlending(Blending.None);

			int x = Math.round(pixmapChange.x) - dstWidth / 2;
			int y = Math.round(pixmapChange.y) - dstHeight / 2;

			if (x + dstWidth > width)
				x = width - dstWidth;
			else if (x < 0)
				x = 0;

			if (y + dstHeight > height)
				y = height - dstHeight;
			else if (y < 0) {
				y = 0;
			}

			renderPixmap.drawPixmap(pixmap, 0, 0, x, y, dstWidth, dstHeight);

			
			Gdx.gl.glTexSubImage2D(GL20.GL_TEXTURE_2D, 0, x, y, dstWidth, dstHeight, //
					renderPixmap.getGLFormat(), renderPixmap.getGLType(), renderPixmap.getPixels());

		}

		lastModification = 0;
	}

	/**
	 * Reload all the pixmap data to the opengl texture, to be used after the game was resumed.
	 */
	public void reload() {
//		 texture.dispose();
//		 if (texture.getTextureObjectHandle() == 0)
		texture.load(new PixmapTextureData(pixmap, pixmap.getFormat(), false, false));
	}
	
	public void reset() {		
		pixmap.dispose();
		if(orgPixmap != null){
			pixmap = new Pixmap(orgPixmap);		
			 if (texture.getTextureObjectHandle() == 0)
					texture.load(new PixmapTextureData(pixmap, pixmap.getFormat(), false, false));
			for (int i = 0; i < modifications.length / 2; i++) {
				for (int j = 0; j < modifications.length / 2; j++) {

					modifications[i] = new PixmapChange();
					modifications[i].set(i * 100, j * 100, 120);
				}
			}
		}			
		else
		{
			pixmap = new Pixmap(pixmap.getWidth(),pixmap.getHeight(), pixmap.getFormat());
			pixmap.setColor(0f, 0f, 0f, 1f);
			pixmap.fill();
			
			pixmap.setColor(0f,0f,0f,0f);
			pixmap.drawPixel(0, 0);
			
		}
	}

	@Override
	public void dispose() {
		this.pixmap.dispose();
		this.texture.dispose();
		this.renderPixmap32.dispose();
		this.renderPixmap64.dispose();
		this.renderPixmap128.dispose();
		this.renderPixmap256.dispose();
	}

}