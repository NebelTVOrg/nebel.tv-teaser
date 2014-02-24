/**
 * Copyright (C) 2014 Nebel TV (http://nebel.tv)
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.nebel_tv.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Scanner;

import com.flurry.android.FlurryAgent;

public class FileUtils {

	private static final String TAG = FileUtils.class.getName();
	private static final String START_OF_STRING_REG_EX = "\\A";

	public static void saveFileFromInputStream(InputStream in, File file) {
		if (in == null || file == null) {
			return;
		}
		OutputStream out = null;
		try {
			out = new FileOutputStream(file);
			copyFile(in, out);
			out.flush();
			out.close();
			out = null;
		} catch (IOException e) {
			D.e(e, false);
			FlurryAgent.onError(TAG, e.getMessage(), e);
		} finally {
			try {
				in.close();
				in = null;
			} catch (IOException ex) {
				D.e(ex, false);
			}
		}
	}

	public static String convertStreamToString(InputStream is) {
		Scanner s = new Scanner(is).useDelimiter(START_OF_STRING_REG_EX);
		return s.hasNext() ? s.next() : "";
	}

	private static void copyFile(InputStream in, OutputStream out) throws IOException {
		byte[] buffer = new byte[1024];
		int read;
		while ((read = in.read(buffer)) != -1) {
			out.write(buffer, 0, read);
		}
	}
}
