/*	This file is part of PhotoShow.

	Copyright 2015 Andrew Zuelsdorf. Licensed under GNU GPL

    PhotoShow is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    PhotoShow is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with PhotoShow.  If not, see <http://www.gnu.org/licenses/>.
*/

package birthday;

public class Driver {
	public static void main(String args[]) {
        PhotoShow ps = new PhotoShow("Happy 21st Birthday, Amanda!");
	
        ps.createAndDisplayGUI();
	}
}
