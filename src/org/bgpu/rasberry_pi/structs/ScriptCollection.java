package org.bgpu.rasberry_pi.structs;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;

import org.bgpu.rasberry_pi.exception.ScriptIsEmptyException;
import org.bgpu.rasberry_pi.exception.ScriptNotFoundException;
import org.bgpu.rasberry_pi.exception.WrongFormatCommandException;

public class ScriptCollection {
	
	/**
	 * имя файла, где хранится коллекция
	 */
	private static final String FILE_NAME = ConfigLoader.instance().getValue("pathToFileScripts");
	
	/**
	 * множество скриптов, которые уже были добавленны
	 */
	private Set<Script> scripts = new HashSet<>();
	
	/**
	 * колеекция скриптов, хранится в единственном экзепляре
	 */
	private static final ScriptCollection SCRIPT_COLLECTION = new ScriptCollection();
	
	/**
	 * получить коллекцию скриптов
	 * @return коллекцию скриптов
	 */
	public static ScriptCollection instance() {
		return SCRIPT_COLLECTION;
	}
	
	private ScriptCollection() {
		try {
			checkFile(ScriptCollection.FILE_NAME);
			load(ScriptCollection.FILE_NAME);
		} catch (WrongFormatCommandException ex) {
			scripts.clear();
			System.out.println("can't read file " + ScriptCollection.FILE_NAME);
			ex.printStackTrace();
		} catch (IOException ioe) {ioe.printStackTrace();}
	}
	
	/**
	 * добавить новый скритп в коллекцию
	 * @param newScript новый скрипт
	 * @throws IllegalArgumentException если скрипт, не создержит не одной команды, то exception
	 */
	public synchronized void add(Script newScript) throws ScriptIsEmptyException {
		if (newScript.size() <= 0)
			throw new ScriptIsEmptyException();
		scripts.add(newScript);
		unload(ScriptCollection.FILE_NAME);
	}
	
	/**
	 * загрузает коллекцию скриптов из файла
	 * @param fileName имя файла
	 */
	private void load(String fileName) throws WrongFormatCommandException {
		try (BufferedReader reader = new BufferedReader(new FileReader(new File(ScriptCollection.FILE_NAME)))) {
			scripts.clear();
			String line = null;
			while ((line = reader.readLine()) != null) {
				StringTokenizer tokenizer = new StringTokenizer(line);
				Script script = new Script(tokenizer.nextToken());
				while (tokenizer.hasMoreTokens())
					script.addCommand(new Command(tokenizer.nextToken()));
				scripts.add(script);
			}
		} catch (IOException ex) {ex.printStackTrace();}
	}
	
	/**
	 * выгружает коллекцию скриптов в файл
	 * @param fileName
	 */
	private void unload(String fileName) {
		try (BufferedWriter writer = new BufferedWriter(new FileWriter(new File(fileName)))) {
			for(Script s : scripts)
				writer.write(s + System.getProperty("line.separator"));
			writer.flush();
		} catch (IOException ex) {ex.printStackTrace();}
	}
	
	/**
	 * проверяет а есть ли такой файл, если нет то создает его
	 * @param fileName имя файла
	 * @throws IOException нельзя создать файл
	 */
	private void checkFile(String fileName) throws IOException {
		File file = new File(fileName);
		if (!file.exists()) {
			System.out.println("file " + ScriptCollection.FILE_NAME + " doesn't exists");
			file.createNewFile();
			System.out.println("created file " + ScriptCollection.FILE_NAME);
		}
			
	}
	
	public synchronized Script getScript(String nameScript) throws ScriptNotFoundException {
		for(Script s : scripts)
			if (s.getName().equals(nameScript))
				return s;
		throw new ScriptNotFoundException(nameScript);
	}
}
