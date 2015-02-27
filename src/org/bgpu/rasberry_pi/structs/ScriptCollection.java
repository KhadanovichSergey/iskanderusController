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

import org.bgpu.rasberry_pi.exception.CollectionIsEmptyException;
import org.bgpu.rasberry_pi.exception.ScriptIsEmptyException;
import org.bgpu.rasberry_pi.exception.ScriptNotFoundException;
import org.bgpu.rasberry_pi.exception.WrongFormatCommandException;

public class ScriptCollection {
	
	/**
	 * имя файла, где хранится коллекция
	 */
	private static final String DIR_NAME = ConfigLoader.instance().getValue("pathToDirWithScripts");
	
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
		checkDirectory(ScriptCollection.DIR_NAME);
		loadCollection(ScriptCollection.DIR_NAME);
	}
	
	/**
	 * добавить новый скритп в коллекцию
	 * @param newScript новый скрипт
	 * @throws IllegalArgumentException если скрипт, не создержит не одной команды, то exception
	 */
	public void add(Script newScript) throws ScriptIsEmptyException {
		if (newScript.size() <= 0)
			throw new ScriptIsEmptyException();
		try {
			deleteScript(newScript.getName());
		} catch (ScriptNotFoundException snfe) {}
		
		synchronized (scripts) {
			scripts.add(newScript);
			unloadCollection(ScriptCollection.DIR_NAME);
		}
	}

	/**
	 * 
	 * @param dirName
	 * @throws WrongFormatCommandException
	 */
	private void loadCollection(String dirName) {
		File dir = new File(dirName);
		File[] files = dir.listFiles((f) -> f.getName().endsWith(".script"));
		scripts.clear();
		for(File f : files)
			try {
				loadScript(f);
			} catch (WrongFormatCommandException wfce) {
				System.out.println("can't read file " + f.getName());
				wfce.printStackTrace();
			}
	}
	
	/**
	 * загружает скрипт из файла
	 * @param file файл, откуда надо загрузить скрипт
	 * @throws WrongFormatCommandException если файл не прочитался, команда в неправильном формате
	 */
	private void loadScript(File file) throws WrongFormatCommandException {
		try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
			String line = reader.readLine();
			StringTokenizer tokenizer = new StringTokenizer(line);
			Script script = new Script(tokenizer.nextToken());
			while (tokenizer.hasMoreTokens())
				script.addCommand(new Command(tokenizer.nextToken()));
			scripts.add(script);
		} catch (IOException ioe) {ioe.printStackTrace();}
	}
	
	/**
	 * выгружает все скрипты по отдельным файлам в директорию
	 * @param dirName имя директории, в которую нужно записать все скрипты
	 */
	private void unloadCollection(String dirName) {
		for(Script s : scripts) {
			try (BufferedWriter writer = new BufferedWriter(new FileWriter(ScriptCollection.DIR_NAME + "/" + s.getName() + ".script"))) {
				writer.write(s.toString() + System.getProperty("line.separator"));
				writer.flush();
			} catch (IOException ioe) {ioe.printStackTrace();}
		}
	}
	
	/**
	 * проверяет наличие целевой директории
	 * если ее нет, то она создается
	 * @param dirName имя директории
	 */
	private void checkDirectory(String dirName) {
		File file = new File(dirName);
		if (!file.exists()) {
			System.out.println("directory " + ScriptCollection.DIR_NAME+ " doesn't exists");
			file.mkdirs();
			System.out.println("created directory " + ScriptCollection.DIR_NAME);
		}
	}
	
	/**
	 * возращает скрипт по по имени
	 * @param nameScript имя скрипта
	 * @return объект script
	 * @throws ScriptNotFoundException если скрипта в коллекции нет
	 */
	public Script getScript(String nameScript) throws ScriptNotFoundException {
		synchronized (scripts) {
			for(Script s : scripts)
				if (s.getName().equals(nameScript))
					return s;
			throw new ScriptNotFoundException(nameScript);
		}
	}
	
	/**
	 * получить список имен скриптов в виде строки через пробел)
	 * @return список имен скриптов в виде строки через пробел
	 * @throws CollectionIsEmptyException если в коллекции нет ни одного скрипта
	 */
	public String getListNameScripts() throws CollectionIsEmptyException {
		StringBuilder builder = new StringBuilder("");
		synchronized (scripts) {
			for(Script s : scripts)
				builder.append(" " + s.getName());
			String result = builder.toString().trim();
			if (result.equals(""))
				throw new CollectionIsEmptyException();
			return result;
		}
	}
	
	/**
	 * удаляет скрипт из коллекции
	 * скрипт удаляется как из внутреннего множества скриптов, так и из директории с файлами
	 * @param scriptName имя скрипта, который нужно удалить
	 * @throws ScriptNotFoundException если скрипта с таким именем нет
	 */
	public void deleteScript(String scriptName) throws ScriptNotFoundException {
		Script s = getScript(scriptName);
		File file = new File(ScriptCollection.DIR_NAME + "/" + s.getName() + ".script");
		if (file.exists()) file.delete();
		synchronized (scripts) {
			scripts.remove(s);
		}
	}
}
