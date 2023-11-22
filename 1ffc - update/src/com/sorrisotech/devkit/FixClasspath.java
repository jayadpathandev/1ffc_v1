/*
 * (c) Copyright 2023 Sorriso Technologies, Inc(r), All Rights Reserved, Patents
 * Pending.
 *
 * This product is distributed under license from Sorriso Technologies, Inc. Use
 * without a proper license is strictly prohibited. To license this software,
 * you may contact Sorriso Technologies at:
 *
 * Sorriso Technologies, Inc. 40 Nagog Park Acton, MA 01720 +1.978.635.3900
 *
 * "Sorriso Technologies", "You and Your Customers Together, Online", "Persona
 * Solution Suite by Sorriso", the Sorriso Logo and Persona Solution Suite Logo
 * are all Registered Trademarks of Sorriso Technologies, Inc. "Information Is
 * The New Online Currency", "e-TransPromo", "Persona Enterprise Edition",
 * "Persona SaaS", "Persona Services", "SPN - Synergy Partner Network",
 * "Sorriso Synergy", "Our DNA Is In Online", "Persona E-Bill & E-Pay",
 * "Persona E-Service", "Persona Customer Intelligence", "Persona Active
 * Marketing", and "Persona Powered By Sorriso" are trademarks of Sorriso
 * Technologies, Inc.
 */
package com.sorrisotech.devkit;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FixClasspath {
	
	private static List<Path> findFiles() throws IOException {
		final Path       cwd    = FileSystems.getDefault().getPath("");
		final List<Path> retval = new ArrayList<Path>();
		
		try (DirectoryStream<Path> stream = Files.newDirectoryStream(cwd)) { 
			for(Path path : stream) {
				if (Files.isDirectory(path)) {
					final Path classfile = path.resolve(".classpath");
					
					if (Files.exists(classfile) && Files.isRegularFile(classfile)) {
						retval.add(classfile);
					}
				}
			}
		}
		return retval;
	}
	
	private static Path findMatch(
				final Path   devkit,
				final String partial
			) throws IOException {
		final Path    partialPath = Path.of(partial);
		final String  prefix      = partialPath.getFileName().toString();
		final Path    dir         = devkit.resolve(partialPath).getParent();
		final Pattern regex       = Pattern.compile("^[0-9].*\\.jar$");
		
		try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir)) { 
			for(Path path : stream) {
				final String fileName = path.getFileName().toString();
				
				if (fileName.startsWith(prefix)) {
					final Matcher matches = regex.matcher(fileName.substring(prefix.length()));
					if (matches.matches()) {
						return partialPath.getParent().resolve(fileName);
					}
				}
			}
		}
		return null;
	}
	
	private static void parseFile(
					final Path devkit,
					final Path path
				) throws IOException {
		final StringBuilder retval = new StringBuilder();
		final String        input  = Files.readString(path);
		final Pattern       regex  = Pattern.compile("\"/([^\"]*-)[0-9][^\"]*\\.jar\"");
		final Matcher       matcher = regex.matcher(input);
		
		System.out.println("Processing: " + path.toString());
		int pos = 0;
		while (matcher.find()) {
			final String fileBase = matcher.group(1);
			final Path   replace  = findMatch(devkit, fileBase);
			
			if (replace != null) {
				final String source = matcher.group(0);
				final String target = "\"/" + replace + "\"";

				if (!source.equals(target)) {
					System.out.println("   " + source + " => " + target +".");

					final int from = matcher.start();
					final int to   = matcher.end();
				
					retval.append(input.substring(pos,  from));
					retval.append(target);

					pos = to;				
				}
			} else {
				System.err.println("Could not find library " + matcher.group(0));
			}			
		}
		retval.append(input.substring(pos));
		
		Files.writeString(
			path.getParent().resolve(".classpath"),
			retval.toString()
		);
	}
	
	                                      
	public static void parseAll(
				final Path devkit
			) throws IOException {
		for(final Path path : findFiles()) {
			parseFile(devkit, path);
		}
	}
}
