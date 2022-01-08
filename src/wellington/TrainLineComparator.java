package wellington;

import java.util.Comparator;

public class TrainLineComparator implements Comparator<TrainLine> {

	@Override
	public int compare(TrainLine t1, TrainLine t2) {
		return t1.getName().compareTo(t2.getName());
	}

}
