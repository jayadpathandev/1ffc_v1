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
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;

public class Run {
	
	private static Path verifyPath(
			final String path
			) throws IOException {
		final Path cwd    = FileSystems.getDefault().getPath("");
		Path       retval = Paths.get(path);
		
		if (Files.exists(retval) == false) {
			System.err.println();
			System.err.println("Directory [" + retval + "] does not exist.");
			System.err.println();
			retval = null;
		} else if (Files.isDirectory(retval) == false) {
			System.err.println();
			System.err.println("Path [" + retval + "] points to a file.");
			System.err.println();
			retval = null;
		} else if (Files.isSameFile(cwd, retval)) {
			System.err.println();
			System.err.println("The devkit directory [" + retval + "] cannot also be the current directory.");
			System.err.println();
			retval = null;			
		}
		return retval;		
	}
	
	private static Path devkitDir() throws IOException {
		final Scanner in = new Scanner(System.in);
		
		Path retval = null;
		
		while (retval == null) {
			System.out.println();
			System.out.println("Where is the devkit installed?");
			System.out.print("> ");
			
			final String input = in.nextLine();
			
			retval = verifyPath(input);
		}
		in.close();
		
		return retval;
	}
		
	public static void main(String[] args) {
		try {	
			Path devkit = null;
			
			if (args.length > 0) {
				devkit = verifyPath(args[0]);
				if (devkit == null) {
					System.exit(1);
				}
			} else {
				devkit = devkitDir();
			}			
			
			BuildProperties.generate(devkit);
			FixClasspath.parseAll(devkit);
			
		} catch(Throwable e) {
			e.printStackTrace(System.err);
		}
	}
}
