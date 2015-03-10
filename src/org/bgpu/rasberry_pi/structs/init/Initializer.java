package org.bgpu.rasberry_pi.structs.init;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.regex.Pattern;

import org.bgpu.rasberry_pi.structs.Pair;
/** 
 * <h1>Initializer - инициализатор</h1>
 * <p>Производит первоначальную инициализацию различных классов,
 * необходимых для работы сервера. Инициалзицаия производится по
 * параметрам, которые передаются в консоль к jar файлу.
 * 
 * @author Khadanovich Sergey
 * @since 2015-03-10
 */
public class Initializer {
	
	@SuppressWarnings("unchecked")
	private static Pair<String, Boolean>[] pairs = new Pair[2];
	static {
		pairs[0] = new Pair<String, Boolean>("config", false);
		pairs[1] = new Pair<String, Boolean>("log", false);
	}
	
	public static void init(String... args) throws Exception {
		if (args.length < 2) // количество параметров должно быть минимум два
			throw new Exception("error count of jar's parameters");
		
		for(String par : args) {
			if (Pattern.compile("^config=.*$").matcher(par).matches()) {
				String fileName = par.replace("config=", "").trim();
				File file = new File(fileName);
				if (!file.exists())
					throw new Exception("config file not exists");
				else {
					ConfigLoader.setDestination(fileName);
					pairs[0].setValue(true);
				}
			} else if (Pattern.compile("^log=.*$").matcher(par).matches()) {
				File file = new File("/var/log/raspberry.log");
				if (!file.exists())
					file.createNewFile();
				String action = par.replace("log=", "").trim();
				switch (action) {
					case "rewrite" :
						try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
							writer.write("");
							writer.flush();
						}
					case "append" :
						// default action
						break;
					default : throw new Exception("log option not initialize");
				}
				pairs[1].setValue(true);
			}
		}
		
		// все ли параметры были установленны
		StringBuilder builder = new StringBuilder("not all parameters was be initialize");
		boolean mark = false;
		for(Pair<String, Boolean> p : pairs)
			if (!p.getValue()) {
				mark = true;
				builder.append(", " + p.getKey() + " not initialize");
			}
		
		if (mark)
			throw new Exception(builder.toString());
		
		// инициализация необходимых классов
		Class.forName("org.bgpu.rasberry_pi.core.IskanderusController");
		Class.forName("org.bgpu.rasberry_pi.structs.ScriptCollection");
	}
	
}
