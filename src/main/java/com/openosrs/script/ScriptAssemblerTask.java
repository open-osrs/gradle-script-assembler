package com.openosrs.script;

import com.google.common.io.Files;
import com.google.common.io.MoreFiles;
import com.google.common.io.RecursiveDeleteOption;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import net.runelite.cache.definitions.ScriptDefinition;
import net.runelite.cache.definitions.savers.ScriptSaver;
import net.runelite.cache.script.RuneLiteInstructions;
import net.runelite.cache.script.assembler.Assembler;
import org.gradle.api.DefaultTask;
import org.gradle.api.logging.Logger;
import org.gradle.api.tasks.InputDirectory;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;

public class ScriptAssemblerTask extends DefaultTask {
	private String scriptDirectory_;
	private String outputDirectory_;

	private final Logger log = getLogger();

	@InputDirectory
	public String getScriptDirectory() {
		return scriptDirectory_;
	}

	public void setScriptDirectory(String scriptDirectory_) {
		this.scriptDirectory_ = scriptDirectory_;
	}

	@OutputDirectory
	public String getOutputDirectory() {
		return outputDirectory_;
	}

	public void setOutputDirectory(String outputDirectory) {
		this.outputDirectory_ = outputDirectory;
	}

	@TaskAction
	public void assemble() {
		File scriptDirectory = new File(scriptDirectory_);
		File outputDirectory = new File(outputDirectory_);

		RuneLiteInstructions instructions = new RuneLiteInstructions();
		instructions.init();

		Assembler assembler = new Assembler(instructions);
		ScriptSaver saver = new ScriptSaver();

		int count = 0;
		File scriptOut = new File(outputDirectory, "scripts");
		scriptOut.mkdirs();

		// Clear the target directory to remove stale entries
		try {
			MoreFiles.deleteDirectoryContents(scriptOut.toPath(), RecursiveDeleteOption.ALLOW_INSECURE);
		} catch (IOException e) {
			throw new RuntimeException("Could not clear scriptOut: " + scriptOut, e);
		}

		for (File scriptFile : scriptDirectory.listFiles((dir, name) -> name.endsWith(".rs2asm"))) {
			log.lifecycle("[debug] Assembling " + scriptFile);

			try (FileInputStream fin = new FileInputStream(scriptFile)) {
				ScriptDefinition script = assembler.assemble(fin);
				byte[] packedScript = saver.save(script);

				File targetFile = new File(scriptOut, scriptFile.getName().replace(".rs2asm", ""));
				Files.write(packedScript, targetFile);

				count++;
			} catch (IOException ex) {
				throw new RuntimeException("unable to open file", ex);
			}
		}

		log.lifecycle("[info] Assembled " + count + " scripts");
	}
}
