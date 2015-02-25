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

public class ScriptCollection {
	
	/**
	 * имя файла, где хранится коллекция
	 */
	private String fileName = "scripts";
	
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
		load(fileName);
	}
	
	/**
	 * добавить новый скритп в коллекцию
	 * @param newScript новый скрипт
	 * @throws IllegalArgumentException если скрипт, не создержит не одной команды, то exception
	 */
	public void add(Script newScript) throws IllegalArgumentException {
		if (newScript.size() <= 0)
			throw new IllegalArgumentException("script must contain though once command");
		scripts.add(newScript);
		unload(fileName);
	}
	
	/**
	 * загрузает коллекцию скриптов из файла
	 * @param fileName имя файла
	 */
	public synchronized void load(String fileName) {
		try (BufferedReader reader = new BufferedReader(new FileReader(new File(fileName)))) {
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
	public synchronized void unload(String fileName) {
		try (BufferedWriter writer = new BufferedWriter(new FileWriter(new File(fileName)))) {
			for(Script s : scripts)
				System.out.println(s);
		} catch (IOException ex) {ex.printStackTrace();}
	}
}
