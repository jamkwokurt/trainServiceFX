package wellington;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import ecs100.UI;

/** 
 * Represents Wellington train main system. 
 * Provides information of stations, train lines and service time of Wellington trains.
 * Enable users to track for traveling options, live departure from a specified station.
 * Users can plan journey between two stations.
 * @author Jam
 * @version 1.0
 * @since 1.0
 */

public class Main {
	/** 
	 * Main control.
	 * Create hashmaps to store stations, train lines and train services.
	 * Additional list to store train lines that meet conditions for some methods. 
	 * @param to take values from user input.
	 */
	private HashMap<String, Station> stations = new HashMap<>();
	private HashMap<String, TrainLine> trainLines = new HashMap<>();
	private HashMap<String, TrainService> trainServices = new HashMap<>();
	private ArrayList<TrainLine> filteredLines = new ArrayList<>();
	private String from, to;
	private String stationSearchBox;
	private TrainLine selected;
	private int index;
	private int departure;
	private int arrival;
	/** 
	 * Main constructor to set up interface.
	 * Buttons and text field for user interations.
	 */
	public Main() {
		UI.initialise();
		UI.println("^^^^^^ Welcome to Wellington Train System ^^^^^^");
		loadStationData();
		loadTrainLineData();
		loadTrainServiceData();
		UI.addButton("Display Stations", this::printStationInfo);
		UI.addButton("Display Train Lines", this::printTrainLineInfo);
		UI.addTextField("Search Train Line by Station", this::findTrainLineByStation);
		UI.addTextField("List Stations of Train Line", this::listStationsByTrainLine);
		UI.addButton("Show Map", this::printMapImage);
		UI.addTextField("From", this::getFrom);
		UI.addTextField("To", this::getTo);
		UI.addButton("Line Options", this::listLineOption);
		UI.addButton("Plan Journey", this::journeyPlanner);
		UI.addTextField("Next Service at Station", this::searchAtStation);
		UI.addButton("Live Departure", this::listServiceByTime);
		UI.addButton("Quit", UI::quit);
	}
	/**
	 * Load station data from station file and store in hashmap.
	 */
	public void loadStationData() {
		try {
			Scanner scan = new Scanner(new File("stations.data"));
			while (scan.hasNext()) {
				String name = scan.next();
				int zone = scan.nextInt();
				double distance = scan.nextDouble();
				Station s = new Station(name, zone, distance);
				stations.put(name, s);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	/**
	 * Load train line data from files and store in hashmap.
	 * Add stations accordingly to train lines.
	 */
	public void loadTrainLineData() {
		try {
			Scanner scan = new Scanner(new File("train-lines.data"));
			while (scan.hasNext()) {
				String name = scan.next();
				TrainLine tl = new TrainLine(name);
				trainLines.put(name, tl);
				String fileName = name + "-stations.data";
				Scanner sc = new Scanner(new File(fileName));
				while(sc.hasNext()) {
					String stationToAdd = sc.next();
					if(stations.containsKey(stationToAdd)) {
						Station sta = stations.get(stationToAdd);
						tl.addStation(sta);
						sta.addTrainLine(tl);
					}
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}	
	}
	/**
	 * Print sorted station data.
	 */
	public void printStationInfo() {
		UI.println("==== Stations ====");
		ArrayList<Station>stns = new ArrayList<>();
		for(Station s:stations.values()) {
			stns.add(s);
		}
		StationComparator sc = new StationComparator();
		Collections.sort(stns,sc);
		Collections.sort(stns);
		for(Station s:stns) {
			UI.println(s);
		}
	}
	/**
	 * Print sorted train line data.
	 */
	public void printTrainLineInfo() {
		UI.println("==== Train Lines ====");
		ArrayList<TrainLine>lns = new ArrayList<>();
		for(TrainLine tl:trainLines.values()) {
			lns.add(tl);
		}
		TrainLineComparator tlc = new TrainLineComparator();
		Collections.sort(lns,tlc);
		for(TrainLine l:lns) {
			UI.println(l);
		}
	}
	/**
	 * Find a train line goes through a station by user's input from text field and print out result.
	 * @param Station name as a key to search for value in map.
	 */
	public void findTrainLineByStation(String stationName) {
		if(stations.containsKey(stationName)) {
			UI.println("==================");
			UI.println("Station "+stationName+" found on:");
			UI.println(" ");
			for(TrainLine tl:trainLines.values()) {
				if(tl.getStations().contains(stations.get(stationName))) {
					UI.println(tl.toString());
				}
			}
		}else {
			UI.println("*Invalid input. Station "+stationName+" does not exist or wrong spelling.");
			UI.println("*Name must start with uppercased letter.");
		}
	}
	/**
	 * Find a train line by its name with user's input from text field and print out result.
	 * @param train line name as a key to search for value in map.
	 */
	public void listStationsByTrainLine(String trainLineName) {
		if(trainLines.containsKey(trainLineName)) {
			UI.println("===============");
			UI.println("Station found on "+trainLineName+":");
			UI.println(" ");
			for(TrainLine tl:trainLines.values()) {
				if(trainLineName.equalsIgnoreCase(tl.getName())) {
					tl.getStations().forEach(UI::println);
				}
			}
		}else {
			UI.println("\n*Invalid input. Train line "+trainLineName+" does not exist or wrong spelling.");
			UI.println("*Train line name must be Starting and Destination stations connected by a underscore.");
			UI.println("*First letter of each name in uppercase.");
		}
	}
	/**
	 * Get starting station from user and keep as a String value for later use.
	 * @param a passes the String to departing station name to String from in class field.
	 */
	public void getFrom(String a) {
		if(stations.containsKey(a)) {
			from = a;
		}else {
			UI.println("\n*Invalid input. Station "+a+" does not exist or wrong spelling.");
			UI.println("*Name must start with uppercased letter.");
		}
	}
	/**
	 * Get destination station from user and keep as a String value for later use.
	 * @param b passes the String to destination station name to the String to in class field.
	 */
	public void getTo(String b) {
		if(stations.containsKey(b)) {
			to = b;
		}else {
			UI.println("*Invalid input. Station "+b+" does not exist or wrong spelling.");
			UI.println("*Name must start with uppercased letter.");
		}
	}
	/**
	 * Helper method to get the index of a station on a specified train line.
	 * @param s takes station object.
	 * @param tl takes train line object.
	 * @return the index for later use in other method.
	 */
	public int getIndex(Station s,TrainLine tl) {
		if(s != null) {
			index = tl.getStations().indexOf(s);
			return index;
			}
		return 0;
	}
	/**
	 * Helper method to get the departure time at a particular station on a specified train line 
	  		from a train service object, returning time from which has the same index as departing station on the train line.
	 * @param tl takes train line object.
	 * @param ts takes train service object.
	 * @param dep takes departure station.
	 * @return departure time from that station at the given time.
	 */
	public int getDepartureTime(TrainLine tl, TrainService ts, Station dep) {
		if(ts != null) {
			int de = getIndex(dep,tl);
			departure = ts.getTimes().get(de);
			return departure;
		}return 0;
	}
	/**
	 * Helper method to get the arrival time at a particular station on a specified train line 
	 * 	from a train service object, returning time from which has the same index as arriving station on the train line.
	 * @param tl takes train line object.
	 * @param ts takes train service object.
	 * @param dep takes departure station.
	 * @return arrival time from that station at the given time.
	 */
	public int getArrivalTime(TrainLine tl,TrainService ts, Station arr) {
		if(ts != null) {
			int ar = getIndex(arr,tl);
			arrival = ts.getTimes().get(ar);
			return arrival;
		}return 0;
	}
	/**
	 * Get traveling options between two stations. 
	 * The list is initialized when calling this method as it is only used for that search.
	 * Get two station objects from station map and track for train lines
	  		that run through both stations and store them in a list.
	 * Print out result to user with number sequence.
	 * This method could also be used for journey planning method.
	 */
	public void listLineOption() {
		filteredLines.removeAll(filteredLines);
		if(stations.containsKey(from) && stations.containsKey(to)) {
			Station a = stations.get(from);
			for(TrainLine tl:a.getTrainLines()) {
				ArrayList<Station>stns = new ArrayList<>(tl.getStations());
				int i = stns.indexOf(a);
				for(int j = i; j < stns.size(); j++) {
					if(stns.get(j).getName().equalsIgnoreCase(to)) {
						filteredLines.add(tl);
					}
				}
			}
		}else {
			UI.println("*Couldn't find stations.");
		}
		if(!filteredLines.isEmpty() && from != null && to != null) {
			UI.println("============================================================");
			UI.println("Train lines between "+from+" and "+to+" found: ");
			UI.println(" ");
			for(int i = 0; i < filteredLines.size(); i++) {
				TrainLine l=filteredLines.get(i);
				UI.println((i+1)+":"+l.getName());
			}
		}else {
			UI.println("\n******No train operating between "+from+" and "+to+"****** ");
		}
	}
	/**
	 * Load train service data from files and store in hashmap.
	 * Add train service accordingly to train lines.
	 */
	public void loadTrainServiceData() {
		try {
			for(Map.Entry<String, TrainLine>e:trainLines.entrySet()) {
				TrainLine tl = e.getValue();
				String fileName = tl.getName() + "-services.data";
				Scanner sc = new Scanner(new File(fileName));
				while(sc.hasNextLine()) {
					String str = sc.nextLine();			
					TrainService ts = new TrainService(tl);
					String timeSequence = str;
					Scanner scan = new Scanner(timeSequence);
					scan.useDelimiter(" ");
					while(scan.hasNextInt()) {
						for(Station s:tl.getStations()) {
							if(tl.getStations().indexOf(s)==0) {
								int time = scan.nextInt();
								ts.addTime(time,true);
							}else {
								int time = scan.nextInt();
								ts.addTime(time,false);
							}
						}
					}
					scan.close();
					String ID = ts.getTrainID();
					trainServices.put(ID, ts);
					tl.addTrainService(ts);
				}
				sc.close();
			}			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	/**
	 * Method to take station from user input.
	 * If the input has a match from station map, store it to a station object from field for later use.
	 * @param station from user input.
	 */
	public void searchAtStation(String station) {
		if(stations.containsKey(station)) {
			stationSearchBox = station;
		}else {
			UI.println("*\nInvalid input. Station "+station+" does not exist or wrong spelling.");
			UI.println("*Name must start with uppercased letter.");
		}
	}
	/**
	 * Get station from user input using filed parameter.
	 * Ask for a specified time from user with helper method.
	 * Get index of that station of each train line goes through it.
	 * Track train service time at that station by the same index and print accordingly. 
	 */
	public void listServiceByTime() {
		Station st = stations.get(stationSearchBox);
		UI.println();
		int time = askTimeDeparture();
		if(st != null) {
			for(TrainLine tl: st.getTrainLines()) {
				int ind = getIndex(st,tl);
				TrainService ts = nextService(tl, time, ind);
				if(ts != null) {
					UI.println("on train line "+ts.getTrainLine().getName());
				}else{
					UI.println("\nNo train on "+tl.getName()+" at/ after "+time);
				}
			}
		}else{
			UI.println("\nInvalid station.");
		}
	}
	/**
	 * A helper method to get the live departure. 
	 * @param tl takes the train line of that service.
	 * @param time takes time stated.
	 * @param index used to get the station from a train line.
	 * @return a train service object with given parameters.
	 */
	public TrainService nextService(TrainLine tl, int time, int index) {
		for(TrainService ts:tl.getTrainServices()) {
			Station st = tl.getStations().get(index);
			int depart = getDepartureTime(tl, ts, st);
			if(depart >= time) {
				UI.println();
				UI.println("Next service of "+st.getName()+" Station is at "+depart);
				return ts;
			}
		}
		return null;
	}
	/**
	 * A helper method to ask time from user input.
	 * Can be utilized in journey planning and live departure.
	 * @return an integer within the service time range.
	 */
	public int askTimeDeparture() {
		int timeDeparture = UI.askInt("Please enter time based on 24-hour system"
		+"\nHMM or HHMM; H/HH for hour, MM for minute");
		if(timeDeparture>=0 && timeDeparture<2359) {
			return timeDeparture;
		}else {
			UI.println("\n*Invalid time. Time must be an integer greater than 0 and less than 2359");
			UI.println("*First train service of each line at this station is listed below.");
		}return 0;
	}
	/**
	 * A helper method that take parameters to get the next service 
	 		from departing station to destination station through a train line selected by user.
	 * Calculate zones traveled between two stations.
	 * Prints result to user.
	 * @param tl takes train line object.
	 * @param select take user selection which is an integer.
	 * @param time take time as an integer from user.
	 * Helper methods are used.
	 */
	public void journeyHelper(TrainLine tl,int select,int time) {
		Station depart = stations.get(from);
		Station arrive = stations.get(to);
		TrainService selection = nextService(selected, time, getIndex(depart,tl));
		int deT = getDepartureTime(tl,selection,depart);
		int arT = getArrivalTime(tl,selection,arrive);
		int farezoneD = stations.get(from).getZone();
		int farezoneA = stations.get(to).getZone();
		int zoneTravelled  = Math.abs(farezoneA - farezoneD);
		if(selected != null) {
			UI.println("on train line "+selected.getName());
			UI.println("departing from Station "+from+" at "+deT+" and arriving Station "+to+" at "+arT);
			UI.println(zoneTravelled+" zone(s) travelled.");
		}else{
			UI.println("\nNo train available on "+selected.getName()+" at/ after "+time);
		}
	}
	/**
	 * List train lines select between two stations specified by user 
	  		with helper method which is also used for line options.
	 * Take user selection of train line and again use helper method to get train service.
	 */
	public void journeyPlanner() {
		if(from != null && to != null) {
			filteredLines.removeAll(filteredLines);
			listLineOption();
			if(!filteredLines.isEmpty()) {
				UI.println();
				int select = UI.askInt("Please choose a train line by entering a number:");
				if(select>0 && select<=filteredLines.size()) {
					selected = filteredLines.get(select-1);
					int time = askTimeDeparture();
					journeyHelper(selected,select,time);
				}else {
					UI.println("\n*Invalid input. Must be an integer within selection range.");
				}
			}else {
				UI.println("\nNo train line available. Please plan for another route.");
			}
		}else {
			UI.println("*\nPlease enter both stations to start searching.");
		}
	}
	/**
	 * Print out train lines and stations in Wellington by loading a png file.
	 */
	public void printMapImage() {
		UI.drawImage("system-map.png", 0, 0);
	}

	public static void main(String[] args) {
		new Main();
	}

}
