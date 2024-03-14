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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.nio.file.Path;

public class BuildProperties {
	private static final String[] paths = {
	    "1ffc - admin",
	    "1ffc - auth",
	    "1ffc - user",
		"1ffc - extdoc",
	};

	public static void generate(
				final Path   devkit,
				final String url
			) throws FileNotFoundException {
		for(final var path : paths) {
			final var target = new File(path, "build.properties");
			final var out    = new PrintWriter(target);

			System.out.println("Creating file: " + target.toString());
			out.println("install.dir = " + devkit.toAbsolutePath().toString().replace('\\', '/'));
			out.println("user.url = " + url + "user/");
			out.close();
		}
	}
}
