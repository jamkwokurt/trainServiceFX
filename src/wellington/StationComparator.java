package wellington;

import java.util.Comparator;

public class StationComparator implements Comparator<Station> {

	@Override
	public int compare(Station s1, Station s2) {
		return s1.getName().compareTo(s2.getName());
	}

}
