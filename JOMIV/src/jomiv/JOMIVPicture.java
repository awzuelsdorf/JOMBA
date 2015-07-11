package jomiv;

/*
Copyright 2015 Andrew Zuelsdorf.
Licensed under GNU GPL version 3.0.

This file is part of JOMIV

JOMIV is free software:
you can redistribute it and/or modify it under the terms of the
GNU General Public License as published by the Free Software 
Foundation, either version 3 of the License, or (at your option)
any later version. This program is distributed in the hope that
it will be useful, but WITHOUT ANY WARRANTY; without even the
implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.
You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

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
import javax.swing.ImageIcon;

public class JOMIVPicture implements Comparable<JOMIVPicture>, Serializable {

	private static final String imageFileExtensions[] = {".png", ".jpg", ".svg", ".gif"};
	private static final long serialVersionUID = 5045470541644776894L;
	private String title, location, caption, imagePath;
	private ArrayList<String> tags;
	private byte copy[]; //A copy of this image's contents. Useful
	//in case the picture gets moved.
	private Date lastViewed, created, lastModified;

	//The full-argument constructor.
	public JOMIVPicture(List<String> tags, String caption,
			String title, String imagePath, String location, Date lastViewed,
			Date created, Date lastModified) {

		setImagePath(imagePath);
		
		setTags(tags);

		setTitle(title);

		setCaption(caption);
		
		setCreated(created);

		setLastViewed(lastViewed);

		setLastModified(lastModified); 

		setLocation(location);

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

	public static boolean hasImageFileExtension(String filePath) {
		if (filePath == null) {
			return false;
		}
		
		for (int i = 0; i < imageFileExtensions.length; i += 1) {
			if (filePath.endsWith(imageFileExtensions[i])) {
				return true;
			}
		}
		
		return false;
	}
	
	public JOMIVPicture(String caption, String title, String imagePath) {
		this(null, caption, title, imagePath, "",
				new Date(), new Date(), new Date());
	}

	public JOMIVPicture(String imagePath) {
		this(null, "", "",
				imagePath, "", new Date(), new Date(), new Date());
	}

	public JOMIVPicture(List<String> tags, String imagePath) {
		this (tags, "", "", imagePath, "",
				new Date(), new Date(), new Date());
	}

	public String getTitle() {
		return new String(title);
	}

	public void setTitle(String title) {
		this.title = new String(title == null ? "" : title);
		this.lastModified = new Date();
	}

	public String getCaption() {
		return new String(caption);
	}

	public void setCaption(String caption) {
		this.caption = new String(caption == null ? "" : caption);
		this.lastModified = new Date(); //Update last modified time.
	}

	public ArrayList<String> getTags() {
		ArrayList<String> toReturn = new ArrayList<String>(this.tags);
		toReturn.addAll(this.tags);
		return toReturn;
	}

	public void setTags(List<String> tags) {
		//Special case. Do not allow null number of tags.
		if (tags == null) {
			this.tags = new ArrayList<String>();
			this.lastModified = new Date();
			return;
		}
		
		if (this.tags == null) {
			this.tags = new ArrayList<String>(tags.size());
		}
		this.tags.clear();
		this.tags.addAll(tags);
		this.lastModified = new Date();
	}

	public String getImagePath() {
		return new String(imagePath);
	}

	public void setImagePath(String imagePath) {//Check that we received a path to an actual image file.
		if (imagePath == null) {
			throw new RuntimeException("Received a null image path");
		}
		else {
			//For rest of our verification, we want to operate
			//on the full path to our image.
			this.imagePath = new File(imagePath).getAbsolutePath();
			
			if (new File(this.imagePath).exists() == false) {
				throw new RuntimeException("There is no file at location \"" +
						imagePath + "\".");
			}
			else if (new File(this.imagePath).isDirectory() == true) {
				throw new RuntimeException("The file at location \"" +
						imagePath + "\" is a directory.");
			}
			//Probably need to integrate with Apache Tika to get a more comprehensive check...
			else if (hasImageFileExtension(this.imagePath) == false) {
				throw new RuntimeException("The file at location \"" +
						imagePath + "\" is not an image file.");
			}
			else {
				//At this point, we probably have a legitimate image file on our hands.
			}
		}
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
		this.created = created != null ? (Date)created.clone() : new Date();
		this.lastModified = new Date(); //Update last modified time.
	}

	public Date getLastModified() {
		return (Date)lastModified.clone();
	}

	public void setLastModified(Date lastModified) {
		this.lastModified = lastModified != null ? (Date)lastModified.clone() : new Date();
	}

	public void setLocation(String location) {
		this.location = new String(location != null ? location : "");
		this.lastModified = new Date();
	}

	public String getLocation() {
		return new String(this.location);
	}
	
	public byte[] getCopy() {
		if (copy == null) {
			return null;
		}
		
		byte toReturn[] = new byte[copy.length];
		
		for (int i = 0; i < copy.length; i += 1) {
			toReturn[i] = copy[i];
		}
		
		return toReturn;
	}
	
	public ImageIcon getImageIcon() {
		if (copy == null) {
			return null;
		}
		
		return new ImageIcon(getCopy());
	}

	/**
	 * Two pictures' similarity is equivalent to the
	 * similarity of their absolute paths.
	 * @author Andrew Zuelsdorf
	 */
	@Override
	public int compareTo(JOMIVPicture otherPicture) {
		return otherPicture.imagePath.compareTo(this.imagePath);
	}

	/**
	 * Two pictures are identical iff they have the same imagePath.
	 * @author Andrew Zuelsdorf
	 */
	@Override
	public boolean equals(Object other) {
		if (other instanceof JOMIVPicture) {
			return ((JOMIVPicture)other).imagePath.equals(this.imagePath);
		}
		return false;
	}
}