package com.nicksamoylov.storycheck.domain;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.nicksamoylov.storycheck.domain.test.Test;
import com.nicksamoylov.storycheck.domain.test.Testgroup;
import com.nicksamoylov.storycheck.domain.test.results.TestResult;
import com.nicksamoylov.storycheck.domain.test.results.TesterResult;
import com.nicksamoylov.storycheck.domain.test.results.TestgroupResult;
import com.nicksamoylov.storycheck.domain.users.Request;
import com.nicksamoylov.storycheck.domain.users.Role;
import com.nicksamoylov.storycheck.domain.users.Tester;

public class SampleData {
	private static final Map<String, String> REQUEST_TITLES_EN = new HashMap<String, String>();
	static {
		REQUEST_TITLES_EN.put("w", "Looking for a proofreader, editor and translator");
		REQUEST_TITLES_EN.put("p", "Available for proofreading");
		REQUEST_TITLES_EN.put("e", "Experienced editor");
		REQUEST_TITLES_EN.put("w", "Can translate from English to Russian");
	}
	private static final Map<String, String> REQUEST_TITLES_RU = new HashMap<String, String>();
	static {
		REQUEST_TITLES_RU.put("w", "Требуется помощь коррректора, редактора и переводчика");
		REQUEST_TITLES_RU.put("p", "Держу корректуру");
		REQUEST_TITLES_RU.put("e", "Опытный редактор предлагает свои услуги");
		REQUEST_TITLES_RU.put("w", "Перевожу с русского на английский");
	}
	private static final Map<String, String> REQUESTS_EN = new HashMap<String, String>();
	static {
		REQUESTS_EN.put("w", "I am finishing a book - fiction about love, hatred and fun on the way to the most remote place in Himalaya. I would need help in proofreading, editing and, eventually, translating it to Russian.");
		REQUESTS_EN.put("p", "Can proofread a book quickly with a good quality.");
		REQUESTS_EN.put("e", "Did editing all formats for many years.");
		REQUESTS_EN.put("w", "Native Russian speaker, I have studied English in school and lived in the US for 10 years already.");
	}
	private static final Map<String, String> REQUESTS_RU = new HashMap<String, String>();
	static {
		REQUESTS_RU.put("w", "Я заканчиваю книгу о приключениях небольшой группы исследователей в высоких Гималаях и планирую перевести ее на английский тоже.");
		REQUESTS_RU.put("p", "Корректорую быстро и с высоким качеством.");
		REQUESTS_RU.put("e", "За долгую карьеру редактора мне приходилось иметь дело с самым разным материалом.");
		REQUESTS_RU.put("w", "Я вырос в США в семье эмигрантов из России и свободно владею как русским, так и английским.");
	}
	public static Request request(boolean isRussian, String shortRole){
		String role = Role.roleExists(shortRole)?shortRole:"w";
		Request r = new Request();
		r.setTitle(isRussian?REQUEST_TITLES_RU.get(role):REQUEST_TITLES_EN.get(role));
		r.setRequestText(isRussian?REQUESTS_RU.get(role):REQUESTS_EN.get(role));
		return r;
	}
	public static List<Request> requests(boolean isRussian, String shortRole){
		List<Request> l = new ArrayList<Request>();
		l.add(request(isRussian, shortRole));
		return l;
	}
	private static final String sampleWriterId = "0";
	private static final int sampleWriterIdInt = Integer.parseInt(sampleWriterId);
	private static final String[][] TESTERS_EN = new String[][]{
		{"1", sampleWriterId, "samuel.langhorne@clemens.com", "Mark", "Twain", "351 Farmington Ave", "Hartford", "CT", "06105", "USA", "lang.en"},
		{"2", sampleWriterId, "to.kill@mockingbird.com", "Harper", "Lee", "South Alabama Avenue", "Monroeville", "Alabama", "36460", "USA", "lang.en"},
		{"3", sampleWriterId, "oliver.twist@pickwick.com", "Charles", "Dickens", "Gad's Hill Place", "Higham", "Kent", "", "England", "lang.en"},
		{"4", sampleWriterId, "yoknapatawpha.oford@Mississippi.com", "Whilliam", "Faulkner", "Rowan Oak, Old Taylor Rd", "Oxford", "MS", "38655", "USA", "lang.en"},
		{"5", sampleWriterId, "macbeth@hamlet.com", "William", "Shakespeare", "Henley Street", "Stratford-upon-Avon", "Warwickshire", "", "England", "lang.en"},
		{"6", sampleWriterId, "uncle.tom@cabin.com", "Harriet Beecher", "Stowe", "2950 Gilbert Ave", "Cincinnati", "Ohio", "45206", "USA", "lang.en"}
	};
	private static final String[][] TESTERS_RU = new String[][]{
		{"1", sampleWriterId, "ruslan.lyudmyla@charodei.com", "Александр", "Пушкин", "Набережная реки Мойки, 12", "Санкт-Петербург", "", "191186", "Россия", "lang.ru"},
		{"2", sampleWriterId, "love.love@death.com", "Марина", "Цветаева", "пер.Сивцев Вражек, д.19", "Москва", "", "119002", "Россия", "lang.ru"},
		{"3", sampleWriterId, "crime.punishment@idiot.com", "Федор", "Достоевский", "Кузнечный переулок, 5, кв.10", "Санкт-Петербург", "", "191002", "Россия", "lang.ru"},
		{"4", sampleWriterId, "winter.night@hamlet.com", "Борис", "Пастернак", "ул. Павленко, д.3", "Переделкино", "Московская область", "142783", "Россия", "lang.ru"},
		{"5", sampleWriterId, "water.at@well.com", "Велимир", "Хлебников", "Вторая гора, дом Ульянова", "Казань", "Республика Татарстан", "", "Россия", "lang.ru"},
		{"6", sampleWriterId, "21.night@monday.com", "Анна", "Ахматова", "Тучков переулок, дом 17, кв. 29", "Санкт-Петербург", "", "199004", "Россия", "lang.ru"}
	};
	
	private static final String EMAIL_TEXT_SHORT_EN = "Ernest Hemingway asks if you would be willing to read the following text fragment and answer a few questions about it.";
	private static final String EMAIL_TEXT_SHORT_RU = "Лев Николаевич просит вас прочесть приведенный ниже фрагмент текста и ответить на несколько вопросов.";
	private static final String EMAIL_TEXT_LONG_EN = "FirstThreeChapters.doc";
	private static final String EMAIL_TEXT_LONG_RU = "ТриГлавы.doc";
	
	public static final Tester tester(int id, final boolean isRussian){
		if(id < 1 || id > 6){
			id = 1;
		}
		Tester tester;
		if(isRussian){
			tester = new Tester(TESTERS_RU[id-1]);
		}
		else {
			tester = new Tester(TESTERS_EN[id-1]);
		}
		return tester;
	}
	public static final List<Tester> testers (int groupNumber, final boolean isRussian){
		if(groupNumber < 1 || groupNumber > 2){
			groupNumber = 1;
		}
		final List<Tester> testers = new ArrayList<Tester>();
		switch(groupNumber){
		case (1): 
			if(isRussian){
				for(int i=0; i<3; i++){
					testers.add(new Tester(TESTERS_RU[i]));
				}
			}
			else {
				for(int i=0; i<3; i++){
					testers.add(new Tester(TESTERS_EN[i]));
				}
			}
		break;
		case (2): 
			if(isRussian){
				for(int i=3; i<6; i++){
					testers.add(new Tester(TESTERS_RU[i]));
				}
			}
			else {
				for(int i=3; i<6; i++){
					testers.add(new Tester(TESTERS_EN[i]));
				}
			}
		break;
		default:
			if(isRussian){
				for(int i=0; i<3; i++){
					testers.add(new Tester(TESTERS_RU[i]));
				}
			}
			else {
				for(int i=0; i<3; i++){
					testers.add(new Tester(TESTERS_EN[i]));
				}
			}
		}
		return testers;
	}
	public static List<Tester> testers(boolean isRussian){
		final List<Tester> testers = testers(1, isRussian);
		testers.addAll(testers(2, isRussian));
		return testers;
	}
	public static final List<Testgroup> testgroups(int testId, boolean isRussian){
		final List<Testgroup> groups = new ArrayList<Testgroup>();
		Testgroup tg = testgroup(1, isRussian);
		tg.setText(testText(testId, 1, isRussian));
		groups.add(tg);
		tg = testgroup(2, isRussian);
		tg.setText(testText(testId, 2, isRussian));
		groups.add(tg);
		return groups;
	}
	public static final List<Testgroup> testgroups(final boolean isRussian){
		final List<Testgroup> groups = new ArrayList<Testgroup>();
		groups.add(testgroup(1, isRussian));
		groups.add(testgroup(2, isRussian));    		
		return groups;
	}
	public static final Testgroup testgroup (int groupNumber, final boolean isRussian){
		if(groupNumber < 1 || groupNumber > 2){
			groupNumber = 1;
		}
		final List<Testgroup> testGroups = new ArrayList<Testgroup>();
		switch(groupNumber){
		case (1): 
			if(isRussian){
				Testgroup tg = new Testgroup(groupNumber, sampleWriterIdInt, "Тест группа 1");
				tg.setSize("3");
				testGroups.add(tg);
			}
			else {
				Testgroup tg = new Testgroup(groupNumber, sampleWriterIdInt, "Test group 1");
				tg.setSize("3");
				testGroups.add(tg);
			}
		break;
		case (2): 
			if(isRussian){
				Testgroup tg = new Testgroup(groupNumber, sampleWriterIdInt, "Тест группа 2");
				tg.setSize("3");
				testGroups.add(tg);
			}
			else {
				Testgroup tg = new Testgroup(groupNumber, sampleWriterIdInt, "Test group 2");
				tg.setSize("3");
				testGroups.add(tg);
			}
		break;
		default:
			Testgroup tg = new Testgroup(groupNumber, sampleWriterIdInt, "Test group 1");
			tg.setSize("3");
			testGroups.add(tg);
		}
		return testGroups.get(0);
	}
	
	public static Map<Integer, List<Test>> testerTestsMap(boolean isRussian){
		Map<Integer, List<Test>> testsMap = new HashMap<Integer, List<Test>>();
		testsMap.put(1,tests(isRussian));
		testsMap.put(2,tests(isRussian));
		testsMap.put(3,tests(isRussian));
		testsMap.put(4,tests(isRussian));
		testsMap.put(5,tests(isRussian));
		testsMap.put(6,tests(isRussian));
		return testsMap;
	}

	public static Map<Integer, List<Testgroup>> testerTestgroupsMap(boolean isRussian){
		Map<Integer, List<Testgroup>> testsMap = new HashMap<Integer, List<Testgroup>>();
		testsMap.put(1,testgroups(isRussian));
		testsMap.put(2,testgroups(isRussian));
		testsMap.put(3,testgroups(isRussian));
		testsMap.put(4,testgroups(isRussian));
		testsMap.put(5,testgroups(isRussian));
		testsMap.put(6,testgroups(isRussian));
		return testsMap;
	}

	public static Map<Integer, List<Test>> tgTestsMap(boolean isRussian){
		Map<Integer, List<Test>> testsMap = new HashMap<Integer, List<Test>>();
		for(Testgroup tg: testgroups(isRussian)){
			List<Test> tests = tests(isRussian); 
			testsMap.put(tg.getId(), tests);
		}
		return testsMap;
	}

	public static final List<Test> tests(final boolean isRussian){
		final List<Test> tests = new ArrayList<Test>();
		tests.add(test(1, isRussian));
		tests.add(test(2, isRussian));    		
		return tests;
	}

	public static final Test test (int testNumber, final boolean isRussian){
		if(testNumber < 1 || testNumber > 2){
			testNumber = 1;
		}
		Test test;
		switch(testNumber){
		case (1): 
			if(isRussian){
				test = new Test(testNumber, sampleWriterIdInt, "Первая глава Воспоминаний - моя редакция против редакции Бориса", Test.STATUS_NO_GROUPS_NO_TEXT, Test.TEXT_LENGTH_SHORT, EMAIL_TEXT_SHORT_RU);
				test.addTestgroupId(1);
				test.setStatus(Test.STATUS_PARTIAL_TEXT);
			}
			else {
				test = new Test(testNumber, sampleWriterIdInt, "Memories - first chapter - my editing vs Jim's", Test.STATUS_NO_GROUPS_NO_TEXT, Test.TEXT_LENGTH_SHORT, EMAIL_TEXT_SHORT_EN);
				test.addTestgroupId(1);
				test.setStatus(Test.STATUS_PARTIAL_TEXT);
			}
		break;
		case (2): 
			if(isRussian){
				test = new Test(testNumber, sampleWriterIdInt, "Конец рассказа Сумерки на дороге - без ребенка в окне и с ребенком ", Test.STATUS_NO_GROUPS_NO_TEXT, Test.TEXT_LENGTH_SHORT, EMAIL_TEXT_SHORT_RU);
				test.addTestgroupId(1);
				test.setStatus(Test.STATUS_NO_EMAIL);
			}
			else {
				test = new Test(testNumber, sampleWriterIdInt, "End of story Unforgiven - with and without the boy in the window", Test.STATUS_NO_GROUPS_NO_TEXT, Test.TEXT_LENGTH_SHORT, EMAIL_TEXT_SHORT_EN);
				test.addTestgroupId(1);
				test.setStatus(Test.STATUS_NO_EMAIL);
			}
		break;
		default:
			test = new Test(testNumber, sampleWriterIdInt, "End of story Unforgiven - with and without the stranger", Test.STATUS_NO_GROUPS_NO_TEXT, Test.TEXT_LENGTH_SHORT, EMAIL_TEXT_SHORT_EN);
		}
		return test;
	}
	public static final String testText (int testNumber, int tgId, boolean isRussian){
		if(testNumber < 1 || testNumber > 2){
			testNumber = 1;
		}
		if(tgId < 1 || tgId > 2){
			tgId = 1;
		}
		String text = "";
		switch(testNumber){
		case (1): 
			if(isRussian){
				switch(tgId){
				case(1):
					text = "- Эй! Tы куда? Я не мог придумать, что ответить, и продолжал идти по коридору. Позади "
				           +"раздавался звук приближающихся шагов. Я сомневался, что у меня получится дойти до той комнаты, "
						   +"но надеялся, что мне ничего не будет, если меня остановят. \"Попытка - не пытка. Правильно, "
				           +"Лаврентий Павлович?\"";
				break;
				case(2):
					text = "Я прошел в здание незамеченным. Охранник дремал на своем стуле. Он проснулся только когда я уже "
				           +"прошел мимо него и подходил к повороту коридора, метрах в десяти от охранника. Я услышал его голос, "
						   +"в котором смешались злость и страх: Эй, ты куда? Я не знал, что ответить, и даже не глянул в его "
				           +"сторону. Смогу ли я добраться до той комнаты? Именно этот вопрос занимал меня сейчас больше всего. "
						   +"А что мне оставалось? Сделать все возможное, а остальное – на волю судьбы.";
				}
			}
			else {
				switch(tgId){
				case(1):
					text = "- Hey! Stop! Where are you going? I could not come up with a quick answer and continued "
				           +"walking along the corridor. Behind me, I heard the sound of approaching steps. I doubted I could "
						   +"make it all the way to that room anyway. But I hoped there should be not many consequences, "
				           +"in case I were caught. So, I did not stop. “There are two mistakes one can make along the road "
						   +"to truth: not going all the way, and not starting”, - Buddha said. Wise man!";
				break;
				case(2):
					text = "I walked into the building unnoticed. The guard was dosing on the chair. He woke up only after "
				           +"I was good thirty feet from him, almost at the corner. I heard his voice filled with anger and "
						   +"surprise: - Hey! Stop! Where are you going? I was not sure what to say and did not respond, "
				           +"even did not look at him. Can I make all the way to the room? That was the question that occupied me. "
						   +"All I could do was to give my best. The rest was out of my control.";
				}
			}
		break;
		case (2): 
			if(isRussian){
				switch(tgId){
				case(1):
					text = "Виктор протянул руку: - Будь здоров. Володя плотно обхватил его ладонь своей широкой и крепкой ладонью. "
				           +"Они встретились глазами, простились. Шустов повернулся и пошел к Монаху. Володя смотрел ему в спину "
						   +"некоторое время, потом покачал головой, то ли неодобрительно, то ли сочувственно, ступил внутрь двора "
				           +"и закрыл ворота.";
				break;
				case(2):
					text = "Виктор заметил, как колыхнулась штора в окне, и посмотрел Володе в глаза. Тот никак не отреагировал. "
				           +"– Ну, я пошел, сказал Виктор, протягивая руку. Володя плотно обхватил его ладонь своей широкой и "
						   +"крепкой ладонью. Они встретились глазами. Нет, Володя никак не выдавал того, что заметил, как Виктор "
				           +"глянул на окно. Значит, он все-таки нашел ребенка. Ну, тут я ничего не могу поделать, сказал себе "
						   +"Виктор и решительно зашагал по направлению к Монаху. Володя еще постоял немного, глядя Виктору вслед, "
				           +"потом ступил внутрь двора и закрыл ворота.";
				}
			}
			else {
				switch(tgId){
				case(1):
					text = "Viktor extended his hand: - Good bye! Volodia squeezed his hand in its strong grasp. Their eyes met "
				           +"and posed for a moment. Shustov turned and walked away to The Monk. Volodia watched him for a while, "
						   +"than shake his head, either disapprovingly or with compassion, stepped back into his backyard and "
				           +"locked the gates.";
				break;
				case(2):
					text = "Viktor noticed the movement of the window curtain and looked at Volodia. He did not react in any way. "
				           +"– Well, I will get going, Viktor said and extended the hand. Volodia grasped and shook it. Their eyes "
						   +"met. No, Volodia did not indicate he noticed that Viktor looked at the window. So, he found the boy, "
				           +"after all, and cannot do anything about it, Viktor said to himself, then turned and walked away in "
						   +"the direction of The Monk. Volodia waited a bit, following Viktor’s, than stepped inside his yard and "
				           +"locked the gates.";
				}
			}
		}
		return text;
	}
	public static List<String> comments(int tgId, boolean isRussian){
		if(tgId < 1 || tgId > 2){
			tgId = 1;
		}
		List<String> comments = new ArrayList<String>();
		switch(tgId){
		case (1): 
			if(isRussian){
				comments.add("Хорошо написано. Мне нравится, что темп не замедляется описаниями.");
				comments.add("Можно убрать некоторые слова и сделать развитие более динамичным.");
			}
			else {
				comments.add("That is quite well written, although I can see teh author is not native English speaker. But it does not detract from the plot.");
				comments.add("Some words can be removed and the development can be made more dynamic.");
			}
		break;
		case(2):
			if(isRussian){
				comments.add("Остается впечатление, что редактирование еще может улучшить качество языка.");
				comments.add("Довольно неплохо. Хочется прочесть все.");
			}
			else {
				comments.add("Some more ediitng might improve the language.");
				comments.add("It is quite well written fragment. Very dynamic and emotional.");
			}
		}
		return comments;
	}
	public static TesterResult testerResult(final boolean isRussian){
		TesterResult tr = new TesterResult(tester(1, isRussian), 1, "abc", false);
		tr.setId(1);
		return tr;
	}
	public static List<TesterResult> testerResults(int tgId, boolean isRussian){
		List<TesterResult> trs = new ArrayList<TesterResult>();
		int init = tgId==1?1:4;
		for(int testerId=init;testerId<init+3;testerId++){
			TesterResult tr = new TesterResult(tester(testerId, isRussian), tgId, "abc", false);
			tr.setId(testerId);
			trs.add(tr);
		}
		return trs;
	}
	public static TestResult testResult(int testId, boolean isRussian){
		TestResult tr = new TestResult(test(testId, isRussian), 0, "abc", 6);
		tr.setId(testId);
		tr.setNumberAnswered("4");
		tr.setCreatedDate(new Date(System.currentTimeMillis()-1000*60*60*24));
		return tr;
	}
	public static List<TestResult> testResults(final boolean isRussian){
		List<TestResult> tests = new ArrayList<TestResult>();
		TestResult tr1 = testResult(1, isRussian);
		tr1.setNumberAnswered("4");
		tests.add(tr1);
		TestResult tr2 = testResult(2, isRussian);
		tr2.setNumberAnswered("4");
		tests.add(tr2);
		return tests;
	}
	public static TestgroupResult testgroupResult(int testId, int tgId, boolean isRussian){
		Testgroup tg = testgroup(tgId, isRussian);
		tg.setText(testText(testId, tgId, isRussian));
		TestgroupResult tgr = new TestgroupResult(tg.getId(), testId, tg.getName(), 3, tg.getText());
		if(tgId == 1){
			tgr.setQuestionAnswers(1, new String[]{"0","0","1","1"});
			tgr.setQuestionAnswers(2, new String[]{"0","0","1","1"});
			tgr.setQuestionAnswers(3, new String[]{"0","2"});
			tgr.setNumberAnswered("2");
		}
		else {
			tgr.setQuestionAnswers(1, new String[]{"0","1","1","0"});
			tgr.setQuestionAnswers(2, new String[]{"0","1","1","0"});
			tgr.setQuestionAnswers(3, new String[]{"1","1"});
			tgr.setNumberAnswered("2");
		}
		return tgr;
	}
	public static List<TestgroupResult> testgroupsTestResults(int testId, boolean isRussian){
		List<TestgroupResult> l = new ArrayList<TestgroupResult>();
		l.add(testgroupResult(testId, 1, isRussian));
		l.add(testgroupResult(testId, 2, isRussian));
		return l;
	}
	public static Map<Integer, List<TestgroupResult>> testgroupResultsMap(final boolean isRussian){
		Map<Integer, List<TestgroupResult>> testgroupsMap = new HashMap<Integer, List<TestgroupResult>>();
		List<TestgroupResult> l1 = testgroupsTestResults(1, isRussian);
		testgroupsMap.put(1, l1);
		List<TestgroupResult> l2 = testgroupsTestResults(2, isRussian);
		testgroupsMap.put(2, l2);
		return testgroupsMap;
	}
}
