package glowredman.ruintemplatechanger;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;
import java.util.LinkedList;
import java.util.List;

public class Main {

	private static File sourceDir;
	private static String targetString;
	private static String replaceString;
	private static boolean excludeGeneric = false;

	public static void main(String[] args) {

		// read arguments
		for (int i = 0; i < args.length; i++) {
			switch (args[i]) {
			case "source":
				i++;
				sourceDir = new File(args[i]);
				break;
			case "target":
				i++;
				targetString = args[i];
				break;
			case "replacement":
				i++;
				replaceString = args[i];
				break;
			case "excludeGeneric":
				excludeGeneric = true;
			default:
				break;
			}
		}

		// check mandatory arguments
		if (sourceDir == null || targetString == null || replaceString == null) {
			throw new IllegalArgumentException("At least one of these arguments is not set: source target replacement");
		}
		System.out.println("Done! Modified a total of " + modify(sourceDir) + " files");
	}

	private static boolean hasChanged = false;

	private static int modify(File file) {
		int fileCount = 0;

		// checks if the file is a sub-directory
		if (file.isDirectory()) {

			// if the directory is a "generic" directory and should be ignored, return to
			// the parent directory
			if (excludeGeneric && file.getPath().endsWith("generic")) {
				return 0;
			}
			System.out.println("Executing in directory " + file.getPath());

			// modify the files in this directory
			for (File f : file.listFiles()) {
				fileCount += modify(f);
			}

		} else {
			try {
				List<String> content = new LinkedList<>();

				// read the file
				Files.lines(file.toPath()).forEachOrdered(line -> {

					// if the line starts with the target string, modify it and add it to the list
					if (line.startsWith(targetString)) {
						content.add(line.replace(targetString, replaceString));
						hasChanged = true;

						// otherwise only add it to the list
					} else {
						content.add(line);
					}
				});

				// rewrite the file
				if (hasChanged) {
					BufferedWriter writer = new BufferedWriter(new FileWriter(file));
					content.forEach(line -> {
						try {
							writer.write(line);
							writer.newLine();
						} catch (Exception e) {
							e.printStackTrace();
						}
					});
					writer.close();
					fileCount++;
				} else {
					return 0;
				}

			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				hasChanged = false;
			}
		}
		return fileCount;
	}

}
