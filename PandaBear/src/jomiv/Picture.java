package jomiv;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Picture implements Comparable<Picture>, Serializable {
	
	private static final long serialVersionUID = 5045470541644776894L;
	protected String title, caption, imagePath;
	protected ArrayList<String> tags;
	protected byte copy[]; //A copy of this image's contents. Useful
	//in case the picture gets moved.
	protected Date lastViewed, created, lastModified;
	
	public Picture(String tags[], String caption,
			String title, String imagePath, Date lastViewed,
			Date created, Date lastModified) {
		this.tags = new ArrayList<String>(tags.length);
		
		for (int i = 0; i < tags.length; i += 1) {
			this.tags.add(new String(tags[i]));
		}
	
		if (imagePath != null) {
			this.imagePath = new File(imagePath).getAbsolutePath();
		}
		else {
			this.imagePath = new String("");
		}
		
		this.title = new String(title == null ? "" : title);
		this.caption = new String(caption == null ? "" : caption);
	
		if (created != null) {
			this.created = (Date)created.clone();
		}
		else {
			this.created = new Date();
		}
	
		if (lastViewed != null) {
			this.lastViewed = (Date)lastViewed.clone();
		}
		else {
			this.lastViewed = new Date();
		}
		
		if (lastModified != null) {
			this.lastModified = (Date)lastModified.clone();
		}
		else {
			this.lastViewed = new Date();
		}
		
		//Make copy of image's contents.
		try {
			Path path = Paths.get(imagePath);
			this.copy = Files.readAllBytes(path);
		} catch (IOException e) {
			this.copy = null;
		} catch (InvalidPathException e) {
			this.copy = null;
		}
	}

	public Picture(String caption, String title, String imagePath) {
		this(new String[0], caption, title, imagePath,
				new Date(), new Date(), new Date());
	}
	
	public Picture(String imagePath) {
		this(new String[0], new String(""), new String(""),
				imagePath, new Date(), new Date(), new Date());
	}
	
	public Picture(String tags[], String imagePath) {
		this (tags, new String(""), new String(""), imagePath,
				new Date(), new Date(), new Date());
	}
	
	public String getTitle() {
		return new String(title);
	}

	public void setTitle(String title) {
		this.title = new String(title);
		this.lastModified = new Date();
	}

	public String getCaption() {
		return new String(caption);
	}

	public void setCaption(String caption) {
		this.caption = new String(caption);
		this.lastModified = new Date(); //Update last modified time.
	}

	public ArrayList<String> getTags() {
		ArrayList<String> toReturn = new ArrayList<String>(this.tags);
		toReturn.addAll(this.tags);
		return toReturn;
	}

	public void setTags(List<String> tags) {
		this.tags.clear();
		this.tags.addAll(tags);
		this.lastModified = new Date();
	}

	public String getImagePath() {
		return new String(imagePath);
	}

	public void setImagePath(String imagePath) {
		this.imagePath = new String(imagePath);
		this.lastModified = new Date(); //Update last modified time.
	}

	public Date getLastViewed() {
		return (Date)lastViewed.clone();
	}

	public void setLastViewed(Date lastViewed) {
		this.lastViewed = (Date)lastViewed.clone();
		this.lastModified = new Date();
	}

	public Date getCreated() {
		return (Date)created.clone();
	}

	public void setCreated(Date created) {
		this.created = (Date)created.clone();
		this.lastModified = new Date(); //Update last modified time.
	}

	public Date getLastModified() {
		return (Date)lastModified.clone();
	}

	public void setLastModified(Date lastModified) {
		this.lastModified = (Date)lastModified.clone();
	}

	/**
	 * Two pictures' similarity is equivalent to the
	 * similarity of their absolute paths.
	 * @author Andrew Zuelsdorf
	 */
	@Override
	public int compareTo(Picture otherPicture) {
		return otherPicture.imagePath.compareTo(this.imagePath);
	}
	
	/**
	 * Two pictures are identical iff they have the same imagePath.
	 * @author Andrew Zuelsdorf
	 */
	@Override
	public boolean equals(Object other) {
		if (other instanceof Picture) {
			return ((Picture)other).imagePath.equals(this.imagePath);
		}
		return false;
	}
}