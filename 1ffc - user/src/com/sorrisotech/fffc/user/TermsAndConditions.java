package com.sorrisotech.fffc.user;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;

import com.sorrisotech.svcs.external.IFileLocator;
import com.sorrisotech.svcs.external.IServiceLocator2;

public class TermsAndConditions {
	public static String loadFile(
			final IServiceLocator2 locator,
			final String           filename
			) {
		final IFileLocator  files  = locator.findService(IFileLocator.class);
		final InputStream   stream = files.getFileAsStream(
			"linked-root" + File.separator + "consent" + File.separator + filename
			);

		StringBuilder retval = new StringBuilder();
		if (stream != null) {
			try {
				final InputStreamReader reader = new InputStreamReader(stream, "utf-8");
				final char[]            buffer = new char[8192];
				
				int read = 0;
				while((read = reader.read(buffer, 0, 8192)) > 0) {
					retval.append(buffer, 0, read);
				}
				reader.close();
				
			} catch(Throwable e) {
				retval = new StringBuilder(
					"<h1>Could not read file: <b>" + filename + "</b> - " + e.getMessage() + "</h1>"
				);
				try { stream.close(); } catch(Throwable e2) {}
			}			
		} else {
			retval.append("<h1>Could not find HTML template <b>" + filename + "</b>.</h1>");
		}
		
		return retval.toString();
	}
}
